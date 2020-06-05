package com.aau.moodle20.entity;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.payload.response.AssignedStudent;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.payload.response.UserPresentedResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name ="course")
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
    private Boolean includeThird;

    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Set<ExerciseSheet> exerciseSheets;

    @OneToMany(mappedBy = "course")
    Set<UserInCourse> students; // TODO find better name

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", referencedColumnName = "matriculation_Number")
    User owner;
    public Course(Long id)
    {
        this.id = id;
    }
    public Course()
    {
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

    public Boolean getIncludeThird() {
        return includeThird;
    }

    public void setIncludeThird(Boolean includeThird) {
        this.includeThird = includeThird;
    }

    public CourseResponseObject createCourseResponseObject()
    {
        CourseResponseObject responseObject = new CourseResponseObject();
        responseObject.setId(getId());
        responseObject.setName(getName());
        responseObject.setNumber(getNumber());
        responseObject.setOwner(getOwner().getMatriculationNumber());
        return responseObject;
    }

    public Course copy()
    {
        Course course = new Course();
        course.setMinKreuzel(getMinKreuzel());
        course.setName(getName());
        course.setMinPoints(getMinPoints());
        course.setNumber(getNumber());
        course.setOwner(getOwner());
        course.setDescriptionTemplate(getDescriptionTemplate());
        course.setIncludeThird(getIncludeThird());

        return course;
    }

    public CourseResponseObject createCourseResponseObject_FullInfo() {
        CourseResponseObject responseObject = new CourseResponseObject();
        responseObject.setId(getId());
        responseObject.setName(getName());
        responseObject.setNumber(getNumber());
        responseObject.setMinKreuzel(getMinKreuzel());
        responseObject.setMinPoints(getMinPoints());
        responseObject.setDescriptionTemplate(getDescriptionTemplate());
        responseObject.setIncludeThird(getIncludeThird());
        responseObject.setSemesterId(getSemester().getId());
        if (getExerciseSheets() != null)
            responseObject.setExerciseSheets(getExerciseSheets().stream()
                    .map(ExerciseSheet::getResponseObjectLessInfo)
                    .sorted(Comparator.comparing(ExerciseSheetResponseObject::getSubmissionDate))
                    .collect(Collectors.toList()));

        List<AssignedStudent> assignedStudents = new ArrayList<>();
        for(UserInCourse userInCourse : getStudents())
        {
            if(userInCourse.getRole().equals(ECourseRole.Student) && userInCourse.getUser().getFinishedExamples()!=null)
            {
                AssignedStudent assignedStudent = new AssignedStudent();
                List<UserPresentedResponse> userPresentedResponseList = new ArrayList<>();
                for(FinishesExample finishesExample: userInCourse.getUser().getFinishedExamples())
                {
                    if(!finishesExample.getHasPresented())
                        continue;

                    UserPresentedResponse userPresentedResponse = new UserPresentedResponse();
                    userPresentedResponse.setExampleId(finishesExample.getExample().getId());
                    userPresentedResponse.setExampleName(finishesExample.getExample().getName());
                    userPresentedResponse.setOrder(finishesExample.getExample().getOrder());
                    if(finishesExample.getExample().getParentExample()!=null)
                        userPresentedResponse.setParentOrder(finishesExample.getExample().getParentExample().getOrder());
                    userPresentedResponseList.add(userPresentedResponse);
                }
                assignedStudent.setPresentedExamples(userPresentedResponseList);
                assignedStudent.setMatriculationNumber(userInCourse.getUser().getMatriculationNumber());
                assignedStudents.add(assignedStudent);
            }
        }
        responseObject.setAssignedStudents(assignedStudents);


        return responseObject;

    }
}
