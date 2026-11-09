---
description: Redis caching strategies, Cache-Aside, Distributed Locking with Redisson, and Session/Blacklist management.
globs: **/redis/**, **/cache/**, **/*Redis*.java, **/*Cache*.java
alwaysApply: false
---

# Redis Caching & Distributed Synchronization Rules

## 1. Key Naming & Serialization Standards

- **Key Namespace Convention**:
  - Always prefix keys with domain and environment: `{service}:{environment}:{entity}:{id}`:
    - Example: `order-service:prod:orders:10928`
    - Example: `auth-service:prod:token-blacklist:jti-8812`
- **Serialization**:
  - Avoid default Java serialization (`JdkSerializationRedisSerializer`) due to security flaws and performance penalties.
  - Use `GenericJackson2JsonRedisSerializer` or custom `Jackson2JsonRedisSerializer` configured with `ObjectMapper` registering `JavaTimeModule`.
  - Keys must always be serialized with `StringRedisSerializer`.

## 2. Caching Patterns & Stampede Prevention

- **Cache-Aside (Lazy Loading)**:
  - Read: Check Redis -> If hit, return. If miss, fetch from DB -> Write to Redis with TTL -> return.
  - Write: Update DB -> Evict cache (`redisTemplate.delete(key)`). Never update cache directly in concurrent systems to prevent race conditions.
- **Mandatory TTL (Time-To-Live)**:
  - Every single cached entry must have an explicit TTL. Never write indefinite (`TTL = -1`) keys unless strictly managed static reference data.
- **Cache Stampede & Thundering Herd Defense**:
  - **TTL Jitter**: Add random offset to TTLs (e.g., base 60m + random 0~10m) to prevent simultaneous bulk expirations.
  - **Mutex Lock**: For expensive database aggregates, use distributed lock to let only 1 thread recalculate on cache miss while others wait or return stale data.

## 3. Distributed Locking (Redisson)

- **Avoid Raw `SETNX` Scripts**:
  - Use **Redisson**'s `RLock` which provides automatic lease renewal (Watchdog) and deadlock prevention:
  ```java
  @Service
  @RequiredArgsConstructor
  public class InventoryService {
      private final RedissonClient redisson;
      private final InventoryRepository inventoryRepository;

      public void deductStock(String productId, int quantity) {
          String lockKey = "lock:inventory:" + productId;
          RLock lock = redisson.getLock(lockKey);

          try {
              // wait up to 3s, lease for 5s
              if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                  try {
                      // Perform critical stock deduction
                      inventoryRepository.decrease(productId, quantity);
                  } finally {
                      lock.unlock();
                  }
              } else {
                  throw new BusinessException(ErrorCode.LOCK_ACQUISITION_TIMEOUT);
              }
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new BusinessException(ErrorCode.LOCK_INTERRUPTED);
          }
      }
  }
  ```

## 4. Redis Cluster & Memory Governance

- **Avoid BigKeys**: Keep individual keys/values under 50KB. Large lists or hashes must be chunked or partitioned.
- **Avoid Blocking Commands**:
  - Never execute `KEYS *`, `FLUSHALL`, or `HGETALL` on large datasets in production. Use `SCAN`, `HSCAN`, or explicit secondary keys.
- **Eviction Policy**: Ensure production Redis instances configure `maxmemory-policy: volatile-lru` or `allkeys-lru`.
