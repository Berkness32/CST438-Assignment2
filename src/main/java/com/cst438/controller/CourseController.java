package com.cst438.controller;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.dto.CourseDTO;
import com.cst438.service.GradebookServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    TermRepository termRepository;

    @Autowired
    GradebookServiceProxy gradebookService;

    @PostMapping("/courses")
    public CourseDTO addCourse(@RequestBody CourseDTO course) {
        Course c = new Course();
        c.setCredits(course.credits());
        c.setTitle(course.title());
        c.setCourseId(course.courseId());
        courseRepository.save(c);
        CourseDTO courseDTO = new CourseDTO(
                c.getCourseId(),
                c.getTitle(),
                c.getCredits()
        );
        gradebookService.addCourse(courseDTO);
        return courseDTO;
    }

    @PutMapping("/courses")
    public CourseDTO updateCourse(@RequestBody CourseDTO course) {
        Course c = courseRepository.findById(course.courseId()).orElse(null);
        if (c == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found " + course.courseId());
        } else {
            c.setTitle(course.title());
            c.setCredits(course.credits());
            courseRepository.save(c);
            CourseDTO courseDTO = new CourseDTO(
                    c.getCourseId(),
                    c.getTitle(),
                    c.getCredits()
            );
            gradebookService.updateCourse(courseDTO);
            return courseDTO;
        }
    }

    @DeleteMapping("/courses/{courseid}")
    public void deleteCourse(@PathVariable String courseid) {
        Course c = courseRepository.findById(courseid).orElse(null);
        if (c != null) {
            courseRepository.delete(c);
            gradebookService.deleteCourse(courseid);
        }
    }

    @GetMapping("/courses")
    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAllByOrderByCourseIdAsc();
        List<CourseDTO> dto_list = new ArrayList<>();
        for (Course c : courses) {
            dto_list.add(new CourseDTO(c.getCourseId(), c.getTitle(), c.getCredits()));
        }
        return dto_list;
    }

    @GetMapping("/terms")
    public List<Term> getAllTerms() {
        return termRepository.findAllByOrderByTermIdDesc();
    }

}
