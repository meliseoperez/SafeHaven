package es.meliseoperez.safehaven.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AlertDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Alerts.db";

    public AlertDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + AlertContract.AlertEntry.TABLE_NAME + " (" +
                    AlertContract.AlertEntry.COLUMN_ID + " INTEGER," +
                    AlertContract.AlertEntry.COLUMN_EFFECTIVE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_ONSET + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_EXPIRES + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_SENDER_NAME + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_HEADLINE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_DESCRIPTION + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_INSTRUCTION + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_LANGUAGE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_POLYGON + " TEXT)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AlertContract.AlertEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        // Crear el trigger para eliminar alertas expiradas cada vez que se inserte un nuevo
        // registro
        String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER IF NOT EXISTS delete_expired_alerts " +
                        "AFTER DELETE ON " + AlertContract.AlertEntry.TABLE_NAME + " " +
                        "BEGIN " +
                        "   DELETE FROM " + AlertContract.AlertEntry.TABLE_NAME + " " + // Asegúrate de tener un espacio antes de WHERE
                        "   WHERE datetime(" + AlertContract.AlertEntry.COLUMN_EXPIRES + ")  < datetime('now', 'localtime'); " +
                        "END;";
        db.execSQL(SQL_CREATE_TRIGGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
