package com.zikraashik.smart_marks.model;

public class Subject {
    private String name;
    private String marks;

    public Subject() {
    }

    public Subject(String name, String marks) {
        this.name = name;
        this.marks = marks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }
}
