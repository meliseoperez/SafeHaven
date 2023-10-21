package es.meliseoperez.safehaven.api.aemet;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtils {

    public static void saveToFile(Context context, String content, String fileName) {
        try {
            // Utiliza el método openFileOutput que es parte del contexto de la aplicación.
            // Este método es específico para escribir archivos en el almacenamiento interno de la aplicación.
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            // El resto de tu proceso de escritura se mantiene igual.
            if (content != null) {
                outputStream.write(content.getBytes());
            }
            outputStream.close();

            // Registro de la ruta del archivo guardado.
            // La siguiente línea puede no ser necesaria si no deseas exponer la estructura de directorios de tu aplicación,
            // pero la mantendré aquí por motivos de depuración.
            File file = new File(context.getFilesDir(), fileName);
            String filePath = file.getAbsolutePath();
            Log.d(DownloadAndStoreXMLAlerts.TAG, "Archivo guardado en: " + filePath);

        } catch (Exception e) {
            Log.e(DownloadAndStoreXMLAlerts.TAG, "Error al guardar el contenido en el archivo", e);
        }
    }

}
