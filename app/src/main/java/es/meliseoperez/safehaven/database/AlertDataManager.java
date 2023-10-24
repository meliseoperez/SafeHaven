package es.meliseoperez.safehaven.database;

import android.content.Context;
import android.util.Log;

import es.meliseoperez.safehaven.api.aemet.AlertsExtractor;
import es.meliseoperez.safehaven.api.aemet.AlertInfo;

import java.util.List;

/**
 * DatabaseManager: Clase centralizada para manejar las operaciones relacionadas con la base de datos.
 * Esta clase coordina la extracción de datos desde el XML, y la inserción de esos datos en la base de datos SQLite.
 */
public class AlertDataManager {
    private static final String TAG = "DatabaseManager";
    private Context context;

    /**
     * Constructor: Inicializa el manager con el contexto actual.
     * @param context Contexto de la aplicación o actividad.
     */
    public AlertDataManager(Context context) {
        this.context = context;
    }

    /**
     * Método para refrescar la base de datos con las alertas extraídas del XML.
     */
    public void refreshDatabaseWithAlerts() {
        try {
            // Paso 1: Extraer alertas del XML
            AlertsExtractor extractor = new AlertsExtractor(context);
            List<AlertInfo> alerts = extractor.extractAlertsInfo();

            // Si no hay alertas, no tiene sentido continuar
            if (alerts == null || alerts.isEmpty()) {
                Log.i(TAG, "No se encontraron alertas en el XML para procesar.");
                return;
            }

            // Paso 2: Insertar alertas en la base de datos
            AlertRepository repository = new AlertRepository(context);
            for (AlertInfo alert : alerts) {
                repository.insertAlert(alert);
            }

            Log.i(TAG, "Base de datos refrescada con éxito con las alertas extraídas.");

        } catch (Exception e) {
            Log.e(TAG, "Error al refrescar la base de datos con las alertas", e);
        }
    }
}
