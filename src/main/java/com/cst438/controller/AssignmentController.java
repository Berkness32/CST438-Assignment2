package com.cst438.controller;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cst438.domain.*;
import com.cst438.dto.SectionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.GradeDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    UserRepository userRepository;

    // instructor lists assignments for a section.  Assignments ordered by due date.
    // logged in user must be the instructor for the section
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo) {


        List<Assignment> alist = assignmentRepository.findBySectionNoOrderByDueDate(secNo);

        List<AssignmentDTO> dlist = new ArrayList<>();

        for (Assignment a : alist) {
            dlist.add(new AssignmentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDateAsString(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    a.getSection().getSectionNo()));
        }

        return dlist;
    }

    // get Sections for an instructor
    @GetMapping("/sections")
    public List<SectionDTO> getSectionsForInstructor(
            @RequestParam("email") String instructorEmail,
            @RequestParam("year") int year ,
            @RequestParam("semester") String semester )  {


        List<Section> sections = sectionRepository.findByInstructorEmailAndYearAndSemester(instructorEmail, year, semester);

        List<SectionDTO> dto_list = new ArrayList<>();
        for (Section s : sections) {
            User instructor = null;
            if (s.getInstructorEmail()!=null) {
                instructor = userRepository.findByEmail(s.getInstructorEmail());
            }
            dto_list.add(new SectionDTO(
                    s.getSectionNo(),
                    s.getTerm().getYear(),
                    s.getTerm().getSemester(),
                    s.getCourse().getCourseId(),
                    s.getCourse().getTitle(),
                    s.getSecId(),
                    s.getBuilding(),
                    s.getRoom(),
                    s.getTimes(),
                    (instructor!=null) ? instructor.getName() : "",
                    (instructor!=null) ? instructor.getEmail() : ""
            ));
        }
        return dto_list;
    }

    // add assignment
    // user must be instructor of the section
    // return AssignmentDTO with assignmentID generated by database
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO dto) {
        Assignment a = new Assignment();
        a.setTitle(dto.title());
        a.setDueDate(dto.dueDate());
        Section s = sectionRepository.findById(dto.secNo()).orElse(null);
        if (s==null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "section not found");
        }

        // Check if due date is past the end date of the class
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dueDate = sdf.parse(dto.dueDate());
            Date endDate = s.getTerm().getEndDate(); // Assuming this is already a Date object
            if (dueDate.after(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid due date");
            }
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format");
        }
        a.setSection(s);
        assignmentRepository.save(a);

        return new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDueDateAsString(),
                a.getSection().getCourse().getCourseId(),
                a.getSection().getSecId(),
                a.getSection().getSectionNo());
    }

    // update assignment for a section.  Only title and dueDate may be changed.
    // user must be instructor of the section
    // return updated AssignmentDTO
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {
        Assignment a = assignmentRepository.findById(dto.id()).orElse(null);
        if (a == null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "assignment not found");
        }
        a.setTitle(dto.title());
        a.setDueDate(dto.dueDate());
        assignmentRepository.save(a);

        return new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDueDateAsString(),
                a.getSection().getCourse().getCourseId(),
                a.getSection().getSecId(),
                a.getSection().getSectionNo());
    }

    // delete assignment for a section
    // logged in user must be instructor of the section
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId).orElse(null);
        if (a != null) {
            assignmentRepository.delete(a);
        }
    }


    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId).orElse(null);
        if (a==null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "assignment not found");
        }
        List<GradeDTO> dlist = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(a.getSection().getSectionNo());
        for (Enrollment e : enrollments) {
            Grade g = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), a.getAssignmentId());
            if (g==null) {
                // create a grade with null score.
                g = new Grade();
                g.setAssignment(a);
                g.setEnrollment(e);
                gradeRepository.save(g);
            }
            dlist.add(new GradeDTO(
                    g.getGradeId(),
                    e.getStudent().getName(),
                    e.getStudent().getEmail(),
                    a.getTitle(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    g.getScore()));
        }
        return dlist;
    }

    // instructor uploads grades for assignment
    // user must be instructor for the section
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {
        for (GradeDTO g: dlist) {
            Grade grade = gradeRepository.findById(g.gradeId()).orElse(null);
            if (grade!=null) {
                grade.setScore(g.score());
                gradeRepository.save(grade);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "grade not found "+ g.gradeId());
            }
        }
    }



    // student lists their assignments/grades for an enrollment ordered by due date
    // student must be enrolled in the section
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {

        // check that this enrollment is for the logged in user student.

        List<AssignmentStudentDTO> dlist = new ArrayList<>();
        List<Assignment> alist = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);
        for (Assignment a : alist) {

            Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(a.getSection().getSectionNo(), studentId);
            if (e==null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "enrollment not found studentId:"+studentId+" sectionNo:"+a.getSection().getSectionNo());
            }

            // if assignment has been graded, include the score
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId( e.getEnrollmentId(), a.getAssignmentId());

            System.out.println(grade);

            dlist.add(new AssignmentStudentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    (grade!=null)? grade.getScore(): null ));

        }
        return dlist;
    }
}