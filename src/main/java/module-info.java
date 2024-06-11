module com.github.nxmbit.ferriessimulator {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.github.nxmbit.ferriessimulator to javafx.fxml;
    exports com.github.nxmbit.ferriessimulator;
}