package com.aau.moodle20.entity;

import com.aau.moodle20.payload.request.ExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name ="example")
public class Example {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_Sheet_id", referencedColumnName = "id")
    private ExerciseSheet exerciseSheet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parentExample_id", referencedColumnName = "id")
    private Example parentExample;
    @OneToMany(mappedBy="parentExample", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval=true)
    private Set<Example> subExamples = new HashSet<Example>();

    private String name;
    private String description;
    private Integer points;
    private Integer weighting;
    private Boolean mandatory;
    @Column(name = "sort_order")
    private Integer order;
    private Boolean submitFile;
    private String customFileTypes;
    @OneToMany(mappedBy = "example", fetch = FetchType.LAZY)
    Set<SupportFileType> supportFileTypes;


    public Example() {
    }

    public Example(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExerciseSheet getExerciseSheet() {
        return exerciseSheet;
    }

    public void setExerciseSheet(ExerciseSheet exerciseSheet) {
        this.exerciseSheet = exerciseSheet;
    }

    public Example getParentExample() {
        return parentExample;
    }

    public void setParentExample(Example parentExample) {
        this.parentExample = parentExample;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getWeighting() {
        return weighting;
    }

    public void setWeighting(Integer weighting) {
        this.weighting = weighting;
    }

    public Set<Example> getSubExamples() {
        return subExamples;
    }

    public void setSubExamples(Set<Example> subExamples) {
        this.subExamples = subExamples;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getCustomFileTypes() {
        return customFileTypes;
    }

    public void setCustomFileTypes(String customFileTypes) {
        this.customFileTypes = customFileTypes;
    }

    public Set<SupportFileType> getSupportFileTypes() {
        return supportFileTypes;
    }

    public void setSupportFileTypes(Set<SupportFileType> supportFileTypes) {
        this.supportFileTypes = supportFileTypes;
    }

    public Boolean getSubmitFile() {
        return submitFile;
    }

    public void setSubmitFile(Boolean submitFile) {
        this.submitFile = submitFile;
    }

    public List<String> getCustomFileTypesList() {
        List<String> list = new ArrayList<>();
        if (customFileTypes != null && customFileTypes.length()>0) {
            String[] array = customFileTypes.split(",");
            if (array != null && array.length > 0) {
                for (int i = 0; i < array.length; i++)
                    list.add(array[i]);
            }
        }
        return list;
    }

    public void setCustomFileTypesList(List<String> customFileTypesList)
    {
        if(customFileTypesList!=null && !customFileTypesList.isEmpty())
        {
           this.customFileTypes = customFileTypesList.stream().collect(Collectors.joining(","));
        }
    }

    public void fillValuesFromRequestObject(ExampleRequest exampleRequest) {
        setName(exampleRequest.getName());
        setDescription(exampleRequest.getDescription());
        setMandatory(exampleRequest.getMandatory());
        setOrder(exampleRequest.getOrder());
        setPoints(exampleRequest.getPoints());
        setWeighting(exampleRequest.getWeighting());
        setSubmitFile(exampleRequest.getSubmitFile());
        setCustomFileTypesList(exampleRequest.getCustomFileTypes());
        if (exampleRequest.getParentId() != null)
            setParentExample(new Example(exampleRequest.getParentId()));
        if (exampleRequest.getExerciseSheetId() != null)
            setExerciseSheet(new ExerciseSheet(exampleRequest.getExerciseSheetId()));
    }

    public ExampleResponseObject createExampleResponseObject() {
        ExampleResponseObject exampleResponseObject = new ExampleResponseObject();
        exampleResponseObject.setId(getId());
        if (getParentExample() != null)
            exampleResponseObject.setParentId(getParentExample().getId());
        exampleResponseObject.setDescription(getDescription());
        exampleResponseObject.setMandatory(getMandatory());
        exampleResponseObject.setName(getName());
        exampleResponseObject.setPoints(getPoints());
        exampleResponseObject.setWeighting(getWeighting());
        exampleResponseObject.setOrder(getOrder());
        exampleResponseObject.setCustomFileTypes(getCustomFileTypesList());
        exampleResponseObject.setSubmitFile(getSubmitFile());

        if(getSubExamples()!=null) {
            exampleResponseObject.setSubExamples(getSubExamples().stream()
                    .map(Example::createExampleResponseObject).collect(Collectors.toList()));
            exampleResponseObject.getSubExamples().sort(Comparator.comparing(ExampleResponseObject::getOrder));

        }

        if (getExerciseSheet() != null)
            exampleResponseObject.setExerciseSheetId(getExerciseSheet().getId());
        if (getSupportFileTypes() != null) {
            List<FileTypeResponseObject> fileTypeResponseObjects = getSupportFileTypes().stream()
                    .map(supportFileType -> supportFileType.getFileType().createFileTypeResponseObjectOnlyId())
                    .collect(Collectors.toList());
            exampleResponseObject.setSupportedFileTypes(fileTypeResponseObjects);
        }

        return exampleResponseObject;
    }

    public Example copy()
    {
        Example example = new Example();
        example.setSubmitFile(getSubmitFile());
        example.setWeighting(getWeighting());
        example.setPoints(getPoints());
        example.setOrder(getOrder());
        example.setCustomFileTypes(getCustomFileTypes());
        example.setMandatory(getMandatory());
        example.setName(getName());
        example.setDescription(getDescription());


        return example;
    }
}
