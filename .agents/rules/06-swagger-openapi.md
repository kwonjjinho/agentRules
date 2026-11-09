---
description: OpenAPI 3.0 / Swagger (Springdoc) standards, API contract design, and documentation schemas.
globs: **/*Controller*.java, **/*Api*.java, **/openapi*.yaml, **/swagger/**, **/dto/**
alwaysApply: false
---

# Swagger & OpenAPI 3.x Documentation Rules

## 1. OpenAPI & Springdoc Configuration

- **Library**: Use `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.x`.
- **Global Security Schemes**:
  - Register global Bearer JWT security scheme in `OpenAPI` Bean configuration:
  ```java
  @Configuration
  public class OpenApiConfig {
      @Bean
      public OpenAPI customOpenAPI() {
          return new OpenAPI()
              .info(new Info().title("Order Service API").version("v1.0.0"))
              .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
              .components(new Components().addSecuritySchemes("BearerAuth",
                  new SecurityScheme()
                      .name("BearerAuth")
                      .type(SecurityScheme.Type.HTTP)
                      .scheme("bearer")
                      .bearerFormat("JWT")));
      }
  }
  ```
- **Path Separation**:
  - Expose API docs at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.
  - Disable Swagger UI in strictly public production environments if required, or protect behind internal VPN / admin roles.

## 2. Controller & Endpoint Annotations

- Every Controller must declare `@Tag` for grouping.
- Every mutating or critical endpoint must specify:
  - `@Operation`: High-level summary and detailed description.
  - `@ApiResponses`: Complete breakdown of 200/201, 400 (Bad Request), 401 (Unauthorized), 403 (Forbidden), 404 (Not Found), and 500 (Internal Server Error).

```java
@Tag(name = "Orders", description = "Order Lifecycle Management APIs")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    @Operation(summary = "Place a new order", description = "Creates a new order in pending payment status.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failure",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Inventory conflict or duplicate idempotency key",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        ...
    }
}
```

## 3. DTO & Field Schema Descriptions

- Annotate DTO fields with `@Schema`:
  - Provide `description`, realistic `example`, and indicate `requiredMode = Schema.RequiredMode.REQUIRED` where applicable.
  ```java
  public record CreateOrderRequest(
      @Schema(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotBlank String customerId,

      @Schema(description = "Order line items", requiredMode = Schema.RequiredMode.REQUIRED)
      @NotEmpty List<OrderItemDto> items
  ) {}
  ```
- Keep DTOs self-documenting; frontend developers must be able to generate TypeScript types directly from the OpenAPI JSON specification.
