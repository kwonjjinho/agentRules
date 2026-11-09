---
description: GitHub Actions CI/CD pipeline, Multi-service Matrix builds, Container security scanning, and GitOps integration rules.
globs: .github/workflows/*.yml, .github/workflows/*.yaml
alwaysApply: false
---

# GitHub Actions CI/CD Pipeline Standards

## 1. Multi-Microservice Matrix & Path Filtering

- **Path-Triggered Workflows**:
  - Prevent rebuilding all microservices when only one changes. Use path triggers:
  ```yaml
  name: CI - Order Service
  on:
    push:
      branches: [ main, develop ]
      paths:
        - 'services/order-service/**'
        - '.github/workflows/ci-order-service.yml'
    pull_request:
      branches: [ main ]
      paths:
        - 'services/order-service/**'
  ```
- **Matrix Builds for Multi-Service Verification**:
  - Use GitHub Actions matrix strategies for batch linting or unit testing shared libraries.

## 2. Fast & Deterministic Builds (Caching)

- **Dependency & Build Caching**:
  - Use `actions/cache` or setup actions' built-in cache (`setup-java` with `cache: 'maven'` or `'gradle'`, `setup-node` with `cache: 'pnpm'`).
- **Docker Layer Caching (GitHub Cache / Registry Cache)**:
  - Use `docker/build-push-action@v5` with `cache-from: type=gha` and `cache-to: type=gha,mode=max`.

## 3. Security Scanning & Shift-Left Gates

- **Static Analysis (SAST)**: Run SpotBugs, SonarCloud, or Semgrep in PR check stage.
- **Container Vulnerability Scanning (Trivy)**:
  - Fail the build if `CRITICAL` or `HIGH` vulnerabilities with available fixes exist:
  ```yaml
  - name: Scan Docker Image with Trivy
    uses: aquasecurity/trivy-action@master
    with:
      image-ref: ${{ env.IMAGE_TAG }}
      format: 'table'
      exit-code: '1'
      ignore-unfixed: true
      vuln-type: 'os,library'
      severity: 'CRITICAL,HIGH'
  ```

## 4. GitOps Deployment Strategy

- **Immutable Image Tags**:
  - Never deploy using `latest`. Tag images using Git commit SHA and semantic version: `ghcr.io/org/order-service:${{ github.sha }}`.
- **GitOps Manifest Update**:
  - After successful image push, update the Kubernetes manifest repository (or Helm values in GitOps repo) via a dedicated PR or automated commit to trigger ArgoCD / FluxCD synchronization:
  ```yaml
  - name: Update GitOps Repository
    run: |
      git clone https://x-access-token:${{ secrets.GITOPS_REPO_PAT }}@github.com/org/k8s-gitops.git
      cd k8s-gitops/apps/order-service
      yq -i '.image.tag = "${{ github.sha }}"' values.yaml
      git config user.name "github-actions[bot]"
      git config user.email "github-actions[bot]@users.noreply.github.com"
      git commit -am "chore(deploy): bump order-service to ${{ github.sha }}"
      git push origin main
  ```
