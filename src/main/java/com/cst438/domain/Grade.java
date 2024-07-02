package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Grade {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="grade_id")
    private int gradeId;

    @ManyToOne
    @JoinColumn(name="assignment_id", nullable=false)
    private Assignment assignment;
    @ManyToOne
    @JoinColumn(name="enrollment_id", nullable=false)
    private Enrollment enrollment;
    private int score;

    public int getGradeId() { return gradeId; }

    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public int getScore() { return score; }

    public void setScore(int score) {
        this.score = score;
    }

}
