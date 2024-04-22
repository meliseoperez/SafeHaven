package es.meliseoperez.safehaven.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AlertDBHelperTest {

    private AlertDBHelper dbHelper;
    private SQLiteDatabase database;

    /**
     * Configura el entorno antes de cada test.
     * Este método se ejecuta antes de cada test. Prepara un contexto de aplicación de prueba
     * y establece una base de datos limpia para asegurar que los tests sean independientes
     * entre sí y no tengan efectos secundarios cruzados.
     */
    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(AlertDBHelper.DATABASE_NAME); // Elimina cualquier instancia previa de la BD para asegurar un entorno limpio
        dbHelper = new AlertDBHelper(context); // Crea una nueva instancia del helper de la BD
        database = dbHelper.getWritableDatabase(); // Abre la base de datos en modo escritura
    }

    /**
     * Limpia recursos después de cada test.
     * Este método se ejecuta después de cada test. Cierra la base de datos y la elimina,
     * garantizando que cada test se ejecute en un estado inicial limpio.
     */
    @After
    public void tearDown() throws Exception {
        database.close(); // Cierra la conexión a la base de datos
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(AlertDBHelper.DATABASE_NAME); // Elimina la base de datos para evitar contaminación entre tests
    }

    /**
     * Verifica la correcta creación de la base de datos y su estructura.
     * Este test confirma que la base de datos se crea con la estructura de tablas esperada,
     * validando la presencia de todas las columnas necesarias en la tabla de alertas.
     */
    @Test
    public void testDatabaseCreation() {
        assertTrue("La base de datos debería estar abierta", database.isOpen());

        Cursor cursor = database.rawQuery("PRAGMA table_info(" + AlertContract.AlertEntry.TABLE_NAME + ");", null);
        assertTrue("La tabla alerts debe tener columnas", cursor.getCount() > 0);

        List<String> columnNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            columnNames.add(cursor.getString(cursor.getColumnIndex("name")));
        }

        // Asegura que todas las columnas necesarias están presentes.
        List<String> expectedColumns = Arrays.asList(AlertContract.AlertEntry.COLUMN_ID, AlertContract.AlertEntry.COLUMN_DESCRIPTION,
                AlertContract.AlertEntry.COLUMN_EFFECTIVE, AlertContract.AlertEntry.COLUMN_SENDER_NAME, AlertContract.AlertEntry.COLUMN_EXPIRES,
                AlertContract.AlertEntry.COLUMN_HEADLINE, AlertContract.AlertEntry.COLUMN_LANGUAGE, AlertContract.AlertEntry.COLUMN_ONSET,
                AlertContract.AlertEntry.COLUMN_POLYGON);
        assertTrue("Deben existir todas las columnas esperadas", columnNames.containsAll(expectedColumns));

        cursor.close();
    }

    /**
     * Prueba la lógica de actualización de la base de datos.
     * Este test simula una actualización de la base de datos incrementando su versión,
     * y verifica que la tabla se recrea correctamente según lo definido en el método onUpgrade.
     */
    @Test
    public void testDatabaseUpgrade() {
        int oldVersion = 1;
        int newVersion = 2;
        dbHelper.onUpgrade(database, oldVersion, newVersion);

        Cursor cursor = database.rawQuery("PRAGMA table_info(" + AlertContract.AlertEntry.TABLE_NAME + ");", null);
        assertNotNull("El cursor no debería ser nulo", cursor);
        assertTrue("La tabla debería haber sido recreada", cursor.getCount() > 0);
        cursor.close();

        database.setVersion(newVersion);
    }

    /**
     * Verifica el funcionamiento de un trigger en la base de datos.
     * Este test inserta dos registros de alertas, una expirada y otra válida, y verifica que el trigger
     * elimina correctamente la alerta expirada tras una inserción.
     */
    @Test
    public void testTrigger() {
        insertAlert("2000-01-01 00:00:00", "Test Expired Alert");
        insertAlert("2999-12-31 23:59:59", "Test Valid Alert");

        Cursor cursor = database.rawQuery("SELECT * FROM " + AlertContract.AlertEntry.TABLE_NAME + " WHERE " +
                "datetime(" + AlertContract.AlertEntry.COLUMN_EXPIRES + ") < datetime('now', 'localtime')", null);
        assertFalse("La alerta expirada debería haber sido eliminada por el trigger", cursor.moveToFirst());
        cursor.close();
    }

    /**
     * Método de ayuda para insertar alertas en la base de datos.
     * @param expires Fecha de expiración de la alerta.
     * @param description Descripción de la alerta.
     */
    private void insertAlert(String expires, String description) {
        ContentValues values = new ContentValues();
        values.put(AlertContract.AlertEntry.COLUMN_EXPIRES, expires);
        values.put(AlertContract.AlertEntry.COLUMN_DESCRIPTION, description);
        database.insert(AlertContract.AlertEntry.TABLE_NAME, null, values);
    }
}
