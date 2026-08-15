CREATE UNIQUE INDEX uq_purchase_operations_pending_owner_item
    ON purchase_operations (owner_type, owner_id, personalization_id)
    WHERE status = 'PENDING';
