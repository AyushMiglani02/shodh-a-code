package com.shodhacode.contest.repo;
import com.shodhacode.contest.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProblemRepo extends JpaRepository<Problem, Long> {
  List<Problem> findByContestId(Long contestId);
}
