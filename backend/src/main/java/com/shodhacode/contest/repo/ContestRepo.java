package com.shodhacode.contest.repo;

import com.shodhacode.contest.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ContestRepo extends JpaRepository<Contest, Long> {
  Optional<Contest> findByCode(String code);
}
