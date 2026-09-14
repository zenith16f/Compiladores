package automaton.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Shell.fxml"));
        Parent raiz = loader.load();

        Scene escena = new Scene(raiz, 900, 600);
        escena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("Compilador - Gestor de AFN");
        stage.setScene(escena);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
