package es.meliseoperez;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.api.DownloadAndStoreXMLAlerts;
import es.meliseoperez.safehaven.api.AlertXMLHandler;
import es.meliseoperez.safehaven.api.AlertsExtractor;

public class MainActivity extends AppCompatActivity {

    // Servicio de ejecución para manejar tareas en segundo plano y un Handler para comunicarse con el hilo principal.
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Define la UI a partir del archivo XML de diseño.

        // Al iniciar la actividad, directamente se intenta la obtención de datos desde la API, ya que no necesitamos permisos especiales.
        fetchDataFromApi();

        // Configuración de un botón en la UI para permitir a los usuarios iniciar la descarga de datos manualmente.
        Button fetchApiDataButton = findViewById(R.id.fetchApiDataButton);
        fetchApiDataButton.setOnClickListener(v -> fetchDataFromApi());
    }

    // Método para iniciar la descarga y procesamiento de datos de la API.
    private void fetchDataFromApi() {
        // Tarea para ejecutar en segundo plano.
        Runnable fetchDataRunnable = () -> {
            try {
                // Iniciar la descarga y procesamiento de datos.
                DownloadAndStoreXMLAlerts dataDownloader = new DownloadAndStoreXMLAlerts();
                dataDownloader.downloadData(MainActivity.this);

                // Procesamiento adicional de datos y extracción de alertas.
                AlertXMLHandler alertXMLHandler = new AlertXMLHandler(MainActivity.this);
                alertXMLHandler.processAndSaveXML();
                AlertsExtractor alertsExtractor = new AlertsExtractor(MainActivity.this);
                alertsExtractor.extractAlertsInfo();

                // Enviar un mensaje al hilo principal indicando la finalización exitosa del proceso.
                handler.post(() -> Toast.makeText(MainActivity.this, "Datos procesados correctamente", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                // En caso de error, enviar un mensaje al hilo principal con la información del error.
                handler.post(() -> Toast.makeText(MainActivity.this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        };

        // Ejecutar la tarea en el servicio de ejecución.
        executorService.execute(fetchDataRunnable);
    }

    // Método llamado cuando la actividad está a punto de ser destruida.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Asegurarse de que el ExecutorService se cierre para evitar hilos sueltos corriendo en segundo plano.
        executorService.shutdown();
    }
}
