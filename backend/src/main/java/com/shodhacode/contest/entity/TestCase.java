package com.shodhacode.contest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="test_cases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestCase {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="problem_id", nullable=false)
  private Problem problem;

  @Column(nullable=false, columnDefinition="text")
  private String inputText;

  @Column(nullable=false, columnDefinition="text")
  private String expectedOutput;
}
