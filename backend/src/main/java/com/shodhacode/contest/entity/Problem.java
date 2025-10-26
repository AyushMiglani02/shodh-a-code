package com.shodhacode.contest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="problems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Problem {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="contest_id", nullable=false)
  private Contest contest;

  @Column(nullable=false)
  private String title;

  @Column(nullable=false, columnDefinition="text")
  private String statement;

  @Column(nullable=false)
  private Integer points;
}
