package es.meliseoperez.safehaven.api.comments;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import es.meliseoperez.safehaven.R;

/**
 * Actividad de prueba para demostrar la implementación de un RecyclerView con comentarios.
 * Esta actividad muestra una lista estática de comentarios para probar la visualización.
 */
public class TestActivity extends AppCompatActivity {

    private List<Comentario> comentarios;
    private ComentariosAdapter adapter;

    /**
     * Método onCreate se llama cuando se crea la actividad. Configura la vista de la actividad
     * y el RecyclerView que muestra los comentarios.
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        // Inicializa la lista de comentarios con algunos datos de ejemplo.
        comentarios = new ArrayList<>();
        comentarios.add(new Comentario("Comentario 1", "URL 1"));
        comentarios.add(new Comentario("Comentario 2", "URL 2"));

        // Encuentra el RecyclerView en el layout y asegura que no es nulo.
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        assert recyclerView != null : "RecyclerView no debe ser nulo";

        // Crea el adaptador para el RecyclerView y asigna la lista de comentarios.
        adapter = new ComentariosAdapter(comentarios);

        // Establece el adaptador al RecyclerView para manejar la visualización de datos.
        recyclerView.setAdapter(adapter);

        // Configura el LayoutManager que maneja la disposición de elementos en el RecyclerView.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
