---
description: Apache Kafka & Event-Driven Architecture (EDA) standards, Transactional Outbox, Idempotent Consumers, and DLQ handling.
globs: **/kafka/**, **/event/**, **/events/**, **/*Producer*.java, **/*Consumer*.java, **/*Listener*.java
alwaysApply: false
---

# Event-Driven Architecture (EDA) & Apache Kafka Rules

## 1. Event Specification & Schema Design

- **CloudEvents Standard Envelope**:
  - All domain and integration events must follow the CloudEvents standard structure:
  ```json
  {
    "specversion": "1.0",
    "type": "com.company.order.created",
    "source": "https://service.company.com/order-service",
    "id": "A234-1234-1234",
    "time": "2026-11-09T10:45:00Z",
    "datacontenttype": "application/json",
    "data": {
      "orderId": "ORD-10023",
      "customerId": "CUST-9912",
      "totalAmount": 150000
    }
  }
  ```
- **Partition Key Strategy**:
  - Always assign an explicit Partition Key (e.g., `aggregateId` like `orderId` or `userId`) to guarantee strict in-order message delivery per aggregate.
  - Never publish business events with null keys unless strict round-robin balancing is intentional.

## 2. Kafka Producer Standards

- **Reliability Configurations**:
  - `acks=all` (ensure replication across in-sync replicas).
  - `enable.idempotence=true` (guarantee exactly-once producer semantics without duplicate batches).
  - `retries=Integer.MAX_VALUE`.
  - `compression.type=zstd` (or `snappy` for high throughput, low latency).
- **Transactional Outbox Pattern**:
  - Do NOT call Kafka producer directly inside DB transactional write methods.
  - Write domain events to the local `outbox_events` table inside the same DB transaction:
  ```java
  @Transactional
  public Order createOrder(CreateOrderCommand cmd) {
      Order order = orderRepository.save(new Order(cmd));
      outboxRepository.save(new OutboxEvent(
          "com.company.order.created",
          order.getId().toString(),
          toJson(new OrderCreatedEvent(order))
      ));
      return order;
  }
  ```

## 3. Kafka Consumer & Idempotency Rules

- **At-Least-Once Delivery & Idempotent Processing**:
  - Consumers must anticipate duplicate delivery. Maintain an `idempotency_keys` or `processed_events` table in DB:
  ```java
  @KafkaListener(topics = "${app.kafka.topics.order-events}", groupId = "payment-service-group")
  @Transactional
  public void handleOrderEvent(@Header(KafkaHeaders.RECEIVED_KEY) String key,
                               @Payload EventEnvelope<OrderCreatedEvent> envelope,
                               Acknowledgment ack) {
      if (processedEventRepository.existsById(envelope.id())) {
          log.warn("Event {} already processed, skipping.", envelope.id());
          ack.acknowledge();
          return;
      }

      paymentService.processPayment(envelope.data());
      processedEventRepository.save(new ProcessedEvent(envelope.id(), Instant.now()));
      ack.acknowledge();
  }
  ```
- **Manual Acknowledgment**:
  - Set `spring.kafka.listener.ack-mode=MANUAL_IMMEDIATE` for critical transactional consumers.

## 4. Error Handling & Dead Letter Queue (DLQ)

- **Retry with Exponential Backoff**:
  - Configure `DefaultErrorHandler` with exponential backoff (e.g., 1s, 2s, 4s, max 3 attempts) for transient failures (e.g., DB connection drops).
- **Dead Letter Topic (DLT)**:
  - Non-retryable errors (deserialization, business validation failure) must be routed directly to `<topic-name>.DLT` to prevent poison pills from blocking the consumer partition.
  - Set up alerting and automated replay tooling for messages landing in DLT.
