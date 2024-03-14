package es.meliseoperez.safehaven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.database.AlertContract;
import es.meliseoperez.safehaven.database.AlertRepository;

public class AlertRepositoryTest {

    private AlertRepository alertRepository;
    private SQLiteDatabase database;

    @Before
    public void setUp() {
        // Contexto de la aplicación de prueba
        Context context = ApplicationProvider.getApplicationContext();
        assertNotNull("El contexto no debe ser nulo", context);

        // Inicializar el repositorio
        alertRepository = new AlertRepository(context);
        assertNotNull("El repositorio de alertas no debe ser nulo", alertRepository);

        // Intenta abrir la base de datos
        try {
            alertRepository.open();
            database = alertRepository.getDatabase();
            assertNotNull("La instancia de la base de datos no debe ser nula", database);
            assertTrue("La base de datos debería estar abierta", database.isOpen());
        } catch (Exception e) {
            fail("La inicialización de la base de datos falló con una excepción: " + e.getMessage());
        }
    }


    @After
    public void tearDown() {
        // Cerrar la base de datos después de cada prueba
        alertRepository.close();
    }

    @Test
    public void testOpen() {
        // Caso de prueba para verificar que la base de datos se abre correctamente
        assertTrue("La base de datos debería estar abierta", database.isOpen());
    }

    @Test
    public void testInsertAlert() {
        // Caso de prueba para verificar la inserción correcta de una alerta
        AlertInfo alert = new AlertInfo(); // Crear una instancia de AlertInfo con datos de prueba
        // Configurar los datos de la alerta...
        
        long result = alertRepository.insertAlert(alert, AlertContract.AlertEntry.TABLE_NAME);
        assertTrue("La inserción debería devolver un ID positivo", result > 0);
    }

    @Test
    public void testGetAllAlerts() {
        // Caso de prueba para verificar la recuperación de todas las alertas
        List<AlertInfo> alerts = alertRepository.getAllAlerts();
        assertNotNull("La lista de alertas no debería ser nula", alerts);
        // Aquí podríamos insertar algunas alertas de prueba y luego verificar si se recuperan correctamente
    }

    @Test
    public void testGetAlertById() {
        // Caso de prueba para verificar la recuperación de una alerta por su ID
        int testId = 391; // Este ID debería corresponder a una alerta existente en la base de datos de prueba
        AlertInfo alert = alertRepository.getAlertById(testId);
        assertNotNull("La alerta recuperada no debería ser nula", alert);
        assertEquals("El ID de la alerta recuperada debería coincidir con el solicitado", testId, alert.id);
    }

    @Test
    public void testExtractColumnValue() {
        // Define las columnas del cursor. Aquí solo hay una columna para el ejemplo.
        String[] columns = new String[] { AlertContract.AlertEntry.COLUMN_DESCRIPTION };
        
        // Crea un MatrixCursor y agrega una fila con un valor de prueba.
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] {"Descripción de prueba"});

        // Asegúrate de mover el cursor a la primera fila antes de intentar leer de él.
        assertTrue("El cursor debería moverse a la primera fila", matrixCursor.moveToFirst());

        // Ahora, matrixCursor se comporta como si hubiera obtenido esos datos de la base de datos.
        
        // Asume que alertRepository ya está inicializado y puede usar este cursor.
        Cursor cursor = matrixCursor; // Esto simula obtener un cursor de la base de datos.
        String columnName = AlertContract.AlertEntry.COLUMN_DESCRIPTION;
        String expectedValue = "Descripción de prueba";
        
        // Aquí asumes que extractColumnValue es un método que lee del cursor el valor de la columna especificada.
        String actualValue = alertRepository.extractColumnValue(cursor, columnName);
        
        assertEquals("El valor extraído debería coincidir con el esperado", expectedValue, actualValue);
    }

    // Aquí podríamos agregar más casos de prueba para cubrir situaciones de error, como columnas no encontradas, etc.

}
