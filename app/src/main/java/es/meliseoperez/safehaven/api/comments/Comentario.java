package es.meliseoperez.safehaven.api.comments;

import com.google.gson.annotations.SerializedName;

/**
 * Clase Comentario: Modela la estructura de un comentario realizado por un usuario.
 * Esta clase se utiliza para la deserialización de datos JSON con GSON, representando
 * la información de un comentario específico asociado a una alerta.
 */
public class Comentario {
    @SerializedName("id") // Anotación que indica el mapeo con el campo 'id' en JSON.
    private int id;

    @SerializedName("alert_id") // Anotación que indica el mapeo con el campo 'alert_id' en JSON.
    private int alertId;

    @SerializedName("user_id") // Anotación que indica el mapeo con el campo 'user_id' en JSON.
    private int userId;

    @SerializedName("comment_text") // Anotación que indica el mapeo con el campo 'comment_text' en JSON.
    private String commentText;

    @SerializedName("image_url") // Anotación que indica el mapeo con el campo 'image_url' en JSON.
    private String imageUrl;

    /**
     * Constructor completo de la clase Comentario.
     * @param id Identificador único del comentario.
     * @param alertId Identificador de la alerta a la que está asociado el comentario.
     * @param userId Identificador del usuario que ha realizado el comentario.
     * @param commentText Texto del comentario.
     * @param imageUrl URL de una imagen asociada al comentario, si existe.
     */
    public Comentario(int id, int alertId, int userId, String commentText, String imageUrl) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.commentText = commentText;
        this.imageUrl = imageUrl;
    }

    /**
     * Constructor alternativo omitiendo el ID de la alerta.
     */
    public Comentario(int id, int userId, String commentText, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.commentText = commentText;
        this.imageUrl = imageUrl;
    }

    public Comentario() {

    }

    public Comentario(String s, String s1) {
        this.commentText = s;
        this.imageUrl = s1;
    }

    // Métodos getters y setters para cada propiedad.

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
