package com.cst438.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface CourseRepository extends CrudRepository<Course, String> {
    List<Course> findAllByOrderByCourseIdAsc();
}
