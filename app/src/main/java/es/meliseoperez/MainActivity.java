package es.meliseoperez;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.api.aemet.DownloadAndStoreXMLAlerts;
import es.meliseoperez.safehaven.api.aemet.AlertXMLHandler;
import es.meliseoperez.safehaven.api.aemet.AlertsExtractor;
import es.meliseoperez.safehaven.api.aemet.MyCallBack;
import es.meliseoperez.safehaven.api.googlemaps.CustomMapsFragment;
import es.meliseoperez.safehaven.api.googlemaps.Zona;

import es.meliseoperez.safehaven.database.AlertRepository;
import es.meliseoperez.safehaven.pruebabbdd.DisplayDatabaseActivity;

public class MainActivity extends AppCompatActivity {

        // Variables para el manejo de la base de datos y la lista de alertas.

        AlertRepository alertRepository;
        List<AlertInfo> listaAlertas;

        // Constantes y herramientas para el manejo de hilos y permisos.
        private static final String TAG = "MainActivity";
        private final ExecutorService executorService = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        public static List<String> polygonList;
        private static final int LOCATION_REQUEST_CODE=101;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Establezco el contenido de la vista desde el layout del recurso XML.
            setContentView(R.layout.activity_main);

            // Inicio la recuperación de datos desde la API.
            fetchDataFromApi();

            // Verifica y elimina la base de datos si ya existe.
            checkAndDeleteDatabase();
            // Preparo el sistema de gestión de la base de datos.

            // Inicio la transacción para incluir el mapa personalizado en la UI.
            CustomMapsFragment customMapsFragment = new CustomMapsFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, customMapsFragment);
            fragmentTransaction.commit();

            // Solicito permisos de ubicación, ya que mi aplicación los necesita para su funcionalidad esencial.
            requestLocationPermission();




            // Configuro el botón en la UI para que el usuario también pueda iniciar la descarga de datos.
            Button fetchApiDataButton = findViewById(R.id.fetchApiDataButton);
            fetchApiDataButton.setOnClickListener(v -> {
                polygonList=customMapsFragment.cargarZonas(alertRepository);
                List<Zona> zonas=new ArrayList<>();
                int contador = 1; // Un contador para asignar nombres a las zonas de forma dinámica
            for (String coordenada : polygonList) {
                Zona zona = new Zona(coordenada, "Zona " + contador); // Creando una nueva Zona con nombre dinámico
                zonas.add(zona); // Agregando la Zona a la lista
                contador++;
           }
                customMapsFragment.addZonesToMap(zonas);

            });
            Button showData = findViewById(R.id.showData);
                showData.setOnClickListener(view -> {
                    Intent intent=new Intent(MainActivity.this, DisplayDatabaseActivity.class);
                    startActivity(intent);
            });
        }

        private void requestLocationPermission() {
            // Verifico si los permisos ya están otorgados; de lo contrario, solicito explícitamente.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }

        private void insertAlertsIntoDatabase(List<AlertInfo> listaAlertas) {
            // Establezco conexión con la base de datos y preparo para la inserción de datos.
            alertRepository = new AlertRepository(MainActivity.this);
            try {
                alertRepository.open();

                // Inserto cada alerta en la base de datos, registrando cualquier problema en el log.
                for (AlertInfo alerta : listaAlertas) {
                    long id = alertRepository.insertAlert(alerta);
                    if (id == -1) {
                        Log.e(TAG, "Error al insertar alerta: " + alerta);
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error en la base de datos", e);
            } finally {
                alertRepository.close(); // Siempre aseguro el cierre de la base de datos.
            }
        }

        private void fetchDataFromApi() {
            // Defino una tarea para el hilo secundario, evitando sobrecargar el hilo principal.
            Runnable fetchDataRunnable = () -> {
                try {
                    // Proceso de descarga y manipulación de datos.
                    DownloadAndStoreXMLAlerts dataDownloader = new DownloadAndStoreXMLAlerts();
                    dataDownloader.downloadData(new MyCallBack() {
                        @Override
                        public void onCompleted() {
                            AlertXMLHandler alertXMLHandler = new AlertXMLHandler(MainActivity.this);
                            alertXMLHandler.processAndSaveXML(new MyCallBack() {
                                @Override
                                public void onCompleted() {
                                    AlertsExtractor alertsExtractor = new AlertsExtractor(MainActivity.this);
                                    listaAlertas = alertsExtractor.extractAlertsInfo();
                                    insertAlertsIntoDatabase(listaAlertas); // Almacenamiento en DB.

                                }
                            });
                        }
                    }, MainActivity.this);

                    // Informo al usuario sobre el estado de la tarea.
                    handler.post(() -> Toast.makeText(MainActivity.this, "Datos procesados correctamente", Toast.LENGTH_LONG).show());
                } catch (Exception e) {
                    handler.post(() -> Toast.makeText(MainActivity.this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            };

            // Ejecuto la tarea en el hilo secundario.
            executorService.execute(fetchDataRunnable);
        }
    private void checkAndDeleteDatabase() {
        String dbName = "Alerts.db";
        File dbFile = getDatabasePath(dbName);
        if (dbFile.exists()) {
            boolean result = dbFile.delete();
            if (!result) {
                Log.e(TAG, "Error al eliminar la base de datos existente: " + dbName);
            } else {
                Log.d(TAG, "Base de datos existente eliminada correctamente: " + dbName);
            }
        }
    }
        @Override
        protected void onDestroy() {
            super.onDestroy();
            // Antes de destruir la actividad, cierro los servicios para evitar fugas de memoria.
            executorService.shutdown();
        }
}
