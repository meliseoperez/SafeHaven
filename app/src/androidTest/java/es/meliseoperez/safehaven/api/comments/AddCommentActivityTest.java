package es.meliseoperez.safehaven.api.comments;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import es.meliseoperez.safehaven.R;

/**
 * Pruebas de interfaz para {@link AddCommentActivity} utilizando Espresso.
 * Verifica que la actividad de añadir comentarios funcione correctamente.
 */
@RunWith(AndroidJUnit4.class)
public class AddCommentActivityTest {

    /**
     * Test para verificar que la actividad AddCommentActivity se comporta como se espera al añadir un comentario.
     */
    @Test
    public void testAddCommentActivity() {
        // Crea un intent para iniciar AddCommentActivity con un contexto de aplicación y un parámetro necesario.
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AddCommentActivity.class);
        intent.putExtra("ZONA_ID", 1); // Proporciona los extras necesarios que espera recibir la actividad.

        // Inicia la actividad con el intent configurado en un entorno controlado de prueba.
        try (ActivityScenario<AddCommentActivity> scenario = ActivityScenario.launch(intent)) {
            // Verifica que el EditText para ingresar comentarios está visible.
            Espresso.onView(ViewMatchers.withId(R.id.editTextComment))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            // Introduce texto en el EditText donde el usuario escribe el comentario.
            Espresso.onView(ViewMatchers.withId(R.id.editTextComment))
                    .perform(ViewActions.typeText("Este es un comentario de prueba"), ViewActions.closeSoftKeyboard());

            // Simula un clic en el botón de enviar para subir el comentario.
            Espresso.onView(ViewMatchers.withId(R.id.buttonSubmit))
                    .perform(ViewActions.click());
        }
    }
}
