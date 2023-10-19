package es.meliseoperez.safehaven.api;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.util.regex.Pattern;

public class AlertXMLHandler {

    private static final String FILE_NAME = "alertas.xml"; // Este es el nombre del archivo que vamos a procesar
    private static final String TAG = "AlertXMLHandler"; // Etiqueta para los mensajes de log

    private Context context; // Contexto de la aplicación, necesario para acceder a sus archivos internos

    // Constructor que requiere el contexto de la actividad/fragmento que lo invoca
    public AlertXMLHandler(Context context) {
        this.context = context;
    }

    public void processAndSaveXML() {
        // Obtener el directorio de archivos internos de la aplicación
        File directory = context.getFilesDir();
        // Crear un archivo apuntando al archivo XML que queremos leer
        File file = new File(directory, FILE_NAME);

        // Verificar si el archivo realmente existe
        if (!file.exists()) {
            Log.e(TAG, "Archivo no existe!");
            return;
        }

        Log.i(TAG, "Procesando archivo XML...");

        try {
            // Leer el contenido del archivo en un String
            String dataResponseBody = readFileToString(file);

            // Eliminar todo contenido antes de la declaración del XML
            int startOfXML = dataResponseBody.indexOf("<?xml");
            if (startOfXML > 0) {
                dataResponseBody = dataResponseBody.substring(startOfXML);
            }

            // Eliminar líneas específicas que coinciden con un patrón
            String patternString = "^Z_CAP_C_LEMM_.*$";
            Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
            String[] lines = dataResponseBody.split("\n");
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                if (!pattern.matcher(line.trim()).matches()) {
                    sb.append(line).append("\n");
                }
            }

            // Envolver el contenido restante dentro de un elemento raíz y reconstruir la declaración XML
            int indexOfXmlDeclarationEnd = sb.indexOf("?>") + 2; // +2 para incluir '?>'
            String xmlWithoutDeclaration = sb.substring(indexOfXmlDeclarationEnd).trim();
            String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<alerts>\n" + xmlWithoutDeclaration + "</alerts>\n";

            // Sobrescribir el archivo con el contenido modificado
            writeFile(file, xmlContent);

        } catch (IOException e) {
            Log.e(TAG, "Error procesando archivo XML.", e);
        }

        Log.i(TAG, "Archivo XML procesado correctamente.");
    }

    // Método helper para leer un archivo y convertirlo en String
    private String readFileToString(File file) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    // Método helper para escribir contenido en un archivo
    private void writeFile(File file, String content) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) { // false indica que queremos sobrescribir el archivo existente
            outputStream.write(content.getBytes());
            outputStream.flush();
        }
    }
}
