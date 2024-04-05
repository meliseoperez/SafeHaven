package es.meliseoperez.safehaven.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AlertDBHelperTest {

    private AlertDBHelper dbHelper;
    private SQLiteDatabase database;

    @Before
    public void setUp() throws Exception {
        // Conseguir el contexto de la aplicación de prueba
        Context context = ApplicationProvider.getApplicationContext();
        // Asegurarse de que cada prueba comience con una base de datos limpia
        context.deleteDatabase(AlertDBHelper.DATABASE_NAME);
        // Inicializar el dbHelper para la prueba
        dbHelper = new AlertDBHelper(context);
        // Obtener la base de datos en modo escritura
        database = dbHelper.getWritableDatabase();
    }

    @After
    public void tearDown() throws Exception {
        // Cerrar la base de datos después de cada prueba
        database.close();
        // Eliminar la base de datos después de la prueba
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(AlertDBHelper.DATABASE_NAME);
    }

    @Test
    public void testDatabaseCreation() {
        // Verificar que la base de datos está abierta
        assertTrue(database.isOpen());

        //Verifica la existencia de la tabla y la estructura de la tabla
        Cursor cursor = database.rawQuery("PRAGMA table_info(" + AlertContract.AlertEntry.TABLE_NAME + ");", null);        assertTrue("La tabla alerts debe tener columnas", cursor.getCount()>0);

        int columnNameIndex = cursor.getColumnIndex("name");
        List<String> columnNames = new ArrayList<>();
        while (cursor.moveToNext()){
            columnNames.add(cursor.getString(columnNameIndex));
        }
        //Verifia que las columnas esperadas existen
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_ID));
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_DESCRIPTION));
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_EFFECTIVE));
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_SENDER_NAME));
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_EXPIRES));
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_HEADLINE));
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_LANGUAGE));
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_ONSET));
        assertTrue(columnNames.contains(AlertContract.AlertEntry.COLUMN_POLYGON));

        cursor.close();
    }
    @Test
    public void testDatabaseUpgrade() {
        // Obtener la instancia actual de la base de datos y establecer manualmente una versión antigua
        SQLiteDatabase tempDatabase = dbHelper.getWritableDatabase();
        int currentVersion = tempDatabase.getVersion();
        int newVersion = currentVersion + 1;
        tempDatabase.setVersion(currentVersion - 1); // Establece una versión antigua para forzar la actualización
        tempDatabase.close();

        // Instanciar nuevamente AlertDBHelper debería ahora disparar onUpgrade debido a la discrepancia de versión
        AlertDBHelper newDbHelper = new AlertDBHelper(ApplicationProvider.getApplicationContext());
        SQLiteDatabase newDatabase = newDbHelper.getWritableDatabase();

        // Verificar que la versión de la base de datos se haya actualizado
        assertEquals("La versión de la base de datos debería haberse incrementado", newVersion, newDatabase.getVersion());
        newDatabase.close();
    }



    @Test
    public void testTrigger() {
        // Insertar una alerta con fecha de expiración pasada
        ContentValues values = new ContentValues();
        values.put(AlertContract.AlertEntry.COLUMN_EXPIRES, "2000-01-01 00:00:00"); // Fecha en el pasado
        // Añadir otros valores necesarios según tu esquema
        long id = database.insert(AlertContract.AlertEntry.TABLE_NAME, null, values);
        assertTrue(id != -1);

        // Realizar una nueva inserción para activar el trigger
        ContentValues newValues = new ContentValues();
        newValues.put(AlertContract.AlertEntry.COLUMN_EXPIRES, "2999-12-31 23:59:59"); // Fecha en el futuro
        // Añadir otros valores necesarios según tu esquema
        long newId = database.insert(AlertContract.AlertEntry.TABLE_NAME, null, newValues);
        assertTrue(newId != -1);

        // Verificar que la alerta expirada ha sido eliminada
        Cursor cursor = database.query(AlertContract.AlertEntry.TABLE_NAME,
                new String[] {AlertContract.AlertEntry.COLUMN_ID},
                AlertContract.AlertEntry.COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)},
                null, null, null);

        assertFalse("La alerta expirada debería haber sido eliminada por el trigger", cursor.moveToFirst());
        cursor.close();
    }

}
