module com.example.gfminibar {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.gfminibar to javafx.fxml;
    exports com.example.gfminibar;
}