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

    private ObservableList<Teacher> teachers;
    private ObservableList<String> periods = FXCollections.observableArrayList("Неделя", "Месяц");

    @FXML
    public void initialize() {
        setupPeriodCombo();
        setupTeacherCombo();
        setupScheduleTable();
    }

    private void setupPeriodCombo() {
        periodCombo.setItems(periods);
        periodCombo.getSelectionModel().selectFirst();
    }

    private void setupTeacherCombo() {
        teachers = FXCollections.observableArrayList(Database.getAllTeachers());
        teacherCombo.setItems(teachers);
        teacherCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });
        teacherCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });
    }

    private void setupScheduleTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        groupColumn.setCellValueFactory(new PropertyValueFactory<>("group"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hours"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("lessonType"));

        // Make hours editable
        hoursColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        hoursColumn.setOnEditCommit(event -> {
            WorkRecord record = event.getRowValue();
            int newHours = event.getNewValue();
            record.setHours(newHours);
            Database.updateWorkRecord(record.getId(), newHours);
        });
    }

    @FXML
    private void handleTeacherSelected() {
        Teacher selected = teacherCombo.getSelectionModel().getSelectedItem();
        String period = periodCombo.getSelectionModel().getSelectedItem();

        if (selected != null && period != null) {
            loadSchedule(selected.getId(), period);
        }
    }

    @FXML
    private void handlePeriodChanged() {
        Teacher selected = teacherCombo.getSelectionModel().getSelectedItem();
        String period = periodCombo.getSelectionModel().getSelectedItem();

        if (selected != null && period != null) {
            loadSchedule(selected.getId(), period);
        }
    }

    private void loadSchedule(int teacherId, String period) {
        LocalDate startDate, endDate;

        if ("Неделя".equals(period)) {
            startDate = LocalDate.now().with(DayOfWeek.MONDAY);
            endDate = startDate.plusDays(6);
        } else {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        List<WorkRecord> records = Database.getScheduleForTeacher(teacherId, startDate, endDate);
        scheduleTable.setItems(FXCollections.observableArrayList(records));
    }
}