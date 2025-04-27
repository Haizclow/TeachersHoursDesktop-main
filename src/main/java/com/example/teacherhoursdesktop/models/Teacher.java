package com.example.teacherhoursdesktop.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Teacher {
    private final IntegerProperty id;
    private final StringProperty fullName;
    private final StringProperty department;

    public Teacher(int id, String fullName, String department) {
        this.id = new SimpleIntegerProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.department = new SimpleStringProperty(department);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }
    public void setFullName(String fullName) { this.fullName.set(fullName); }

    public String getDepartment() { return department.get(); }
    public StringProperty departmentProperty() { return department; }
    public void setDepartment(String department) { this.department.set(department); }
}