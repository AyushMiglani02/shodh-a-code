
insert into contests (id, code, title) values (1, 'ABC123', 'Sample Coding Contest') on conflict do nothing;

insert into problems (id, contest_id, title, statement, points) values
  (1, 1, 'Sum A+B', 'Read two integers and print their sum.', 100),
  (2, 1, 'Echo Lines', 'Read lines until EOF and echo them without change.', 100),
  (3, 1, 'Factorial N', 'Read integer N and print N! for 0<=N<=10.', 100)
on conflict do nothing;

insert into test_cases (problem_id, input_text, expected_output) values
  (1, '2 3\n', '5\n'),
  (1, '10 20\n', '30\n'),
  (2, 'hello\nworld\n', 'hello\nworld\n'),
  (3, '5\n', '120\n'),
  (3, '0\n', '1\n');
