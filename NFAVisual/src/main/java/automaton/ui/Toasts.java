package automaton.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Notificaciones tipo "toast" (como sonner): tarjetas pequenas en la esquina inferior derecha que
 * entran deslizando, se apilan, se cierran solas y se pausan al pasar el mouse. No bloquean la ventana.
 */
public final class Toasts {

    private enum Tipo {
        EXITO("toast-exito", "✓", 3.5),
        AVISO("toast-aviso", "!", 4.5),
        ERROR("toast-error", "✕", 6.0);

        final String estilo;
        final String icono;
        final double segundos;

        Tipo(String estilo, String icono, double segundos) {
            this.estilo = estilo;
            this.icono = icono;
            this.segundos = segundos;
        }
    }

    private static final int MAXIMO_VISIBLES = 3;

    private static VBox contenedor;

    private Toasts() {
    }

    /** Se llama una vez desde la pantalla principal: indica donde se dibujan los toasts. */
    public static void instalar(VBox contenedorToasts) {
        contenedor = contenedorToasts;
    }

    public static void exito(String titulo, String descripcion) {
        mostrar(Tipo.EXITO, titulo, descripcion);
    }

    public static void aviso(String titulo, String descripcion) {
        mostrar(Tipo.AVISO, titulo, descripcion);
    }

    public static void error(String titulo, String descripcion) {
        mostrar(Tipo.ERROR, titulo, descripcion);
    }

    private static void mostrar(Tipo tipo, String titulo, String descripcion) {
        if (contenedor == null) {
            // Aun no hay pantalla donde dibujarlo: que al menos quede registrado
            System.err.println("[" + tipo + "] " + titulo + (descripcion == null ? "" : " - " + descripcion));
            return;
        }

        HBox tarjeta = crearTarjeta(tipo, titulo, descripcion);

        // Sonner limita los visibles: si ya hay muchos, se retira el mas viejo
        while (contenedor.getChildren().size() >= MAXIMO_VISIBLES) {
            contenedor.getChildren().remove(0);
        }
        contenedor.getChildren().add(tarjeta);

        // Entrada: desliza desde la derecha mientras aparece
        tarjeta.setOpacity(0);
        tarjeta.setTranslateX(40);
        FadeTransition aparecer = new FadeTransition(Duration.millis(220), tarjeta);
        aparecer.setToValue(1);
        TranslateTransition deslizar = new TranslateTransition(Duration.millis(220), tarjeta);
        deslizar.setToX(0);
        new ParallelTransition(aparecer, deslizar).play();

        // Cierre automatico; con el mouse encima se pausa y al salir continua
        PauseTransition espera = new PauseTransition(Duration.seconds(tipo.segundos));
        espera.setOnFinished(e -> cerrar(tarjeta));
        tarjeta.setOnMouseEntered(e -> espera.pause());
        tarjeta.setOnMouseExited(e -> espera.play());
        tarjeta.setOnMouseClicked(e -> {
            espera.stop();
            cerrar(tarjeta);
        });
        espera.play();
    }

    private static HBox crearTarjeta(Tipo tipo, String titulo, String descripcion) {
        Label icono = new Label(tipo.icono);
        icono.getStyleClass().addAll("toast-icono", tipo.estilo);

        VBox textos = new VBox(2);
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("toast-titulo");
        lblTitulo.setWrapText(true);
        textos.getChildren().add(lblTitulo);

        if (descripcion != null && !descripcion.isBlank()) {
            Label lblDescripcion = new Label(descripcion);
            lblDescripcion.getStyleClass().add("toast-descripcion");
            lblDescripcion.setWrapText(true);
            textos.getChildren().add(lblDescripcion);
        }

        HBox tarjeta = new HBox(10, icono, textos);
        tarjeta.getStyleClass().add("toast");
        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setPrefWidth(320);
        tarjeta.setMaxWidth(320);
        HBox.setHgrow(textos, Priority.ALWAYS);
        return tarjeta;
    }

    private static void cerrar(HBox tarjeta) {
        if (!contenedor.getChildren().contains(tarjeta)) {
            return; // ya se retiro (clic + temporizador, o desplazado por otro toast)
        }
        FadeTransition desvanecer = new FadeTransition(Duration.millis(180), tarjeta);
        desvanecer.setToValue(0);
        TranslateTransition salir = new TranslateTransition(Duration.millis(180), tarjeta);
        salir.setToX(40);
        ParallelTransition salida = new ParallelTransition(desvanecer, salir);
        salida.setOnFinished(e -> contenedor.getChildren().remove(tarjeta));
        tarjeta.setOnMouseClicked(null);
        tarjeta.setOnMouseEntered(null);
        tarjeta.setOnMouseExited(null);
        salida.play();
    }
}
