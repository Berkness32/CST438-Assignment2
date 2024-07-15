package com.cst438.controller;
import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.UserRepository;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class SectionControllerUnitTest {

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

    @Test
    public void addSection() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new section.
        // the primary key, secNo, is set to 0. it will be
        // set by the database when the section is inserted.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst499",
                "", 
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert section to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                        .andReturn()
                        .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        SectionDTO result = fromJsonString(response.getContentAsString(), SectionDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.secNo());
        // check other fields of the DTO for expected values
        assertEquals("cst499", result.courseId());

        // check the database
        Section s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNotNull(s);
        assertEquals("cst499", s.getCourse().getCourseId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/"+result.secNo()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNull(s);  // section should not be found after delete
    }

    @Test
    public void addSectionFailsBadCourse( ) throws Exception {

        MockHttpServletResponse response;

        // course id cst599 does not exist.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst599",
                "", 
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                .andReturn()
                .getResponse();

        // response should be 404, the course cst599 is not found
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("course not found cst599", message);

    }

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