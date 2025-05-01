package com.example.teacherhoursdesktop.controllers;

import com.example.teacherhoursdesktop.models.Teacher;
import com.example.teacherhoursdesktop.utils.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Pair;
import javafx.util.converter.IntegerStringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainController {
    @FXML private TableView<Teacher> teachersTable;
    @FXML private TableColumn<Teacher, String> fullNameColumn;
    @FXML private TableColumn<Teacher, String> departmentColumn;
    @FXML private TableColumn<Teacher, Integer> plannedHoursColumn;
    @FXML private TableColumn<Teacher, Integer> completedHoursColumn;
    @FXML private Button addTeacherButton;

    @FXML
    public void initialize() {
        setupTeachersTable();
        loadTeachers();
    }

    private void setupTeachersTable() {
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        plannedHoursColumn.setCellValueFactory(new PropertyValueFactory<>("plannedHours"));
        completedHoursColumn.setCellValueFactory(new PropertyValueFactory<>("completedHours"));

        plannedHoursColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        plannedHoursColumn.setOnEditCommit(event -> {
            Teacher teacher = event.getRowValue();
            int newHours = event.getNewValue();
            if (newHours >= 0) {
                teacher.setPlannedHours(newHours);
                Database.updateTeacherHours(teacher.getId(), newHours);
            } else {
                showAlert("Ошибка", "Часы не могут быть отрицательными");
                teachersTable.refresh();
            }
        });

        teachersTable.setEditable(true);
    }

    private void loadTeachers() {
        ObservableList<Teacher> teachers = FXCollections.observableArrayList(Database.getAllTeachers());
        teachersTable.setItems(teachers);
    }

    @FXML
    private void handleAddTeacher() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить преподавателя");
        dialog.setHeaderText("Введите данные преподавателя");
        dialog.setContentText("ФИО:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(fullName -> {
            TextInputDialog deptDialog = new TextInputDialog();
            deptDialog.setTitle("Кафедра");
            deptDialog.setHeaderText("Введите кафедру для " + fullName);
            deptDialog.setContentText("Кафедра:");

            Optional<String> deptResult = deptDialog.showAndWait();
            deptResult.ifPresent(department -> {
                TextInputDialog groupsDialog = new TextInputDialog();
                groupsDialog.setTitle("Группы");
                groupsDialog.setHeaderText("Введите группы через запятую");
                groupsDialog.setContentText("Группы:");

                Optional<String> groupsResult = groupsDialog.showAndWait();
                groupsResult.ifPresent(groups -> {
                    List<String> groupList = Arrays.asList(groups.split(","));
                    if (Database.addTeacher(fullName, department, groupList)) {
                        loadTeachers(); // Refresh table
                    } else {
                        showAlert("Ошибка", "Не удалось добавить преподавателя");
                    }
                });
            });
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}