package com.shodhacode.contest.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class DockerRunner {

  @Value("${app.judge.workdir}")
  private String workdir;

  @Value("${app.judge.image}")
  private String judgeImage;

  @Value("${app.judge.timeLimitSeconds}")
  private int timeLimitSeconds;

  @Value("${app.judge.memory}")
  private String memoryLimit;

  @Value("${app.judge.cpus}")
  private String cpuLimit;

  public static class RunResult {
    public int exitCode;
    public String stdout;
    public String stderr;
    public boolean timedOut;
  }

  public Path prepareSource(Long submissionId, String code) throws IOException {
    Path base = Paths.get(workdir, "s" + submissionId);
    Files.createDirectories(base);
    // For MVP support only Java
    Path src = base.resolve("Main.java");
    Files.writeString(src, code, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    return base;
  }

  public RunResult runAgainstInput(Path mountDir, String input) throws IOException, InterruptedException, TimeoutException {
    // docker run with resource limits, mount code, compile and run Main.java
    List<String> cmd = List.of(
      "docker","run","--rm",
      "-m", memoryLimit,
      "--cpus", cpuLimit,
      "-v", mountDir.toAbsolutePath()+":/work:ro",
      judgeImage,
      "bash","-lc",
      // copy to tmp writeable, compile, run with input
      "cp -r /work /tmp/job && cd /tmp/job && javac Main.java && " +
      "timeout " + timeLimitSeconds + "s java Main"
    );

    ProcessBuilder pb = new ProcessBuilder(cmd);
    Process proc = pb.start();

    try (OutputStream os = proc.getOutputStream()) {
      os.write(input.getBytes());
      os.flush();
    }

    // collect output with timeout
    boolean finished = proc.waitFor((long) (timeLimitSeconds + 2), TimeUnit.SECONDS);
    RunResult rr = new RunResult();
    if (!finished) {
      rr.timedOut = true;
      proc.destroyForcibly();
      rr.exitCode = 124;
      rr.stdout = "";
      rr.stderr = "Time Limit Exceeded";
      return rr;
    }
    rr.exitCode = proc.exitValue();
    rr.stdout = new String(proc.getInputStream().readAllBytes());
    rr.stderr = new String(proc.getErrorStream().readAllBytes());
    rr.timedOut = false;
    return rr;
  }
}
