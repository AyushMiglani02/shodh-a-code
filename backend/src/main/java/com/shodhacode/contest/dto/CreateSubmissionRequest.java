package com.shodhacode.contest.dto;

import jakarta.validation.constraints.*;

public record CreateSubmissionRequest(
  @NotBlank String username,
  @NotBlank String contestCode,
  @NotNull Long problemId,
  @NotBlank String code,
  @NotBlank String language
) {}
