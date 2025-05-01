package com.example.teacherhoursdesktop.controllers;

import com.example.teacherhoursdesktop.models.Teacher;
import com.example.teacherhoursdesktop.models.WorkRecord;
import com.example.teacherhoursdesktop.utils.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.Comparator;

public class TeacherTableController {
    @FXML private TableView<WorkRecord> recordsTable;
    @FXML private TableColumn<WorkRecord, String> dateColumn;
    @FXML private TableColumn<WorkRecord, String> groupColumn;
    @FXML private TableColumn<WorkRecord, String> typeColumn;
    @FXML private TableColumn<WorkRecord, Integer> hoursColumn;

    @FXML private Label teacherNameLabel;
    @FXML private Label departmentLabel;
    @FXML private Label totalHoursLabel;

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> groupFilterCombo;

    private Teacher teacher;
    private ObservableList<WorkRecord> allRecords = FXCollections.observableArrayList();
    private FilteredList<WorkRecord> filteredRecords = new FilteredList<>(allRecords);
    private ObservableList<String> groups = FXCollections.observableArrayList();

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
        updateUI();
        loadRecords();
        loadGroups();
    }

    private void updateUI() {
        teacherNameLabel.setText(teacher.getFullName());
        departmentLabel.setText(teacher.getDepartment());
        totalHoursLabel.setText("Всего часов: " + teacher.getCompletedHours());
    }

    private void loadRecords() {
        allRecords.clear();
        allRecords.addAll(Database.getWorkRecordsForTeacher(teacher.getId()));
        recordsTable.setItems(filteredRecords);
    }

    private void loadGroups() {
        groups.clear();
        groups.addAll(teacher.getGroups());
        groups.add(0, "Все группы");
        groupFilterCombo.setItems(groups);
        groupFilterCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        groupColumn.setCellValueFactory(new PropertyValueFactory<>("group"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("lessonType"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hours"));

        fromDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        toDatePicker.setValue(LocalDate.now());

        // Сортировка по дате по умолчанию
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        recordsTable.getSortOrder().add(dateColumn);
    }

    @FXML
    private void handleFilter() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String selectedGroup = groupFilterCombo.getValue();

        filteredRecords.setPredicate(record -> {
            LocalDate recordDate = LocalDate.parse(record.getDate());

            // Фильтр по дате
            if (fromDate != null && recordDate.isBefore(fromDate)) {
                return false;
            }
            if (toDate != null && recordDate.isAfter(toDate)) {
                return false;
            }

            // Фильтр по группе
            if (selectedGroup != null && !selectedGroup.equals("Все группы") &&
                    !record.getGroup().equals(selectedGroup)) {
                return false;
            }

            return true;
        });

        // Обновляем общее количество часов
        int total = filteredRecords.stream().mapToInt(WorkRecord::getHours).sum();
        totalHoursLabel.setText("Всего часов: " + total + " (отфильтровано)");
    }
}