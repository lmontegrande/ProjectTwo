package com.example.lmont.projecttwo;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

// This activity displays all the cards in a card game
public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String GAME_KEY = "gamekey";
    public Context context;

    private static String lastGame;
    private String gameName;
    private Button addButton;
    private Button backButton;
    private ListView cardList;
    private TextView gameNameTextView;
    private CardDatabaseHelper dbHelper;
    private CursorAdapter cursorAdapter;
    private boolean restrictHeight = true;

    private static final String TAG = "LEO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setup();
        bindData();
        handleIntent();
    }

    // Bind data to the views
    private void bindData() {
        gameNameTextView.setText(gameName);
        attachCursorAdapter();
    }

    // Initialize varibales and views
    private void setup() {
        context = this;
        gameName = getIntent().getStringExtra(GAME_KEY);
        addButton = (Button) findViewById(R.id.game_activity_add_button);
        backButton = (Button) findViewById(R.id.game_activity_back_button);
        gameNameTextView = (TextView) findViewById(R.id.game_activity_header_textview);
        cardList = (ListView) findViewById(R.id.game_activity_listview);
        dbHelper = CardDatabaseHelper.getInstance(this);

        if (gameName == null) {
            gameName = lastGame;
        } else {
            lastGame = gameName;
        }

        addButton.setBackgroundResource(R.mipmap.brown_add_icon_noback);
        backButton.setBackgroundResource(android.R.drawable.ic_delete);
        addButton.setOnClickListener(this);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Attaches the cursor adapter to the listview
    private void attachCursorAdapter() {
        cursorAdapter = new CursorAdapter(this, dbHelper.getCardCursor(gameName)) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                String[] attributes = dbHelper.getGameAttributes(gameName);
                LinearLayout v = new LinearLayout(context);
                v.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                v.setOrientation(LinearLayout.HORIZONTAL);
                TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                layoutParams.setMargins(10, 10, 10, 10);
                v.setLayoutParams(layoutParams);
                v.setPadding(10, 10, 10, 10);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setBackground(getDrawable(R.drawable.layout_bg));
                }

                for (int x=1; x<attributes.length; x++) {
                    String attribute = attributes[x];
                    TextView textView = new TextView(context);
                    if (restrictHeight) textView.setMaxHeight(100);
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(20);

                    TableLayout.LayoutParams params = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    params.setMargins(10, 10, 10, 10);
                    textView.setLayoutParams(params);
                    textView.setGravity(Gravity.CENTER);

                    textView.setText(cursor.getString(cursor.getColumnIndex(attribute)));

                    v.addView(textView);
                }
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.animation_1);
                v.startAnimation(animation);
                return v;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final String[] gameAttributes = dbHelper.getGameAttributes(gameName);
                final String value = cursor.getString(cursor.getColumnIndex(gameAttributes[1]));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(GameActivity.this, CardActivity.class);
                        intent.putExtra(CardActivity.GAME_KEY, gameName);
                        intent.putExtra(CardActivity.CARD_KEY, value);
                        startActivity(intent);
                    }
                });
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        dbHelper.removeCard(gameName, value);
                        updateList();
                        return false;
                    }
                });
            }
        };

        cardList.setAdapter(cursorAdapter);
    }

    // Custom onClick to be called when adding a new card
    public void onClick(View view) {
        addNewCard(view);
    }

    // Method that takes in opens up a dialog for the user to input the information for the new
    // card they are trying to add
    public void addNewCard(View view) {
        LayoutInflater inflater = this.getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate New Layout
        final View dialogView = inflater.inflate(R.layout.alert_layout, null);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        String[] attributes = dbHelper.getGameAttributes(gameName);

        final ArrayList<EditText> editTexts = new ArrayList<>();

        for (int x=1; x<attributes.length; x++) {
            EditText cardParamEditText = new EditText(this);
            cardParamEditText.setHint(attributes[x].toUpperCase());
            linearLayout.addView(cardParamEditText);
            editTexts.add(cardParamEditText);
        }

        builder.setView(linearLayout);

        builder.setMessage("Add Card")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ArrayList<String> userValues = new ArrayList<String>();
                        for (EditText userValue: editTexts) {
                            userValues.add(userValue.getText().toString().replaceAll("'", " ").trim());
                        }
                        dbHelper.createNewCard(gameName, userValues);
                        updateList();
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

    // Change the cursor used by the cursoradapter to match the new data in the database
    private void updateList() {
        cursorAdapter.changeCursor(dbHelper.getCardCursor(gameName));
    }

    // Sets up the search functionality
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

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    // Checks to see if the activity was called with a search.  Updates the cursorAdapter with the
    // cursor that matches the SQL
    public void handleIntent() {
        Intent intent = getIntent();
        if (!intent.ACTION_SEARCH.equals(intent.getAction())) return;
        cursorAdapter.changeCursor(dbHelper.getCardCursor(intent.getStringExtra(SearchManager.QUERY), gameName));
    }

    // Sets up the reset button to reset the cursorAdapter to the default cursor
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                cursorAdapter.changeCursor(dbHelper.getCardCursor("", gameName));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
