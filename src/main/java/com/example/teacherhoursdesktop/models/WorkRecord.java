package com.example.teacherhoursdesktop.models;

import javafx.beans.property.*;

public class WorkRecord {
    private final IntegerProperty id;
    private final IntegerProperty teacherId;
    private final StringProperty date;
    private final StringProperty group;
    private final IntegerProperty hours;
    private final StringProperty lessonType;

    public WorkRecord(int id, int teacherId, String date, String group, int hours, String lessonType) {
        this.id = new SimpleIntegerProperty(id);
        this.teacherId = new SimpleIntegerProperty(teacherId);
        this.date = new SimpleStringProperty(date);
        this.group = new SimpleStringProperty(group);
        this.hours = new SimpleIntegerProperty(hours);
        this.lessonType = new SimpleStringProperty(lessonType);
    }

    // Getters and setters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public int getTeacherId() { return teacherId.get(); }
    public IntegerProperty teacherIdProperty() { return teacherId; }

    public String getDate() { return date.get(); }
    public StringProperty dateProperty() { return date; }

    public String getGroup() { return group.get(); }
    public StringProperty groupProperty() { return group; }

    public int getHours() { return hours.get(); }
    public IntegerProperty hoursProperty() { return hours; }
    public void setHours(int hours) { this.hours.set(hours); }

    public String getLessonType() { return lessonType.get(); }
    public StringProperty lessonTypeProperty() { return lessonType; }
}