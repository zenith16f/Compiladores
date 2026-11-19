package automaton.ui;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class ShellController {

    @FXML private StackPane contenedorBurger;
    @FXML private Button botonBurger;
    @FXML private HBox pillNav;
    @FXML private Button botonNavAfn;
    @FXML private Button botonNavAfd;
    @FXML private Button botonNavAnalizador;
    @FXML private Button botonNavLL1;
    @FXML private Button botonNavLR0;
    @FXML private Button botonNavDescenso;
    @FXML private StackPane areaContenido;
    @FXML private VBox contenedorToasts;

    private final AppContext contexto = new AppContext();
    private boolean menuAbierto = false;

    @FXML
    public void initialize() {
        Toasts.instalar(contenedorToasts);
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
    private void onNavAfd() {
        mostrarVista("AfdHub-view.fxml");
        marcarActivo(botonNavAfd);
        cerrarMenuSiAbierto();
    }

    @FXML
    private void onNavAnalizador() {
        mostrarVista("AnalizadorLexico-view.fxml");
        marcarActivo(botonNavAnalizador);
        cerrarMenuSiAbierto();
    }

    @FXML
    private void onNavLL1() {
        mostrarVista("LL1-view.fxml");
        marcarActivo(botonNavLL1);
        cerrarMenuSiAbierto();
    }

    @FXML
    private void onNavLR0() {
        mostrarVista("LR0-view.fxml");
        marcarActivo(botonNavLR0);
        cerrarMenuSiAbierto();
    }

    @FXML
    private void onNavDescenso() {
        mostrarVista("DescensoRecursivo-view.fxml");
        marcarActivo(botonNavDescenso);
        cerrarMenuSiAbierto();
    }

    private void cerrarMenuSiAbierto() {
        if (menuAbierto) {
            onToggleMenu();
        }
    }

    private void marcarActivo(Button activo) {
        for (Button boton : new Button[]{botonNavAfn, botonNavAfd, botonNavAnalizador, botonNavLL1, botonNavLR0, botonNavDescenso}) {
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
