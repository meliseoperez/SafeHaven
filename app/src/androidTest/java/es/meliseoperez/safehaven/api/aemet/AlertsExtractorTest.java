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

@RunWith(MockitoJUnitRunner.class)
public class AlertsExtractorTest {

    @Mock
    private Context mockContext;

    // Antes de cada prueba, configura el entorno simulado.

    @Before
    public void setUp() throws IOException {
        // Configura el contexto para devolver un directorio específico como directorio de archivos.
        File mockFileDir = new File("/data/data/es.meliseoperez.safehaven/cache/");
        when(mockContext.getFilesDir()).thenReturn(mockFileDir);

        // Write an empty JSON object to the alertas2.json file
        File testFile = new File(mockFileDir, "alertas2.json");
        try (java.io.FileWriter fileWriter = new java.io.FileWriter(testFile)) {
            fileWriter.write("{}");
        }
    }
       @Test
    public void extractAlertsInfo_ReturnsAlertsList_WhenFileExists() throws IOException {
        // Simula un archivo JSON válido en el directorio de archivos.
        String validJson = "{\"data\":[{\"id\":\"1\",\"description\":\"Test Alert\"}]}";
        File testFile = new File(mockContext.getFilesDir(), "alertas2.json");
        try (java.io.FileWriter fileWriter = new java.io.FileWriter(testFile)) {
            fileWriter.write(validJson);
        }

        AlertsExtractor extractor = new AlertsExtractor(mockContext, "alertas2.json");
        assertFalse("La lista de alertas no debe estar vacía", extractor.extractAlertsInfo().isEmpty());
    }
    @Test
    public void extractAlertsInfo_ThrowsIOException_WhenFileDoesNotExist() throws IOException {
        AlertsExtractor extractor = new AlertsExtractor(mockContext, "nonexistent.json");
        extractor.extractAlertsInfo();
    }
    @Test
    public void extractAlertsInfo_ReturnsEmptyList_WhenJsonIsEmpty() throws IOException {
        File mockFileDir = mockContext.getFilesDir();
        File testFile = new File(mockFileDir, "empty.json");
        try (java.io.FileWriter fileWriter = new java.io.FileWriter(testFile)) {
            fileWriter.write("[]"); // Usa un array vacío en lugar de un objeto vacío
            FileReader fr = new FileReader(testFile);
            Log.e("CONTENIDO JSON TEST: ",fr.toString());
        }
        AlertsExtractor extractor = new AlertsExtractor(mockContext, "empty.json");
        assertTrue("La lista de alertas debe estar vacía", extractor.extractAlertsInfo().isEmpty());
    }



}
