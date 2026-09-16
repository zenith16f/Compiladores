package automaton.ui;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controlador de la ventana unica (el "shell"): nunca se destruye,
 * solo intercambia lo que hay dentro de areaContenido cada vez que
 * el usuario navega, en vez de abrir Stage nuevos por cada pantalla.
 *
 * El menu principal solo tiene los tres grandes conjuntos del
 * proyecto: AFN (que por dentro tiene sus propias pestanas para
 * Catalogo/Unir/Concatenar/Cerraduras, ver AfnHubController), AFN -> AFD
 * y Unir para Lexico. El menu es una pastilla horizontal oscura que
 * se despliega junto al boton hamburguesa (estilo "OpenMenu").
 */
public class ShellController {

    @FXML private StackPane contenedorBurger;
    @FXML private Button botonBurger;
    @FXML private HBox pillNav;
    @FXML private Button botonNavAfn;
    @FXML private Button botonNavConvertirAfd;
    @FXML private Button botonNavUnirLexico;
    @FXML private StackPane areaContenido;

    private final AppContext contexto = new AppContext();
    private boolean menuAbierto = false;

    @FXML
    public void initialize() {
        mostrarVista("AfnHub-view.fxml");
        marcarActivo(botonNavAfn);
    }

    @FXML
    private void onToggleMenu() {
        menuAbierto = !menuAbierto;

        contenedorBurger.setVisible(!menuAbierto);
        contenedorBurger.setManaged(!menuAbierto);

        pillNav.setVisible(menuAbierto);
        pillNav.setManaged(menuAbierto);

        if (menuAbierto) {
            FadeTransition fundido = new FadeTransition(Duration.millis(180), pillNav);
            fundido.setFromValue(0);
            fundido.setToValue(1);
            fundido.play();
        }
    }

    @FXML
    private void onNavAfn() {
        mostrarVista("AfnHub-view.fxml");
        marcarActivo(botonNavAfn);
        cerrarMenuSiAbierto();
    }

    @FXML
    private void onNavConvertirAfd() {
        mostrarVista("ConvertirAFD-view.fxml");
        marcarActivo(botonNavConvertirAfd);
        cerrarMenuSiAbierto();
    }

    @FXML
    private void onNavUnirLexico() {
        mostrarVista("UnirLexico-view.fxml");
        marcarActivo(botonNavUnirLexico);
        cerrarMenuSiAbierto();
    }

    private void cerrarMenuSiAbierto() {
        if (menuAbierto) {
            onToggleMenu();
        }
    }

    private void marcarActivo(Button activo) {
        for (Button boton : new Button[]{botonNavAfn, botonNavConvertirAfd, botonNavUnirLexico}) {
            boton.getStyleClass().remove("boton-nav-pill-activo");
        }
        activo.getStyleClass().add("boton-nav-pill-activo");
    }

    private void mostrarVista(String archivoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(archivoFxml));
            Parent vista = loader.load();

            Object controlador = loader.getController();
            if (controlador instanceof VistaConContexto vistaConContexto) {
                vistaConContexto.setContext(contexto);
            }

            areaContenido.getChildren().setAll(vista);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista: " + archivoFxml, e);
        }
    }
}
