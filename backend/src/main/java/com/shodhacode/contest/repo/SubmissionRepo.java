package com.shodhacode.contest.repo;
import com.shodhacode.contest.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepo extends JpaRepository<Submission, Long> {
}
