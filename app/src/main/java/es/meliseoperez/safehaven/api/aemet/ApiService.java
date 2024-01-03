package es.meliseoperez.safehaven.api.aemet;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/api/alertas")
    Call<List<AlertInfo>> getAlertas();
}
