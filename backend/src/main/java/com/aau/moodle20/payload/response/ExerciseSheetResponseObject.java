package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExerciseSheetResponseObject {

    private Long id;
    private Long courseId;
    private String name;
    private Integer minKreuzel;
    private Integer minPoints;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime submissionDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime issueDate;
    private String description;
    private List<ExampleResponseObject> examples;
    private String courseName;
    private String courseNumber;
    private Boolean includeThird;

    private Integer exampleCount;
    private Integer pointsTotal;
    private Integer kreuzel;
    private Integer points;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public List<ExampleResponseObject> getExamples() {
        return examples;
    }

    public void setExamples(List<ExampleResponseObject> examples) {
        this.examples = examples;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public Boolean getIncludeThird() {
        return includeThird;
    }

    public void setIncludeThird(Boolean includeThird) {
        this.includeThird = includeThird;
    }

    public Integer getExampleCount() {
        return exampleCount;
    }

    public void setExampleCount(Integer exampleCount) {
        this.exampleCount = exampleCount;
    }

    public Integer getPointsTotal() {
        return pointsTotal;
    }

    public void setPointsTotal(Integer pointsTotal) {
        this.pointsTotal = pointsTotal;
    }

    public Integer getKreuzel() {
        return kreuzel;
    }

    public void setKreuzel(Integer kreuzel) {
        this.kreuzel = kreuzel;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
