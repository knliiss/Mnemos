ALTER TABLE purchase_operations
    ADD CONSTRAINT ck_purchase_operations_coin_owner
        CHECK (
            currency <> 'COIN'
                OR owner_type = 'ACCOUNT'
        ),
    ADD CONSTRAINT ck_purchase_operations_user_owner_buyer
        CHECK (
            owner_type <> 'USER'
                OR owner_id = buyer_user_id
        );
