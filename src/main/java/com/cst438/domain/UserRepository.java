package com.cst438.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends 
                 CrudRepository<User, Integer>{

	List<User> findAllByOrderByIdAsc();

	User findByEmail(String email);
}
