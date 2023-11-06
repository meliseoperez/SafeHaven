package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.internal.cache.FaultHidingSink;

public class AlertsExtractor {

    // Constante utilizada para logs.
    private static final String TAG = "AlertsExtractor";
    private final Context context;
    private final String fileXML;

    // Constructor que toma el contexto de la actividad o aplicación.
    public AlertsExtractor(Context context,String fileXML) {
        this.context = context;
        this.fileXML=fileXML;
    }

    // Método principal para extraer información de alertas.
    public List<AlertInfo> extractAlertsInfo() {
        // Lista para almacenar las alertas extraídas.
        List<AlertInfo> alerts = new ArrayList<>();

        try {
            // Ubicación del archivo XML en el almacenamiento interno.
            File xmlFile = new File(context.getFilesDir(), fileXML);

            // Configurando la fábrica para el analizador de documentos.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            // Obteniendo la lista de nodos "alert" del documento.
            NodeList alertList = doc.getElementsByTagName("alert");

            // Iterando a través de cada nodo "alert".
            for (int i = 0; i < alertList.getLength(); i++) {
                Node alertNode = alertList.item(i);

                // Asegurándonos de que estamos tratando con un nodo de tipo ELEMENT.
                if (alertNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element alertElement = (Element) alertNode;
                    String language = getTagValue("language", alertElement);

                    // Solo estamos interesados en alertas en español ("es-ES").
                    if ("es-ES".equalsIgnoreCase(language)) {
                        AlertInfo currentAlert = new AlertInfo();
                        // Extrayendo y configurando los valores de los diferentes tags.
                        currentAlert.effective = getTagValue("effective", alertElement);
                        currentAlert.onset = getTagValue("onset", alertElement);
                        currentAlert.expires = getTagValue("expires", alertElement);
                        currentAlert.senderName = getTagValue("sendername", alertElement);
                        currentAlert.headline = getTagValue("headline", alertElement);
                        currentAlert.description = getTagValue("description", alertElement);
                        currentAlert.instruction = getTagValue("instruction", alertElement);
                        currentAlert.language = language;
                        currentAlert.polygon = getTagValue("polygon", alertElement);

                        // Añadiendo la alerta extraída a la lista.
                        alerts.add(currentAlert);
                    }
                }
            }

        } catch (Exception e) {
            // En caso de cualquier excepción, registra el error.
            Log.e(TAG, "Error al extraer información de las alertas", e);
        }

        // Muestra las alertas extraídas en el log.
        displayAlerts(alerts);
        return alerts;
    }

    // Método auxiliar para obtener el valor de un tag específico de un elemento.
    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0 && nodeList.item(0).hasChildNodes()) {
            Node node = (Node) nodeList.item(0).getChildNodes().item(0);
            return node.getNodeValue();
        } else {
            // Devuelve null si el tag no tiene valor.
            return null;
        }
    }

    // Método para mostrar la información de las alertas en el log.
    public void displayAlerts(List<AlertInfo> alerts) {
        if (alerts == null || alerts.isEmpty()) {
            Log.i(TAG, "No hay alertas para mostrar.");
            return;
        }

        Log.i(TAG, "===== Mostrando Alertas =====");
        for (AlertInfo alert : alerts) {
            // Mostrando cada detalle de la alerta.
            Log.i(TAG, "----------------------------------");
            Log.i(TAG, "Effective: " + alert.effective);
            Log.i(TAG, "Onset: " + alert.onset);
            Log.i(TAG, "Expires: " + alert.expires);
            Log.i(TAG, "Sender Name: " + alert.senderName);
            Log.i(TAG, "Headline: " + alert.headline);
            Log.i(TAG, "Description: " + alert.description);
            Log.i(TAG, "Instruction: " + alert.instruction);
            Log.i(TAG, "Language: " + alert.language);
            Log.i(TAG, "Polygon: " + alert.polygon);
        }
        Log.i(TAG, "===== Fin de Alertas =====");
    }

}
