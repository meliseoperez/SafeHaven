package es.meliseoperez;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.api.aemet.AlertsExtractor;
import es.meliseoperez.safehaven.api.aemet.DownloadAndStoreJSONAlerts;
import es.meliseoperez.safehaven.api.aemet.MyCallBack;
import es.meliseoperez.safehaven.api.comments.ComentariosActivity;
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
    private Integer id;
    private String tipo;

    private static final int LOCATION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tipo = "";
        id = null;

        // Inicializa alertRepository aquí
        alertRepository = new AlertRepository(this);
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
    /**
     * Configura la interfaz de usuario inicializando y estableciendo el fragmento del mapa.
     * Este método prepara el {@link CustomMapsFragment} y lo agrega al contenedor de fragmentos
     * en la actividad. También establece un callback para ser notificado cuando el mapa esté listo,
     * lo que permite cargar las zonas en el mapa una vez que esté disponible.
     */
    private void setupUI() {
        // Crea una nueva instancia del CustomMapsFragment.
        customMapsFragment = new CustomMapsFragment();

        // Establece un callback para ser notificado cuando el mapa esté listo.
        // Esto se hace a través de la interfaz OnMapReadyCallback que debe ser implementada por el CustomMapsFragment.
        customMapsFragment.setMapReadyCallback(new CustomMapsFragment.MapReadyCallback() {
            @Override
            public void onMapReady() {
                // Cuando el mapa esté listo, carga las zonas en él.
                processAlertsAndDisplayOnMap();
            }
        });

        // Inicia una transacción de fragmentos para agregar el CustomMapsFragment al contenedor.
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, customMapsFragment);

        // Confirma la transacción de fragmentos.
        fragmentTransaction.commit();
    }
    private void requestLocationPermission() {
        // Verifico si los permisos ya están otorgados; de lo contrario, solicito explícitamente.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

   private void insertAlertsIntoDatabase(@NonNull List<AlertInfo> listaAlertas, String nameTable) {
       long startTime = System.nanoTime(); // Inicio del tiempo de medición
       try (AlertRepository alertRepo = new AlertRepository(this)) {
           alertRepo.open(); // Asegúrate de que esto es seguro para llamar así.
           // Inserto cada alerta en la base de datos, registrando cualquier problema en el log.
           for (AlertInfo alerta : listaAlertas) {
               long id = alertRepo.insertAlert(alerta, AlertContract.AlertEntry.TABLE_NAME);
               Log.e(TAG, "Alerta insertada : " + alerta.getId());
               if (id == -1) {
                   Log.e(TAG, "Error al insertar alerta: " + alerta);
               }
           }
       } catch (SQLException e) {
           Log.e(TAG, "Error en la base de datos", e);
       }
       // No es necesario un bloque finally para cerrar el AlertRepository, try-with-resources lo maneja.
       long endTime = System.nanoTime(); // Fin del tiempo de medición
       long duration = (endTime - startTime); // Duración en nanosegundos

       // Convertir la duración a milisegundos para una mejor comprensión
       double durationInMilliseconds = duration / 1_000_000.0;
       Log.d(TAG, "La inserción de alertas tomó: " + durationInMilliseconds + " ms");
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
               // processAlertsAndDisplayOnMap();
            }
        },MainActivity.this);

    }

    private void processAlertsAndDisplayOnMap() {
        AlertsExtractor alertsExtractor=new AlertsExtractor(MainActivity.this,"alertas2.json");
        listaAlertas = alertsExtractor.extractAlertsInfo();
        Log.e(TAG, "numero de alertas: " + listaAlertas.size());

        insertAlertsIntoDatabase(listaAlertas,AlertContract.AlertEntry.TABLE_NAME);
        loadZonesOnMap();
    }
    private void loadZonesOnMap() {
        List<AlertInfo> alertas= customMapsFragment.cargarZonas(alertRepository);
        //polygonList=customMapsFragment.cargarZonas(alertRepository);
        List<Zona> zonas=new ArrayList<>();
        for(AlertInfo alerta: alertas){
            Zona zona= new Zona(alerta.getCoordenadas(),alerta.getColor(),alerta.getDescription(), alerta.getInstruction(),alerta.getId());
            zonas.add(zona);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_usuario:

                return true;
            case R.id.menu_comentarios:
                if(acesoPermitido())
                {
                    Intent intent = new Intent(MainActivity.this, ComentariosActivity.class);
                    startActivity(intent);
                }else
                    Toast.makeText(getApplicationContext(), "Solo para usuarios Premium.",Toast.LENGTH_LONG).show();

                return true;
            case R.id.menu_salir:
                // Manejar la acción de salir
                finish();
                return true;
            case R.id.menu_log_out:
                // Manejar la acción de salir
                menu_log_out();
                return true;
            case R.id.menu_acerca_de:
                // Mostrar un dialogo o actividad sobre "Acerca de"
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void menu_log_out(){
        //Obtener el SharedPreferencees
        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        //Obtener el editor de SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Eliminar las claves token y idUsuario
        //editor.remove("token");
        //editor.remove("idUsuarioo");
        editor.clear();
        editor.apply();
        finish();
    }
    public boolean acesoPermitido(){
        boolean permitido=true;
        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String tipoUsuario=sharedPreferences.getString("tipoUsuario","basico");
        if(tipoUsuario.equals("null") || tipoUsuario.equals("basico")){
            permitido=false;
        }
        return permitido;
    }
}
