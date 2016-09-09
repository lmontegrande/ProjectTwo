package com.example.lmont.projecttwo;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

// This activity displays a card's information
public class CardActivity extends AppCompatActivity {

    public static final String
        CARD_KEY = "cardname",
        GAME_KEY = "gamename";

    Button backButton;
    TextView cardNameTextView;
    ListView cardInfoListView;
    String cardName, gameName;
    CardDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        setup();
        bindData();
    }

    // Binds the card's data with the data from the database to the listview
    private void bindData() {
        cardNameTextView.setText(gameName);
        ArrayList<String> cardData = dbHelper.getCardInfo(gameName, cardName);

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.card_data_list_item, cardData);
        cardInfoListView.setAdapter(arrayAdapter);
    }

    // Initialize variables and views
    private void setup() {
        Intent intent = getIntent();
        backButton = (Button) findViewById(R.id.card_activity_back_button);
        cardNameTextView = (TextView) findViewById(R.id.card_activity_header_textview);
        cardInfoListView = (ListView) findViewById(R.id.card_activity_information_listview);
        cardName = intent.getStringExtra(CARD_KEY);
        gameName = intent.getStringExtra(GAME_KEY);
        dbHelper = CardDatabaseHelper.getInstance(this);

        backButton.setBackgroundResource(android.R.drawable.ic_delete);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
