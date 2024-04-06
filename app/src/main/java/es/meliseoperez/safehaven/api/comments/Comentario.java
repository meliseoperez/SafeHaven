package es.meliseoperez.safehaven.api.comments;

import com.google.gson.annotations.SerializedName;

public class Comentario {
    @SerializedName("id")
    private int id;

    @SerializedName("alert_id")
    private int alertId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("comment_text")
    private String commentText;

    @SerializedName("image_url")
    private String imageUrl;

    public Comentario(int id, int alertId, int userId, String commentText, String imageUrl) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.commentText = commentText;
        this.imageUrl = imageUrl;
    }

    public Comentario(int id, int userId, String commentText, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.commentText = commentText;
        this.imageUrl = imageUrl;
    }

    public Comentario() {
        this.id = id;
    }

    public Comentario(String s, String s1) {
    }


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

