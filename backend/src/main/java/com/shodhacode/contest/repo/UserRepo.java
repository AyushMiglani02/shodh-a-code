package com.shodhacode.contest.repo;
import com.shodhacode.contest.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepo extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
}
