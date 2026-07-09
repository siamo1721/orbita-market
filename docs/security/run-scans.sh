#!/usr/bin/env bash
# Повторный запуск сканирования ИБ (ТЗ раздел 8)

set -euo pipefail
cd "$(dirname "$0")/../.."
mkdir -p docs/security/reports

echo "=== Gitleaks ==="
gitleaks detect --source . \
  --report-format json \
  --report-path docs/security/reports/gitleaks-report.json \
  --no-banner

echo "=== Semgrep ==="
semgrep scan --config auto \
  --json --output docs/security/reports/semgrep-report.json

echo "Отчёты: docs/security/reports/"
echo "Триаж: docs/security/triage.md"
