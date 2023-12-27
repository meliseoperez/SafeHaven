package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class AlertXMLHandler {

    private static final String FILE_NAME = "alertas.xml"; // Este es el nombre del archivo que vamos a procesar
    private static final String TAG = "AlertXMLHandler"; // Etiqueta para los mensajes de log

    private final Context context; // Contexto de la aplicación, necesario para acceder a sus archivos internos

    // Constructor que requiere el contexto de la actividad/fragmento que lo invoca
    public AlertXMLHandler(Context context) {

        this.context = context;
    }

    public void processAndSaveXML(MyCallBack myCallBack) {
        // Obtener el directorio de archivos internos de la aplicación
        File directory = context.getFilesDir();
        // Crear un archivo apuntando al archivo XML que queremos leer
        File file = new File(directory, FILE_NAME);

        // Verificar si el archivo realmente existe
        if (!file.exists()) {
            Log.e(TAG, "Archivo no existe!");
            return;
        }

        ensureCorrectEncoding();

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
            String patternString = "Z_CAP_C_LEMM_.*";
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
            //writeAndDisplayPath(xmlContent);


        } catch (IOException e) {
            Log.e(TAG, "****************Error procesando archivo XML.", e);
        }
        myCallBack.onCompleted();
        Log.i(TAG, "*************Archivo XML procesado correctamente.");
    }
    // Método helper para corregir problemas de codificación en la cadena de texto XML
    private String correctEncodingIssues(String xmlContent) {
        // Aquí reemplazamos los caracteres incorrectos por los correctos
        xmlContent = xmlContent.replace("Ã©", "é");
        xmlContent = xmlContent.replace("Ã³", "ó");
        xmlContent = xmlContent.replace("Ãº", "ú");
        xmlContent = xmlContent.replace("Ã¡", "á");
        xmlContent = xmlContent.replace("Ã¯", "í");
        xmlContent = xmlContent.replace("Ãº", "ú");
        xmlContent = xmlContent.replace("Ã±", "ñ");
        // Añadir más reemplazos si es necesario
        return xmlContent;
    }
    // Método helper para leer un archivo y convertirlo en String
    private String readFileToString(File file) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return correctEncodingIssues(stringBuilder.toString());
    }

    // Método helper para escribir contenido en un archivo
    private void writeFile(File file, String content) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8))){ // false indica que queremos sobrescribir el archivo existente
            writer.write(content);
            writer.flush();

        }
    }
    public void ensureCorrectEncoding() {
        File directory = context.getFilesDir();
        File file = new File(directory, FILE_NAME);

        if (!file.exists()) {
            Log.e(TAG, "Archivo no existe!");
            return;
        }

        Log.i(TAG, "Asegurando la codificación correcta del archivo XML...");

        try {
            // Leer el contenido del archivo original
            String originalContent = readFileToString(file);

            // Verificar si la declaración de la codificación es correcta
            String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            if (!originalContent.startsWith(xmlDeclaration)) {
                // Si no es correcta, reemplazarla o agregarla
                String correctedContent = xmlDeclaration + "\n" + originalContent.replaceAll("(?i)<\\?xml.+\\?>", "").trim();

                // Sobrescribir el archivo con la nueva codificación
                writeFile(file, correctedContent);
            }

            Log.i(TAG, "Codificación asegurada correctamente.");
        } catch (IOException e) {
            Log.e(TAG, "Error al asegurar la codificación del archivo XML.", e);
        }
    }

}
