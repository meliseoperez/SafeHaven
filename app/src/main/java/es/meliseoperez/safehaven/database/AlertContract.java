package es.meliseoperez.safehaven.database;

import android.provider.BaseColumns;

/**
 * Define el esquema de la base de datos para las alertas.
 * Utiliza la interfaz BaseColumns de Android para heredar campos estándar del sistema,
 * como el _ID, que la mayoría de las bases de datos Android utilizan.
 */
public class AlertContract {

    // Constructor privado para prevenir la instanciación.
    private AlertContract() {}

    /**
     * Define la estructura de la tabla de alertas.
     */
    public static class AlertEntry implements BaseColumns {
        public static final String TABLE_NAME = "alerts"; // Nombre de la tabla de alertas.
        public static final String COLUMN_ID = "id"; // Columna para el ID de la alerta. BaseColumns ya incluye un _ID por defecto.
        public static final String COLUMN_EFFECTIVE = "effective"; // Fecha de inicio efectiva de la alerta.
        public static final String COLUMN_ONSET = "onset"; // Fecha de inicio de la alerta.
        public static final String COLUMN_EXPIRES = "expires"; // Fecha de expiración de la alerta.
        public static final String COLUMN_SENDER_NAME = "sendername"; // Nombre del emisor de la alerta.
        public static final String COLUMN_HEADLINE = "headline"; // Título de la alerta.
        public static final String COLUMN_DESCRIPTION = "description"; // Descripción detallada de la alerta.
        public static final String COLUMN_INSTRUCTION = "instruction"; // Instrucciones adicionales proporcionadas en la alerta.
        public static final String COLUMN_LANGUAGE = "language"; // Idioma de la alerta.
        public static final String COLUMN_POLYGON = "polygon"; // Datos del polígono que define la zona geográfica de la alerta.
    }

}
