module com.andrew.javafx_uni {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.github.oshi;
    requires javafx.graphics;

    opens com.andrew.javafx_uni to javafx.fxml;
    exports com.andrew.javafx_uni;
}