package com.example.teacherhoursdesktop.controllers;

import com.example.teacherhoursdesktop.models.Teacher;
import com.example.teacherhoursdesktop.models.WorkRecord;
import com.example.teacherhoursdesktop.utils.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class ScheduleController {
    @FXML private ComboBox<Teacher> teacherCombo;
    @FXML private ComboBox<String> periodCombo;
    @FXML private TableView<WorkRecord> scheduleTable;
    @FXML private TableColumn<WorkRecord, String> dateColumn;
    @FXML private TableColumn<WorkRecord, String> groupColumn;
    @FXML private TableColumn<WorkRecord, Integer> hoursColumn;
    @FXML private TableColumn<WorkRecord, String> typeColumn;

    @FXML
    public void initialize() {
        // Инициализация списка периодов
        periodCombo.setItems(FXCollections.observableArrayList("Неделя", "Месяц"));
        periodCombo.getSelectionModel().selectFirst();

        // Загрузка преподавателей
        loadTeachers();

        // Настройка таблицы
        setupScheduleTable();

        // Обработчики изменений
        teacherCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> updateSchedule());
        periodCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> updateSchedule());
    }

    private void loadTeachers() {
        List<Teacher> teachers = Database.getAllTeachers();
        ObservableList<Teacher> teacherList = FXCollections.observableArrayList(teachers);
        teacherCombo.setItems(teacherList);

        // Настройка отображения ФИО преподавателей в ComboBox
        teacherCombo.setCellFactory(param -> new ListCell<Teacher>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });

        teacherCombo.setButtonCell(new ListCell<Teacher>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });

        // Автоматически выбираем первого преподавателя, если есть
        if (!teacherList.isEmpty()) {
            teacherCombo.getSelectionModel().selectFirst();
        }
    }

    private void setupScheduleTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        groupColumn.setCellValueFactory(new PropertyValueFactory<>("group"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hours"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("lessonType"));

        hoursColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        hoursColumn.setOnEditCommit(event -> {
            WorkRecord record = event.getRowValue();
            int newHours = event.getNewValue();

            // Валидация вводимых часов
            if (newHours < 0) {
                showAlert("Ошибка", "Количество часов не может быть отрицательным");
                scheduleTable.refresh();
                return;
            }

            // Обновляем запись в базе данных
            boolean success = Database.updateWorkRecord(record.getId(), newHours);

            if (success) {
                record.setHours(newHours);
            } else {
                showAlert("Ошибка", "Не удалось обновить запись");
                scheduleTable.refresh();
            }
        });
    }

    private void updateSchedule() {
        Teacher selectedTeacher = teacherCombo.getSelectionModel().getSelectedItem();
        String selectedPeriod = periodCombo.getSelectionModel().getSelectedItem();

        if (selectedTeacher != null && selectedPeriod != null) {
            LocalDate startDate, endDate;

            if ("Неделя".equals(selectedPeriod)) {
                startDate = LocalDate.now().with(DayOfWeek.MONDAY);
                endDate = startDate.plusDays(6);
            } else {
                startDate = LocalDate.now().withDayOfMonth(1);
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            }

            List<WorkRecord> records = Database.getScheduleForTeacher(
                    selectedTeacher.getId(), startDate, endDate);

            scheduleTable.getItems().setAll(records);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}