package team16.project.team.orbis;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import team16.project.team.orbis.global.uiclass.AppCompatColourActivity;

/**
 * Underlying code for the Events screen, which lists the upcoming events in a building
 */
public class EventsActivity extends AppCompatColourActivity {

    // UI References
    private CalendarView mCalendarView;
    private TextView mTextView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_events);
        setupVariables();
        setupCalendarView();
        setupListeners();
        super.onCreate(savedInstanceState);
    }

    /**
     * Setup the listeners for objects on screen which need them
     */
    private void setupListeners() {
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // When the clicked date is changed, change the date displayed on screen
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                mTextView.setText(date);
            }
        });
    }

    /**
     * Set the calendar control to show today's date as default
     */
    private void setupCalendarView() {
        // Set the default date shown to be today
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        String todayDate = df.format(Calendar.getInstance().getTime());

        mTextView.setText(todayDate);
    }

    /**
     * Setup the references for the variables declared at the start of the class
     */
    private void setupVariables() {
        mCalendarView = findViewById(R.id.calendarView);
        mTextView = findViewById(R.id.eventsView);
    }

}
