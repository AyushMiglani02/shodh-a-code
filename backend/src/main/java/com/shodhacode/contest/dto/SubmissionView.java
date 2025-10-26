package com.shodhacode.contest.dto;

public record SubmissionView(
  Long submissionId,
  String status,
  String resultText
) {}
