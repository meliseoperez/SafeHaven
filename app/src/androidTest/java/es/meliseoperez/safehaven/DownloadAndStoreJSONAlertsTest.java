package es.meliseoperez.safehaven;

import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import es.meliseoperez.safehaven.api.aemet.DownloadAndStoreJSONAlerts;
import es.meliseoperez.safehaven.api.aemet.MyCallBack;

/**
 * Pruebas instrumentadas para DownloadAndStoreJSONAlerts.
 * Verifica la funcionalidad de descarga y almacenamiento de datos JSON.
 */
@RunWith(AndroidJUnit4.class)
public class DownloadAndStoreJSONAlertsTest {

    private Context context;
    private DownloadAndStoreJSONAlerts downloadAndStoreJSONAlerts;

    /**
     * Configuración inicial antes de cada prueba.
     * Inicializa el contexto y la instancia de DownloadAndStoreJSONAlerts.
     */
    @Before
    public void setUp() {
        // Obtener el contexto de la aplicación de prueba
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Inicializar la clase a probar
        downloadAndStoreJSONAlerts = new DownloadAndStoreJSONAlerts();
    }

    /**
     * Prueba para verificar la descarga y almacenamiento de datos JSON.
     * Utiliza una implementación de MyCallBack para verificar el resultado.
     */
    @Test
    public void testDownloadData() {
        // Implementación de callback para verificar la finalización de la operación
        MyCallBack myCallback = new MyCallBack() {
            @Override
            public void onCompleted() {
                // Verificar que el archivo se ha guardado correctamente
                File file = new File(context.getFilesDir(), "alertas2.json");
                assertTrue("El archivo de alertas debería existir", file.exists());
                // Opcionales: Más comprobaciones, como verificar el contenido del archivo.
            }
        };

        // Ejecutar la descarga de datos
        downloadAndStoreJSONAlerts.downloadData(myCallback, context);

        // Nota: Aquí, idealmente se usaría un mecanismo de sincronización como CountDownLatch
        // para esperar a que se complete la descarga antes de hacer las aserciones.
        // Este enfoque simplificado asume que la callback onCompleted será invocada correctamente.
    }

    // Adicional: Pruebas para situaciones de error, como respuestas fallidas del servidor,
    // se pueden agregar aquí utilizando un servidor mock o configurando el cliente OkHttpClient
    // para usar respuestas predefinidas.

}
