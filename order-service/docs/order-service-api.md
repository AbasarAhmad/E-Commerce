# Order Service APIs

## Create Order

POST /api/v1/orders

Headers:

Authorization: Bearer <JWT>
Idempotency-Key: <unique-key>

Request:

{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}

Response:

201 Created

A repeated request with the same Idempotency-Key returns:

200 OK

with the same order.

---

## Get Order

GET /api/v1/orders/{orderId}

Authorization: Bearer <JWT>

Users can access their own orders.

Admins can access all orders.

---

## Get Orders

GET /api/v1/orders?page=0&size=10

Authorization: Bearer <JWT>

Supports:

- Pagination
- Sorting
- Maximum page size: 50

Default sorting:

createdAt DESC

---

## Get Order Items

GET /api/v1/orders/{orderId}/items?page=0&size=10

Authorization: Bearer <JWT>

Supports:

- Pagination
- Sorting
- Maximum page size: 50

Default sorting:

createdAt DESC

---

## Update Order Status

PATCH /api/v1/orders/{orderId}/status

Authorization: Bearer <ADMIN_JWT>

Request:

{
  "status": "CONFIRMED"
}

Only administrators can update order status.

---

## Get Order Status

GET /api/v1/orders/{orderId}/status

Authorization: Bearer <JWT>

---

## Cancel Order

POST /api/v1/orders/{orderId}/cancel

Authorization: Bearer <JWT>

The order owner or administrator can cancel the order.

Inventory is restored when an order is cancelled.

---

## Get Order History

GET /api/v1/orders/{orderId}/history?page=0&size=10

Authorization: Bearer <JWT>

Supports:

- Pagination
- Sorting
- Maximum page size: 50

Default sorting:

changedAt ASC