CREATE INDEX idx_status_history_order_changed
ON order_status_history (order_id, changed_at);