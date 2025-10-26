package com.shodhacode.contest.service;

import com.shodhacode.contest.entity.*;
import com.shodhacode.contest.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class SubmissionService {
  private final SubmissionRepo submissionRepo;
  private final UserRepo userRepo;
  private final ContestRepo contestRepo;
  private final ProblemRepo problemRepo;

  @Transactional
  public Submission createPending(String username, String contestCode, Long problemId, String code, String lang) {
    User user = userRepo.findByUsername(username).orElseGet(() -> {
      User u = new User();
      u.setUsername(username);
      return userRepo.save(u);
    });
    Contest contest = contestRepo.findByCode(contestCode)
      .orElseThrow(() -> new IllegalArgumentException("Contest not found"));
    Problem problem = problemRepo.findById(problemId)
      .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

    Submission s = new Submission();
    s.setUser(user);
    s.setContest(contest);
    s.setProblem(problem);
    s.setCode(code);
    s.setLanguage(lang.toLowerCase());
    s.setStatus("Pending");
    s.setResultText(null);
    return submissionRepo.save(s);
  }

  @Transactional
  public void markRunning(Submission s) {
    s.setStatus("Running");
    submissionRepo.save(s);
  }

  @Transactional
  public void finalizeStatus(Submission s, String status, String resultText) {
    s.setStatus(status);
    s.setResultText(resultText);
    submissionRepo.save(s);
  }
}
