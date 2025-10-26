create table if not exists users (
  id bigserial primary key,
  username varchar(64) unique not null
);

create table if not exists contests (
  id bigserial primary key,
  code varchar(32) unique not null,
  title varchar(255) not null
);

create table if not exists problems (
  id bigserial primary key,
  contest_id bigint not null references contests(id) on delete cascade,
  title varchar(255) not null,
  statement text not null,
  points int not null default 100
);

create table if not exists test_cases (
  id bigserial primary key,
  problem_id bigint not null references problems(id) on delete cascade,
  input_text text not null,
  expected_output text not null
);

create table if not exists submissions (
  id bigserial primary key,
  user_id bigint not null references users(id),
  contest_id bigint not null references contests(id),
  problem_id bigint not null references problems(id),
  code text not null,
  language varchar(16) not null,
  status varchar(32) not null,          -- Pending, Running, Accepted, Wrong Answer, TLE, RE
  result_text text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index if not exists idx_submissions_user on submissions(user_id);
create index if not exists idx_submissions_contest on submissions(contest_id);
