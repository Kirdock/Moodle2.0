package com.aau.moodle20.entity;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "exercise_sheet")
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
    @Column(name = "submission_Date", columnDefinition = "DATETIME")
    private LocalDateTime submissionDate;
    @Column(name = "issue_Date", columnDefinition = "DATETIME")
    private LocalDateTime issueDate;
    private String description;
    private Boolean includeThird;
    @OneToMany(
            mappedBy = "exerciseSheet",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    private Set<Example> examples;

    public ExerciseSheet() {
    }

    public ExerciseSheet(Long id) {
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

    public Boolean getIncludeThird() {
        return includeThird;
    }

    public void setIncludeThird(Boolean includeThird) {
        this.includeThird = includeThird;
    }


    public ExerciseSheetResponseObject getResponseObject(String assignedUserMatriculationNumber) {
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        responseObject.setId(getId());
        responseObject.setCourseId(getCourse().getId());
        responseObject.setMinKreuzel(getMinKreuzel());
        responseObject.setMinPoints(getMinPoints());
        responseObject.setName(getName());
        responseObject.setSubmissionDate(getSubmissionDate());
        responseObject.setIssueDate(getIssueDate());
        responseObject.setDescription(getDescription());
        responseObject.setIncludeThird(getIncludeThird());
        if (getExamples() != null) {
            responseObject.setExamples(getExamples().stream()
                    .filter(example -> example.getParentExample() == null)
                    .map(example -> example.createExampleResponseObject(assignedUserMatriculationNumber))
                    .collect(Collectors.toList()));

            responseObject.getExamples().sort(Comparator.comparing(ExampleResponseObject::getOrder));
        }

        responseObject.setCourseName(course.getName());
        responseObject.setCourseNumber(course.getNumber());
        responseObject.setUploadCount(course.getUploadCount());
        return responseObject;
    }

    public ExerciseSheetResponseObject getResponseObjectLessInfo() {
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        responseObject.setId(getId());
        responseObject.setName(getName());
        responseObject.setSubmissionDate(getSubmissionDate());
        responseObject.setMinPoints(getMinPoints());
        responseObject.setMinKreuzel(getMinKreuzel());

        return responseObject;
    }

    public ExerciseSheetResponseObject getResponseObjectLessInfo_WithExampleInfo(String matriculationNumber) {
        ExerciseSheetResponseObject responseObject = getResponseObjectLessInfo();
        if (getExamples() != null) {
            Integer totalPoints = 0;
            Integer kreuzel = 0;
            Integer points = 0;
            Long exampleCount = getExamples().stream().filter(example -> example.getSubExamples().isEmpty()).count();
            responseObject.setExampleCount(Math.toIntExact(exampleCount));

            for (Example example : getExamples()) {
                if (example.getPoints() != null && example.getWeighting() != null)
                    totalPoints = totalPoints + (example.getPoints() * example.getWeighting());
                Boolean hasFinishedExample = example.getExamplesFinishedByUser().stream()
                        .anyMatch(finishesExample -> finishesExample.getId().getMatriculationNumber().equals(matriculationNumber)
                                && (EFinishesExampleState.YES.equals(finishesExample.getState()) || EFinishesExampleState.MAYBE.equals(finishesExample.getState())));
                if (Boolean.TRUE.equals(hasFinishedExample)) {
                    kreuzel++;
                    points = points + (example.getPoints() * example.getWeighting());
                }
            }
            responseObject.setPointsTotal(totalPoints);
            responseObject.setKreuzel(kreuzel);
            responseObject.setPoints(points);
        }
        return responseObject;
    }

    public Set<Example> getExamples() {
        return examples;
    }

    public void setExamples(Set<Example> examples) {
        this.examples = examples;
    }

    public Integer getTotalPoints() {
        Integer totalPoints = 0;
        for (Example example : getExamples()) {
            totalPoints = totalPoints + (example.getPoints() * example.getWeighting());
        }
        return totalPoints;
    }

    public ExerciseSheet copy() {
        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setDescription(getDescription());
        exerciseSheet.setIssueDate(getIssueDate());
        exerciseSheet.setMinKreuzel(getMinKreuzel());
        exerciseSheet.setMinPoints(getMinPoints());
        exerciseSheet.setIncludeThird(getIncludeThird());
        exerciseSheet.setSubmissionDate(getSubmissionDate());
        exerciseSheet.setName(getName());

        return exerciseSheet;
    }
}
