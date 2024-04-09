package es.meliseoperez.safehaven.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlertDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Alerts.db";

    // SQL para crear la tabla de alertas.
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + AlertContract.AlertEntry.TABLE_NAME + " (" +
                    AlertContract.AlertEntry.COLUMN_ID + " INTEGER PRIMARY KEY," + // Asignado como clave primaria.
                    AlertContract.AlertEntry.COLUMN_EFFECTIVE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_ONSET + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_EXPIRES + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_SENDER_NAME + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_HEADLINE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_DESCRIPTION + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_INSTRUCTION + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_LANGUAGE + " TEXT," +
                    AlertContract.AlertEntry.COLUMN_POLYGON + " TEXT)";

    // SQL para eliminar la tabla de alertas, utilizado en actualizaciones de la base de datos.
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AlertContract.AlertEntry.TABLE_NAME;

    public AlertDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Ejecuta el SQL para crear la tabla de alertas.
        db.execSQL(SQL_CREATE_ENTRIES);

        // SQL para crear un trigger que elimina alertas expiradas después de cada borrado.
        String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER IF NOT EXISTS delete_expired_alerts " +
                        "AFTER INSERT ON " + AlertContract.AlertEntry.TABLE_NAME + " " +
                        "BEGIN " +
                        "   DELETE FROM " + AlertContract.AlertEntry.TABLE_NAME + " " +
                        "   WHERE datetime(" + AlertContract.AlertEntry.COLUMN_EXPIRES + ") < datetime('now', 'localtime'); " +
                        "END;";
        db.execSQL(SQL_CREATE_TRIGGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En caso de actualización de la base de datos, elimina la tabla existente y crea una nueva.
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
