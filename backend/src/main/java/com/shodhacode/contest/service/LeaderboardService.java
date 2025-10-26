package com.shodhacode.contest.service;

import com.shodhacode.contest.entity.Submission;
import com.shodhacode.contest.repo.SubmissionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/** Simple leaderboard: count accepted submissions per user in a contest */
@Service @RequiredArgsConstructor
public class LeaderboardService {
  private final SubmissionRepo submissionRepo;

  public List<Map<String,Object>> leaderboard(Long contestId) {
    var all = submissionRepo.findAll().stream()
      .filter(s -> s.getContest().getId().equals(contestId))
      .toList();

    Map<String, Long> accepted = all.stream()
      .filter(s -> "Accepted".equals(s.getStatus()))
      .collect(Collectors.groupingBy(s -> s.getUser().getUsername(), Collectors.counting()));

    return accepted.entrySet().stream()
      .sorted((a,b) -> Long.compare(b.getValue(), a.getValue()))
      .map(e -> Map.<String,Object>of("username", e.getKey(), "score", e.getValue()))
      .toList();
  }
}
