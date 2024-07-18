package com.cst438.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cst438.domain.Course;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
public class EnrollmentControllerUnitTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SectionRepository sectionRepository;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TermRepository termRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    // Student Successfully Enrolls into a Section
    @Test
    public void testEnrollStudent() throws Exception {
        int sectionNo = 5;
        int studentId = 3;

        Section section = new Section();
        section.setSectionNo(sectionNo);
        
        Term term = new Term();
        term.setAddDeadline(Date.valueOf(LocalDate.now().plusDays(1))); // Set a future add deadline
        section.setTerm(term);

        Course course = new Course();
        course.setCourseId("CS101");
        section.setCourse(course);

        User student = new User();
        student.setId(studentId);

        when(sectionRepository.findById(sectionNo)).thenReturn(Optional.of(section));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArgument(0));

        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(0, null, studentId, null, null, course.getCourseId(), 0, sectionNo, null, null, null, 0, 0, null);

        mvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionNo").value(sectionNo))
                .andExpect(jsonPath("$.studentId").value(studentId));
    }

    // Student Attempts to Enroll in a Section but Fails Because the Student is Already Enrolled
    @Test
    public void testEnrollStudentAlreadyEnrolled() throws Exception {
        int sectionNo = 5;
        int studentId = 3;

        Section section = new Section();
        section.setSectionNo(sectionNo);

        Term term = new Term();
        term.setAddDeadline(Date.valueOf(LocalDate.now().plusDays(1))); // Set a future add deadline
        section.setTerm(term);

        Course course = new Course();
        course.setCourseId("CS101");
        section.setCourse(course);

        User student = new User();
        student.setId(studentId);
        Enrollment existingEnrollment = new Enrollment();
        existingEnrollment.setSection(section);
        existingEnrollment.setStudent(student);

        when(sectionRepository.findById(sectionNo)).thenReturn(Optional.of(section));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId))
                .thenReturn(existingEnrollment);

        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(0, null, studentId, null, null, course.getCourseId(), 0, sectionNo, null, null, null, 0, 0, null);

        mvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Student is already enrolled in this section"));
    }

    // Student Attempts to Enroll in a Section but the Section Number is Invalid
    @Test
    public void testEnrollInvalidSection() throws Exception {
        int invalidSectionNo = 9999;
        int studentId = 3;

        when(sectionRepository.findById(invalidSectionNo)).thenReturn(Optional.empty());

        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(0, null, studentId, null, null, null, 0, invalidSectionNo, null, null, null, 0, 0, null);

        mvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Section not found"));
    }

    // Student Attempts to Enroll in a Section but It Is Past the Add Deadline
    @Test
    public void testEnrollPastAddDeadline() throws Exception {
        int sectionNo = 5;
        int studentId = 3;

        Section section = new Section();
        section.setSectionNo(sectionNo);
        Term term = new Term();
        term.setAddDeadline(Date.valueOf(LocalDate.now().minusDays(1))); // Set a past add deadline
        section.setTerm(term);

        Course course = new Course();
        course.setCourseId("CS101");
        section.setCourse(course);

        User student = new User();
        student.setId(studentId);

        when(sectionRepository.findById(sectionNo)).thenReturn(Optional.of(section));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(0, null, studentId, null, null, course.getCourseId(), 0, sectionNo, null, null, null, 0, 0, null);

        mvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot enroll past the add deadline"));
    }
}
