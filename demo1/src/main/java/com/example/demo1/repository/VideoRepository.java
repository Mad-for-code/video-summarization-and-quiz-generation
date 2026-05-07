package com.example.demo1.repository;

import com.example.demo1.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findAllByOriginalFilename(String originalFilename);

    Optional<Video> findByYoutubeUrl(String youtubeUrl);

    }



