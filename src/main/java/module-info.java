module com.example.gfminibar {
    requires javafx.controls;
    requires javafx.fxml;
    requires gf.core.master;



    opens com.example.gfminibar to javafx.fxml;
    exports com.example.gfminibar;
}