package com.example.lmont.projecttwo;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Context activityContext;
    Button addButton;
    TextView headerTextView;
    ListView gameListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
    }

    protected void setup() {
        activityContext = this;
        addButton = (Button) findViewById(R.id.game_add_button);
        headerTextView = (TextView) findViewById(R.id.game_header_textview);
        gameListView = (ListView) findViewById(R.id.game_listview);

        addButton.setBackgroundResource(R.mipmap.brown_add_icon_noback);
        addButton.setOnClickListener(this);
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

                        if (number > 0)
                            nameCardAttributes(nameTextView.getText().toString(), number);
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
                        CardDatabaseHelper.getInstance(activityContext).createCardTable(activityContext, gameName, attributes);
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

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
