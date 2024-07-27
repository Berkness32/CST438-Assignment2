package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.service.GradebookServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    GradebookServiceProxy gradebookService;

    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") Optional<Integer> studentId) {
        if (studentId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "must have request param for studentId");
        }
        User student = userRepository.findById(studentId.get()).orElse(null);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user id not found");
        }
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId.get());
        List<EnrollmentDTO> dlist = new ArrayList<>();
        for (Enrollment e : enrollments) {
            dlist.add(new EnrollmentDTO(
                    e.getEnrollmentId(),
                    e.getGrade(),
                    studentId.get(),
                    student.getName(),
                    student.getEmail(),
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

    @GetMapping("/enrollments")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            @RequestParam("studentId") int studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);
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

    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            @RequestParam("studentId") int studentId) {
        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        if (e != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "already enrolled in this section");
        }
        e = new Enrollment();
        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student id not found");
        }
        e.setStudent(student);
        Section section = sectionRepository.findById(sectionNo).orElse(null);
        if (section == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "section number not found");
        }
        Date now = new Date();
        if (now.before(section.getTerm().getAddDate()) || now.after(section.getTerm().getAddDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot enroll in this section due to date");
        }
        e.setSection(section);
        enrollmentRepository.save(e);
        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(
                e.getEnrollmentId(),
                e.getGrade(),
                student.getId(),
                student.getName(),
                student.getEmail(),
                e.getSection().getCourse().getCourseId(),
                e.getSection().getSecId(),
                e.getSection().getSectionNo(),
                e.getSection().getBuilding(),
                e.getSection().getRoom(),
                e.getSection().getTimes(),
                e.getSection().getCourse().getCredits(),
                e.getSection().getTerm().getYear(),
                e.getSection().getTerm().getSemester());
        gradebookService.courseEnroll(enrollmentDTO);
        return enrollmentDTO;
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {
        Enrollment e = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "enrollment not found");
        }
        Date now = new Date();
        if (now.after(e.getSection().getTerm().getDropDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enrollment can not be deleted due to the drop deadline date");
        }
        enrollmentRepository.delete(e);
        gradebookService.dropCourse(enrollmentId);
    }
}
