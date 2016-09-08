package com.example.lmont.projecttwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by lmont on 9/6/2016.
 */
public class CardDatabaseHelper extends SQLiteOpenHelper {

    //TABLE NAMES
    public static final String GAME_TABLE_NAME = "cardgames";
    public static final String GAME_TABLE_ID_NAME = "_id";
    public static final String GAME_TABLE_FOREIGN_KEY = "games";

    //CREATE TABLE IF NOT EXISTS cardgames (_id INTEGER PRIMARY KEY AUTOINCREMENT, games TEXT UNIQUE);
    private static final String CREATE_GAME_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + GAME_TABLE_NAME + " (" + GAME_TABLE_ID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GAME_TABLE_FOREIGN_KEY + " TEXT UNIQUE);";


    static final int DB_VERSION = 1;
    static final String DB_NAME = "cardbase.db";
    static final String TAG = "LEO";

    private static CardDatabaseHelper instance;

    public static CardDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CardDatabaseHelper(context);
        }

        return instance;
    }

    private CardDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_GAME_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        clearDB();
    }

    public void createCardTable(String gameName, ArrayList<String> cardAttributes) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            // Add game name to games table
            ContentValues contentValues = new ContentValues();
            contentValues.put(GAME_TABLE_FOREIGN_KEY, gameName);
            db.insert(GAME_TABLE_NAME, null, contentValues);

            // Create new cardgame table for game
            // CREATE TABLE IF NOT EXISTS gameName (_id INTEGER PRIMARY KEY AUTOINCREMENT,
            String createGame = "CREATE TABLE IF NOT EXISTS " + gameName + "(_id INTEGER PRIMARY KEY AUTOINCREMENT";
            for (String cardAttribute : cardAttributes) {
                createGame += ", " + cardAttribute + " TEXT";
            }
            createGame += ")";

            db.execSQL(createGame);
        } catch (SQLiteException e) {
            Log.d(TAG, "createCardTable: FAILED");
            db.execSQL("DELETE FROM " + GAME_TABLE_NAME + " WHERE " + GAME_TABLE_FOREIGN_KEY + " = '" + gameName + "'");
            removeGame(gameName);
        }
    }

    public void createCardTable(Context context, String gameName, ArrayList<String> cardAttributes) {
        createCardTable(gameName, cardAttributes);
        Toast.makeText(context, gameName + " " + cardAttributes.toString(), Toast.LENGTH_SHORT).show();
    }

    public int getIndexOfGame(String gameName) {
        return getCardGameNames().indexOf(gameName);
    }

    public ArrayList<String> getCardGameNames() {
        ArrayList<String> cardGameNames = new ArrayList<>();
        // SELECT games FROM cardgames
        Cursor gameNamesSQL = getReadableDatabase().rawQuery(
                "SELECT " + GAME_TABLE_FOREIGN_KEY + " FROM " + GAME_TABLE_NAME,
                null);
        while (gameNamesSQL.moveToNext()) {
            String game = gameNamesSQL.getString(gameNamesSQL.getColumnIndex(GAME_TABLE_FOREIGN_KEY));
            cardGameNames.add(game);
        }
        return cardGameNames;
    }

    public Cursor getCardGamesCursor() {
        Cursor gameNamesSQL = getReadableDatabase().rawQuery(
                "SELECT * FROM " + GAME_TABLE_NAME,
                null);
        return gameNamesSQL;
    }

    public String[] getGameAttributes(String game) {
        Cursor dbCursor = getReadableDatabase().query(game, null, null, null, null, null, null);
        return dbCursor.getColumnNames();
    }

    public void createNewCard(String game, ArrayList<String> values) {
        String[] colNames = getGameAttributes(game);
        ContentValues contentValues = new ContentValues();
        for(int x=0; x<values.size(); x++) {
            contentValues.put(colNames[x+1], values.get(x));
        }
        getWritableDatabase().insert(game, null, contentValues);
    }

    public void removeCard(String game, String cardID) {
        getWritableDatabase().delete(
                game,
                getGameAttributes(game)[1] + " = " + "'" + cardID + "'",
                null);
    }

    public Cursor getGameCursor(String game) {
        return getReadableDatabase().rawQuery("SELECT * FROM " + game, null);
    }

    public void removeGame(String game) {
        try {
            getWritableDatabase().execSQL("DELETE FROM " + GAME_TABLE_NAME + " WHERE " + GAME_TABLE_FOREIGN_KEY + " = '" + game + "'");
            getWritableDatabase().execSQL("DROP TABLE " + game);
        } catch (SQLiteException e) {
            Log.d(TAG, "removeGame: FAILED");
        }
    }

    public ArrayList<String> getCardInfo(String game, String cardName) {
        int x=0;
        ArrayList<String> cardInfo = new ArrayList<>();
        String[] attributes = getGameAttributes(game);
//        String getCardInfoSQL = "SELECT * FROM ? WHERE ? = '?'";
//        String[] getCardInfoSQLArgs = new String[]{game, attributes[1], cardName};
//        Cursor cursor = getReadableDatabase().rawQuery(getCardInfoSQL, getCardInfoSQLArgs);
        Cursor cursor = getReadableDatabase().query(game, attributes, attributes[1] + " = '" + cardName + "'", null, null, null, null);

        cursor.moveToFirst();
        for (String attribute: attributes) {
            if (attribute.equals("_id")) continue;
            cardInfo.add(attribute + ": " + cursor.getString(cursor.getColumnIndex(attribute)));
        }

        return cardInfo;
    }

    public void clearDB() {
        onCreate(getWritableDatabase());
        for (String gameName: getCardGameNames()) {
            try {
                getWritableDatabase().execSQL("DROP TABLE " + gameName);
            } catch (SQLException e) {
                Log.d(TAG, "clearDB: DROP TABLE " + gameName + " failed");
            }
        }
        getWritableDatabase().execSQL("DROP TABLE " + GAME_TABLE_NAME);
        onCreate(getWritableDatabase());
    }
}
