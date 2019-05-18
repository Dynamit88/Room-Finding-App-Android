package team16.project.team.orbis;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import team16.project.team.orbis.global.methods.LocalPreferences;
import team16.project.team.orbis.global.objectclass.Building;
import team16.project.team.orbis.global.objectclass.BuildingMapNode;
import team16.project.team.orbis.global.objectclass.BuildingMapNodeType;
import team16.project.team.orbis.global.objectclass.Person;
import team16.project.team.orbis.global.uiclass.AppCompatColourActivity;

import static team16.project.team.orbis.global.variables.FirebaseVariables.getFirebaseDatabase;

public class SearchActivity extends AppCompatColourActivity {

    private ListView mOfficesListView;
    private List<Person> personList, searchList;
    private Button mOfficeSort;
    private Button mNameSort;
    private Building building;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupVariables();
        setupListeners();

        // TODO - load people from the specific building (need to use variable above)
        getFirebaseDatabase().getReference("/people/" + building.getId() + "/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // For every person
                for (DataSnapshot personData : dataSnapshot.getChildren()) {
                    // last item in DataSnapshot is the ID of the building - parent of all people inside building
                    // this if statement is to prevent error
                    // TODO: use dbPath instead of "usb"
                    if (personData.getKey().equals(building.getId())) {
                        break;
                    }

                    Person person = Person.setupPersonFromFirebase(personData);

                    // adding formatted string to list
                    personList.add(person);

                }

                // by default list of people is sorted alphabetically
                Collections.sort(personList, OFFICE_ORDER);
                display(personList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupListeners() {
        mOfficeSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(personList, OFFICE_ORDER);
                if (searchList.isEmpty()) {
                    Collections.sort(personList, OFFICE_ORDER);
                    display(personList);
                } else {
                    Collections.sort(searchList, OFFICE_ORDER);
                    display(searchList);
                }
            }
        });

        mNameSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (searchList.isEmpty()) {
                    Collections.sort(personList, ALPHABETICAL_ORDER);
                    display(personList);
                } else {
                    Collections.sort(searchList, ALPHABETICAL_ORDER);
                    display(searchList);
                }

            }
        });

        mOfficesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ********* To be replaced with a Map instance and navigate called on *********
                // Using ArrayList as it implements Serializable. List does not
                ArrayList<BuildingMapNode> testDirections = new ArrayList<>();
                BuildingMapNode foyer = new BuildingMapNode("foyer", "foyer", 0, BuildingMapNodeType.ROOM);
                BuildingMapNode stairs_f0 = new BuildingMapNode("stairs_f0", "ground staircase", 0, BuildingMapNodeType.STAIRS);
                BuildingMapNode stairs_f4 = new BuildingMapNode("stairs_f4", "floor 4 staircase", 4, BuildingMapNodeType.STAIRS);
                BuildingMapNode f4_turn_top_stairs = new BuildingMapNode("stairs_f4_turn", "left turn", 4, BuildingMapNodeType.LEFT_TURN);
                BuildingMapNode f4_common_area = new BuildingMapNode("f4_ca", "floor 4 common area", 4, BuildingMapNodeType.ROOM);
                BuildingMapNode f4_turn_common_area = new BuildingMapNode("f4_ca_turn", "left turn", 4, BuildingMapNodeType.LEFT_TURN);
                BuildingMapNode f4_msc = new BuildingMapNode("f4_msc_cluster", "MSc Cluster", 4, BuildingMapNodeType.ROOM);
                BuildingMapNode f4_turn_msc = new BuildingMapNode("f4_msc_turn", "left turn", 4, BuildingMapNodeType.LEFT_TURN);
                BuildingMapNode f4_4004 = new BuildingMapNode("f4_4.004", "USB 4.004", 4, BuildingMapNodeType.ROOM);
                //need to test multiple more than 5 before turn

                testDirections.add(foyer);
                testDirections.add(stairs_f0);
                testDirections.add(stairs_f4);
                testDirections.add(f4_turn_top_stairs);
                testDirections.add(f4_common_area);
                testDirections.add(f4_turn_common_area);
                testDirections.add(f4_msc);
                testDirections.add(f4_turn_msc);
                testDirections.add(f4_4004);
                // ********* Keep all code underneath this *********

                switchActivity(NavigationActivity.class, "", getString(R.string.directions_extra_key), testDirections);
            }
        });

    }

    private void setupVariables() {
        personList = new ArrayList<>();
        searchList = new ArrayList<>();
        mOfficeSort = findViewById(R.id.btn_sort_office);
        mNameSort = findViewById(R.id.btn_sort_name);
        mOfficesListView = findViewById(R.id.lv_office_list);
        building = ((Building) LocalPreferences.getObjectFromShared(getApplicationContext(),
                getString(R.string.building_key_shared_pref),
                Building.class));
    }

    /**
     * Creates menu item - search menu
     *
     * @param menu Application menu
     * @return created menu item
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();

        // typing in menu listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                searchList.clear();
                // clear ListView
                mOfficesListView.setAdapter(null);

                // loop data inside personList
                for (Person data : personList) {

                    if (data.getName().toLowerCase().contains(newText.toLowerCase())) {
                        searchList.add(data);
                        display(searchList);
                    }
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Method which display given list to ListView
     *
     * @param list - list with items to display in ListView
     */
    public void display(List<Person> list) {
        ArrayAdapter<Person> adapter = new ArrayAdapter<>(this, R.layout.activity_search_item, list);
        mOfficesListView.setAdapter(adapter);
    }

    /**
     * Method used for sorting people by their name
     */
    private static Comparator<Person> ALPHABETICAL_ORDER = new Comparator<Person>() {

        @Override
        public int compare(Person o1, Person o2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            if (res == 0) {
                res = o1.getName().compareTo(o2.getName());
            }
            return res;
        }
    };
    /**
     * Method used for sorting people by their office number
     */
    private static Comparator<Person> OFFICE_ORDER = new Comparator<Person>() {

        @Override
        public int compare(Person o1, Person o2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(o1.getOffice(), o2.getOffice());
            if (res == 0) {
                res = o1.getOffice().compareTo(o2.getOffice());
            }
            return res;
        }
    };

}
