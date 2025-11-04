module mic1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens mic1 to javafx.graphics;
    opens mic1.controller to javafx.fxml;
    opens mic1.model to javafx.base;
    opens mic1.core to javafx.base;
}
