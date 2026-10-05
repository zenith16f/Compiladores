package automaton.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.net.URL;

/**
 * Mensajes al usuario en un solo lugar. Los avisos son toasts (no bloquean); solo las confirmaciones
 * que deben detener la accion (eliminar) son una ventana modal.
 */
public final class Avisos {

    private Avisos() {
    }

    /** Operacion realizada correctamente (toast verde). */
    public static void info(String titulo, String mensaje) {
        Toasts.exito(titulo, mensaje);
    }

    /** Falta algo o el dato no es valido: el usuario puede corregirlo (toast ambar). */
    public static void aviso(String mensaje) {
        Toasts.aviso(mensaje, null);
    }

    /** Algo salio mal por causas ajenas al dato escrito: archivo, disco... (toast rojo). */
    public static void error(String mensaje) {
        Toasts.error("Ocurrió un error", mensaje);
    }

    /**
     * Pregunta de confirmacion con botones propios ("Eliminar" / "Cancelar").
     *
     * @return true solo si el usuario presiona el boton de aceptar
     */
    public static boolean confirmar(String titulo, String encabezado, String mensaje, String textoAceptar) {
        ButtonType aceptar = new ButtonType(textoAceptar, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = crear(Alert.AlertType.CONFIRMATION, titulo, encabezado, mensaje);
        alerta.getButtonTypes().setAll(aceptar, cancelar);
        return alerta.showAndWait().filter(boton -> boton == aceptar).isPresent();
    }

    private static Alert crear(Alert.AlertType tipo, String titulo, String encabezado, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado); // null = sin franja de encabezado
        alerta.setContentText(mensaje);

        URL css = Avisos.class.getResource("/automaton/ui/styles.css");
        if (css != null) {
            alerta.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
        return alerta;
    }
}
