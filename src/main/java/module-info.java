module com.example.teacherhoursdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.teacherhoursdesktop to javafx.fxml;
    opens com.example.teacherhoursdesktop.controllers to javafx.fxml;
    opens com.example.teacherhoursdesktop.models to javafx.fxml;
    opens com.example.teacherhoursdesktop.utils to javafx.fxml;

    exports com.example.teacherhoursdesktop;
    exports com.example.teacherhoursdesktop.controllers;
    exports com.example.teacherhoursdesktop.models;
    exports com.example.teacherhoursdesktop.utils;
}