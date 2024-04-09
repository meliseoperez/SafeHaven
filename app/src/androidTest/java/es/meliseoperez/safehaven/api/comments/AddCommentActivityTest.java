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

@RunWith(AndroidJUnit4.class)
public class AddCommentActivityTest {

    @Test
    public void testAddCommentActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AddCommentActivity.class);
        intent.putExtra("ZONA_ID", 1); // Si tu actividad espera un extra, asegúrate de proporcionarlo

        // Inicia la actividad con el intent que acabas de configurar
        try (ActivityScenario<AddCommentActivity> scenario = ActivityScenario.launch(intent)) {
            // Asegúrate de que el EditText donde el usuario introduce el comentario se muestra
            Espresso.onView(ViewMatchers.withId(R.id.editTextComment))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            // Introduce texto en el EditText
            Espresso.onView(ViewMatchers.withId(R.id.editTextComment))
                    .perform(ViewActions.typeText("Este es un comentario de prueba"), ViewActions.closeSoftKeyboard());

            // Hace clic en el botón para enviar el comentario
            Espresso.onView(ViewMatchers.withId(R.id.buttonSubmit))
                    .perform(ViewActions.click());

        }
    }
}
