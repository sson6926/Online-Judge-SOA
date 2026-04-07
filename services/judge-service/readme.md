# judge-service

Consumes judge jobs from RabbitMQ, runs submissions in a Docker sandbox, publishes `JudgeResultDTO` to the results queue.

## Configuration

| Property / env | Default | Description |
|----------------|---------|-------------|
| `services.problem-service.url` / `PROBLEM_SERVICE_URL` | `http://problem-service:5001` | Base URL for Feign to load test cases from problem-service (`GET /problems/internal/{id}/testcases`). |
| `judge.workspace.cleanup-enabled` / `JUDGE_WORKSPACE_CLEANUP` | `true` | Delete per-job workspace dirs after each message. |
| `judge.workspace.root` / `JUDGE_WORKSPACE_ROOT` | `/tmp/judge-jobs` | Root for temporary workspaces. |

Ensure `problemId` is set on the job payload; otherwise the service returns `SE` with `Missing problem id`.

## Health

`GET /health` returns `{"status":"ok"}`.
