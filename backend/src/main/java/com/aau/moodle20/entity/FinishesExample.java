package com.aau.moodle20.entity;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.payload.response.FinishesExampleResponse;

import javax.persistence.*;

@Entity
@Table(name ="finishes_example")
public class FinishesExample {

    @EmbeddedId
    private FinishesExampleKey id;

    @ManyToOne
    @MapsId("matriculationNumber")
    @JoinColumn(name = "matriculation_Number")
     private User user;

    @ManyToOne
    @MapsId("example_id")
    @JoinColumn(name = "example_id")
    private Example example;

    private String description;

    @Transient
    private byte [] attachmentContent;
    private String fileName;

    private Boolean valid = false;
    private Boolean hasPresented = false;
    private EFinishesExampleState state ;
    private Integer remainingUploadCount;

    public FinishesExample()
    {
    }

    public FinishesExampleKey getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setId(FinishesExampleKey id) {
        this.id = id;
    }

    public Example getExample() {
        return example;
    }

    public void setExample(Example example) {
        this.example = example;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String reason) {
        this.description = reason;
    }

    public byte[] getAttachmentContent() {
        return attachmentContent;
    }

    public void setAttachmentContent(byte[] attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Boolean getHasPresented() {
        return hasPresented;
    }

    public void setHasPresented(Boolean hasPresented) {
        this.hasPresented = hasPresented;
    }

    public EFinishesExampleState getState() {
        return state;
    }

    public void setState(EFinishesExampleState state) {
        this.state = state;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getRemainingUploadCount() {
        return remainingUploadCount;
    }

    public void setRemainingUploadCount(Integer remainingUploadCount) {
        this.remainingUploadCount = remainingUploadCount;
    }

    public FinishesExampleResponse getFinishesExampleResponse() {
        FinishesExampleResponse response = new FinishesExampleResponse();
        response.setExampleName(getExample().getName());
        response.setExampleId(getExample().getId());
        response.setOrder(getExample().getOrder());
        if (getExample().getParentExample() != null)
            response.setParentOrder(getExample().getParentExample().getOrder());

        return response;
    }
}
