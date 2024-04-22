package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import es.meliseoperez.safehaven.R;

/**
 * Activity para mostrar los detalles de una zona específica y permitir a los usuarios realizar acciones relacionadas con comentarios.
 * Los usuarios pueden crear un nuevo comentario o ver los comentarios existentes para la zona.
 * Algunas funcionalidades están restringidas a usuarios Premium.
 */
public class ZonaDetallesActivity extends AppCompatActivity {

    private TextView textDescripcion;
    private TextView textIndicaciones;
    private int zonaID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zona_detalles);

        // Inicialización de la UI y recuperación de datos pasados a esta Activity
        initializeUI();
    }

    /**
     * Inicializa los componentes de la UI y configura los listeners de los botones.
     * Recupera los datos de la zona pasados a esta Activity y los muestra en los TextView correspondientes.
     */
    private void initializeUI() {
        zonaID = getIntent().getIntExtra("ZONA_ID", 0);
        String descripcion = getIntent().getStringExtra("ZONA_DESCRIPCION");
        String indicaciones = getIntent().getStringExtra("ZONA_INSTRUCCIONES");

        textDescripcion = findViewById(R.id.texVistaDescripcion);
        textIndicaciones = findViewById(R.id.texVistaInstrucciones);
        textDescripcion.setText(descripcion == null ? "No hay descripción para esta alerta" : descripcion);
        textIndicaciones.setText(indicaciones == null ? "No hay indicaciones para esta alerta" : indicaciones);


        Button buttonCrearComentario = findViewById(R.id.crearComentario);
        Button buttonVerComentarios = findViewById(R.id.verComentario);

        // Configura el comportamiento al pulsar los botones de crear y ver comentarios
        buttonVerComentarios.setOnClickListener(view -> abrirComentariosActivity());
        buttonCrearComentario.setOnClickListener(view -> abrirAddCommentActivity());

        // Habilita el botón de retorno en la ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Navega hacia la Activity ComentariosActivity para mostrar los comentarios de la zona actual.
     * Esta acción está disponible para todos los usuarios, pero la visualización de ciertos comentarios puede estar restringida.
     */
    private void abrirComentariosActivity() {
        if (acesoPermitido()) {
            Intent intent = new Intent(getApplicationContext(), ComentariosActivity.class);
            intent.putExtra("ZONA_ID", zonaID);
            intent.putExtra("TIPO", "alert");
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Solo para usuarios Premium.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Navega hacia la Activity AddCommentActivity para permitir al usuario crear un nuevo comentario.
     * Esta acción está restringida a usuarios Premium.
     */
    private void abrirAddCommentActivity() {
        if (acesoPermitido()) {
            Intent intent = new Intent(getApplicationContext(), AddCommentActivity.class);
            intent.putExtra("ZONA_ID", zonaID);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Solo para usuarios Premium.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Gestiona la acción de pulsar el botón de retorno en la ActionBar.
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Comprueba si el usuario actual tiene acceso a funcionalidades Premium.
     *
     * @return true si el usuario es Premium, de lo contrario false.
     */
    private boolean acesoPermitido() {
        SharedPreferences sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);
        String tipoUsuario = sharedPreferences.getString("tipoUsuario", "basico");
        return !tipoUsuario.equals("null") && !tipoUsuario.equals("basico");
    }
}
