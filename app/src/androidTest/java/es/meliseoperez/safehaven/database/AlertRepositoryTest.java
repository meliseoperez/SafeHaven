package es.meliseoperez.safehaven.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import es.meliseoperez.safehaven.api.aemet.AlertInfo;

/**
 * Pruebas unitarias para {@link AlertRepository}, enfocadas en la interacción con la base de datos de alertas.
 */
public class AlertRepositoryTest {

    private AlertRepository alertRepository;
    private SQLiteDatabase database;

    /**
     * Configura el entorno antes de cada prueba, incluyendo la inicialización del repositorio y la apertura de la base de datos.
     */
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        assertNotNull("El contexto no debe ser nulo", context);

        alertRepository = new AlertRepository(context);
        assertNotNull("El repositorio de alertas no debe ser nulo", alertRepository);

        try {
            alertRepository.open();
            database = alertRepository.getDatabase();
            assertNotNull("La instancia de la base de datos no debe ser nula", database);
            assertTrue("La base de datos debería estar abierta", database.isOpen());
        } catch (Exception e) {
            fail("La inicialización de la base de datos falló con una excepción: " + e.getMessage());
        }
    }

    /**
     * Limpia los recursos utilizados después de cada prueba, cerrando la base de datos.
     */
    @After
    public void tearDown() {
        alertRepository.close();
    }

    /**
     * Prueba la capacidad del repositorio para abrir la base de datos correctamente.
     */
    @Test
    public void testOpen() {
        assertTrue("La base de datos debería estar abierta", database.isOpen());
    }

    /**
     * Prueba la inserción de una alerta en la base de datos, verificando que el ID devuelto sea positivo.
     */
    @Test
    public void testInsertAlert() {
        AlertInfo alert = new AlertInfo();
        alert.setId(1);
        alert.setEffective("2024-04-01T08:00:00Z");
        alert.setOnset("2024-04-01T09:00:00Z");
        alert.setExpires("2024-04-01T17:00:00Z");
        alert.setSenderName("Agencia Estatal de Meteorología");
        alert.setHeadline("Alerta de prueba");
        alert.setDescription("Descripción detallada de la alerta de prueba.");
        alert.setInstruction("Manténgase a salvo, siga las instrucciones.");
        alert.setLanguage("es");
        alert.setPolygon("POLYGON(...)");
        alert.setColor("#FF0000");

        List<LatLng> coordenadas = new ArrayList<>();
        coordenadas.add(new LatLng(40.416775, -3.703790));
        alert.setCoordenadas(coordenadas);

        long result = alertRepository.insertAlert(alert, AlertContract.AlertEntry.TABLE_NAME);
        assertTrue("La inserción debería devolver un ID positivo, recibido: " + result, result > 0);
    }

    /**
     * Prueba la recuperación de todas las alertas almacenadas en la base de datos.
     */
    @Test
    public void testGetAllAlerts() {
        List<AlertInfo> alerts = alertRepository.getAllAlerts();
        assertNotNull("La lista de alertas no debería ser nula", alerts);
    }

    /**
     * Prueba la extracción de un valor de una columna específica de un cursor.
     * Se verifica que el valor extraído coincida con el esperado, basado en un cursor simulado.
     */
    @Test
    public void testExtractColumnValue() {
        String[] columns = new String[] { AlertContract.AlertEntry.COLUMN_DESCRIPTION };
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] {"Descripción de prueba"});
        assertTrue("El cursor debería moverse a la primera fila", matrixCursor.moveToFirst());

        Cursor cursor = matrixCursor;  // Simulación de obtener un cursor de la base de datos.
        String columnName = AlertContract.AlertEntry.COLUMN_DESCRIPTION;
        String expectedValue = "Descripción de prueba";

        String actualValue = alertRepository.extractColumnValue(cursor, columnName);
        assertEquals("El valor extraído debería coincidir con el esperado", expectedValue, actualValue);
    }
}
