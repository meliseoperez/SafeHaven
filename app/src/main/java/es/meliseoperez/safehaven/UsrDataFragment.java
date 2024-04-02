package es.meliseoperez.safehaven;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.meliseoperez.MainActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UsrDataFragment extends Fragment {
    private EditText editTextUserName, editTextUserEmail, editTextUserPassword;
    private RadioGroup radioGroupUserType;
    private Button buttonSaveUserData, buttonCancelUserData;
    private String  typeUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_user_data, container, false);

        editTextUserName = view.findViewById(R.id.editTextUserName);
        editTextUserEmail = view.findViewById(R.id.editTextUserEmail);
        editTextUserPassword = view.findViewById(R.id.editTextUserPassword);

        radioGroupUserType = view.findViewById(R.id.fragment_rgTypeUser);
        radioGroupUserType.setOnCheckedChangeListener((group,checkedId) -> {
            RadioButton selectedRadioButton = view.findViewById(checkedId);
            typeUser = selectedRadioButton.getText().toString().trim();
        });
        buttonSaveUserData = view.findViewById(R.id.btnSave);

        getUserData();

        buttonSaveUserData.setOnClickListener(v->{
            updateUserData();
        });

        buttonCancelUserData  = view.findViewById(R.id.btnCancel);
        buttonCancelUserData.setOnClickListener(V->{
            getActivity().getSupportFragmentManager().popBackStack();
        });
        return view;

    }

    private void getUserData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String idUserStr = prefs.getString("idUsuario", "0");
        int idUser = Integer.parseInt(idUserStr);
        String token = prefs.getString("token","");

        OkHttpClient client = new OkHttpClient();
        String url = "http://" + MainActivity.serverIP + ":8000/api/v1/showUserData/"+ idUser;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Manejo de fallo
                getActivity().runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Error al obtener los datos del usuario", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String responseBody = response.body().string();
                    //Parseo el responseBody a un objeto JSON
                    try{
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject data = jsonResponse.getJSONObject("data");

                        String name = data.optString("name", "");
                        String email = data.optString("email", "");
                        String typeUser = data.optString("type_user","");

                        //Actualizar la UI con los datos del usuario
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EditText editTextUserName = getView().findViewById(R.id.editTextUserName);
                                EditText editTextUserEmail = getView().findViewById(R.id.editTextUserEmail);
                                RadioButton rbBasic = getView().findViewById(R.id.rbBasic);
                                RadioButton rbAdvanced = getView().findViewById(R.id.rbAdvanced);

                                editTextUserName.setText(name);
                                editTextUserEmail.setText(email);
                                if ("Básico".equalsIgnoreCase(typeUser)) {
                                    rbBasic.setChecked(true);
                                } else if ("Avanzado".equalsIgnoreCase(typeUser)) {
                                    rbAdvanced.setChecked(true);
                                }
                            }

                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void updateUserData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String idUserStr = prefs.getString("idUsuario", "0"); // Recupera como String
        int idUser = Integer.parseInt(idUserStr);
        String token = prefs.getString("token","");

        OkHttpClient client = new OkHttpClient();
        String url = "http://" + MainActivity.serverIP + ":8000/api/v1/user/" + idUser;

        EditText editTextUserName = getView().findViewById(R.id.editTextUserName);
        EditText editTextUserEmail = getView().findViewById(R.id.editTextUserEmail);
        EditText editTextUserPassword = getView().findViewById(R.id.editTextUserPassword);
        RadioButton rbBasic = getView().findViewById(R.id.rbBasic);
        String typeUser = rbBasic.isChecked() ? "Básico" : "Avanzado";

        // Creando el cuerpo de la solicitud
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("name", editTextUserName.getText().toString())
                .add("email", editTextUserEmail.getText().toString())
                .add("type_user", typeUser);

        // Añadir la contraseña solo si el campo no está vacío
        if (!editTextUserPassword.getText().toString().isEmpty()) {
            formBuilder.add("password", editTextUserPassword.getText().toString());
        }

        RequestBody formBody = formBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .patch(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al actualizar los datos del usuario", Toast.LENGTH_LONG).show());
                Log.e("ERROR DATOS USER", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Datos del usuario actualizados correctamente", Toast.LENGTH_LONG).show());
                    // Actualizar UI o realizar alguna acción después de la actualización
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al actualizar los datos del usuario++", Toast.LENGTH_LONG).show());
                    Log.e("respuesta correcta ERROR DATOS USER", String.valueOf(response.code()));
                }
            }
        });
    }

}