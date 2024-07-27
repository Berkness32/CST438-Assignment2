package com.cst438.service;

import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class GradebookServiceProxy {

    Queue gradebookServiceQueue = new Queue("gradebook_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "registrar_service")
    public void receiveFromGradebook(String message) {
        try {
            // TODO: Implement the message handling logic
        } catch (Exception e) {
            // Log the exception and do not rethrow to avoid infinite loop
        }
    }

    //---------- CourseController sendMessage ----------

    public void addCourse(CourseDTO course) {
        sendMessage("Add course: " + asJsonString(course));
    }

    public void updateCourse(CourseDTO course) {
        sendMessage("Update course: " + asJsonString(course));
    }

    public void deleteCourse(String courseId) {
        sendMessage("Delete course: " + courseId);
    }

    //---------- SectionController sendMessage ----------

    public void addSection(SectionDTO section) {
        sendMessage("Add section: " + asJsonString(section));
    }

    public void updateSection(SectionDTO section) {
        sendMessage("Update section: " + asJsonString(section));
    }

    public void deleteSection(int sectionId) {
        sendMessage("Delete section: " + sectionId);
    }

    //---------- StudentController sendMessage ----------

    public void courseEnroll(EnrollmentDTO enrollment) {
        sendMessage("Course enroll: " + asJsonString(enrollment));
    }

    public void dropCourse(int enrollmentId) {
        sendMessage("Drop course: " + enrollmentId);
    }

    //---------- UserController sendMessage ----------

    public void addUser(UserDTO user) {
        sendMessage("Add user: " + asJsonString(user));
    }

    public void updateUser(UserDTO user) {
        sendMessage("Update user: " + asJsonString(user));
    }

    public void deleteUser(int userId) {
        sendMessage("Delete user: " + userId);
    }

    //---------- Helper Functions ----------

    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(gradebookServiceQueue.getName(), s);
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
