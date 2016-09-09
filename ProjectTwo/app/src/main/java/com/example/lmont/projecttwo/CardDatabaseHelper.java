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

    // The singleton method used for getting the instance of the CardDatabaseHelper class
    public static CardDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CardDatabaseHelper(context);
        }

        return instance;
    }

    // Private constructor class
    private CardDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    // Called a new instance of the db is created
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_GAME_TABLE_SQL);
    }

    // Called when the db version number is increased
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        clearDB();
    }

    // Creates a new card game table
    public void createCardTable(String gameName, ArrayList<String> cardAttributes) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            // Add game name to games table
            ContentValues contentValues = new ContentValues();
            contentValues.put(GAME_TABLE_FOREIGN_KEY, gameName);
            db.insert(GAME_TABLE_NAME, null, contentValues);

            // Create new cardgame table for game
            // CREATE TABLE IF NOT EXISTS gameName (_id INTEGER PRIMARY KEY AUTOINCREMENT,
            String createGame = "CREATE TABLE IF NOT EXISTS '" + gameName + "'(_id INTEGER PRIMARY KEY AUTOINCREMENT";
            for (String cardAttribute : cardAttributes) {
                createGame += ", '" + cardAttribute + "' TEXT";
            }
            createGame += ")";

            db.execSQL(createGame);
        } catch (SQLiteException e) {
            Log.d(TAG, "createCardTable: FAILED");
            db.execSQL("DELETE FROM " + GAME_TABLE_NAME + " WHERE " + GAME_TABLE_FOREIGN_KEY + " = '" + gameName + "'");
            removeGame(gameName);
        }
    }

    // Creates a new card game table while also outputting a toast naming the new card game
    // attributes
    public void createCardTable(Context context, String gameName, ArrayList<String> cardAttributes) {
        createCardTable(gameName, cardAttributes);
        Toast.makeText(context, gameName + " " + cardAttributes.toString(), Toast.LENGTH_SHORT).show();
    }

    // Return the position of a card game in the cardgames table
    public int getIndexOfGame(String gameName) {
        return getCardGameNames().indexOf(gameName);
    }

    // Returns an ArrayList containing all the names of the card games in the cardgames table
    public ArrayList<String> getCardGameNames() {
        ArrayList<String> cardGameNames = new ArrayList<>();
        // SELECT games FROM cardgames
        Cursor gameNamesSQL = getReadableDatabase().rawQuery(
                "SELECT " + GAME_TABLE_FOREIGN_KEY + " FROM '" + GAME_TABLE_NAME + "'",
                null);
        while (gameNamesSQL.moveToNext()) {
            String game = gameNamesSQL.getString(gameNamesSQL.getColumnIndex(GAME_TABLE_FOREIGN_KEY));
            cardGameNames.add(game);
        }
        return cardGameNames;
    }

    // This method returns a cursor with all the card games in the cardgames table
    public Cursor getCardGamesCursor() {
        Cursor gameNamesSQL = getReadableDatabase().rawQuery(
                "SELECT * FROM '" + GAME_TABLE_NAME + "'",
                null);
        return gameNamesSQL;
    }

    // This method returns a cursor that points to all card games whose name contains a certain
    // string
    public Cursor getCardGamesCursor(String contains) {
        String searchSQL = "SELECT * FROM '" + GAME_TABLE_NAME + "' WHERE " + GAME_TABLE_FOREIGN_KEY + " LIKE '%" + contains + "%'";
        Cursor gameNamesSQL = getReadableDatabase().rawQuery(searchSQL, null);
        return gameNamesSQL;
    }

    // This method returns a String array of all the columns/attributes associated to a card game
    public String[] getGameAttributes(String game) {
        Cursor dbCursor = getReadableDatabase().query("'" + game + "'", null, null, null, null, null, null);
        return dbCursor.getColumnNames();
    }

    // This method is used to add a new card to a card game
    public void createNewCard(String game, ArrayList<String> values) {
        String[] colNames = getGameAttributes(game);
        ContentValues contentValues = new ContentValues();
        for(int x=0; x<values.size(); x++) {
            contentValues.put(colNames[x+1], values.get(x));
        }
        getWritableDatabase().insert("'" + game + "'", null, contentValues);
    }

    // This method removes a card from a specific card game
    public void removeCard(String game, String cardID) {
        getWritableDatabase().delete(
                "'" + game + "'",
                getGameAttributes(game)[1] + " = " + "'" + cardID + "'",
                null);
    }

    // This method returns a cursor for all the cards in a game
    public Cursor getCardCursor(String game) {
        return getReadableDatabase().rawQuery("SELECT * FROM '" + game + "'", null);
    }

    // This method returns a cursor that points to all the cards in a game that match contain
    // a certain string
    public Cursor getCardCursor(String contains, String game) {
        String searchSQL = "SELECT * FROM '" + game + "' WHERE \"" + getGameAttributes(game)[1] + "\" LIKE '%" + contains + "%'";
        Log.d(TAG, "getCardCursor: " + searchSQL);
        return getReadableDatabase().rawQuery(searchSQL, null);
    }

    // This method removes a card game from the cardgames table
    public void removeGame(String game) {
        try {
            getWritableDatabase().execSQL("DELETE FROM '" + GAME_TABLE_NAME + "' WHERE " + GAME_TABLE_FOREIGN_KEY + " = '" + game + "'");
            getWritableDatabase().execSQL("DROP TABLE '" + game + "'");
        } catch (SQLiteException e) {
            Log.d(TAG, "removeGame: FAILED");
        }
    }

    // This method returns an ArrayList with all of the attributes of a card
    public ArrayList<String> getCardInfo(String game, String cardName) {
        int x=0;
        ArrayList<String> cardInfo = new ArrayList<>();
        String[] attributes = getGameAttributes(game);
        Cursor cursor = getReadableDatabase().query("'" + game + "'", attributes, attributes[1] + " = '" + cardName + "'", null, null, null, null);

        cursor.moveToFirst();
        for (String attribute: attributes) {
            if (attribute.equals("_id")) continue;
            cardInfo.add(attribute + ": " + cursor.getString(cursor.getColumnIndex(attribute)));
        }

        return cardInfo;
    }

    // This method is used for clearing out the database safely
    public void clearDB() {
        onCreate(getWritableDatabase());
        for (String gameName: getCardGameNames()) {
            try {
                getWritableDatabase().execSQL("DROP TABLE '" + gameName + "'");
            } catch (SQLException e) {
                Log.d(TAG, "clearDB: DROP TABLE " + gameName + " failed");
            }
        }
        getWritableDatabase().execSQL("DROP TABLE '" + GAME_TABLE_NAME + "'");
        onCreate(getWritableDatabase());
    }
}
