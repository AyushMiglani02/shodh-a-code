package com.shodhacode.contest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;

@Entity @Table(name="submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne @JoinColumn(name="user_id", nullable=false)
  private User user;

  @ManyToOne @JoinColumn(name="contest_id", nullable=false)
  private Contest contest;

  @ManyToOne @JoinColumn(name="problem_id", nullable=false)
  private Problem problem;

  @Column(nullable=false, columnDefinition="text")
  private String code;

  @Column(nullable=false, length=16)
  private String language; // "java" for this MVP

  @Column(nullable=false, length=32)
  private String status; // Pending, Running, Accepted, Wrong Answer, TLE, RE

  @Column(columnDefinition="text")
  private String resultText;

  @Column(nullable=false)
  private OffsetDateTime created_at = OffsetDateTime.now();

  @UpdateTimestamp
  private OffsetDateTime updated_at;
}
