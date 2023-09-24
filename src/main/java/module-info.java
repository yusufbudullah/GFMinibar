module com.example.gfminibar {
    requires javafx.controls;
    requires javafx.fxml;
    requires gf.core.master;
    requires com.google.common;


    opens com.example.gfminibar to javafx.fxml;
    exports com.example.gfminibar;
}