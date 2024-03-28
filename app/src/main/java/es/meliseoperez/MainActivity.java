package es.meliseoperez;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.meliseoperez.safehaven.BuildConfig;
import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.UsrDataFragment;
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
        fetchDataIfNecessary();
        try {
            cleanUpExpiredAlerts();
        } catch (Exception e) {
            String error = e.getMessage();
            String msg = "No se ha podido limpiar registros " + error;
            Log.d("ERROR AL LIMPIAR: ", msg);
        }

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
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar un Snackbar con una explicación y una acción para solicitar el permiso
                Snackbar.make(findViewById(android.R.id.content), "Esta aplicación necesista el permiso de ubicación para funcionar correctamente.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Conceder", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        }).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            }
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

    }

    private void insertAlertsIntoDatabase(@NonNull List<AlertInfo> listaAlertas, String nameTable) {
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
    }

    private void fetchDataFromApi() {
        Runnable fectchDataRunnable = () -> {
            try {
                processApiData();
                handler.post(() -> Toast.makeText(MainActivity.this, "Datos procesados correctamente", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(MainActivity.this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        };
        executorService.execute(fectchDataRunnable);
    }

    private void markAlertsAsUpdated() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_alerts_update", System.currentTimeMillis());
        editor.apply();
    }

    private void saveUpdatePreferences(String updateFrequency) {
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("update_frequency", updateFrequency);//"siempre","cada_hora","cada_seis_horas"
        editor.apply();
    }

    private void fetchDataIfNecessary() {
        if (isNetworkAvailable()) {
            //Comprueba si es necesario actualizar basándose en la última marca de tiempo de actualización
            if (shouldUpdateAlerts()) {
                fetchDataFromApi();
            } else {
                //funcionamiento modo offline
                handler.post(() -> Toast.makeText(getApplicationContext(),
                        "No hay conexión de red disponible. Mostrando datos locales.",
                        Toast.LENGTH_LONG).show());

                processAlertsAndDisplayOnMap();
            }
        }
    }

    private boolean shouldUpdateAlerts() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String updateFrequency = prefs.getString("update_frequency", "cada_hora");
        long lastUpdate = prefs.getLong("last_alerts_update", 0);
        //Calcula el intervalo de tiempo desde la útlima actualización
        long elapsedTimeSinceLasUpdate = System.currentTimeMillis() - lastUpdate;
        switch (updateFrequency) {
            case "siempre":
                return true;//Siempre actualizada
            case "cada_hora":
                return elapsedTimeSinceLasUpdate > 1 * 60 * 60 * 1000; // Cada hora
            case "cada_seis_horas":
                return elapsedTimeSinceLasUpdate > 6 * 60 * 60 * 1000; // Cada seis horas
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        // Obtiene el ConnectivityManager para manejar las conexiones de red.
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Comprueba si ConnectivityManager no es nulo.
        if (connectivityManager != null) {
            // Obtiene la red activa.
            Network network = connectivityManager.getActiveNetwork();
            // Si no hay red activa, devuelve falso.
            if (network == null) return false;
            // Obtiene las capacidades de la red activa.
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            // Verifica si la red tiene capacidades válidas y es Wi-Fi, celular o Ethernet.
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }

        // Si ConnectivityManager es nulo, asume que no hay red disponible.
        return false;
    }


    private void processApiData() {
        DownloadAndStoreJSONAlerts downloadAndStoreJSONAlerts = new DownloadAndStoreJSONAlerts();
        markAlertsAsUpdated();
        downloadAndStoreJSONAlerts.downloadData(new MyCallBack() {
            @Override
            public void onCompleted() {
                // processAlertsAndDisplayOnMap();
            }
        }, MainActivity.this);

    }

    private void processAlertsAndDisplayOnMap() {
        AlertsExtractor alertsExtractor = new AlertsExtractor(MainActivity.this, "alertas2.json");
        listaAlertas = alertsExtractor.extractAlertsInfo();
        Log.e(TAG, "numero de alertas: " + listaAlertas.size());

        insertAlertsIntoDatabase(listaAlertas, AlertContract.AlertEntry.TABLE_NAME);
        loadZonesOnMap();
    }

    private void loadZonesOnMap() {
        List<AlertInfo> alertas = customMapsFragment.cargarZonas(alertRepository);
        //polygonList=customMapsFragment.cargarZonas(alertRepository);
        List<Zona> zonas = new ArrayList<>();
        for (AlertInfo alerta : alertas) {
            Zona zona = new Zona(alerta.getCoordenadas(), alerta.getColor(), alerta.getDescription(), alerta.getInstruction(), alerta.getId());
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

    private List<Integer> getIdAlertasExpiradas() {
        List<Integer> idsExpiradas = new ArrayList<>();
        List<AlertInfo> todasLasAlertas = alertRepository.getAllAlerts();
        Instant ahora = Instant.now();

        for (AlertInfo alerta : todasLasAlertas) {
            Instant fechaExpiracion = ZonedDateTime.parse(alerta.expires).toInstant();
            if (fechaExpiracion.isBefore(ahora)) {
                idsExpiradas.add(alerta.id);
            }
        }

        return idsExpiradas;
    }

    private void eliminarAlertasExpiradas() {
        List<Integer> idsExpiradas = getIdAlertasExpiradas();
        try (AlertRepository alertRepository = new AlertRepository(getApplicationContext())) {
            alertRepository.open();
            for (Integer id : idsExpiradas) {
                alertRepository.eliminarAlertaPorId(id);
            }
        }
    }

    private void cleanUpExpiredAlerts() {
        eliminarAlertasExpiradas();
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
                launchUserDataFragment();
                return true;
            case R.id.menu_comentarios:
                if (acesoPermitido()) {
                    Intent intent = new Intent(MainActivity.this, ComentariosActivity.class);
                    startActivity(intent);
                } else
                    Toast.makeText(getApplicationContext(), "Solo para usuarios Premium.", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_configuracion:
                showUpTimeFrecuencyDialog();
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
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchUserDataFragment() {
        UsrDataFragment userDataFragment = new UsrDataFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, userDataFragment);
        fragmentTransaction.addToBackStack(null);// Permite regresar al fragmento anterior presionando el botón atrás
        fragmentTransaction.commit();

    }

    private void showAboutDialog() {
        String versionName = BuildConfig.VERSION_NAME; // Obtiene la versión de la app desde BuildConfig
        String appName = getString(R.string.app_name);
        String developerName = getString(R.string.developer_name);
        String versionText = getString(R.string.app_version, versionName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_title))
                .setMessage(appName + "\n" + versionText + "\n" +
                        "Desarrollo por: " + developerName)
                .setPositiveButton("OK", null)
                .show();

    }

    public void showUpTimeFrecuencyDialog() {
        //Las opciones de frecuencia de actualización
        final String[] items = {"Siempre", "Cada hora", "Cada 6 horas"};
        int checkedItem = getCurrentSelection(); // // Cambia esto segúan la selección actual guardda en SharedPreferences
        //No enteindo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Frecuencia de actualización")
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Guarda la selección del usuario
                        saveUpdatePreferences(items[which]);
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int getCurrentSelection() {
        SharedPreferences pref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String currentFrequency = pref.getString("update_frequency", "Cada_hora");
        switch (currentFrequency) {
            case "Siempre":
                return 0;
            case "Cada_hora":
                return 1;
            case "Cada 6 horas":
                return 2;
            default:
                return 1; //por defecto cada hora.

        }
    }

    public void menu_log_out() {
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

    public boolean acesoPermitido() {
        boolean permitido = true;
        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String tipoUsuario = sharedPreferences.getString("tipoUsuario", "basico");
        if (tipoUsuario.equals("null") || tipoUsuario.equals("basico")) {
            permitido = false;
        }
        return permitido;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Antes de destruir la actividad, cierro los servicios para evitar fugas de memoria.
        executorService.shutdown();
    }
}
