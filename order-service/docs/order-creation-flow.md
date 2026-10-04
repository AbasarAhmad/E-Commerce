# Order Creation Flow

## Services

Order creation involves:

1. API Gateway
2. Order Service
3. Auth Service
4. Product Service

## Flow

Client
  ↓
API Gateway
  ↓
Order Service
  ↓
Auth Service
  ↓
Product Service
  ↓
Order Service Database

## Transaction Boundary

The `@Transactional` boundary exists inside Order Service.

It does not span Auth Service or Product Service.

## Inventory Compensation

If order creation fails after inventory has been decreased:

Order Service
  ↓
Restore inventory in Product Service

This provides compensation for the distributed operation.

## Idempotency

The `Idempotency-Key` prevents duplicate order creation when a client retries the same request.

The key is stored in the `orders` table with a unique constraint.