package com.example.demo1.repository;

import com.example.demo1.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VideoRepository extends JpaRepository<Video, Long> {
    }



