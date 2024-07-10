package com.cst438.dto;


/*
 * Data Transfer Object for assignment data
 */
public record NewAssignmentDTO(
        String title,
        java.sql.Date dueDate, // This was String originally
        String courseId, // This was String originally
        int secNo

) {
}
