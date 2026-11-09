# ==============================================================================
# Build Stage: Next.js Standalone
# ==============================================================================
FROM node:20-alpine AS builder
WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY package.json pnpm-lock.yaml* ./
# RUN pnpm install --frozen-lockfile

COPY . .
# RUN pnpm build

# ==============================================================================
# Production Runtime Stage: Minimal Node Alpine (Non-root user)
# ==============================================================================
FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

# Create non-root user
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

USER nextjs

EXPOSE 3000

CMD ["node", "server.js"]
