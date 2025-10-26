"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function JoinPage() {
  const [contestCode, setContestCode] = useState("ABC123");
  const [username, setUsername] = useState("");
  const router = useRouter();

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-md bg-white rounded-2xl shadow p-6">
        <h1 className="text-2xl font-bold mb-4 text-center">Shodh-a-Code</h1>
        <div className="space-y-3">
          <div>
            <label className="text-sm font-semibold">Contest Code</label>
            <input value={contestCode} onChange={e=>setContestCode(e.target.value)}
              className="mt-1 w-full border rounded-lg p-2" placeholder="ABC123"/>
          </div>
          <div>
            <label className="text-sm font-semibold">Username</label>
            <input value={username} onChange={e=>setUsername(e.target.value)}
              className="mt-1 w-full border rounded-lg p-2" placeholder="your name"/>
          </div>
          <button
            disabled={!username || !contestCode}
            onClick={()=> router.push(`/contest/${contestCode}?u=${encodeURIComponent(username)}`)}
            className="w-full bg-black text-white rounded-lg py-2 font-semibold disabled:opacity-50">
            Join Contest
          </button>
        </div>
      </div>
    </main>
  );
}
