package com.aau.moodle20.entity;

import com.aau.moodle20.payload.response.ViolationHistoryResponse;
import com.aau.moodle20.payload.response.ViolationResponse;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name ="violation_history")
public class ViolationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date", columnDefinition="DATETIME")
    private LocalDateTime date;


    @OneToMany(
            mappedBy = "violationHistory",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    private Set<ViolationEntity> violations;

    @JoinColumns({
            @JoinColumn(name="example_id", referencedColumnName="example_id"),
            @JoinColumn(name="matriculation_Number", referencedColumnName="matriculation_Number")
    })
    @ManyToOne
    private FinishesExample finishesExample;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Set<ViolationEntity> getViolations() {
        return violations;
    }

    public void setViolations(Set<ViolationEntity> violations) {
        this.violations = violations;
    }

    public FinishesExample getFinishesExample() {
        return finishesExample;
    }

    public void setFinishesExample(FinishesExample finishesExample) {
        this.finishesExample = finishesExample;
    }

    public ViolationHistoryResponse createViolationHistoryResponse()
    {
        ViolationHistoryResponse response = new ViolationHistoryResponse();
        response.setDate(getDate());
        List<ViolationResponse> violations = getViolations().stream().map(ViolationEntity::createViolationResponse).collect(Collectors.toList());
        response.setViolations(violations);

        return response;
    }
}
