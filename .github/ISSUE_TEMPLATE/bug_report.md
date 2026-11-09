---
name: "🐛 Bug Report"
about: Report a defect, exception, performance degradation, or data inconsistency
title: "fix(<scope>): <short description>"
labels: ["bug", "needs-triage"]
assignees: []
---

## 💥 Bug Summary
<!-- A clear and concise description of the bug. -->

## 🔁 Reproduction Steps
1. Send request `POST /api/v1/...` with payload:
2. Observe downstream event on Kafka topic `...`
3. Check error log / Actuator response:

## 🔍 Expected vs. Actual Behavior
- **Expected**: 
- **Actual**: 

## 🪵 Diagnostic Evidence & Logs
```text
<!-- Paste relevant stack trace, JSON structured log with TraceId, or HTTP response -->
```
- **Trace ID**: `...`
- **Environment**: `production` | `staging` | `local-testcontainers`

## 🛠️ Root Cause Hypothesis (Optional)
<!-- Any preliminary analysis or suspect commits/lines -->
