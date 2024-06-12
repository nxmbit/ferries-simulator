module com.github.nxmbit.ferriessimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;


    opens com.github.nxmbit.ferriessimulator to javafx.fxml;
    exports com.github.nxmbit.ferriessimulator;
}