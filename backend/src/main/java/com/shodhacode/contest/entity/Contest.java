package com.shodhacode.contest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="contests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contest {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, unique=true)
  private String code;

  @Column(nullable=false)
  private String title;
}
