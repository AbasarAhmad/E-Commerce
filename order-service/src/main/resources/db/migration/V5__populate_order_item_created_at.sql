UPDATE order_items
SET created_at = (
    SELECT created_at
    FROM orders
    WHERE orders.id = order_items.order_id
)
WHERE created_at IS NULL;