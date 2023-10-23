package es.meliseoperez;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.api.aemet.DownloadAndStoreXMLAlerts;
import es.meliseoperez.safehaven.api.aemet.AlertXMLHandler;
import es.meliseoperez.safehaven.api.aemet.AlertsExtractor;
import es.meliseoperez.safehaven.api.googlemaps.CustomMapsFragment;

public class MainActivity extends AppCompatActivity {

    // Servicio de ejecución para manejar tareas en segundo plano y un Handler para comunicarse con el hilo principal.
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    //Constante para identificar la solicitud de permiso de ubicación j
    private static final int LOCATION_REQUEST_CODE=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Define la UI a partir del archivo XML de diseño.
        //Creación de una nueva instancia del fragmento personalizado de mapa.
        CustomMapsFragment customMapsFragment=new CustomMapsFragment();
        //Iniciar una transacción de fragmento para añadir el fragmento al contenedor.
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        //Reemplazar el fragmento en el contenedor con el fragmento personalizado de mapa.
        fragmentTransaction.replace(R.id.fragment_container,customMapsFragment);
        fragmentTransaction.commit();
        //Solicitar permiso de ubicación al usuario
        requestLocationPermission();

//
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, new CustomMapsFragment())
//                    .commit();
//        }
//
//        // Al iniciar la actividad, directamente se intenta la obtención de datos desde la API, ya que no necesitamos permisos especiales.
//        fetchDataFromApi();
//
//        // Configuración de un botón en la UI para permitir a los usuarios iniciar la descarga de datos manualmente.
//        Button fetchApiDataButton = findViewById(R.id.fetchApiDataButton);
//        fetchApiDataButton.setOnClickListener(v -> fetchDataFromApi());
    }

    private void requestLocationPermission() {
        //Verificar si ya se ha otorgado el permiso para acceder a la ubicación
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            //Si no se ha otorgado el permiso, solicitarlo al usuario.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
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
