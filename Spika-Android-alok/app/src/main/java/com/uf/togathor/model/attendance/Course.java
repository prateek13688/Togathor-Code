package com.uf.togathor.model.attendance;

/**
 * Created by Alok on 1/16/2015.
 */
public class Course {

    private String name;
    private String id;
    private String instructor;

    public Course() {
        super();
    }

    public Course(String courseName, String courseID, String courseInstructor)  {
        this.name = courseName;
        this.id = courseID;
        this.instructor = courseInstructor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }
}
