package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    private Integer uploadCount;


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

    public Integer getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(Integer uploadCount) {
        this.uploadCount = uploadCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExerciseSheetResponseObject)) return false;
        ExerciseSheetResponseObject that = (ExerciseSheetResponseObject) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getCourseId(), that.getCourseId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getMinKreuzel(), that.getMinKreuzel()) &&
                Objects.equals(getMinPoints(), that.getMinPoints()) &&
                Objects.equals(getSubmissionDate(), that.getSubmissionDate()) &&
                Objects.equals(getIssueDate(), that.getIssueDate()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getExamples(), that.getExamples()) &&
                Objects.equals(getCourseName(), that.getCourseName()) &&
                Objects.equals(getCourseNumber(), that.getCourseNumber()) &&
                Objects.equals(getIncludeThird(), that.getIncludeThird()) &&
                Objects.equals(getExampleCount(), that.getExampleCount()) &&
                Objects.equals(getPointsTotal(), that.getPointsTotal()) &&
                Objects.equals(getKreuzel(), that.getKreuzel()) &&
                Objects.equals(getPoints(), that.getPoints()) &&
                Objects.equals(getUploadCount(), that.getUploadCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCourseId(), getName(), getMinKreuzel(), getMinPoints(), getSubmissionDate(), getIssueDate(), getDescription(), getExamples(), getCourseName(), getCourseNumber(), getIncludeThird(), getExampleCount(), getPointsTotal(), getKreuzel(), getPoints(), getUploadCount());
    }
}
