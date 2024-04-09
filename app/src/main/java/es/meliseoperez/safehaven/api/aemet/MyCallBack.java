package es.meliseoperez.safehaven.api.aemet;

/**
 * Interfaz MyCallBack: Define un método de callback para notificar la finalización de una operación.
 * Se utiliza en operaciones asíncronas para notificar a los llamantes cuando una tarea ha concluido,
 * permitiendo así que el flujo de ejecución del programa continúe adecuadamente tras completar
 * tareas que pueden tomar tiempo indeterminado, como descargas de red o procesamientos intensivos.
 */
public interface MyCallBack {
    /**
     * Método que se invoca cuando una operación asíncrona se completa.
     * Los implementadores de esta interfaz deben definir la lógica específica a ejecutar
     * una vez completada la operación en este método.
     */
    void onCompleted();
}
