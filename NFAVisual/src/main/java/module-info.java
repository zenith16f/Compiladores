module com.nfa.visual {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.nfa.visual to javafx.fxml;
    exports com.nfa.visual;
}