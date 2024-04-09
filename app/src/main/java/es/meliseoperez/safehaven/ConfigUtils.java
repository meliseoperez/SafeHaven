package es.meliseoperez.safehaven;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilidad para cargar configuraciones de la aplicación desde un archivo de propiedades.
 */
public class ConfigUtils {
    private static final String CONFIG_FILE_NAME = "config.properties"; // Nombre del archivo de propiedades.

    /**
     * Obtiene la dirección IP del servidor desde el archivo de configuración.
     *
     * @param context Contexto de la aplicación, necesario para acceder a los assets.
     * @return La dirección IP del servidor como una cadena de texto.
     * @throws RuntimeException Si ocurre un error al abrir o leer el archivo de propiedades.
     */
    public static String getServerIp(Context context) {
        Properties properties = new Properties(); // Crea una instancia de Properties para cargar las configuraciones.
        // Abre el archivo de propiedades desde los assets.
        try (InputStream inputStream = context.getAssets().open(CONFIG_FILE_NAME)) {
            properties.load(inputStream);// Carga las propiedades del archivo.
            return properties.getProperty("server_ip"); // Retorna el valor de la propiedad 'server_ip'.
        } catch (IOException e) {
            // Lanza una excepción en tiempo de ejecución si hay un problema de I/O.
            throw new RuntimeException("Error al cargar la configuración del servidor: ", e);
        }
    }
}
