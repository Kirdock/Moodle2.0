package com.aau.moodle20.entity;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "finishes_example")
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
    private byte[] attachmentContent;
    private String fileName;

    private Boolean valid = false;
    private Boolean hasPresented = false;
    private EFinishesExampleState state;
    private Integer remainingUploadCount;

    @OneToMany(mappedBy = "finishesExample", fetch = FetchType.LAZY)
    private Set<ViolationHistory> violationHistories;

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

    public Set<ViolationHistory> getViolationHistories() {
        return violationHistories;
    }

    public List<ViolationHistory> getViolationHistoryList() {
        return violationHistories.stream().sorted(Comparator.comparing(ViolationHistory::getDate)).collect(Collectors.toList());
    }

    public void setViolationHistories(Set<ViolationHistory> violationHistories) {
        this.violationHistories = violationHistories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FinishesExample)) return false;
        FinishesExample that = (FinishesExample) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getFileName(), that.getFileName()) &&
                Objects.equals(getValid(), that.getValid()) &&
                Objects.equals(getHasPresented(), that.getHasPresented()) &&
                getState() == that.getState() &&
                Objects.equals(getRemainingUploadCount(), that.getRemainingUploadCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDescription(), getFileName(), getValid(), getHasPresented(), getState(), getRemainingUploadCount());
    }
}
