package es.meliseoperez.safehaven.api.comments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import es.meliseoperez.safehaven.R;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ComentariosAdapterTest {

    private List<Comentario> comentarioList = new ArrayList<>();
    private ComentariosAdapter.OnComentarioClickListener listener;

    @Rule
    public ActivityScenarioRule<AddCommentActivity> activityScenarioRule =
            new ActivityScenarioRule<>(AddCommentActivity.class);

    @Before
    public void setUp() {
        // Creo algunos datos falsos para poblar el adapter
        comentarioList.add(new Comentario("Comentario 1", "URL 1"));
        comentarioList.add(new Comentario("Comentario 2", "URL 2"));

        // Mockeo el listener para verificar la interacción
        listener = Mockito.mock(ComentariosAdapter.OnComentarioClickListener.class);

        // Inicializo el RecyclerView en la actividad utilizando ActivityScenario
        ActivityScenario<AddCommentActivity> scenario = ActivityScenario.launch(AddCommentActivity.class);
        scenario.onActivity(activity -> {
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            // Verifico que el RecyclerView no es null
            assertNotNull("RecyclerView no encontrado", recyclerView);

            // Creo el adapter y lo asigno al RecyclerView
            ComentariosAdapter adapter = new ComentariosAdapter(comentarioList, listener);
            recyclerView.setAdapter(adapter);
        });
    }

    @Test
    public void itemCount_IsCorrect() {
        // Verifico que el número de elementos en el adapter sea correcto
        onView(withId(R.id.recyclerView))
                .check(matches(ViewMatchers.hasImeAction(comentarioList.size())));
    }

    @Test
    public void clickItem_OpensDetail() {
        // Simulo el clic en el primer elemento del RecyclerView
        onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Verifico que el listener fue llamado con el comentario correcto
        Mockito.verify(listener).onComentarioClick(comentarioList.get(0));
    }

    @Test
    public void itemContent_IsCorrect() {
        // Verifico el contenido de un item específico
        onView(withId(R.id.recyclerView))
                .check(matches(hasDescendant(withText("Comentario 1"))));
    }
}
