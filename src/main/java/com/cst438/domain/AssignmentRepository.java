package com.cst438.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AssignmentRepository extends CrudRepository<Assignment, Integer> {

    @Query("select a from Assignment a where a.section.sectionNo = :sectionNo order by a.dueDate")
    List<Assignment> findBySectionNoOrderByDueDate(int sectionNo);

    @Query("select a from Assignment a join a.section.enrollments e " +
            "where a.section.term.year=:year and a.section.term.semester=:semester and" +
            " e.user.id=:studentId order by a.dueDate")
    List<Assignment> findByStudentIdAndYearAndSemesterOrderByDueDate(int studentId, int year, String semester);

    @Query("select a from Assignment a where a.assignmentId = :assignmentId")
    Assignment findByAssignmentId(int assignmentId);
}