package com.example.demo1.entity;

import jakarta.persistence.*;

@Entity
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    @Column(columnDefinition = "LONGTEXT")
    private String transcriptText;

    @Column(columnDefinition = "LONGTEXT")
    private String summaryText;

    @Column(columnDefinition = "LONGTEXT")
    private String quizText;



    private String transcript; // file name
    private String summary;
    private String quiz;


    @ManyToOne
    private User user;

    public Video() {}

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getQuiz() {
        return quiz;
    }

    public void setQuiz(String quiz) {
        this.quiz = quiz;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTranscriptText() {
        return transcriptText;
    }

    public void setTranscriptText(String transcriptText) {
        this.transcriptText = transcriptText;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getQuizText() {
        return quizText;
    }

    public void setQuizText(String quizText) {
        this.quizText = quizText;
    }

}