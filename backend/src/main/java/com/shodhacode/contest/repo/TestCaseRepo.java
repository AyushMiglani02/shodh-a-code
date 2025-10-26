package com.shodhacode.contest.repo;
import com.shodhacode.contest.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TestCaseRepo extends JpaRepository<TestCase, Long> {
  List<TestCase> findByProblemId(Long problemId);
}
