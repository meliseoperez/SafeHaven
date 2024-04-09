package es.meliseoperez.safehaven;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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

/**
 * Fragmento para gestionar la visualización y actualización de datos de usuario.
 * Permite a los usuarios modificar su información personal como nombre, email, contraseña y tipo de usuario.
 */
public class UsrDataFragment extends Fragment {
    // Variables para los componentes de la UI
    private EditText editTextUserName, editTextUserEmail, editTextUserPassword;
    private RadioGroup radioGroupUserType;
    private Button buttonSaveUserData, buttonCancelUserData;
    private String typeUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Habilita que este fragmento pueda tener un menú de opciones propio.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_user_data, container, false);

        // Configura la ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Inicializa los componentes de la UI y establece listeners
        initializeComponents(view);
        getUserData();

        return view;
    }

    /**
     * Inicializa los componentes de la vista y configura los listeners para los botones y el RadioGroup.
     */
    private void initializeComponents(View view) {
        editTextUserName = view.findViewById(R.id.editTextUserName);
        editTextUserEmail = view.findViewById(R.id.editTextUserEmail);
        editTextUserPassword = view.findViewById(R.id.editTextUserPassword);

        radioGroupUserType = view.findViewById(R.id.fragment_rgTypeUser);
        radioGroupUserType.setOnCheckedChangeListener(this::onUserTypeChanged);

        buttonSaveUserData = view.findViewById(R.id.btnSave);
        buttonSaveUserData.setOnClickListener(v -> updateUserData());

        buttonCancelUserData = view.findViewById(R.id.btnCancel);
        buttonCancelUserData.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * Maneja el cambio de selección del tipo de usuario.
     */
    private void onUserTypeChanged(RadioGroup group, int checkedId) {
        RadioButton selectedRadioButton = group.findViewById(checkedId);
        typeUser = selectedRadioButton.getText().toString().trim();
    }

    /**
     * Obtiene los datos del usuario actual del servidor y actualiza la UI.
     */
    private void getUserData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String idUserStr = prefs.getString("idUsuario", "0");
        int idUser = Integer.parseInt(idUserStr);
        String token = prefs.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = "http://" + MainActivity.serverIP + ":8000/api/v1/showUserData/" + idUser;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.error_fetching_user_data), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject data = jsonResponse.getJSONObject("data");
                        String name = data.optString("name", "");
                        String email = data.optString("email", "");
                        String userType = data.optString("type_user", "");

                        getActivity().runOnUiThread(() -> {
                            editTextUserName.setText(name);
                            editTextUserEmail.setText(email);

                            if ("Básico".equalsIgnoreCase(userType)) {
                                ((RadioButton) radioGroupUserType.findViewById(R.id.rbBasic)).setChecked(true);
                            } else if ("Avanzado".equalsIgnoreCase(userType)) {
                                ((RadioButton) radioGroupUserType.findViewById(R.id.rbAdvanced)).setChecked(true);
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("UsrDataFragment", "JSON parsing error", e);
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.error_fetching_user_data), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /**
     * Envía una solicitud al servidor para actualizar los datos del usuario con la información proporcionada.
     */
    private void updateUserData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String idUserStr = prefs.getString("idUsuario", "0");
        int idUser = Integer.parseInt(idUserStr);
        String token = prefs.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = "http://" + MainActivity.serverIP + ":8000/api/v1/user/" + idUser;

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (!editTextUserName.getText().toString().isEmpty()) {
            formBuilder.add("name", editTextUserName.getText().toString());
        }
        if (!editTextUserEmail.getText().toString().isEmpty()) {
            formBuilder.add("email", editTextUserEmail.getText().toString());
        }
        if (!editTextUserPassword.getText().toString().isEmpty()) {
            formBuilder.add("password", editTextUserPassword.getText().toString());
        }
        formBuilder.add("type_user", typeUser.equals(getString(R.string.basic_user)) ? "Básico" : "Avanzado");

        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .patch(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.error_updating_user_data), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.user_data_updated_successfully), Toast.LENGTH_LONG).show());
                } else {
                    String responseBody = response.body().string();
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), responseBody, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Maneja los clics en los elementos del menú.
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
