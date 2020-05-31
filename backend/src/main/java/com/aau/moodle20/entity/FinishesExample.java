package com.aau.moodle20.entity;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;

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
    @Lob
    @Column(name = "attachment", columnDefinition="CLOB")
    private byte [] attachment;

    private Boolean valid = false;
    private Boolean hasPresented = false;
    private EFinishesExampleState state ;

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

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
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
}
