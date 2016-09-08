package com.example.lmont.projecttwo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final String TAG = "LEO";

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
    }

    protected void setup() {
        activityContext = this;
        addButton = (Button) findViewById(R.id.game_add_button);
        headerTextView = (TextView) findViewById(R.id.game_header_textview);
        gameListView = (ListView) findViewById(R.id.game_listview);
        dbHelper = CardDatabaseHelper.getInstance(this);

        addButton.setBackgroundResource(R.mipmap.brown_add_icon_noback);
        addButton.setOnClickListener(this);
    }

    public void clearData() {
        dbHelper.clearDB();
    }

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

    public void updateList() {
        cursorAdapter.changeCursor(dbHelper.getCardGamesCursor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                Toast.makeText(MainActivity.this, "New Game", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.help:
                Toast.makeText(MainActivity.this, "Help", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        nameCardValues(view);
    }

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
                        int number = isNumeric(stringNumber) ? Integer.parseInt(stringNumber) : 0;

                        if (number > 0) {
                            nameCardAttributes(nameTextView.getText().toString(), number);
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
                            attributes.add(((EditText)listView.getChildAt(x)).getText().toString());
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

        builder.show();
    }

    public boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
