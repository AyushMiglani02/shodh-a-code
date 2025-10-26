export const API_BASE = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8080";

export async function getContest(contestCode: string) {
  const r = await fetch(`${API_BASE}/api/contests/${contestCode}`, { cache: "no-store" });
  if (!r.ok) throw new Error("Contest not found");
  return r.json();
}

export async function getLeaderboard(contestCode: string) {
  const r = await fetch(`${API_BASE}/api/contests/${contestCode}/leaderboard`, { cache: "no-store" });
  return r.json();
}

export async function submitSolution(payload: {
  username: string;
  contestCode: string;
  problemId: number;
  code: string;
  language: string;
}) {
  const r = await fetch(`${API_BASE}/api/submissions`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!r.ok) throw new Error("Submission failed");
  return r.json();
}

export async function getSubmission(submissionId: number) {
  const r = await fetch(`${API_BASE}/api/submissions/${submissionId}`, { cache: "no-store" });
  return r.json();
}
