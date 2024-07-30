package com.cst438.service;

import com.cst438.domain.*;
import com.cst438.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class RegistrarServiceProxy {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message) {
        try {
            System.out.println("Received from Registrar: " + message);
            String[] parts = message.split(" ", 2);
            String action = parts[0];
            String data = parts.length > 1 ? parts[1] : null;

            switch (action) {
                case "addCourse":
                case "updateCourse":
                    CourseDTO courseDTO = fromJsonString(data, CourseDTO.class);
                    updateCourse(courseDTO);
                    break;
                case "deleteCourse":
                    courseRepository.deleteById(data);
                    break;
                case "addSection":
                case "updateSection":
                    SectionDTO sectionDTO = fromJsonString(data, SectionDTO.class);
                    updateSection(sectionDTO);
                    break;
                case "deleteSection":
                    sectionRepository.deleteById(Integer.parseInt(data));
                    break;
                case "addUser":
                case "updateUser":
                    UserDTO userDTO = fromJsonString(data, UserDTO.class);
                    updateUser(userDTO);
                    break;
                case "deleteUser":
                    userRepository.deleteById(Integer.parseInt(data));
                    break;
                case "addEnrollment":
                case "updateEnrollment":
                    EnrollmentDTO enrollmentDTO = fromJsonString(data, EnrollmentDTO.class);
                    updateEnrollment(enrollmentDTO);
                    break;
                case "deleteEnrollment":
                    enrollmentRepository.deleteById(Integer.parseInt(data));
                    break;
                default:
                    System.out.println("Unknown action: " + action);
            }
        } catch (Exception e) {
            System.out.println("Exception in receiveFromRegistrar: " + e.getMessage());
        }
    }

    public void sendFinalGrade(EnrollmentDTO enrollmentDTO) {
        String message = "updateFinalGrade " + asJsonString(enrollmentDTO);
        sendMessage(message);
    }

    private void updateCourse(CourseDTO dto) {
        Course course = courseRepository.findById(dto.courseId()).orElse(new Course());
        course.setCourseId(dto.courseId());
        course.setTitle(dto.title());
        course.setCredits(dto.credits());
        courseRepository.save(course);
    }

    private void updateSection(SectionDTO dto) {
        Section section = sectionRepository.findById(dto.secNo()).orElse(new Section());
        section.setSectionNo(dto.secNo());
        section.setSecId(dto.secId());
        section.setBuilding(dto.building());
        section.setRoom(dto.room());
        section.setTimes(dto.times());
        section.setInstructor_email(dto.instructorEmail());
        
        Course course = courseRepository.findById(dto.courseId()).orElse(null);
        if (course != null) {
            section.setCourse(course);
        }
        
        Term term = new Term();
        term.setYear(dto.year());
        term.setSemester(dto.semester());
        section.setTerm(term);
        
        sectionRepository.save(section);
    }

    private void updateUser(UserDTO dto) {
        User user = userRepository.findById(dto.id()).orElse(new User());
        user.setId(dto.id());
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setType(dto.type());
        userRepository.save(user);
    }

    private void updateEnrollment(EnrollmentDTO dto) {
        Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId()).orElse(new Enrollment());
        enrollment.setEnrollmentId(dto.enrollmentId());
        enrollment.setGrade(dto.grade());
        
        User student = userRepository.findById(dto.studentId()).orElse(null);
        if (student != null) {
            enrollment.setStudent(student);
        }
        
        Section section = sectionRepository.findById(dto.sectionId()).orElse(null);
        if (section != null) {
            enrollment.setSection(section);
        }
        
        enrollmentRepository.save(enrollment);
    }

    private void sendMessage(String message) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), message);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
