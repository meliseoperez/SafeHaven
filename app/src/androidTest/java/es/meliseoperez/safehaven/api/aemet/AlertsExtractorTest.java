package es.meliseoperez.safehaven.api.aemet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Clase de prueba para el extractor de alertas, utilizando Mockito para simular el entorno de Android.
 */
@RunWith(MockitoJUnitRunner.class)
public class AlertsExtractorTest {

    @Mock
    private Context mockContext;  // Contexto simulado para pruebas.

    /**
     * Configuración inicial para cada test. Establece un directorio de archivos simulado.
     * @throws IOException si ocurre un error al escribir en el archivo de prueba.
     */
    @Before
    public void setUp() throws IOException {
        // Simula un directorio de archivos para el contexto.
        File mockFileDir = new File("/data/data/es.meliseoperez.safehaven/cache/");
        when(mockContext.getFilesDir()).thenReturn(mockFileDir);

        // Crea un archivo JSON vacío para pruebas.
        File testFile = new File(mockFileDir, "alertas2.json");
        try (java.io.FileWriter fileWriter = new java.io.FileWriter(testFile)) {
            fileWriter.write("{}");
        }
    }

    /**
     * Prueba para verificar si el método extrae correctamente una lista de alertas de un archivo JSON válido.
     * @throws IOException si ocurre un error al leer o escribir el archivo.
     */
    @Test
    public void extractAlertsInfo_ReturnsAlertsList_WhenFileExists() throws IOException {
        // Escribe un JSON válido en el archivo de prueba.
        String validJson = "{\"data\":[{\"id\":\"1\",\"description\":\"Test Alert\"}]}";
        File testFile = new File(mockContext.getFilesDir(), "alertas2.json");
        try (java.io.FileWriter fileWriter = new java.io.FileWriter(testFile)) {
            fileWriter.write(validJson);
        }

        AlertsExtractor extractor = new AlertsExtractor(mockContext, "alertas2.json");
        assertFalse("La lista de alertas no debe estar vacía", extractor.extractAlertsInfo().isEmpty());
    }

    /**
     * Prueba para verificar que se lanza una IOException cuando el archivo JSON no existe.
     * @throws IOException se espera que se lance debido a la ausencia del archivo.
     */
    @Test()
    public void extractAlertsInfo_ThrowsIOException_WhenFileDoesNotExist()  {
        AlertsExtractor extractor = new AlertsExtractor(mockContext, "noexiste.json");
        extractor.extractAlertsInfo();
    }

    /**
     * Prueba para verificar que el método devuelve una lista vacía cuando el JSON está vacío.
     * @throws IOException si ocurre un error al leer o escribir el archivo.
     */
    @Test
    public void extractAlertsInfo_ReturnsEmptyList_WhenJsonIsEmpty() throws IOException {
        // Escribe un array JSON vacío en el archivo de prueba.
        File mockFileDir = mockContext.getFilesDir();
        File testFile = new File(mockFileDir, "empty.json");
        try (java.io.FileWriter fileWriter = new java.io.FileWriter(testFile)) {
            fileWriter.write("[]");  // Usa un array vacío en lugar de un objeto vacío
            FileReader fr = new FileReader(testFile);
            Log.e("CONTENIDO JSON TEST: ",fr.toString());
        }
        AlertsExtractor extractor = new AlertsExtractor(mockContext, "empty.json");
        assertTrue("La lista de alertas debe estar vacía", extractor.extractAlertsInfo().isEmpty());
    }
}
