package com.example.lmont.projecttwo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by lmont on 9/6/2016.
 */
public class CardDatabaseHelper extends SQLiteOpenHelper {

    private static CardDatabaseHelper instance;

    static final int DB_VERSION = 1;
    static final String DB_NAME = "Card base";

    public static CardDatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new CardDatabaseHelper(context);

        return instance;
    }

    public CardDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        SQLiteDatabase db = getWritableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void createCardTable(String gameName, ArrayList<String> cardAttributes) {

    }

    public void createCardTable(Context context, String gameName, ArrayList<String> cardAttributes) {
        Toast.makeText(context, gameName + " " + cardAttributes.toString(), Toast.LENGTH_SHORT).show();
    }
}
