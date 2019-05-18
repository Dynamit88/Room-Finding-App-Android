package team16.project.team.orbis;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import team16.project.team.orbis.global.methods.UserPermissions;
import team16.project.team.orbis.global.objectclass.BuildingMapNode;
import team16.project.team.orbis.global.objectclass.BuildingMapNodeType;
import team16.project.team.orbis.global.uiclass.AppCompatColourActivity;

/**
 * Underlying code for NavigationActivity, which provides a screen with directions
 */
public class NavigationActivity extends AppCompatColourActivity {
    private static final int QR_CALLBACK = 333;
    private int nextNodeIndex = 0;
    private List<BuildingMapNode> directions;
    private ImageView mIcon;
    private TextView mDirection;
    private FloatingActionButton mExit;
    private Button mNextDirection;
    private Button mValidateAndNextDirection;
    private TextToSpeech tts;
    private Activity activity;
    private BuildingMapNode currentNode;
    private NotificationChannel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_navigation);

        // The directions will be shown then the TTS object is initialised in the setupVariables method
        setupVariables();
        setupActionBar();
        super.onCreate(savedInstanceState);
    }

    /**
     * Set the Support Action Bar to the the toolbar object. Also change the title to be related to the navigation
     */
    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Navigate from " + directions.get(0) + " to " + directions.get(directions.size() - 1));
    }


    /**
     * Setup variable references
     */
    private void setupVariables() {
        checkAndSetupData();
        mIcon = findViewById(R.id.direction_icon);
        mDirection = findViewById(R.id.direction);
        mExit = findViewById(R.id.exit_navigation);
        mNextDirection = findViewById(R.id.next_direction_button);
        mValidateAndNextDirection = findViewById(R.id.validate_and_next_direction_button);
        activity = this;
        setupNotificationChannel();
        setupTts();
    }

    /**
     * Instantiates the TTS system and tells the user to wait for it to start
     */
    private void setupTts() {
        createToast(getString(R.string.wait_for_tts));
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Set the language of TTS to be the user's locale
                tts.setLanguage(getResources().getConfiguration().locale);

                // Carry on initialising the UI

                setupListeners();

                // Show the first direction
                changeUiForDirection();
            }
        });
    }

    /**
     * Setup the notification channel used to send navigation notifications if the Android version is greater than Android Oreo (this is a new feature)
     */
    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(getString(R.string.navigation_notification_channel), getString(R.string.navigation_notification_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setName(getString(R.string.navigation_notification_title));
            // Link the new NotificationChannel to the system NotificationService
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    /**
     * Configure the text to be shown on the UI
     */
    private void changeUiForDirection() {
        // Delete any notifications currently shown
        clearNotification();
        tts.stop();

        // If the index of the next node will not cause an IndexOutOfBoundsException
        if (nextNodeIndex < directions.size()) {
            boolean continueFor = true;
            StringBuilder viaRooms = new StringBuilder();

            // For each node in the directions starting from index nextNodeIndex, while the for loop should continue executing
            for (int numberNodes = 0; nextNodeIndex < directions.size() && continueFor; nextNodeIndex++, numberNodes++) {
                final BuildingMapNode nextNode = directions.get(nextNodeIndex);

                // If the number of nodes searched in this iteration is divisible by 5
                if (numberNodes % 5 == 0 && numberNodes >= 5) {
                    if (numberNodes == 5) {
                        // Add the initial via text
                        viaRooms.append("via ");
                    }
                    // Add the current node to the via list (for each 5 nodes add it to the list to be shown (as it only shows turns, lifts, stairs or the final node))
                    viaRooms.append(nextNode);
                }

                // If at the last node
                if (nextNodeIndex == directions.size() - 1) {
                    setupDirectionInformation("Follow the corridor to the destination, " + nextNode, BuildingMapNodeType.ROOM, true);
                    currentNode = nextNode;
                    continueFor = false;
                }
                // If the node is a left turn
                else if (nextNode.getNodeType().equals(BuildingMapNodeType.LEFT_TURN)) {
                    setupDirectionInformation("Turn left at " + directions.get(nextNodeIndex - 1) + " " + viaRooms, BuildingMapNodeType.LEFT_TURN, false);
                    currentNode = nextNode;
                    continueFor = false;
                }
                // If the node is a right turn
                else if (nextNode.getNodeType().equals(BuildingMapNodeType.RIGHT_TURN)) {
                    setupDirectionInformation("Turn right at " + directions.get(nextNodeIndex - 1) + " " + viaRooms, BuildingMapNodeType.RIGHT_TURN, false);
                    currentNode = nextNode;
                    continueFor = false;
                }
                // If the node is a lift and the next node is a lift (i.e. on the new floor)
                else if (nextNode.getNodeType().equals(BuildingMapNodeType.LIFT) && directions.get(nextNodeIndex + 1).getNodeType().equals(BuildingMapNodeType.LIFT)) {
                    setupDirectionInformation("Take the lift to floor " + directions.get(nextNodeIndex + 1).getFloor() + " " + viaRooms, BuildingMapNodeType.LIFT, false);
                    currentNode = directions.get(nextNodeIndex + 1);
                    continueFor = false;
                }
                // If the node is a staircase and the next node is a staircase (i.e. on the new floor)
                else if (nextNode.getNodeType().equals(BuildingMapNodeType.STAIRS) && directions.get(nextNodeIndex + 1).getNodeType().equals(BuildingMapNodeType.STAIRS)) {
                    setupDirectionInformation("Take the stairs to floor " + directions.get(nextNodeIndex + 1).getFloor() + " " + viaRooms, BuildingMapNodeType.STAIRS, false);
                    currentNode = directions.get(nextNodeIndex + 1);
                    continueFor = false;
                }
            }
        } else {
            // Stop the Activity (all nodes have been traversed)
            finish();
        }
    }

    /**
     * Setup the listeners for each of the buttons on screen
     */
    private void setupListeners() {
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete any notifications currently shown
                clearNotification();
                tts.stop();
                finish();
            }
        });
        mNextDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeUiForDirection();
            }
        });
        mValidateAndNextDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRCodeChecking();
            }
        });
    }

    /**
     * Clears any notifications shown
     */
    private void clearNotification() {
        NotificationManagerCompat.from(this).cancelAll();
    }

    /**
     * Start the zxing Activity which scans for QR codes
     */
    private void startQRCodeChecking() {
        // If the camera permission has been granted
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            IntentIntegrator scanner = new IntentIntegrator(activity);
            scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            scanner.setPrompt("Scan the QR code for " + currentNode);
            scanner.initiateScan();

        } else {
            // Request the camera permission
            UserPermissions.requestPermission(activity, Manifest.permission.CAMERA, QR_CALLBACK);
        }
    }

    /**
     * Change the screen to show the relevant text for the direction
     *
     * @param textToShow The text to show for the direction
     * @param nodeType   The type of node the node is
     * @param lastNode   Whether or not the node is the last node
     */
    private void setupDirectionInformation(String textToShow, BuildingMapNodeType nodeType, boolean lastNode) {
        if (lastNode) {
            mIcon.setImageDrawable(getDrawable(R.mipmap.ic_finish));
            mNextDirection.setText(getString(R.string.finish_navigation));
            mValidateAndNextDirection.setText(getString(R.string.validate_and_finish_navigation));
        } else {
            switch (nodeType) {
                case LIFT:
                    mIcon.setImageDrawable(getDrawable(R.mipmap.ic_lift));
                    break;
                case ROOM:
                    mIcon.setImageDrawable(getDrawable(R.mipmap.ic_room));
                    break;
                case STAIRS:
                    mIcon.setImageDrawable(getDrawable(R.mipmap.ic_stairs));
                    break;
                case LEFT_TURN:
                    mIcon.setImageDrawable(getDrawable(R.mipmap.ic_left));
                    break;
                case RIGHT_TURN:
                    mIcon.setImageDrawable(getDrawable(R.mipmap.ic_right));
                    break;
                default:
                    break;
            }
        }

        mDirection.setText(textToShow);
        showNotification(textToShow);
        tts.speak(textToShow, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * Shows the notification for the next navigational step
     *
     * @param textToShow The text to show in the navigation
     */
    private void showNotification(String textToShow) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.navigation_notification_channel))
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(getString(R.string.navigation_notification_title))
                .setContentText(textToShow)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Send the notification
        NotificationManagerCompat.from(this).notify(nextNodeIndex - 1, notificationBuilder.build());
    }

    /**
     * Check the passed data and do appropriate actions with it
     */
    private void checkAndSetupData() {
        if (getIntent().getSerializableExtra(getString(R.string.directions_extra_key)) == null) {
            switchActivity(SearchActivity.class);
        }

        directions = (ArrayList<BuildingMapNode>) getIntent().getSerializableExtra("directions");

        if (directions == null || directions.size() == 0) {
            throw new IllegalArgumentException("There are no directions to the room selected");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Get the data from the QR code
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            // If the contents of the scanned QR code is the ID of the node
            if (result.getContents().equals(currentNode.getId())) {
                createToast(getString(R.string.correct_location));
                changeUiForDirection();
            } else {
                createToast(getString(R.string.incorrect_location));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * This creates a toast message on the screen
     *
     * @param text The message to show
     */
    private void createToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        // Destroy the TTS service
        tts.stop();
        tts.shutdown();

        super.onDestroy();
    }
}
