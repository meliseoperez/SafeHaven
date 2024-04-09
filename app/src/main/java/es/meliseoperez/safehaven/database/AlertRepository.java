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

/**
 * Gestiona operaciones de base de datos para alertas, incluyendo inserción, consulta y eliminación.
 */
public class AlertRepository implements AutoCloseable {

    private static final String TAG = "AlertRepository";
    private SQLiteDatabase database;
    private final AlertDBHelper dbHelper;

    public AlertRepository(Context context) {
        dbHelper = new AlertDBHelper(context);
    }

    /**
     * Abre la base de datos para realizar operaciones de escritura y lectura.
     *
     * @throws SQLException Si la base de datos no se puede abrir para escritura.
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Inserta una nueva alerta en la base de datos.
     *
     * @param alert     La alerta a insertar.
     * @param tableName Nombre de la tabla donde insertar la alerta.
     * @return El ID del nuevo registro insertado, o -1 si ocurre un error.
     */
    public long insertAlert(AlertInfo alert, String tableName) {
        ContentValues values = new ContentValues();
        // Aquí se asume que alert.id es un String, de ser un int se debe remover la conversión.
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

        return database.insert(tableName, null, values);
    }

    /**
     * Recupera todas las alertas de la base de datos.
     *
     * @return Una lista de AlertInfo con todas las alertas almacenadas.
     */
    public List<AlertInfo> getAllAlerts() {
        List<AlertInfo> alerts = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(AlertContract.AlertEntry.TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    alerts.add(cursorToAlert(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return alerts;
    }

    /**
     * Extrae el valor de una columna dada por su nombre del cursor proporcionado.
     *
     * @param cursor     El cursor desde donde extraer el valor.
     * @param columnName El nombre de la columna a extraer.
     * @return El valor de la columna como String, o null si la columna no se encuentra.
     */
    public String extractColumnValue(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return columnIndex != -1 ? cursor.getString(columnIndex) : null;
    }

    /**
     * Convierte un registro del cursor en un objeto AlertInfo.
     *
     * @param cursor El cursor que apunta al registro a convertir.
     * @return Una instancia de AlertInfo con los datos del registro.
     */
    private AlertInfo cursorToAlert(Cursor cursor) {
        AlertInfo alert = new AlertInfo();
        alert.id = Integer.parseInt(extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_ID));
        alert.effective = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_EFFECTIVE);
        alert.onset = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_ONSET);
        alert.expires = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_EXPIRES);
        alert.senderName = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_SENDER_NAME);
        alert.headline = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_HEADLINE);
        alert.description = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_DESCRIPTION);
        alert.instruction = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_INSTRUCTION);
        alert.language = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_LANGUAGE);
        alert.polygon = extractColumnValue(cursor, AlertContract.AlertEntry.COLUMN_POLYGON);
        return alert;
    }

    /**
     * Busca una alerta por su ID y la devuelve.
     *
     * @param alertID El ID de la alerta a buscar.
     * @return La alerta encontrada o null si no se encuentra ninguna alerta con ese ID.
     */
    public AlertInfo getAlertById(int alertID) {
        String selection = AlertContract.AlertEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(alertID)};

        try (Cursor cursor = database.query(AlertContract.AlertEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToAlert(cursor);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error al buscar la alerta por ID: " + alertID, e);
        }
        return null;
    }

    @Override
    public void close() {
        dbHelper.close();
    }

    /**
     * Elimina una alerta de la base de datos por su ID.
     *
     * @param id El ID de la alerta a eliminar.
     */
    public void eliminarAlertaPorId(Integer id) {
        // Abre la base de datos para escritura
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define el criterio de selección, que es nuestra cláusula WHERE en SQL
        // En este caso, estamos buscando una fila con el ID específico
        String selection = AlertContract.AlertEntry.COLUMN_ID + " = ?";

        // Especifica los argumentos en formato de matriz para el criterio de selección
        // SQLite trata estos argumentos para prevenir inyecciones SQL
        String[] selectionArgs = {id.toString()};

        // Realiza la operación de eliminación
        db.delete(AlertContract.AlertEntry.TABLE_NAME, selection, selectionArgs);
    }

    public SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
        return database;
    }

}
