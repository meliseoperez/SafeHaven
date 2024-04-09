package es.meliseoperez.safehaven.api.comments;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Representa la respuesta de una solicitud de API que devuelve una lista de comentarios.
 * Utiliza la anotación @SerializedName para indicar que el campo "data" en la respuesta JSON
 * se mapea a la propiedad "data" en esta clase. Esto permite una deserialización fácil con GSON.
 */
public class ComentariosResponse {
    @SerializedName("data") // Indica el mapeo del campo JSON "data" a esta propiedad.
    private List<Comentario> data; // Lista de objetos Comentario obtenidos de la respuesta.

    /**
     * Obtiene la lista de comentarios.
     *
     * @return Una lista de objetos {@link Comentario} que representa los comentarios.
     */
    public List<Comentario> getData() {
        return data;
    }
}
