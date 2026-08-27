# Player directory presence operations

This runbook defines the operating boundary for the Cristalix-backed player directory and SAO presence pipeline. It is the source of truth for dashboards, alerting, quota sizing, and replica policy until a dedicated Prometheus/Grafana deployment is added to this repository.

## Runtime boundary

`player-directory-service` owns Cristalix identity resolution, presence observations, SAO realm projections, and presence-subscription tracking. Interactive profile reads use the `interactive` credential lane. Scheduled tracking and unresolved nickname reconciliation use the `monitoring` lane. A `reserve` lane is used only after cooldown or upstream rate limiting of a primary lane.

The Resilience4j quota buckets in `CristalixRequestExecutor` are process-local. The monitoring schedulers therefore **must run in exactly one service replica** until the quota limiter is replaced by a distributed token bucket or another shared quota coordinator. Do not scale `player-directory-service` horizontally in Compose or another orchestrator while this restriction exists.

The current Compose definition intentionally uses a fixed `container_name`, which prevents Compose service scaling. Preserve that single-instance boundary in any non-Compose deployment.

## Metrics

### Operational gauges

| Metric | Meaning |
| --- | --- |
| `mnemos.player_directory.tracking.due` | Tracked players whose next poll is due and whose lease is available. |
| `mnemos.player_directory.tracking.oldest_overdue` | Seconds since the oldest currently due tracking poll should have run. |
| `mnemos.player_directory.resolution.queue` | Pending nickname/activity resolution records. |
| `mnemos.player_directory.tracking.online` | Actively tracked players currently observed online. |
| `mnemos.player_directory.tracking.sao` | Actively tracked, non-hidden players currently online in an SAO realm. |
| `mnemos.player_directory.presence.stale` | Actively tracked players whose last presence observation is older than `PLAYER_DIRECTORY_OBSERVABILITY_STALE_PRESENCE_AFTER`. |

The operational snapshot is refreshed every `PLAYER_DIRECTORY_OBSERVABILITY_REFRESH_INTERVAL` and defaults to 15 seconds. Staleness defaults to 5 minutes.

### Presence transition telemetry

`mnemos.player_directory.presence.offline_detection_latency` is a timer from the first offline candidate observation to the observation that confirms an `ONLINE -> OFFLINE` transition. Initial OFFLINE observations and unverifiable `UNKNOWN`/`HIDDEN` observations are not recorded as offline detection latency.

### Cristalix request telemetry

| Metric | Labels | Meaning |
| --- | --- | --- |
| `mnemos.player_directory.cristalix.requests` | `lane`, `outcome` | Request count by credential lane and result. Outcomes include `success`, `failure`, `local_quota_rejected`, and `upstream_429`. |
| `mnemos.player_directory.cristalix.request_duration` | `lane`, `outcome` | Request latency timer. |
| `mnemos.player_directory.cristalix.batch_size` | `operation` | Batch size summary. Operations currently include `by_id` and `by_nickname`. |
| `mnemos.player_directory.cristalix.quota_rejections` | `lane` | Requests rejected by the local Resilience4j quota bucket. |
| `mnemos.player_directory.cristalix.upstream_429` | `lane` | Requests rejected by Cristalix with HTTP 429. |
| `mnemos.player_directory.cristalix.cooldowns` | `lane` | Upstream cooldown windows entered after HTTP 429. |
| `mnemos.player_directory.cristalix.cooldown_skips` | `lane` | Primary/reserve requests skipped because the lane was already cooling down. |
| `mnemos.player_directory.cristalix.reserve_failover` | `primary_lane` | Requests failed over from a primary lane to reserve credentials. |

## Dashboard layout

Use one player-directory dashboard with these panels:

1. Tracking backlog: `tracking.due` and `tracking.oldest_overdue` on the same time range.
2. Identity resolution backlog: `resolution.queue`.
3. Presence health: `tracking.online`, `tracking.sao`, and `presence.stale`.
4. Offline confirmation: p50/p95/p99 and maximum of `presence.offline_detection_latency`.
5. Cristalix request rate: requests/min grouped by `lane` and `outcome`.
6. Cristalix latency: p50/p95/p99 `request_duration` grouped by lane.
7. Quota pressure: rate of `quota_rejections`, `upstream_429`, `cooldowns`, and `cooldown_skips`.
8. Reserve usage: `reserve_failover` grouped by `primary_lane`.
9. Batch efficiency: average and p95 `batch_size` grouped by operation.

## Initial alert policy

These are intentionally conservative starting thresholds. Revisit them after production baselines are available rather than weakening the quota guard.

### Page / critical

- Any increase in `mnemos.player_directory.cristalix.upstream_429` over 5 minutes. An upstream 429 means the service has already reached a credential-side limit and may be failing over to reserve credentials.
- `mnemos.player_directory.tracking.oldest_overdue > 120` seconds for 5 minutes. A continuously due player being more than two minutes late means the scheduler is no longer keeping up with its own requested poll schedule.
- `mnemos.player_directory.presence.stale > 0` continuously for 15 minutes. The default staleness window is already 5 minutes, so a further 15-minute persistence indicates a sustained observation problem.

### Warning

- Any increase in `mnemos.player_directory.cristalix.quota_rejections` over 5 minutes. Local rejection protects Cristalix, but indicates that configured demand has reached the local lane budget.
- Any increase in `mnemos.player_directory.cristalix.reserve_failover` over 10 minutes. Reserve credentials are a degradation path, not normal steady-state capacity.
- `mnemos.player_directory.tracking.oldest_overdue > 30` seconds for 10 minutes.
- `mnemos.player_directory.resolution.queue` increases for 15 minutes without returning toward baseline. Queue count is workload-dependent, so do not use a fixed paging threshold until a production baseline exists.

Do not page solely on `tracking.online`, `tracking.sao`, or offline-detection percentiles until normal daily population and activity-class baselines are established. Those panels are diagnostic signals, not stable absolute SLIs yet.

## Monitoring quota sizing

At the current defaults:

- tracking scheduler delay: 1 second;
- pending-resolution scheduler delay: 1 second;
- tracking batch size: 50 players;
- resolution batch size: 50 nicknames;
- monitoring lane limit: 240 requests/minute.

A scheduler execution issues at most one monitoring HTTP request because each claimed page is sent as one batch. With both queues continuously non-empty, one replica therefore has an approximate request-rate ceiling of:

```text
tracking requests/min  = 60 / tracking scheduler delay seconds
resolution requests/min = 60 / resolution scheduler delay seconds
monitoring requests/min = tracking requests/min + resolution requests/min

at defaults:
60 / 1 + 60 / 1 = 120 requests/min
```

The corresponding maximum claimed record throughput at full 50-item batches is approximately:

```text
60 * 50 tracking players + 60 * 50 resolution nicknames = 6,000 records/min
```

This is an execution ceiling, not a guaranteed throughput figure: fixed-delay scheduling waits for each execution to finish, empty queues issue no request, retries/backoff reduce demand, and upstream response time also lowers actual request rate.

The configured 240 requests/min monitoring bucket therefore leaves roughly 50% request-rate headroom for one continuously saturated replica. Because the bucket is process-local, `N` replicas can independently admit up to `N * 240` monitoring requests/minute. Even before that limiter ceiling, two saturated default replicas can approach 240 scheduler requests/minute and remove the intended headroom. Keep one replica.

When changing scheduler delays, batch sizes, or monitoring quota, recalculate the formula above. Prefer increasing batch utilization before reducing scheduler delay. Do not treat the reserve lane as normal capacity.

## Incident checks

### Tracking falls behind

1. Check `tracking.oldest_overdue`, `tracking.due`, and `presence.stale` together.
2. Check monitoring-lane request outcomes and latency.
3. If quota rejections or 429s are present, do not increase scheduler frequency. Reduce demand or restore credential quota first.
4. If requests are successful but backlog grows, inspect DB latency and batch utilization before tuning batch size.

### Resolution queue grows

1. Compare `resolution.queue` with `batch_size{operation="by_nickname"}`.
2. Check monitoring-lane failures, quota rejections, and 429s.
3. Verify that nickname ownership hits are resolving work locally; only unresolved names should reach Cristalix.
4. Keep retries/backoff enabled. Do not bypass the queue with synchronous HTTP from realm reads.

### Presence becomes stale

1. Check whether tracking is overdue.
2. Check monitoring-lane request success and request duration.
3. Check local quota rejections and upstream cooldown/failover counters.
4. Verify tracking state still has an active retention window or a presence subscription.

### Cristalix starts returning 429

1. Treat the primary lane as degraded and inspect reserve failover immediately.
2. Confirm there is only one `player-directory-service` replica.
3. Compare actual request rate with the configured lane permit count.
4. Respect `Retry-After`/cooldown. Do not raise the local quota above the upstream credential allowance merely to clear backlog.

## Rollout checklist

Before changing presence polling in production:

- keep exactly one service replica;
- verify interactive, monitoring, and reserve credentials are distinct and present;
- verify `quota_rejections`, `upstream_429`, and `reserve_failover` are visible;
- verify the six operational gauges refresh;
- verify offline-detection latency records only confirmed transitions;
- verify no realm-read path performs Cristalix HTTP;
- record the current scheduler delays, batch sizes, and credential quota in the deployment change.
