package com.raffaello.nordic.util;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;

public class DatabaseManager {
    private static Database database;
    private static DatabaseManager instance = null;
    private static String dbName = "nordic";
    public String currentUser = null;

    private DatabaseManager(){}

    public static DatabaseManager getInstance(){
        if (instance == null){
            instance = new DatabaseManager();
        }

        return instance;
    }

    public static Database getDatabase() {
        return database;
    }

    public void initCouchbaseLite(Context context) {
        CouchbaseLite.init(context);
    }

    public void openOrCreateDatabaseForUser(Context context, String username)
    {

        Log.i("messaggio", "Database aperto per utente");
        DatabaseConfiguration config = new DatabaseConfiguration();
        config.setDirectory(String.format("%s/%s", context.getFilesDir(), username));

        currentUser = username;

        try {
            database = new Database(dbName, config);

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

    public void closeDatabaseForUser() {
        Log.i("messaggio", "Database dell'utente chiuso");
        try {
            if (database != null) {
                database.close();
                database = null;
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

}
