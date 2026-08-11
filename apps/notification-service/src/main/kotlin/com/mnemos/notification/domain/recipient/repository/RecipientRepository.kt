package com.mnemos.notification.domain.recipient.repository

import com.mnemos.notification.domain.recipient.model.Recipient

interface RecipientRepository {
    fun upsert(recipient: Recipient)
}
