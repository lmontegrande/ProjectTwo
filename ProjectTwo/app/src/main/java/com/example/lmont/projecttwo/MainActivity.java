package com.example.lmont.projecttwo;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

// This is the main activity for the app.  It displays all the card games
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final String TAG = "LEO";
    static final String SEARCH_KEY = "search_key";

    Context activityContext;
    Button addButton;
    TextView headerTextView;
    ListView gameListView;
    CardDatabaseHelper dbHelper;
    CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
        //clearData();
        bindData();
        handleIntent();
    }

    // Initialize variables and setup views
    protected void setup() {
        activityContext = this;
        addButton = (Button) findViewById(R.id.game_add_button);
        headerTextView = (TextView) findViewById(R.id.game_header_textview);
        gameListView = (ListView) findViewById(R.id.game_listview);
        dbHelper = CardDatabaseHelper.getInstance(this);

        addButton.setBackgroundResource(R.mipmap.brown_add_icon_noback);
        addButton.setOnClickListener(this);
    }

    // Used for testing, calls the CardDatabaseHelper's clearDB function
    public void clearData() {
        dbHelper.clearDB();
    }

    // Binds database data to the listview
    protected void bindData() {
        cursorAdapter = new CursorAdapter(this, dbHelper.getCardGamesCursor(), 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                View v = LayoutInflater.from(context).inflate(R.layout.game_list_item, viewGroup, false);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.animation_1);
                v.startAnimation(animation);
                return v;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView textView = (TextView) view.findViewById(R.id.game_list_item_textView);
                final String currentGameName = cursor.getString(cursor.getColumnIndex(CardDatabaseHelper.GAME_TABLE_FOREIGN_KEY));
                textView.setText(currentGameName);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, GameActivity.class);
                        intent.putExtra(GameActivity.GAME_KEY, currentGameName);
                        startActivityForResult(intent, 0);
                    }
                });

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        dbHelper.removeGame(currentGameName);
                        updateList();
                        Toast.makeText(MainActivity.this, currentGameName + " REMOVED", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        };
        gameListView.setAdapter(cursorAdapter);
    }

    // Changes the cursor for the cursorAdapter and updates the listview
    public void updateList() {
        cursorAdapter.changeCursor(dbHelper.getCardGamesCursor());
    }

    // Sets up the search bar functionality
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    // Checks to see if the activity was called through the search bar and if so, change the cursor
    // based on the passed in query
    public void handleIntent() {
        Intent intent = getIntent();
        if (!intent.ACTION_SEARCH.equals(intent.getAction())) return;

        cursorAdapter.changeCursor(dbHelper.getCardGamesCursor(intent.getStringExtra(SearchManager.QUERY)));
    }

    // Add functionality to the reset menu option.  Used to reset the list to the default cursor
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                cursorAdapter.changeCursor(dbHelper.getCardGamesCursor(""));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Sets up the onClick to be used for adding card games
    @Override
    public void onClick(View view) {
        nameCardValues(view);
    }

    // This function is used to create a new card game.  User types in a name then types in the
    // attributes names with commas to separate each value
    public void nameCardValues(View view) {
        LayoutInflater inflater = this.getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = inflater.inflate(R.layout.alert_layout, null);
        builder.setView(dialogView);

        builder.setMessage("Add New Card Game")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TextView textView = (TextView) dialogView.findViewById(R.id.game_numof_attributes);
                        TextView nameTextView = (TextView) dialogView.findViewById(R.id.game_name);
                        String stringNumber = textView.getText().toString();
                        //int number = isNumeric(stringNumber) ? Integer.parseInt(stringNumber) : 0;

                        String[] attributesArray = textView.getText().toString().split(",");
                        ArrayList<String> attributes = new ArrayList();
                        for (String att: attributesArray) {
                            attributes.add(att.trim());
                        }

                        if (attributes != null) {
                            dbHelper.createCardTable(nameTextView.getText().toString().replaceAll("'", " ").trim(), attributes);
                            updateList();

                            //nameCardAttributes(nameTextView.getText().toString(), number);
                        }
                        else
                            Toast.makeText(MainActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Cancel
                    }
                });
        builder.show();
    }

    // Was originally used to name attributes but would lock up keyboard.  Keeping here for future
    // reference
    public void nameCardAttributes(final String gameName, final int howManyAttributes) {
        LayoutInflater inflater = this.getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = inflater.inflate(R.layout.name_attributes_layout, null);

        final ListView listView = (ListView) dialogView.findViewById(R.id.name_attribute_list);
        final BaseAdapter arrayAdapter = new BaseAdapter() {
            String[] attributes = new String[howManyAttributes];

            @Override
            public int getCount() {
                return howManyAttributes;
            }

            @Override
            public Object getItem(int i) {
                return attributes[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {

                View v = view;

                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.simple_edittext, null);
                }

                EditText editText = (EditText) v.findViewById(R.id.simpleEditText);
                editText.setHint("Attribute  " + (i+1));
                return v;
            }
        };
        listView.setAdapter(arrayAdapter);

        builder.setView(dialogView);
        builder.setMessage("Name The Attributes For " + gameName + " Cards")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ArrayList<String> attributes = new ArrayList<String>();
                        for (int x=0; x<howManyAttributes; x++) {
                            EditText editText = ((EditText)listView.getChildAt(x));
                            attributes.add(editText.getText().toString());
                            editText.requestFocus();
                        }
                        CardDatabaseHelper.getInstance(activityContext).createCardTable(gameName, attributes);
                        updateList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Cancel
                    }
                });

        builder.show().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    // Check to see if a string is a number
    public boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
