package com.cst438.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.User;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AssignmentController.class)
public class AssignmentControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignmentRepository assignmentRepository;

    @MockBean
    private SectionRepository sectionRepository;

    @MockBean
    private GradeRepository gradeRepository;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @MockBean
    private TermRepository termRepository;

    @Test
    public void testAddAssignmentWithInvalidDueDate() throws Exception {
        // Setup mock data and expectations
        AssignmentDTO newAssignmentDTO = new AssignmentDTO(0, "Test Assignment", "2025-01-01", "CST438", 1, 1); // Invalid due date
        Section mockSection = new Section();
        mockSection.setSecId(1);
        mockSection.setSectionNo(1);
        Course mockCourse = new Course();
        mockCourse.setCourseId("CST438");
        mockSection.setCourse(mockCourse);

        Term mockTerm = new Term();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date endDate = new java.sql.Date(sdf.parse("2024-01-01").getTime());
        mockTerm.setEndDate(endDate); // Set a valid end date
        mockSection.setTerm(mockTerm);

        // Mock the repository find method
        when(sectionRepository.findById(1)).thenReturn(Optional.of(mockSection));

        // Perform POST request
        mockMvc.perform(MockMvcRequestBuilders.post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newAssignmentDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid due date"));
    }

    @Test
    public void testAddNewAssignment() throws Exception {
        // Setup mock data and expectations
        AssignmentDTO newAssignmentDTO = new AssignmentDTO(0, "Test Assignment", "2023-12-31", "CST438", 1, 1);
        Section mockSection = new Section();
        mockSection.setSecId(1);
        mockSection.setSectionNo(1);
        Course mockCourse = new Course();
        mockCourse.setCourseId("CST438");
        mockSection.setCourse(mockCourse);

        Term mockTerm = new Term();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date endDate = new java.sql.Date(sdf.parse("2024-01-01").getTime());
        mockTerm.setEndDate(endDate); // Set a valid end date
        mockSection.setTerm(mockTerm);

        Assignment newAssignment = new Assignment();
        newAssignment.setAssignmentId(1);
        newAssignment.setTitle("Test Assignment");
        newAssignment.setDueDate("2023-12-31");
        newAssignment.setSection(mockSection);

        // Mock the repository find and save methods
        when(sectionRepository.findById(1)).thenReturn(Optional.of(mockSection));
        when(assignmentRepository.save(any())).thenReturn(newAssignment);

        // Perform POST request
        mockMvc.perform(MockMvcRequestBuilders.post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newAssignmentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Assignment"))
                .andExpect(jsonPath("$.dueDate").value("2023-12-31"))
                .andExpect(jsonPath("$.secId").value(1));
    }

    @Test
    public void testAddAssignmentWithInvalidSectionNumber() throws Exception {
        // Setup mock data and expectations
        AssignmentDTO newAssignmentDTO = new AssignmentDTO(0, "Test Assignment", "2023-12-31", "CST438", 999, 1); // Invalid section number

        // Mock the repository find method to return empty
        when(sectionRepository.findById(999)).thenReturn(Optional.empty());

        // Perform POST request
        mockMvc.perform(MockMvcRequestBuilders.post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newAssignmentDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("section not found"));
    }

    @Test
    public void testGradeAssignment() throws Exception {
        // Setup mock data and expectations
        int assignmentId = 1;
        Assignment mockAssignment = new Assignment();
        mockAssignment.setAssignmentId(assignmentId);
        mockAssignment.setTitle("Test Assignment");

        Section mockSection = new Section();
        mockSection.setSecId(1);
        mockSection.setSectionNo(1);
        Course mockCourse = new Course();
        mockCourse.setCourseId("CST438");
        mockSection.setCourse(mockCourse);
        mockAssignment.setSection(mockSection);

        User mockStudent1 = new User();
        mockStudent1.setId(1);
        mockStudent1.setName("Student One");
        mockStudent1.setEmail("student1@example.com");

        User mockStudent2 = new User();
        mockStudent2.setId(2);
        mockStudent2.setName("Student Two");
        mockStudent2.setEmail("student2@example.com");

        Enrollment mockEnrollment1 = new Enrollment();
        mockEnrollment1.setEnrollmentId(1);
        mockEnrollment1.setSection(mockSection);
        mockEnrollment1.setStudent(mockStudent1);

        Enrollment mockEnrollment2 = new Enrollment();
        mockEnrollment2.setEnrollmentId(2);
        mockEnrollment2.setSection(mockSection);
        mockEnrollment2.setStudent(mockStudent2);

        Grade mockGrade1 = new Grade();
        mockGrade1.setGradeId(1);
        mockGrade1.setAssignment(mockAssignment);
        mockGrade1.setEnrollment(mockEnrollment1);

        Grade mockGrade2 = new Grade();
        mockGrade2.setGradeId(2);
        mockGrade2.setAssignment(mockAssignment);
        mockGrade2.setEnrollment(mockEnrollment2);

        List<GradeDTO> gradeDTOs = new ArrayList<>();
        gradeDTOs.add(new GradeDTO(1, "Student One", "student1@example.com", "Test Assignment", "CST438", 1, 90));
        gradeDTOs.add(new GradeDTO(2, "Student Two", "student2@example.com", "Test Assignment", "CST438", 1, 85));

        // Mock the repository find methods
        when(gradeRepository.findById(1)).thenReturn(Optional.of(mockGrade1));
        when(gradeRepository.findById(2)).thenReturn(Optional.of(mockGrade2));

        // Perform PUT request to update grades
        mockMvc.perform(MockMvcRequestBuilders.put("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(gradeDTOs)))
                .andExpect(status().isOk());

        // Verify that the grades were saved with the updated scores
        verify(gradeRepository, times(1)).save(mockGrade1);
        verify(gradeRepository, times(1)).save(mockGrade2);

        // Check that the scores were updated correctly
        assertEquals(Integer.valueOf(90), mockGrade1.getScore());
        assertEquals(Integer.valueOf(85), mockGrade2.getScore());
    }

    @Test
    public void testGradeInvalidAssignment() throws Exception {
        // Setup mock data and expectations
        int invalidAssignmentId = 999;

        // Mock the repository find method to return empty
        when(assignmentRepository.findById(invalidAssignmentId)).thenReturn(Optional.empty());

        // Perform GET request to retrieve assignment grades with an invalid assignment ID
        mockMvc.perform(MockMvcRequestBuilders.get("/assignments/{assignmentId}/grades", invalidAssignmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("assignment not found"));
    }

    // Helper method to convert objects to JSON string
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
