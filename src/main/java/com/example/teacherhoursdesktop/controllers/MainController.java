package com.example.teacherhoursdesktop.controllers;

import com.example.teacherhoursdesktop.models.Teacher;
import javafx.fxml.FXML;  // Исправлено с javax на javafx
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.time.LocalDate;

public class MainController {
    @FXML private TableView<Teacher> teachersTable;
    @FXML private TableView<Teacher> hoursTeachersTable;
    @FXML private TableColumn<Teacher, String> fullNameColumn;
    @FXML private TableColumn<Teacher, String> departmentColumn;
    @FXML private TableColumn<Teacher, String> hoursTeacherColumn;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> lessonTypeCombo;
    @FXML private TextField hoursField;

    private final ObservableList<Teacher> teachers = FXCollections.observableArrayList();
    private final ObservableList<String> lessonTypes = FXCollections.observableArrayList(
            "Лекция", "Практика", "Лабораторная", "Консультация", "Экзамен"
    );

    @FXML
    public void initialize() {
        fullNameColumn.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        departmentColumn.setCellValueFactory(cellData -> cellData.getValue().departmentProperty());
        hoursTeacherColumn.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());

        lessonTypeCombo.setItems(lessonTypes);
        loadSampleData();
        teachersTable.setItems(teachers);
        hoursTeachersTable.setItems(teachers);
        datePicker.setValue(LocalDate.now());
    }

    private void loadSampleData() {
        teachers.add(new Teacher(1, "Иванов И.И.", "Кафедра информатики"));
        teachers.add(new Teacher(2, "Петрова С.М.", "Кафедра математики"));
        teachers.add(new Teacher(3, "Сидоров А.В.", "Кафедра физики"));
    }

    @FXML
    private void handleAddRecord() {
        try {
            Teacher selectedTeacher = hoursTeachersTable.getSelectionModel().getSelectedItem();
            LocalDate date = datePicker.getValue();
            String lessonType = lessonTypeCombo.getValue();
            int hours = Integer.parseInt(hoursField.getText());

            if (selectedTeacher == null || date == null || lessonType == null || hours <= 0) {
                showAlert("Ошибка", "Заполните все поля корректно!");
                return;
            }

            System.out.printf("Добавлена запись: %s, %s, %s, %d часов%n",
                    selectedTeacher.getFullName(), date, lessonType, hours);
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректное количество часов!");
        }
    }

    private void clearFields() {
        hoursField.clear();
        lessonTypeCombo.getSelectionModel().clearSelection();
        datePicker.setValue(LocalDate.now());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}