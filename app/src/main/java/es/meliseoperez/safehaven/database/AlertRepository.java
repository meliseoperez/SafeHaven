package es.meliseoperez.safehaven.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import es.meliseoperez.safehaven.api.aemet.AlertInfo;

public class AlertRepository implements AutoCloseable{

    private static final String TAG = "Alert respository";
    // Base de datos y ayudante de SQLite
    private SQLiteDatabase database;
    private final AlertDBHelper dbHelper;

    public AlertRepository(Context context) {

        dbHelper = new AlertDBHelper(context);
    }
    // Abre la base de datos para escritura
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();

    }
    // Cierra la conexión a la base de datos
   /* public void close() {
        dbHelper.close();
    }*/
    // Inserta una nueva alerta en la base de datos
    public long insertAlert(AlertInfo alert, String tableName) {
        ContentValues values = new ContentValues();
        values.put(AlertContract.AlertEntry.COLUMN_ID, alert.id);
        values.put(AlertContract.AlertEntry.COLUMN_EFFECTIVE, alert.effective);
        values.put(AlertContract.AlertEntry.COLUMN_ONSET, alert.onset);
        values.put(AlertContract.AlertEntry.COLUMN_EXPIRES, alert.expires);
        values.put(AlertContract.AlertEntry.COLUMN_SENDER_NAME, alert.senderName);
        values.put(AlertContract.AlertEntry.COLUMN_HEADLINE, alert.headline);
        values.put(AlertContract.AlertEntry.COLUMN_DESCRIPTION, alert.description);
        values.put(AlertContract.AlertEntry.COLUMN_INSTRUCTION, alert.instruction);
        values.put(AlertContract.AlertEntry.COLUMN_LANGUAGE, alert.language);
        values.put(AlertContract.AlertEntry.COLUMN_POLYGON, alert.polygon);

        // Inserta el registro y devuelve el ID del nuevo registro, o -1 si hay un error
        return database.insert(tableName, null, values);
    }
    // Recupera todas las alertas de la base de datos
    public List<AlertInfo> getAllAlerts() {
        List<AlertInfo> alerts = new ArrayList<>();
        Cursor cursor = null;
        try{
            // Consulta todos los registros
            cursor = database.query(AlertContract.AlertEntry.TABLE_NAME, null, null, null, null, null, null);
            // Itera sobre los resultados y convierte cada registro a un objeto AlertInfo
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                AlertInfo alert = cursorToAlert(cursor);
                alerts.add(alert);
                cursor.moveToNext();
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return alerts;
    }

    public String extractColumnValue(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex != -1) {
            return cursor.getString(columnIndex);
        } else {
            // Puedes decidir manejarlo como quieras: lanzar una excepción, loguear un error, etc.
            Log.e(TAG, "Columna no encontrada: " + columnName);
            return null;  // o cualquier valor predeterminado que quieras establecer
        }
    }
    // Convierte un registro de Cursor a un objeto AlertInfo
    private AlertInfo cursorToAlert(Cursor cursor) {
        AlertInfo alert = new AlertInfo();
        alert.id = Integer.parseInt(extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_ID));
        alert.effective = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_EFFECTIVE);
        alert.onset =extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_ONSET);
        alert.expires = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_EXPIRES);
        alert.senderName = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_SENDER_NAME);
        alert.headline = extractColumnValue(cursor,AlertContract.AlertEntry.COLUMN_HEADLINE);
        alert.description = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_DESCRIPTION);
        alert.instruction = extractColumnValue(cursor,AlertContract.AlertEntry.COLUMN_INSTRUCTION);
        alert.language = extractColumnValue(cursor,AlertContract.AlertEntry.COLUMN_LANGUAGE);
        alert.polygon = extractColumnValue(cursor,AlertContract.AlertEntry.COLUMN_POLYGON);

        return alert;
    }

    public AlertInfo getAlertById(int alertID) {
        AlertInfo alert = null;
        Cursor cursor = null;

        // Asegúrate de que 'database' esté inicializado
        if (database != null) {
            try {
                String seleccion = AlertContract.AlertEntry.COLUMN_ID + " = ?";
                String[] selectionArgs = {String.valueOf(alertID)};

                cursor = database.query(
                        AlertContract.AlertEntry.TABLE_NAME,
                        null,
                        seleccion,
                        selectionArgs,
                        null,
                        null,
                        null
                );

                if (cursor.moveToFirst()) {
                    alert = cursorToAlert(cursor);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al buscar la alerta por ID", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Log.e(TAG, "Database is null, cannot query");
        }
        return alert;
    }



    @Override
    public void close() {
        dbHelper.close();
    }

    public SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
        return database;
    }

    public void eliminarAlertaPorId(Integer id) {
        // Abre la base de datos para escritura
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define el criterio de selección, que es nuestra cláusula WHERE en SQL
        // En este caso, estamos buscando una fila con el ID específico
        String selection = AlertContract.AlertEntry.COLUMN_ID + " = ?";

        // Especifica los argumentos en formato de matriz para el criterio de selección
        // SQLite trata estos argumentos para prevenir inyecciones SQL
        String[] selectionArgs = { id.toString() };

        // Realiza la operación de eliminación
        db.delete(AlertContract.AlertEntry.TABLE_NAME, selection, selectionArgs);
    }

}
