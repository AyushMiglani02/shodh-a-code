package com.shodhacode.contest.controller;

import com.shodhacode.contest.entity.*;
import com.shodhacode.contest.repo.*;
import com.shodhacode.contest.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
@CrossOrigin
public class ContestController {
  private final ContestRepo contestRepo;
  private final ProblemRepo problemRepo;
  private final LeaderboardService leaderboardService;

  @GetMapping("/{contestCode}")
  public ResponseEntity<?> getContest(@PathVariable("contestCode") String contestCode) {
    Contest c = contestRepo.findByCode(contestCode).orElse(null);
    if (c == null) return ResponseEntity.notFound().build();
    List<Problem> problems = problemRepo.findByContestId(c.getId());
    Map<String,Object> view = new HashMap<>();
    view.put("id", c.getId());
    view.put("code", c.getCode());
    view.put("title", c.getTitle());
    view.put("problems", problems.stream().map(p -> Map.of(
      "id", p.getId(),
      "title", p.getTitle(),
      "statement", p.getStatement(),
      "points", p.getPoints()
    )).toList());
    return ResponseEntity.ok(view);
  }

  @GetMapping("/{contestCode}/leaderboard")
  public ResponseEntity<?> leaderboard(@PathVariable("contestCode") String contestCode) {
    Contest c = contestRepo.findByCode(contestCode).orElse(null);
    if (c == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(leaderboardService.leaderboard(c.getId()));
  }
}
