package com.aau.moodle20.entity;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.persistence.*;

@Entity
@Table(name ="finishes_example")
public class FinishesExample {

    @EmbeddedId
    private FinishesExampleKey id;

    @ManyToOne
    @MapsId("matrikelNummer")
    @JoinColumn(name = "matrikelNummer")
     private User user;

    @ManyToOne
    @MapsId("example_id")
    @JoinColumn(name = "example_id")
    private Example example;

    private String reason;
    @Lob
    @Column(name = "attachment", columnDefinition="CLOB")
    private byte [] attachment;

    private Boolean valid;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
}
