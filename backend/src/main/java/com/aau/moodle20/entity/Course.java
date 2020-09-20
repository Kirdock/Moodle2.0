package com.aau.moodle20.entity;

import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", referencedColumnName = "id")
    private Semester semester;
    private String number;
    private String name;
    private Integer minKreuzel;
    private Integer minPoints;
    private String descriptionTemplate;
    private String description;
    private Integer uploadCount;

    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Set<ExerciseSheet> exerciseSheets;

    @OneToMany(mappedBy = "course")
    Set<UserInCourse> students;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", referencedColumnName = "matriculation_Number")
    User owner;

    public Course(Long id) {
        this.id = id;
    }

    public Course() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMinKreuzel() {
        return minKreuzel;
    }

    public void setMinKreuzel(Integer minKreuzel) {
        this.minKreuzel = minKreuzel;
    }

    public Integer getMinPoints() {
        return minPoints;
    }

    public void setMinPoints(Integer minPoints) {
        this.minPoints = minPoints;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Set<UserInCourse> getStudents() {
        return students;
    }

    public void setStudents(Set<UserInCourse> students) {
        this.students = students;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public Set<ExerciseSheet> getExerciseSheets() {
        return exerciseSheets;
    }

    public void setExerciseSheets(Set<ExerciseSheet> exerciseSheets) {
        this.exerciseSheets = exerciseSheets;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(Integer uploadCount) {
        this.uploadCount = uploadCount;
    }

    public CourseResponseObject createCourseResponseObject() {
        CourseResponseObject responseObject = new CourseResponseObject();
        responseObject.setId(getId());
        responseObject.setName(getName());
        responseObject.setNumber(getNumber());
        responseObject.setOwner(getOwner().getMatriculationNumber());
        return responseObject;
    }

    public Course copy() {
        Course course = new Course();
        course.setMinKreuzel(getMinKreuzel());
        course.setName(getName());
        course.setMinPoints(getMinPoints());
        course.setNumber(getNumber());
        course.setOwner(getOwner());
        course.setDescriptionTemplate(getDescriptionTemplate());
        course.setDescription(getDescription());
        course.setUploadCount(getUploadCount());
        return course;
    }

    public CourseResponseObject createCourseResponseObjectGetCourse() {
        CourseResponseObject responseObject = new CourseResponseObject();
        responseObject.setId(getId());
        responseObject.setName(getName());
        responseObject.setNumber(getNumber());
        responseObject.setMinKreuzel(getMinKreuzel());
        responseObject.setMinPoints(getMinPoints());
        responseObject.setDescriptionTemplate(getDescriptionTemplate());
        responseObject.setSemesterId(getSemester().getId());
        responseObject.setDescription(getDescription());
        responseObject.setUploadCount(getUploadCount());

        if (getExerciseSheets() != null)
            responseObject.setExerciseSheets(getExerciseSheets().stream()
                    .map(ExerciseSheet::getResponseObjectLessInfo)
                    .sorted(Comparator.comparing(ExerciseSheetResponseObject::getSubmissionDate).thenComparing(ExerciseSheetResponseObject::getName))
                    .collect(Collectors.toList()));

        return responseObject;
    }

    public CourseResponseObject createCourseResponseObjectGetAssignedCourse(String matriculationNumber) {
        CourseResponseObject responseObject = createCourseResponseObjectGetCourse();
        if (getExerciseSheets() != null)
            responseObject.setExerciseSheets(getExerciseSheets().stream()
                    .map(exerciseSheet -> exerciseSheet.getResponseObjectLessInfo_WithExampleInfo(matriculationNumber))
                    .sorted(Comparator.comparing(ExerciseSheetResponseObject::getSubmissionDate).thenComparing(ExerciseSheetResponseObject::getName))
                    .collect(Collectors.toList()));

        return responseObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return  Objects.equals(getSemester(), course.getSemester()) &&
                Objects.equals(getNumber(), course.getNumber()) &&
                Objects.equals(getName(), course.getName()) &&
                Objects.equals(getMinKreuzel(), course.getMinKreuzel()) &&
                Objects.equals(getMinPoints(), course.getMinPoints()) &&
                Objects.equals(getDescriptionTemplate(), course.getDescriptionTemplate()) &&
                Objects.equals(getDescription(), course.getDescription()) &&
                Objects.equals(getUploadCount(), course.getUploadCount()) &&
                Objects.equals(getOwner(), course.getOwner());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSemester(), getNumber(), getName(), getMinKreuzel(), getMinPoints(), getDescriptionTemplate(), getDescription(), getUploadCount(), getOwner());
    }
}
