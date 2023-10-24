package es.meliseoperez.safehaven.database;

import android.content.Context;
import android.util.Log;

import es.meliseoperez.safehaven.api.aemet.AlertInfo;

import java.util.List;

/**
 * DatabaseDisplay: Clase para mostrar todos los registros en la base de datos.
 */
public class DatabaseDisplay {
    private static final String TAG = "DatabaseDisplay";
    private Context context;

    /**
     * Constructor: Inicializa el visualizador con el contexto actual.
     * @param context Contexto de la aplicación o actividad.
     */
    public DatabaseDisplay(Context context) {
        this.context = context;
    }

    /**
     * Método para mostrar todos los registros de alerta en la base de datos.
     */
    public void displayAllAlerts() {
        try {
            AlertRepository repository = new AlertRepository(context);
            List<AlertInfo> alerts = repository.getAllAlerts();

            // Si no hay alertas, mostrar un mensaje correspondiente
            if (alerts == null || alerts.isEmpty()) {
                Log.i(TAG, "No hay alertas en la base de datos para mostrar.");
                return;
            }

            // Mostrar cada alerta en el Logcat
            for (AlertInfo alert : alerts) {
                Log.i(TAG, "---------- Alerta en Base de Datos ----------");
                Log.i(TAG, "Effective: " + alert.effective);
                Log.i(TAG, "Onset: " + alert.onset);
                Log.i(TAG, "Expires: " + alert.expires);
                Log.i(TAG, "Sender Name: " + alert.senderName);
                Log.i(TAG, "Headline: " + alert.headline);
                Log.i(TAG, "Description: " + alert.description);
                Log.i(TAG, "Instruction: " + alert.instruction);
                Log.i(TAG, "Language: " + alert.language);
                Log.i(TAG, "Polygon: " + alert.polygon);
                Log.i(TAG, "---------------------------------------------");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar las alertas desde la base de datos", e);
        }
    }
}
