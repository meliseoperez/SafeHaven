package es.meliseoperez.safehaven.api.comments;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ComentariosResponse {
    @SerializedName("data")
    private List<Comentario> data;

    public List<Comentario> getData() {
        return data;
    }
}