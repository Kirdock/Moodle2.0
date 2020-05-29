package com.aau.moodle20.entity;

import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name ="exercise_sheet")
public class ExerciseSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;
    private String name;
    private Integer minKreuzel;
    private Integer minPoints;
    @Column(name = "submission_Date", columnDefinition="DATETIME")
    private LocalDateTime submissionDate;
    @Column(name = "issue_Date", columnDefinition="DATETIME")
    private LocalDateTime issueDate;
    private String description;
    @OneToMany(
            mappedBy = "exerciseSheet",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    private Set<Example> examples;
    public ExerciseSheet()
    {
    }

    public ExerciseSheet(Long id )
    {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
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

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExerciseSheetResponseObject getResponseObject()
    {
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        responseObject.setId(getId());
        responseObject.setCourseId(getCourse().getId());
        responseObject.setMinKreuzel(getMinKreuzel());
        responseObject.setMinPoints(getMinPoints());
        responseObject.setName(getName());
        responseObject.setSubmissionDate(getSubmissionDate());
        responseObject.setIssueDate(getIssueDate());
        responseObject.setDescription(getDescription());
        if (getExamples() != null) {
            responseObject.setExamples(getExamples().stream()
                    .filter(example -> example.getParentExample() == null)
                    .map(Example::createExampleResponseObject)
                    .collect(Collectors.toList()));

            responseObject.getExamples().sort(Comparator.comparing(ExampleResponseObject::getOrder));
        }

        responseObject.setCourseName(course.getName());
        responseObject.setCourseNumber(course.getNumber());
        return responseObject;
    }

    public Set<Example> getExamples() {
        return examples;
    }

    public void setExamples(Set<Example> examples) {
        this.examples = examples;
    }

    public ExerciseSheet copy()
    {
        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setDescription(getDescription());
        exerciseSheet.setIssueDate(getIssueDate());
        exerciseSheet.setMinKreuzel(getMinKreuzel());
        exerciseSheet.setMinPoints(getMinPoints());
        exerciseSheet.setSubmissionDate(getSubmissionDate());
        exerciseSheet.setName(getName());

        return exerciseSheet;
    }
}
