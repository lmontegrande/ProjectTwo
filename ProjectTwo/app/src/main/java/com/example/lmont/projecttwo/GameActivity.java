package com.example.lmont.projecttwo;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String GAME_KEY = "gamekey";
    public Context context;

    private String gameName;
    private Button addButton;
    private Button backButton;
    private ListView cardList;
    private TextView gameNameTextView;
    private CardDatabaseHelper dbHelper;
    private CursorAdapter cursorAdapter;

    private static final String TAG = "LEO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setup();
        bindData();
    }

    private void bindData() {
        gameNameTextView.setText(gameName);
    }

    private void setup() {
        context = this;
        gameName = getIntent().getStringExtra(GAME_KEY);
        addButton = (Button) findViewById(R.id.game_activity_add_button);
        backButton = (Button) findViewById(R.id.game_activity_back_button);
        gameNameTextView = (TextView) findViewById(R.id.game_activity_header_textview);
        cardList = (ListView) findViewById(R.id.game_activity_listview);
        dbHelper = CardDatabaseHelper.getInstance(this);

        addButton.setBackgroundResource(R.mipmap.brown_add_icon_noback);
        backButton.setBackgroundResource(android.R.drawable.ic_delete);
        addButton.setOnClickListener(this);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        attachCursorAdapter();
    }

    private void attachCursorAdapter() {
        cursorAdapter = new CursorAdapter(this, dbHelper.getGameCursor(gameName)) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                String[] attributes = dbHelper.getGameAttributes(gameName);
                LinearLayout v = new LinearLayout(context);
                v.setOrientation(LinearLayout.HORIZONTAL);

                for (int x=1; x<attributes.length; x++) {
                    String attribute = attributes[x];
                    TextView textView = new TextView(context);
                    textView.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    textView.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                    textView.setText(cursor.getString(cursor.getColumnIndex(attribute)));

                    v.addView(textView);
                }
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.animation_1);
                v.startAnimation(animation);
                return v;
            }

            @Override
            public void bindView(View view, Context context, final Cursor cursor) {
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        String[] gameAttributes = dbHelper.getGameAttributes(gameName);
                        dbHelper.removeCard(gameName, cursor.getString(cursor.getColumnIndex(gameAttributes[1])));
                        updateList();
                        return false;
                    }
                });
            }
        };

        cardList.setAdapter(cursorAdapter);
    }

    public void onClick(View view) {
        addNewCard(view);
    }

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
                            userValues.add(userValue.getText().toString());
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

    private void updateList() {
        cursorAdapter.changeCursor(dbHelper.getGameCursor(gameName));
    }
}
