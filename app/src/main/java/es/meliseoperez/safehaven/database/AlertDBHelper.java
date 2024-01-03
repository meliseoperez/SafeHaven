package es.meliseoperez.safehaven.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AlertDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Alerts.db";


    public AlertDBHelper(@Nullable Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AlertContract.AlertEntry.TABLE_NAME + " (" +
                    AlertContract.AlertEntry._ID + " INTEGER PRIMARY KEY," +
                    AlertContract.AlertEntry.COLUMN_EFFECTIVE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_ONSET + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_EXPIRES + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_SENDER_NAME + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_HEADLINE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_DESCRIPTION + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_INSTRUCTION + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_LANGUAGE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_POLYGON + " TEXT)";
    private static final String SQL_CREATE_SECOND_TABLE =
            "CREATE TABLE " + SecondTableContract.SecondTableEntry.TABLE_NAME + " (" +
                    SecondTableContract.SecondTableEntry._ID + " INTEGER PRIMARY KEY," +
                    SecondTableContract.SecondTableEntry.COLUMN_POLYGON + " TEXT," +
                    SecondTableContract.SecondTableEntry.COLUMN_INSTRUCTION + " TEXT," +
                    SecondTableContract.SecondTableEntry.COLUMN_DESCRIPTION + " TEXT," +
                    SecondTableContract.SecondTableEntry.COLUMN_EXPIRES + " TEXT," +
                    SecondTableContract.SecondTableEntry.COLUMN_HEADLINE + " TEXT"+
                    " );";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AlertContract.AlertEntry.TABLE_NAME;
    private static final String SQL_DELETE_SECOND_TABLE =
            "DROP TABLE IF EXISTS " + SecondTableContract.SecondTableEntry.TABLE_NAME;
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_SECOND_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_SECOND_TABLE);
        onCreate(db);

    }
}
