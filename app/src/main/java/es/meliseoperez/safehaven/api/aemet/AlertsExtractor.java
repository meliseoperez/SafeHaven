package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class AlertsExtractor {

    private static final String TAG = "AlertsExtractor";
    private Context context;

    // Constructor que acepta el contexto, necesario para operaciones de archivo.
    public AlertsExtractor(Context context) {
        this.context = context;
    }

    public List<AlertInfo> extractAlertsInfo() {
        List<AlertInfo> alerts = new ArrayList<>();  // Lista para almacenar las alertas extraídas.

        try {
            // Obtener el archivo XML de los archivos internos de la app.
            File xmlFile = new File(context.getFilesDir(), "alertas_procesadas.xml");
            FileInputStream fis = new FileInputStream(xmlFile);

            // Configurar XmlPullParser para analizar el archivo XML.
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(fis, null);

            // Comenzar a analizar el documento XML.
            int eventType = parser.getEventType();
            AlertInfo currentAlert = null;

            // Continuar hasta el final del documento XML.
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equalsIgnoreCase("alert")) {
                        currentAlert = new AlertInfo(); // Nuevo objeto para almacenar info de la alerta actual.
                        Log.i(TAG, "Extracting alert info...");
                    } else if (currentAlert != null) {
                        // Dependiendo del nombre de la etiqueta, almacenar la información en el objeto.
                        switch (parser.getName().toLowerCase()) {
                            case "effective":
                                currentAlert.effective = parser.nextText();
                                break;
                            case "onset":
                                currentAlert.onset = parser.nextText();
                                break;
                            case "expires":
                                currentAlert.expires = parser.nextText();
                                break;
                            case "sendername":
                                currentAlert.senderName = parser.nextText();
                                break;
                            case "headline":
                                currentAlert.headline = parser.nextText();
                                break;
                            case "description":
                                currentAlert.description = parser.nextText();
                                break;
                            case "instruction":
                                currentAlert.instruction = parser.nextText();
                                break;
                            case "language":  // Añadiendo el campo language
                                currentAlert.language = parser.nextText();
                                break;
                            case "polygon":  // Añadiendo el campo polygon
                                currentAlert.polygon = parser.nextText();
                                break;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("alert") && currentAlert != null) {
                    alerts.add(currentAlert); // Agregar la alerta completada a la lista.
                    Log.i(TAG, "Alert info extracted successfully.");
                }

                eventType = parser.next(); // Avanzar al siguiente evento de parseo.
            }

            fis.close(); // Importante: cerrar el flujo de entrada después de usarlo.
            // Bucle para recorrer todas las alertas extraídas y mostrar la información.
//            for (AlertInfo alert : alerts) {
//                // Usar el método Log.i o Log.d para mostrar la información en la consola.
//                Log.i(TAG, "---------- Alerta Extraída ----------");
//                Log.i(TAG, "Effective: " + alert.effective);
//                Log.i(TAG, "Onset: " + alert.onset);
//                Log.i(TAG, "Expires: " + alert.expires);
//                Log.i(TAG, "Sender Name: " + alert.senderName);
//                Log.i(TAG, "Headline: " + alert.headline);
//                Log.i(TAG, "Description: " + alert.description);
//                Log.i(TAG, "Instruction: " + alert.instruction);
//                Log.i(TAG, "language: " + alert.language);
//                Log.i(TAG, "Polygon: " + alert.polygon);
//                Log.i(TAG, "-------------------------------------");
//            }
//            // Aquí puedes manejar la lista de alertas, como enviarla a una base de datos, etc.
//            for (AlertInfo alert : alerts) {
//                Log.i(TAG, alert.toString());
//            }

            Log.i(TAG, "All alerts extracted successfully.");

        } catch (Exception e) {
            Log.e(TAG, "Error al extraer información de las alertas", e);
        }
        return alerts;
    }


}
