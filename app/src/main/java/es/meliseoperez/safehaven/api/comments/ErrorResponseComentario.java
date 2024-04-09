package es.meliseoperez.safehaven.api.comments;

/**
 * Clase que representa una respuesta de error de la API de comentarios.
 * Se utiliza para deserializar las respuestas de error en un formato manejable dentro de la aplicación.
 */
public class ErrorResponseComentario {
    private String error; // Mensaje de error proporcionado por la API.

    /**
     * Obtiene el mensaje de error.
     *
     * @return El mensaje de error.
     */
    public String getError() {
        return error;
    }
}
