package com.example.teacherhoursdesktop.models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Teacher {
    private final IntegerProperty id;
    private final StringProperty fullName;
    private final StringProperty department;
    private final IntegerProperty plannedHours;
    private final IntegerProperty completedHours;
    private final ObservableList<String> groups;

    public Teacher(int id, String fullName, String department, int plannedHours, int completedHours) {
        this.id = new SimpleIntegerProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.department = new SimpleStringProperty(department);
        this.plannedHours = new SimpleIntegerProperty(plannedHours);
        this.completedHours = new SimpleIntegerProperty(completedHours);
        this.groups = FXCollections.observableArrayList();
    }

    // Геттеры и сеттеры
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getDepartment() { return department.get(); }
    public StringProperty departmentProperty() { return department; }

    public int getPlannedHours() { return plannedHours.get(); }
    public IntegerProperty plannedHoursProperty() { return plannedHours; }
    public void setPlannedHours(int hours) { this.plannedHours.set(hours); }

    public int getCompletedHours() { return completedHours.get(); }
    public IntegerProperty completedHoursProperty() { return completedHours; }

    public ObservableList<String> getGroups() { return groups; }
}