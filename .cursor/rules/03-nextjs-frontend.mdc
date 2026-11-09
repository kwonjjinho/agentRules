---
description: Next.js (App Router), React Server Components (RSC), TypeScript strict standards, State Management, and BFF/Gateway client rules.
globs: **/frontend/**, **/web/**, **/*.tsx, **/*.ts, **/app/**, **/components/**
alwaysApply: false
---

# Next.js & Modern Frontend Architecture Rules

## 1. Next.js App Router & Server/Client Boundary

- **Server-First Default (React Server Components)**:
  - All components in `app/` are React Server Components (RSC) by default.
  - Do NOT mark a component with `'use client'` unless it strictly requires:
    - Browser event listeners (`onClick`, `onChange`).
    - React state / lifecycle hooks (`useState`, `useEffect`, `useReducer`).
    - Browser-only APIs (`window`, `localStorage`).
- **Push `'use client'` to the Leaves**:
  - Keep client boundaries as small as possible. Pass server-fetched data or server components as `children` to client wrappers.

```tsx
// ✅ Correct: Server component fetches data, client component handles interactivity
// app/orders/page.tsx (Server Component)
import { getOrders } from '@/lib/api/orders';
import { OrderListFilter } from '@/components/orders/OrderListFilter';

export default async function OrdersPage() {
  const initialOrders = await getOrders();
  return (
    <main className="container mx-auto py-8">
      <OrderListFilter initialOrders={initialOrders} />
    </main>
  );
}
```

## 2. API Communication & BFF (Backend For Frontend)

- **Next.js Route Handlers as BFF**:
  - Use `app/api/**/route.ts` as a BFF layer when you need to:
    - Aggregate calls from multiple Spring Boot microservices.
    - Securely inject secrets / refresh tokens stored in HttpOnly cookies without exposing them to browser JS.
    - Transform microservice response schemas to UI-optimized payloads.
- **Client-Side Data Fetching**:
  - Use **TanStack Query (React Query)** for asynchronous caching, deduplication, and optimistic updates.
  - Never trigger multiple waterfall `useEffect` fetch requests.

## 3. Strict TypeScript Standards

- **No `any`**:
  - Set `"strict": true` and `"noImplicitAny": true` in `tsconfig.json`.
  - Use Zod schemas to validate all untrusted runtime data (API responses, route params, form inputs).
  ```typescript
  import { z } from 'zod';

  export const OrderItemSchema = z.object({
    productId: z.string().uuid(),
    quantity: z.number().int().positive(),
    unitPrice: z.number().nonnegative(),
  });

  export type OrderItem = z.infer<typeof OrderItemSchema>;
  ```

## 4. Authentication & Security (Frontend)

- **Cookie Security**:
  - Store JWT Access/Refresh tokens in **HttpOnly, Secure, SameSite=Lax (or Strict)** cookies.
  - Never persist authentication tokens in `localStorage` or `sessionStorage` (XSS vulnerability).
- **Next.js Middleware**:
  - Protect authenticated route segments using `middleware.ts`.
  - Verify token expiration and redirect unauthenticated users to `/login?callbackUrl=...` before rendering private pages.

## 5. Performance & Asset Optimization

- Use `next/image` for all images to prevent layout shift (CLS) and leverage WebP/AVIF compression.
- Use dynamic imports (`next/dynamic`) for heavy client libraries (charts, rich-text editors).
- Leverage Next.js caching layers (`fetch` cache, `revalidateTag`, `revalidatePath`) appropriately.
