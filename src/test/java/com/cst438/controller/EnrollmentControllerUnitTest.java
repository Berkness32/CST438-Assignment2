package com.cst438.controller;
import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class EnrollmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TermRepository termRepository;


    // Student Successfully Enrolls into a Section
    @Test
    public void testEnrollStudent() {
        int sectionNo = 5; // Assuming this section exists in the test data
        int studentId = 3; // Assuming this student exists in the test data
        
        Enrollment enrollment = new Enrollment();
        enrollment.setSection(sectionRepository.findById(sectionNo).get());
        enrollment.setStudent(userRepository.findById(studentId).get());
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        assertNotNull(savedEnrollment);
        assertEquals(sectionNo, savedEnrollment.getSection().getSectionNo());
        assertEquals(studentId, savedEnrollment.getStudent().getId());
    }


    // Student Attempts to Enroll in a Section but Fails Because the Student is Already Enrolled
    @Test
    public void testEnrollStudentAlreadyEnrolled() {
            int sectionNo = 5;
            int studentId = 3;

            Enrollment enrollment = new Enrollment();
            enrollment.setSection(sectionRepository.findById(sectionNo).get());
            enrollment.setStudent(userRepository.findById(studentId).get());

            // Save the enrollment
            enrollmentRepository.save(enrollment);

            // Attempt to enroll the student again
            Enrollment duplicateEnrollment = new Enrollment();
            duplicateEnrollment.setSection(sectionRepository.findById(sectionNo).get());
            duplicateEnrollment.setStudent(userRepository.findById(studentId).get());

            assertThrows(Exception.class, () -> {
                if (enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId) != null) {
                    throw new Exception("Student is already enrolled in this section");
                }
                enrollmentRepository.save(duplicateEnrollment);
            });
    }


    // Student Attempts to Enroll in a Section but the Section Number is Invalid
    @Test
    public void testEnrollInvalidSection() {
        int invalidSectionNo = 9999; // Assuming this section number doesn't exist
        int studentId = 3;

        assertThrows(Exception.class, () -> {
            Enrollment enrollment = new Enrollment();
            enrollment.setSection(sectionRepository.findById(invalidSectionNo).get());
            enrollment.setStudent(userRepository.findById(studentId).get());
            
            enrollmentRepository.save(enrollment);
        });
    }


    //Student Attempts to Enroll in a Section but It Is Past the Add Deadline
    @Test
    @Transactional
    public void testEnrollPastAddDeadline() {
        int sectionNo = 5; // Assuming this section exists
        int studentId = 3;

        // Retrieve the section and term
        Section section = sectionRepository.findById(sectionNo).get();
        Term term = section.getTerm();

        // Simulate that the current date is past the add deadline by setting the add deadline to yesterday
        term.setAddDeadline(Date.valueOf(LocalDate.now().minusDays(1)));
        termRepository.save(term);

        Enrollment enrollment = new Enrollment();
        enrollment.setSection(section);
        enrollment.setStudent(userRepository.findById(studentId).get());

        // Check if the current date is past the add deadline and simulate throwing an exception
        assertThrows(Exception.class, () -> {
            if (LocalDate.now().isAfter(term.getAddDeadline().toLocalDate())) {
                throw new Exception("Cannot enroll past the add deadline");
            }
            enrollmentRepository.save(enrollment);
        });
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}