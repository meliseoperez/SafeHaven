package es.meliseoperez;

// Importaciones necesarias para la clase

import android.Manifest;
import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.meliseoperez.safehaven.BuildConfig;
import es.meliseoperez.safehaven.ConfigUtils;
import es.meliseoperez.safehaven.R;
import es.meliseoperez.safehaven.UsrDataFragment;
import es.meliseoperez.safehaven.api.aemet.AlertInfo;
import es.meliseoperez.safehaven.api.aemet.AlertsExtractor;
import es.meliseoperez.safehaven.api.aemet.DownloadAndStoreJSONAlerts;
import es.meliseoperez.safehaven.api.comments.ComentariosActivity;
import es.meliseoperez.safehaven.api.googlemaps.CustomMapsFragment;
import es.meliseoperez.safehaven.api.googlemaps.Zona;
import es.meliseoperez.safehaven.database.AlertContract;
import es.meliseoperez.safehaven.database.AlertRepository;

/**
 * La actividad principal de la aplicación, que actúa como el punto de entrada.
 * Se encarga de inicializar componentes, solicitar permisos, y coordinar la carga de datos y fragmentos.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Un servicio ejecutor para realizar operaciones asincrónicas, como la obtención de datos de la API.
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Un handler para ejecutar acciones en el hilo principal de la UI, como mostrar Toasts.
    private final Handler handler = new Handler(Looper.getMainLooper());
    // El fragmento personalizado del mapa, donde se mostrarán las alertas.
    private CustomMapsFragment customMapsFragment;
    // Repositorio para gestionar las operaciones de la base de datos relacionadas con las alertas.
    private AlertRepository alertRepository;
    // Lista para almacenar las alertas obtenidas de la API.
    private List<AlertInfo> listaAlertas;
    // ID seleccionado por el usuario para filtrar alertas, null si no se aplica filtro.
    private Integer id;
    // Tipo seleccionado por el usuario para filtrar alertas, cadena vacía si no se aplica filtro.
    private String tipo;
    // Dirección IP del servidor, obtenida de los ajustes de configuración.
    public static String serverIP;

    // Código de solicitud para los permisos de ubicación.
    private static final int LOCATION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtiene la dirección IP del servidor desde la configuración.
        serverIP = ConfigUtils.getServerIp(getApplicationContext());
        // Inicializaciones predeterminadas.
        tipo = "";
        id = null;

        // Inicializa el repositorio de alertas para interactuar con la base de datos.
        alertRepository = new AlertRepository(this);

        // Establece la vista de esta actividad desde el archivo de layout XML.
        setContentView(R.layout.activity_main);

        // Verifica y elimina la base de datos existente si es necesario.
        checkAndDeleteDatabase();

        // Configura la interfaz de usuario, incluyendo el mapa personalizado.
        setupUI();

        // Solicita los permisos necesarios para la aplicación.
        requestLocationPermission();

        // Inicia la carga de datos si es necesario, basado en la disponibilidad de red y configuración.
        fetchDataIfNecessary();

        // Intenta limpiar las alertas expiradas de la base de datos.
        try {
            cleanUpExpiredAlerts();
        } catch (Exception e) {
            Log.d("ERROR AL LIMPIAR: ", "No se ha podido limpiar registros " + e.getMessage());
        }
    }
    /**
     * Configura la UI inicializando y estableciendo el fragmento del mapa.
     * Este método prepara el {@link CustomMapsFragment} y lo agrega al contenedor de fragmentos
     * en la actividad. También establece un callback para ser notificado cuando el mapa esté listo,
     * lo que permite cargar las zonas en el mapa una vez que esté disponible.
     */
    private void setupUI() {
        // Crea una nueva instancia del CustomMapsFragment.
        customMapsFragment = new CustomMapsFragment();

        // Establece un callback para ser notificado cuando el mapa esté listo.
        customMapsFragment.setMapReadyCallback(() -> {
            try {
                processAlertsAndDisplayOnMap();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Inicia una transacción de fragmentos para agregar el CustomMapsFragment al contenedor.
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, customMapsFragment);
        fragmentTransaction.commit();
    }

    /**
     * Solicita al usuario permiso para acceder a la ubicación del dispositivo.
     * Esto es necesario para algunas de las funcionalidades centrales de la app.
     */
    private void requestLocationPermission() {
        // Verifica si los permisos ya están otorgados; de lo contrario, solicita explícitamente.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    /**
     * Inserta las alertas obtenidas de la API en la base de datos local para su posterior uso.
     * @param listaAlertas Lista de alertas para insertar en la base de datos.
     * @param nameTable Nombre de la tabla donde insertar las alertas.
     */
    private void insertAlertsIntoDatabase(@NonNull List<AlertInfo> listaAlertas, String nameTable) {
        try (AlertRepository alertRepo = new AlertRepository(this)) {
            alertRepo.open(); // Asegúrate de que esto es seguro para llamar así.
            for (AlertInfo alerta : listaAlertas) {
                long id = alertRepo.insertAlert(alerta, AlertContract.AlertEntry.TABLE_NAME);
                if (id == -1) {
                    Log.e(TAG, "Error al insertar alerta: " + alerta);
                } else {
                    Log.d(TAG, "Alerta insertada con ID: " + id);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error en la base de datos al insertar alertas", e);
        }
    }

    /**
     * Intenta descargar y procesar los datos de la API. Este método gestiona la lógica para realizar
     * la solicitud a la API, procesar los datos recibidos y actualizar la interfaz de usuario
     * acorde con los resultados obtenidos.
     */
    private void fetchDataFromApi() {
        // Tarea para realizar la solicitud a la API en un hilo separado.
        Runnable fetchDataRunnable = () -> {
            try {
                processApiData();
                // Una vez los datos son procesados, actualiza la UI en el hilo principal.
                handler.post(() -> Toast.makeText(MainActivity.this, "Datos procesados correctamente", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                // En caso de error, muestra un mensaje de error en la UI.
                handler.post(() -> Toast.makeText(MainActivity.this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        };
        executorService.execute(fetchDataRunnable);
    }

    /**
     * Marca las alertas como actualizadas en las preferencias compartidas para evitar actualizaciones
     * innecesarias. Esto ayuda a controlar la frecuencia con la que se actualizan las alertas
     * basándose en la configuración del usuario.
     */
    private void markAlertsAsUpdated() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_alerts_update", System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Guarda la preferencia del usuario sobre la frecuencia de actualización de las alertas.
     * Esto permite al usuario controlar cuán frecuentemente se actualizan los datos de alerta
     * en la aplicación.
     * @param updateFrequency La frecuencia de actualización elegida por el usuario.
     */
    private void saveUpdatePreferences(String updateFrequency) {
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("update_frequency", updateFrequency); //"siempre", "cada_hora", "cada_seis_horas"
        editor.apply();
    }

    /**
     * Comprueba si existe conexión a la red y si es necesario actualizar los datos de alerta basándose
     * en la última actualización y la frecuencia configurada por el usuario. Si es necesario,
     * procede con la actualización.
     */
    private void fetchDataIfNecessary() {
        if (isNetworkAvailable()) {
            if (shouldUpdateAlerts()) {
                fetchDataFromApi();
            } else {
                // Si no es necesario actualizar, notifica al usuario y procede a mostrar los datos locales.
                handler.post(() -> Toast.makeText(getApplicationContext(),
                        "No es necesario actualizar. Mostrando datos locales.",
                        Toast.LENGTH_LONG).show());
            }
        } else {
            // Si no hay conexión de red, muestra un mensaje y procede con la visualización de datos locales.
            handler.post(() -> Toast.makeText(getApplicationContext(),
                    "No hay conexión de red disponible. Mostrando datos locales.",
                    Toast.LENGTH_LONG).show());
        }
    }

    /**
     * Determina si se debe proceder con la actualización de los datos de alerta basándose en
     * la configuración de frecuencia de actualización y la última vez que se actualizaron los datos.
     * @return Verdadero si es necesario actualizar los datos, falso en caso contrario.
     */
    private boolean shouldUpdateAlerts() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String updateFrequency = prefs.getString("update_frequency", "cada_hora");
        long lastUpdate = prefs.getLong("last_alerts_update", 0);
        // Calcula el tiempo transcurrido desde la última actualización.
        long elapsedTimeSinceLastUpdate = System.currentTimeMillis() - lastUpdate;
        switch (updateFrequency) {
            case "Siempre":
                return true; // Siempre actualiza.
            case "Cada_hora":
                return elapsedTimeSinceLastUpdate > 1 * 60 * 60 * 1000; // Actualiza cada hora.
            case "Cada_seis_horas":
                return elapsedTimeSinceLastUpdate > 6 * 60 * 60 * 1000; // Actualiza cada seis horas.
            default:
                return false; // No actualiza por defecto.
        }
    }

    /**
     * Verifica la disponibilidad de una conexión de red activa. Esencial para decidir si
     * proceder con la actualización de datos de alerta desde la API.
     * @return Verdadero si hay una conexión de red disponible, falso si no hay conexión.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        return false;
    }

    /**
     * Descarga y procesa los datos de la API. Este método encapsula la lógica para interactuar con la
     * API externa, descargar los datos de alerta, procesarlos y luego actualizar la base de datos local
     * y la UI de la aplicación.
     */
    private void processApiData() {
        // Llama a DownloadAndStoreJSONAlerts para iniciar la descarga de datos.
        DownloadAndStoreJSONAlerts downloadAndStoreJSONAlerts = new DownloadAndStoreJSONAlerts();
        // Marca las alertas como actualizadas una vez finalizada la descarga.
        markAlertsAsUpdated();
        // Define el comportamiento una vez completada la descarga.
        downloadAndStoreJSONAlerts.downloadData(() -> {
            // Aquí iría la lógica para procesar las alertas y mostrarlas en el mapa.
        }, MainActivity.this);
    }

    /**
     * Procesa las alertas descargadas y las muestra en el mapa. Este método extrae las alertas de la
     * base de datos local, las convierte a un formato adecuado para su visualización y las pasa al
     * CustomMapsFragment para ser mostradas en el mapa.
     */
    private void processAlertsAndDisplayOnMap() throws IOException {
        // Utiliza AlertsExtractor para obtener las alertas de un archivo local.
        AlertsExtractor alertsExtractor = new AlertsExtractor(MainActivity.this, "alertas2.json");
        listaAlertas = alertsExtractor.extractAlertsInfo();
        Log.d(TAG, "Número de alertas procesadas: " + listaAlertas.size());
        // Inserta las alertas en la base de datos local.
        insertAlertsIntoDatabase(listaAlertas, AlertContract.AlertEntry.TABLE_NAME);
        // Carga las zonas correspondientes a las alertas en el mapa.
        loadZonesOnMap();
    }

    /**
     * Carga las zonas de alerta en el mapa. Este método transforma las alertas extraídas en objetos Zona,
     * que luego son utilizados para dibujar las áreas correspondientes en el mapa a través del
     * CustomMapsFragment.
     */
    private void loadZonesOnMap() {
        // Obtiene las alertas del repositorio.
        List<AlertInfo> alertas = customMapsFragment.cargarZonas(alertRepository);
        List<Zona> zonas = new ArrayList<>();
        for (AlertInfo alerta : alertas) {
            Zona zona = new Zona(alerta.getCoordenadas(), alerta.getColor(), alerta.getDescription(), alerta.getInstruction(), alerta.getId());
            zonas.add(zona);
        }
        // Pasa las zonas al fragmento del mapa para su visualización.
        customMapsFragment.addZonesToMap(zonas);
    }
    /**
     * Verifica y elimina la base de datos si ya existe. Esto es útil para asegurarse
     * de que la aplicación comience con datos frescos cada vez que se inicie, dependiendo
     * de la lógica de negocio de la aplicación.
     */
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

    /**
     * Recopila las ID de las alertas expiradas para su posterior eliminación.
     * Esto es parte de un proceso de limpieza para mantener la base de datos actualizada
     * con solo alertas relevantes y actuales.
     *
     * @return Una lista de ID de alertas que han expirado.
     */
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

    /**
     * Utiliza la lista de ID obtenida de `getIdAlertasExpiradas` para eliminar las alertas
     * expiradas de la base de datos. Esto ayuda a mantener la base de datos limpia y eficiente,
     * almacenando solo datos relevantes.
     */
    private void eliminarAlertasExpiradas() {
        List<Integer> idsExpiradas = getIdAlertasExpiradas();
        try (AlertRepository alertRepository = new AlertRepository(getApplicationContext())) {
            alertRepository.open();
            for (Integer id : idsExpiradas) {
                alertRepository.eliminarAlertaPorId(id);
            }
        }
    }

    /**
     * Llama a `eliminarAlertasExpiradas` para iniciar el proceso de limpieza de alertas
     * expiradas. Este método se puede llamar en momentos adecuados, como al iniciar la app,
     * para asegurarse de que los datos mostrados al usuario son actuales.
     */
    private void cleanUpExpiredAlerts() {
        eliminarAlertasExpiradas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú; esto añade ítems a la barra de acción si está presente.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Maneja las selecciones de ítems del menú aquí. El sistema de Android llama a este método
        // automáticamente cuando el usuario selecciona un ítem del menú (incluyendo ítems de acción).
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_usuario:
                // Lanza un fragmento donde el usuario puede ver o editar sus datos.
                launchUserDataFragment();
                return true;
            case R.id.menu_comentarios:
                // Verifica si el usuario tiene acceso para ver comentarios y procede en consecuencia.
                if (accesoPermitido()) {
                    // Lanza la actividad de comentarios.
                    Intent intent = new Intent(this, ComentariosActivity.class);
                    startActivity(intent);
                } else {
                    // Informa al usuario que no tiene acceso.
                    Toast.makeText(this, "Acceso restringido a usuarios con permisos adecuados.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_configuracion:
                // Muestra un diálogo para que el usuario elija la frecuencia de actualización de datos.
                showUpdateFrequencyDialog();
                return true;
            case R.id.menu_salir:
                // Finaliza la actividad.
                finish();
                return true;
            case R.id.menu_log_out:
                // Manejar la acción de salir
                menu_log_out();
                return true;
            case R.id.menu_acerca_de:
                // Muestra un diálogo con información sobre la aplicación.
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**
     * Lanza el fragmento que permite al usuario ver o editar sus datos. Este método facilita la
     * navegación hacia una interfaz donde el usuario puede interactuar con su información personal
     * almacenada en la aplicación.
     */
    private void launchUserDataFragment() {
        UsrDataFragment userDataFragment = new UsrDataFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, userDataFragment);
        fragmentTransaction.addToBackStack(null); // Permite regresar al fragmento anterior presionando el botón atrás.
        fragmentTransaction.commit();
    }

    /**
     * Muestra un diálogo informativo acerca de la aplicación, incluyendo el nombre de la app,
     * la versión, y los detalles del desarrollador. Es una forma de proporcionar transparencia y
     * créditos donde corresponde.
     */
    private void showAboutDialog() {
        String appName = getString(R.string.app_name); // Nombre de la aplicación
        String versionName = BuildConfig.VERSION_NAME; // Versión de la aplicación
        String developerName = getString(R.string.developer); // Nombre del desarrollador

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about))
                .setMessage(getString(R.string.about_message, appName, versionName, developerName)) // Pasando tres argumentos
                .setPositiveButton("OK", null)
                .show();
    }


    /**
     * Presenta al usuario un diálogo para seleccionar la frecuencia con la cual la aplicación
     * debería actualizar sus datos. Esta funcionalidad proporciona al usuario un control más
     * detallado sobre el uso de datos y la frecuencia de las actualizaciones de contenido.
     */
    private void showUpdateFrequencyDialog() {
        final String[] updateOptions = getResources().getStringArray(R.array.update_frequency_options);
        int currentSelection = getCurrentSelection(); // Determina la opción actualmente seleccionada basándose en las preferencias almacenadas.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.update_frequency))
                .setSingleChoiceItems(updateOptions, currentSelection, (dialog, which) -> {
                    // Guarda la selección del usuario en las preferencias compartidas.
                    saveUpdatePreferences(updateOptions[which]);
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Determina la opción de frecuencia de actualización actualmente seleccionada por el usuario.
     * Este método lee las preferencias compartidas para obtener la configuración guardada.
     *
     * @return el índice de la opción seleccionada en el arreglo de opciones.
     */
    private int getCurrentSelection() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String currentFrequency = prefs.getString("update_frequency", getString(R.string.default_update_frequency));
        String[] updateOptions = getResources().getStringArray(R.array.update_frequency_options);

        for (int i = 0; i < updateOptions.length; i++) {
            if (updateOptions[i].equals(currentFrequency)) {
                return i;
            }
        }
        return -1; // Retorna un valor por defecto si no se encuentra la preferencia.
    }

    /**
     * Inicia el procedimiento para cerrar la sesión del usuario. Este método elimina las
     * preferencias del usuario y cierra la sesión activa, llevando al usuario de vuelta al
     * inicio o a la pantalla de login.
     */
    public void menu_log_out() {
        SharedPreferences prefs = getSharedPreferences("mis_preferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // Elimina todas las entradas en las preferencias compartidas.
        editor.apply(); // Aplica los cambios.
        finish(); // Finaliza la actividad actual, lo que podría llevar al usuario de vuelta al inicio de sesión.
    }

    /**
     * Verifica si el usuario actual tiene permisos para acceder a ciertas funcionalidades premium
     * de la aplicación. Esta comprobación puede basarse en el tipo de usuario o cualquier otra
     * lógica de negocio aplicable.
     *
     * @return true si el usuario tiene acceso permitido; de lo contrario, false.
     */
    public boolean accesoPermitido() {
        SharedPreferences prefs = getSharedPreferences("mis_preferencias", MODE_PRIVATE);
        String tipoUsuario = prefs.getString("tipoUsuario", "");
        return !tipoUsuario.equals("Básico"); // Suponiendo que "basico" indica un usuario sin acceso premium.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Asegura la finalización adecuada del servicio de ejecutor para evitar fugas de memoria.
        executorService.shutdownNow();
    }
}
