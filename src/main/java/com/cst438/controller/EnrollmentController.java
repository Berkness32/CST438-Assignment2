package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    TermRepository termRepository;

    // instructor downloads student enrollments and grades for a section, ordered by student name
    // user must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo ) {

        List<Enrollment> enrollments = enrollmentRepository
                .findEnrollmentsBySectionNoOrderByStudentName(sectionNo);
        List<EnrollmentDTO> dlist = new ArrayList<>();
        for (Enrollment e : enrollments) {
            dlist.add(new EnrollmentDTO(
                    e.getEnrollmentId(),
                    e.getGrade(),
                    e.getStudent().getId(),
                    e.getStudent().getName(),
                    e.getStudent().getEmail(),
                    e.getSection().getCourse().getCourseId(),
                    e.getSection().getSecId(),
                    e.getSection().getSectionNo(),
                    e.getSection().getBuilding(),
                    e.getSection().getRoom(),
                    e.getSection().getTimes(),
                    e.getSection().getCourse().getCredits(),
                    e.getSection().getTerm().getYear(),
                    e.getSection().getTerm().getSemester()));
        }
        return dlist;
    }

    // instructor uploads final grades for the section
    // user must be instructor for the section
    @PutMapping("/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {
        for (EnrollmentDTO d : dlist) {
            Enrollment e = enrollmentRepository.findById(d.enrollmentId()).orElse(null);
            if (e == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "enrollment not found " + d.enrollmentId());
            } else {
                e.setGrade(d.grade());
                enrollmentRepository.save(e);
            }
        }
    }

    // student enrolls into a section
    @PostMapping("/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO enrollStudent(@RequestBody EnrollmentDTO enrollmentDTO) {
        int sectionNo = enrollmentDTO.sectionNo();
        int studentId = enrollmentDTO.studentId();

        Section section = sectionRepository.findById(sectionNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        // Check if the student is already enrolled
        if (enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is already enrolled in this section");
        }

        // Check if the current date is past the add deadline
        Term term = section.getTerm();
        if (LocalDate.now().isAfter(term.getAddDeadline().toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot enroll past the add deadline");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setSection(section);
        enrollment.setStudent(student);

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return new EnrollmentDTO(
                savedEnrollment.getEnrollmentId(),
                savedEnrollment.getGrade(),
                savedEnrollment.getStudent().getId(),
                savedEnrollment.getStudent().getName(),
                savedEnrollment.getStudent().getEmail(),
                savedEnrollment.getSection().getCourse().getCourseId(),
                savedEnrollment.getSection().getSecId(),
                savedEnrollment.getSection().getSectionNo(),
                savedEnrollment.getSection().getBuilding(),
                savedEnrollment.getSection().getRoom(),
                savedEnrollment.getSection().getTimes(),
                savedEnrollment.getSection().getCourse().getCredits(),
                savedEnrollment.getSection().getTerm().getYear(),
                savedEnrollment.getSection().getTerm().getSemester()
        );
    }
}
