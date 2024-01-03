package es.meliseoperez.safehaven.api.aemet;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.database.SQLException;
import android.os.IBinder;
import android.util.Log;

import es.meliseoperez.safehaven.database.AlertContract;
import es.meliseoperez.safehaven.database.AlertRepository;
import es.meliseoperez.safehaven.database.SecondTableContract;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MiServicio extends Service {
    private AlertRepository alertRepository;
    private static final String TAG = "MiServicio";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand ejecutado, id de inicio: " + startId);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/api/alertas/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<List<AlertInfo>> call = service.getAlertas();
        call.enqueue(new Callback<List<AlertInfo>>() {
            @Override
            public void onResponse(Call<List<AlertInfo>> call, Response<List<AlertInfo>> response) {
                if (response.isSuccessful()) {
                    Log.e(TAG, "Conexión exitosa: " + response.body());
                    List<AlertInfo> alertas = response.body();
                    alertRepository = new AlertRepository(MiServicio.this);
                    try {
                        alertRepository.open();
                        for (AlertInfo alerta : alertas) {
                            long id = alertRepository.insertAlertSecond(alerta, SecondTableContract.SecondTableEntry.TABLE_NAME);
                            if (id == -1) {
                                Log.e(TAG, "Error al insertar alerta: " + alerta);
                            }
                        }
                    } catch (SQLException e) {
                        Log.e(TAG, "Error en la base de datos", e);
                    } finally {
                        alertRepository.close();
                    }
                } else {
                    Log.e(TAG, "Respuesta no exitosa: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<AlertInfo>> call, Throwable t) {
                Log.e(TAG, "Error en la conexión: ", t);
            }

        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
