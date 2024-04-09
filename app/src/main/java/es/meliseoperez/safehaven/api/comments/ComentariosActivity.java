package es.meliseoperez.safehaven.api.comments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import es.meliseoperez.safehaven.R;

/**
 * Actividad que muestra una lista de comentarios.
 * Permite a los usuarios ver comentarios generales o filtrados asociados a una alerta específica.
 * También ofrece la funcionalidad de navegar a la actividad de detalle o edición para un comentario seleccionado.
 */
public class ComentariosActivity extends AppCompatActivity implements ComentariosAdapter.OnComentarioClickListener {

    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private ComentariosAdapter comentariosAdapter;
    private ConsultaComentariosAPI consultaComentariosAPI;
    private Integer id;
    private String tipo;
    private String idComentarios;
    private Button buttonAddComment;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_comentarios);

        // Código para recibir el zonaID

        tipo = getIntent().getStringExtra("TIPO");
        id = getIntent().getIntExtra("ZONA_ID", 0);
        idComentarios = getIntent().getStringExtra("idComent");
        recyclerView = findViewById(R.id.recyclerViewComentarios);

        sharedPreferences = getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE);

        // Si no se proporciona un id, se utiliza el id del usuario actual
        if (id == 0) {
            id = Integer.valueOf(sharedPreferences.getString("idUsuario", ""));
        }
        //Configurar RecyclerView con un adaptador inicial vacío o con algún tipo de indicador de carga
        comentariosAdapter = new ComentariosAdapter(new ArrayList<>(),this);
        recyclerView.setAdapter(comentariosAdapter);

        // Establecer un LayoutManager para el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Activar el botón de retroceso en la barra de acciones
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Crea una instancia de ConsultaComentariosAPI y llama a cargarComentarios
        consultaComentariosAPI = new ConsultaComentariosAPI(this, comentariosAdapter);
        if(id !=0 && tipo != null){
            consultaComentariosAPI.cargaComentarios(id,tipo); // Pasa null para cargar todos los comentarios o un idAlert para comentarios específicos.
        }else{
            consultaComentariosAPI.cargaComentarios(null,tipo);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.comentarios_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idItem = item.getItemId();
        if (idItem == android.R.id.home) {
            finish();
            return true;
        }
        if (idItem == R.id.mis_comentarios) {
            // Crea una instancia de ConsultaComentariosAPI y llama a cargarComentarios
            this.tipo = "user";
            consultaComentariosAPI.cargaComentarios(id != 0 ? id : null, tipo);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Maneja el evento de clic en un comentario específico, navegando a la actividad de detalle o edición.
     * @param comentario El comentario sobre el cual se hizo clic.
     */
    @Override
    public void onComentarioClick(Comentario comentario) {
        Intent intent = new Intent(this, UDCommentActivity.class);
        intent.putExtra("usuarioId", String.valueOf(comentario.getUserId()));
        intent.putExtra("comentarioId", comentario.getId());
        startActivity(intent);
        finish();
    }
}
