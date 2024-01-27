package es.meliseoperez;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.api.aemet.DownloadAndStoreJSONAlerts;
import es.meliseoperez.safehaven.api.aemet.AlertsExtractor;
import es.meliseoperez.safehaven.api.aemet.MyCallBack;
import es.meliseoperez.safehaven.api.googlemaps.CustomMapsFragment;
import es.meliseoperez.safehaven.api.googlemaps.Zona;

import es.meliseoperez.safehaven.database.AlertContract;
import es.meliseoperez.safehaven.database.AlertRepository;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private CustomMapsFragment customMapsFragment;
    private AlertRepository alertRepository;
    private List<AlertInfo> listaAlertas;


    private static final int LOCATION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establezco el contenido de la vista desde el layout del recurso XML.
        setContentView(R.layout.activity_main);

        // Verifica y elimina la base de datos si ya existe.
        checkAndDeleteDatabase();

        // Inicio la transacción para incluir el mapa personalizado en la UI.
        setupUI();

        // Solicito permisos de ubicación, ya que mi aplicación los necesita para su funcionalidad esencial.
        requestLocationPermission();
        // Inicio la recuperación de datos desde la API.
        fetchDataFromApi();


    }

    private void setupUI(){
        customMapsFragment=new CustomMapsFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,customMapsFragment);
        fragmentTransaction.commit();
    }
    private void requestLocationPermission() {
        // Verifico si los permisos ya están otorgados; de lo contrario, solicito explícitamente.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }
    private void insertAlertsIntoDatabase(@NonNull List<AlertInfo> listaAlertas, String nameTable) {
        // Establezco conexión con la base de datos y preparo para la inserción de datos.
        alertRepository = new AlertRepository(MainActivity.this);
        try {
            alertRepository.open();
            // Inserto cada alerta en la base de datos, registrando cualquier problema en el log.
            for (AlertInfo alerta : listaAlertas) {
                long id = alertRepository.insertAlert(alerta, AlertContract.AlertEntry.TABLE_NAME);
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
    private void fetchDataFromApi(){
        Runnable fectchDataRunnable=()->{
            try{
                processApiData();
                handler.post(()->Toast.makeText(MainActivity.this,"Datos procesados correctamente", Toast.LENGTH_LONG).show());
            }catch (Exception e){
                handler.post(()->Toast.makeText(MainActivity.this,"Error al obtener datos: "+ e.getMessage(),Toast.LENGTH_LONG).show());
            }
        };
        executorService.execute(fectchDataRunnable);
    }
    private void processApiData() {
       // DownloadAndStoreXMLAlerts dataDownloader=new DownloadAndStoreXMLAlerts();
        DownloadAndStoreJSONAlerts downloadAndStoreJSONAlerts= new DownloadAndStoreJSONAlerts();
        downloadAndStoreJSONAlerts.downloadData(new MyCallBack() {
            @Override
            public void onCompleted() {
                processAlertsAndDisplayOnMap();
            }
        },MainActivity.this);

    }

    private void processAlertsAndDisplayOnMap() {
        AlertsExtractor alertsExtractor=new AlertsExtractor(MainActivity.this,"alertas.xml");
        listaAlertas = alertsExtractor.extractAlertsInfo();
        insertAlertsIntoDatabase(listaAlertas,AlertContract.AlertEntry.TABLE_NAME);
        loadZonesOnMap();
    }
    private void loadZonesOnMap() {
        List<AlertInfo> alertas= customMapsFragment.cargarZonas(alertRepository);
        //polygonList=customMapsFragment.cargarZonas(alertRepository);
        List<Zona> zonas=new ArrayList<>();
        int contador=1;
        for(AlertInfo alerta: alertas){
            Zona zona= new Zona(alerta.getCoordenadas(),alerta.getColor(),alerta.getDescription(), alerta.getInstruction());
            zonas.add(zona);
            contador++;
        }
        customMapsFragment.addZonesToMap(zonas);
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
