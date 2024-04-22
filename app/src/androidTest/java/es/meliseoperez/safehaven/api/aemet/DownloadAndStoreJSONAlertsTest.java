package es.meliseoperez.safehaven.api.aemet;

import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Pruebas instrumentadas para {@link DownloadAndStoreJSONAlerts}, que verifica la funcionalidad de descarga
 * y almacenamiento de datos JSON relacionados con alertas meteorológicas.
 */
@RunWith(AndroidJUnit4.class)
public class DownloadAndStoreJSONAlertsTest {

    private Context context;
    private DownloadAndStoreJSONAlerts downloadAndStoreJSONAlerts;

    /**
     * Prepara el entorno de prueba antes de ejecutar cada método de test.
     * Inicializa el contexto de la aplicación y la instancia de {@link DownloadAndStoreJSONAlerts}.
     */
    @Before
    public void setUp() {
        // Obtener el contexto de la aplicación de prueba
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Inicializar la clase a probar
        downloadAndStoreJSONAlerts = new DownloadAndStoreJSONAlerts();
    }

    /**
     * Test para verificar la correcta descarga y almacenamiento de datos JSON de alertas.
     * Utiliza una implementación de {@link MyCallBack} para verificar que el proceso se complete con éxito.
     */
    @Test
    public void testDownloadData() {
        // Implementación de callback para verificar la finalización de la operación
        MyCallBack myCallback = new MyCallBack() {
            @Override
            public void onCompleted() {
                // Verificar que el archivo de alertas se haya guardado correctamente
                File file = new File(context.getFilesDir(), "alertas2.json");
                assertTrue("El archivo de alertas debería existir", file.exists());
            }
        };

        // Ejecutar la descarga de datos
        downloadAndStoreJSONAlerts.downloadData(myCallback, context);
    }
}
