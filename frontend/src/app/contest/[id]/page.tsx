"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { getContest, getLeaderboard, submitSolution, getSubmission } from "@/lib/api";
import { useSearchParams } from "next/navigation";

export default function ContestPage({ params }: { params: { id: string } }) {
  const sp = useSearchParams();
  const username = sp.get("u") || "";
  const codeParam = params.id;

  const [contest, setContest] = useState<any>(null);
  const [selectedProblemId, setSelectedProblemId] = useState<number | null>(null);
  const [code, setCode] = useState<string>(
`// Java 21
import java.util.*;
public class Main {
  public static void main(String[] args) throws Exception {
    Scanner sc = new Scanner(System.in);
    // write your solution here
    int a = sc.nextInt(), b = sc.nextInt();
    System.out.println(a+b);
  }
}`
  );
  const [language, setLanguage] = useState("java");
  const [status, setStatus] = useState<string>("");
  const [result, setResult] = useState<string>("");
  const [leaderboard, setLeaderboard] = useState<any[]>([]);

  // fetch contest
  useEffect(() => {
    getContest(codeParam).then((c) => {
      setContest(c);
      if (c.problems?.length) setSelectedProblemId(c.problems[0].id);
    }).catch(() => setContest(null));
  }, [codeParam]);

  // poll leaderboard
  useEffect(() => {
    let t: any;
    async function poll() {
      const lb = await getLeaderboard(codeParam);
      setLeaderboard(lb);
      t = setTimeout(poll, 15000);
    }
    poll();
    return () => clearTimeout(t);
  }, [codeParam]);

  async function handleSubmit() {
    if (!selectedProblemId) return;
    setStatus("Submitting...");
    setResult("");
    const res = await submitSolution({
      username,
      contestCode: codeParam,
      problemId: selectedProblemId,
      code,
      language
    });
    setStatus(res.status);
    // poll this submission
    let tries = 0;
    const poll = async () => {
      tries++;
      const s = await getSubmission(res.submissionId);
      setStatus(s.status);
      setResult(s.resultText || "");
      if (s.status === "Pending" || s.status === "Running") {
        setTimeout(poll, 2500);
      }
    };
    poll();
  }

  if (!contest) {
    return <div className="p-6">Loading or contest not found</div>;
  }

  const problem = useMemo(() => contest.problems.find((p:any)=>p.id===selectedProblemId), [contest, selectedProblemId]);

  return (
    <main className="min-h-screen p-4 bg-gray-50">
      <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-3 gap-4">
        <section className="lg:col-span-2 bg-white rounded-2xl shadow p-4">
          <h2 className="text-xl font-bold">{contest.title}</h2>
          <div className="mt-3 flex gap-2 overflow-x-auto">
            {contest.problems.map((p:any)=>(
              <button key={p.id}
                onClick={()=>setSelectedProblemId(p.id)}
                className={`px-3 py-1 rounded-lg border ${selectedProblemId===p.id?'bg-black text-white':'bg-gray-100'}`}>
                {p.title}
              </button>
            ))}
          </div>
          {problem && (
            <>
              <div className="mt-4">
                <h3 className="font-semibold">Problem</h3>
                <pre className="mt-2 text-sm bg-gray-100 p-3 rounded-lg whitespace-pre-wrap">{problem.statement}</pre>
              </div>
              <div className="mt-4">
                <label className="text-sm font-semibold">Language</label>
                <select value={language} onChange={e=>setLanguage(e.target.value)} className="ml-2 border rounded p-1">
                  <option value="java">Java 21</option>
                </select>
              </div>
              <div className="mt-2">
                <textarea value={code} onChange={e=>setCode(e.target.value)}
                  className="w-full h-64 border rounded-lg p-3 font-mono text-sm" />
              </div>
              <div className="mt-3 flex gap-2">
                <button onClick={handleSubmit} className="bg-black text-white px-4 py-2 rounded-lg">Submit</button>
                <div className="self-center text-sm">
                  <span className="font-semibold">Status:</span> {status}
                </div>
              </div>
              {result && (
                <div className="mt-3">
                  <h4 className="font-semibold">Result</h4>
                  <pre className="bg-gray-100 p-3 rounded-lg text-sm whitespace-pre-wrap">{result}</pre>
                </div>
              )}
            </>
          )}
        </section>
        <aside className="bg-white rounded-2xl shadow p-4">
          <h3 className="text-lg font-bold mb-2">Leaderboard</h3>
          <ol className="space-y-2">
            {leaderboard.length === 0 && <li className="text-sm text-gray-500">No accepted solutions yet</li>}
            {leaderboard.map((row:any, idx:number)=>(
              <li key={row.username} className="flex justify-between border rounded-lg p-2">
                <span>{idx+1}. {row.username}</span>
                <span className="font-semibold">{row.score}</span>
              </li>
            ))}
          </ol>
        </aside>
      </div>
    </main>
  );
}
