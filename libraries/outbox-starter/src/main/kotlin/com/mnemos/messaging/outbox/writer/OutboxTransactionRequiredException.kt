package com.mnemos.messaging.outbox.writer

class OutboxTransactionRequiredException : IllegalStateException(
    "Outbox event must be appended inside an active Spring transaction"
)