package com.shodhacode.contest.service;

import com.shodhacode.contest.entity.*;
import com.shodhacode.contest.repo.*;
import com.shodhacode.contest.util.DockerRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

@Service @RequiredArgsConstructor
public class JudgeService {
  private final TestCaseRepo testCaseRepo;
  private final SubmissionRepo submissionRepo;
  private final SubmissionService submissionService;
  private final DockerRunner dockerRunner;
  private final ExecutorService submissionExecutor;

  public void enqueueAndRunAsync(Long submissionId) {
    submissionExecutor.submit(() -> runSubmission(submissionId));
  }

  private void runSubmission(Long submissionId) {
    Submission s = submissionRepo.findById(submissionId).orElse(null);
    if (s == null) return;
    submissionService.markRunning(s);

    try {
      List<TestCase> cases = testCaseRepo.findByProblemId(s.getProblem().getId());
      Path srcDir = dockerRunner.prepareSource(s.getId(), s.getCode());

      for (TestCase tc : cases) {
        DockerRunner.RunResult rr = dockerRunner.runAgainstInput(srcDir, tc.getInputText());
        if (rr.timedOut) {
          submissionService.finalizeStatus(s, "TLE", "Time limit exceeded");
          return;
        }
        if (rr.exitCode != 0) {
          submissionService.finalizeStatus(s, "RE", "Runtime error:\n" + rr.stderr);
          return;
        }
        String normalized = rr.stdout.replace("\r\n","\n");
        if (!normalized.equals(tc.getExpectedOutput())) {
          submissionService.finalizeStatus(s, "Wrong Answer",
            "Expected:\n" + tc.getExpectedOutput() + "\nGot:\n" + normalized);
          return;
        }
      }
      submissionService.finalizeStatus(s, "Accepted", "All test cases passed");
    } catch (Exception ex) {
      submissionService.finalizeStatus(s, "RE", "Judge exception: " + ex.getMessage());
    }
  }
}
