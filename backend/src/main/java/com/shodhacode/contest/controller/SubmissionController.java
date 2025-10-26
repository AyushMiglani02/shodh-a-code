package com.shodhacode.contest.controller;

import com.shodhacode.contest.dto.CreateSubmissionRequest;
import com.shodhacode.contest.dto.SubmissionView;
import com.shodhacode.contest.entity.Submission;
import com.shodhacode.contest.repo.SubmissionRepo;
import com.shodhacode.contest.service.JudgeService;
import com.shodhacode.contest.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@CrossOrigin
public class SubmissionController {
  private final SubmissionService submissionService;
  private final SubmissionRepo submissionRepo;
  private final JudgeService judgeService;

  @PostMapping
  public ResponseEntity<?> create(@Valid @RequestBody CreateSubmissionRequest req) {
    Submission s = submissionService.createPending(
      req.username(), req.contestCode(), req.problemId(), req.code(), req.language());
    judgeService.enqueueAndRunAsync(s.getId());
    return ResponseEntity.ok(new SubmissionView(s.getId(), s.getStatus(), s.getResultText()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> get(@PathVariable("id") Long id) {
    return submissionRepo.findById(id)
      .<ResponseEntity<?>>map(s -> ResponseEntity.ok(
        new SubmissionView(s.getId(), s.getStatus(), s.getResultText())))
      .orElse(ResponseEntity.notFound().build());
  }
}
