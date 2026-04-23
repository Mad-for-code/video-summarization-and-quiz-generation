package com.example.demo1.repository;

import com.example.demo1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    }

