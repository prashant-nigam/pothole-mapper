package project.saurabh.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import project.saurabh.entity.Potholes;


public class DBController extends SQLiteOpenHelper {
    Context application_context;
    public static final String TAG = "DBController";
    public static final int database_version = 1;
    public static final String database_name = "project.db";

    static final String potholeTableName = "potholesData";
    public DBController(Context context) {
        super(context, database_name, null, database_version);
        this.application_context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String potholesDataQuery = "CREATE TABLE "+potholeTableName+" (Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "latitude TEXT, " +
                "longitude TEXT," +
                "picLocation TEXT," +
                "severity TEXT NOT NULL)";

        try {
            try {
                dropTable(db, potholeTableName);
                db.execSQL(potholesDataQuery);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void dropTable(SQLiteDatabase db, String tableName) {
        String query;
        try {
            query = "DROP TABLE IF EXISTS " + tableName;
            db.execSQL(query);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public List<Potholes> getAllPotholes() {
        SQLiteDatabase database = getReadableDatabase();
        List<Potholes> potholesList = new ArrayList<Potholes>();
        String selectSQLUser = "SELECT Id,latitude,longitude,picLocation,severity FROM "+potholeTableName;
        Cursor cursor = database.rawQuery(selectSQLUser, null);

        if (cursor.moveToFirst()) {
            do {
                Potholes pothole = new Potholes();
                pothole.setId(cursor.getInt(0));
                pothole.setLatitude(cursor.getString(1));
                pothole.setLongitude(cursor.getString(2));
                pothole.setPicLocation(cursor.getString(3));
                pothole.setSeverity(cursor.getString(4));

                potholesList.add(pothole);
            } while (cursor.moveToNext());
        }
        return potholesList;
    }

    public boolean addNewPothole (Potholes pothole) {
        SQLiteDatabase database = getWritableDatabase();
        if (pothole != null) {
            try {
                // Inserting into potholes
                ContentValues values = new ContentValues();
                values.put("latitude", pothole.getLatitude());
                values.put("longitude", pothole.getLongitude());
                values.put("picLocation", pothole.getPicLocation());
                values.put("severity", pothole.getSeverity());
                database.insert(potholeTableName, null, values);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}