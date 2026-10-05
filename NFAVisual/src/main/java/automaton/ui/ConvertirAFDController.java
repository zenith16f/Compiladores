package automaton.ui;

import automaton.AFD;
import automaton.EntradaAFN;
import automaton.GestorAFD;
import automaton.GestorAFN;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;


public class ConvertirAFDController implements VistaConContexto {

    @FXML
    private ComboBox<EntradaAFN> comboAfnOrigen;
    @FXML
    private TextField campoNombreAfd;
    @FXML
    private LienzoAfn lienzo;
    @FXML
    private Label etiquetaVacio;
    @FXML
    private TableView<TablaTransicionesAfn.Fila> tablaTransiciones;

    private GestorAFN gestor;
    private GestorAFD gestorAfd;

    // AFD que se esta mostrando ahora mismo (el que se guarda al presionar "Guardar AFD").
    private AFD afdActual;

    @Override
    public void setContext(AppContext contexto) {
        this.gestor = contexto.getGestor();
        this.gestorAfd = contexto.getGestorAfd();
        comboAfnOrigen.setItems(gestor.getEntradas());
        comboAfnOrigen.valueProperty().addListener((obs, anterior, nuevo) -> actualizarVista(nuevo));
        actualizarVista(comboAfnOrigen.getValue());
    }

    @FXML
    private void onGuardarAfd() {
        if (afdActual == null) {
            mostrarAviso("Selecciona un AFN para convertirlo antes de guardar.");
            return;
        }

        String nombre = campoNombreAfd.getText();
        if (nombre == null || nombre.isBlank()) {
            EntradaAFN origen = comboAfnOrigen.getValue();
            nombre = "AFD de " + origen.getNombre();
        }

        nombre = nombre.trim();
        if (gestorAfd.ObtenerPorNombre(nombre) != null) {
            mostrarAviso("Ya existe un AFD llamado '" + nombre + "'. Escribe otro nombre.");
            return;
        }

        int id = gestorAfd.Registrar(afdActual, nombre);
        try {
            gestorAfd.guardarEnArchivo(id);
        } catch (IOException | RuntimeException e) {
            // Queda en memoria aunque falle el archivo; se avisa para que no se pierda en silencio
            Avisos.error("El AFD se registró, pero no se pudo guardar el archivo: " + e.getMessage());
            campoNombreAfd.clear();
            return;
        }
        campoNombreAfd.clear();
        Avisos.info("AFD guardado", "Se guardó el AFD '" + nombre + "'. Ya aparece en el listado.");
    }

    private void actualizarVista(EntradaAFN seleccion) {
        afdActual = seleccion == null ? null : seleccion.getAfn().convertir_AFN();

        lienzo.dibujarAfd(afdActual);
        etiquetaVacio.setVisible(afdActual == null);
        TablaTransicionesAfn.poblarAfd(tablaTransiciones, afdActual);
    }

    private void mostrarAviso(String mensaje) {
        Avisos.aviso(mensaje);
    }
}
