package com.mnemos.personalization.domain.personalization.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import java.time.Instant

class Personalization(
    val id: PersonalizationId,
    val creatorUserId: UserId,
    val type: PersonalizationType,
    val origin: PersonalizationOrigin,
    visibility: PersonalizationVisibility,
    name: String,
    content: PersonalizationContent,
    price: PersonalizationPrice?,
    status: PersonalizationStatus,
    rejectionReason: String?,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    var visibility: PersonalizationVisibility = visibility
        private set
    var name: String = name
        private set
    var content: PersonalizationContent = content
        private set
    var price: PersonalizationPrice? = price
        private set
    var status: PersonalizationStatus = status
        private set
    var rejectionReason: String? = rejectionReason
        private set
    var updatedAt: Instant = updatedAt
        private set

    init {
        requireName(name)
        requireContentMatchesType(type, content)
        requirePrice(type, origin, visibility, price)
        requireLifecycleState()
    }

    fun updateDraft(
        name: String,
        content: PersonalizationContent,
        price: PersonalizationPrice?,
        now: Instant,
    ) {
        val editable =
            status == PersonalizationStatus.DRAFT ||
                status == PersonalizationStatus.REJECTED ||
                (status == PersonalizationStatus.PRIVATE && origin == PersonalizationOrigin.CUSTOM)

        require(editable) {
            "Personalization cannot be edited from status $status"
        }

        requireName(name)
        requireContentMatchesType(type, content)
        requirePrice(type, origin, visibility, price)

        this.name = name
        this.content = content
        this.price = price
        rejectionReason = null
        status =
            if (origin == PersonalizationOrigin.CUSTOM && visibility == PersonalizationVisibility.PRIVATE) {
                PersonalizationStatus.PRIVATE
            } else {
                PersonalizationStatus.DRAFT
            }
        updatedAt = now
    }

    fun submitForReview(now: Instant) {
        val allowed =
            status == PersonalizationStatus.DRAFT ||
                status == PersonalizationStatus.REJECTED ||
                status == PersonalizationStatus.PRIVATE

        require(allowed) { "Personalization cannot be submitted from status $status" }

        if (origin == PersonalizationOrigin.CUSTOM) {
            require(type == PersonalizationType.NOTIFICATION_PACK) {
                "Only custom notification packs are supported"
            }
        }

        status = PersonalizationStatus.IN_REVIEW
        rejectionReason = null
        updatedAt = now
    }

    fun approve(
        moderatedPrice: PersonalizationPrice,
        now: Instant,
    ) {
        require(status == PersonalizationStatus.IN_REVIEW) { "Personalization is not in review" }
        requirePrice(type, origin, PersonalizationVisibility.PUBLIC, moderatedPrice)

        price = moderatedPrice
        visibility = PersonalizationVisibility.PUBLIC
        status = PersonalizationStatus.APPROVED
        rejectionReason = null
        updatedAt = now
    }

    fun reject(
        reason: String,
        now: Instant,
    ) {
        require(status == PersonalizationStatus.IN_REVIEW) { "Personalization is not in review" }
        require(reason.isNotBlank()) { "Rejection reason cannot be blank" }
        require(reason.length <= MAX_REJECTION_REASON_LENGTH) { "Rejection reason is too long" }

        rejectionReason = reason.trim()
        status = PersonalizationStatus.REJECTED
        updatedAt = now
    }

    fun publish(now: Instant) {
        require(status == PersonalizationStatus.APPROVED) { "Only approved personalization can be published" }
        require(visibility == PersonalizationVisibility.PUBLIC) { "Only public personalization can be published" }
        require(price != null) { "Published personalization requires a price" }

        status = PersonalizationStatus.PUBLISHED
        updatedAt = now
    }

    fun unlist(now: Instant) {
        require(status == PersonalizationStatus.PUBLISHED) { "Only published personalization can be unlisted" }

        status = PersonalizationStatus.UNLISTED
        updatedAt = now
    }

    fun relist(now: Instant) {
        require(status == PersonalizationStatus.UNLISTED) { "Only unlisted personalization can be relisted" }

        status = PersonalizationStatus.PUBLISHED
        updatedAt = now
    }

    fun isPurchasableBy(userId: UserId): Boolean {
        if (status == PersonalizationStatus.PRIVATE) {
            return origin == PersonalizationOrigin.CUSTOM &&
                creatorUserId == userId &&
                price != null
        }

        if (creatorUserId == userId) {
            return false
        }

        return status == PersonalizationStatus.PUBLISHED &&
            visibility == PersonalizationVisibility.PUBLIC &&
            price != null
    }

    private fun requireName(name: String) {
        require(name.isNotBlank()) { "Personalization name cannot be blank" }
        require(name.length in 3..64) { "Personalization name length must be between 3 and 64" }
    }

    private fun requireContentMatchesType(
        type: PersonalizationType,
        content: PersonalizationContent,
    ) {
        val matches =
            when (type) {
                PersonalizationType.NOTIFICATION_PACK -> content is PersonalizationContent.NotificationPack
                PersonalizationType.NICK_STYLE -> content is PersonalizationContent.NickStyle
                PersonalizationType.PROFILE_THEME -> content is PersonalizationContent.ProfileTheme
                PersonalizationType.AVATAR_FRAME -> content is PersonalizationContent.AvatarFrame
                PersonalizationType.PROFILE_EFFECT -> content is PersonalizationContent.ProfileEffect
            }

        require(matches) { "Personalization content does not match type $type" }
    }

    private fun requirePrice(
        type: PersonalizationType,
        origin: PersonalizationOrigin,
        visibility: PersonalizationVisibility,
        price: PersonalizationPrice?,
    ) {
        if (origin == PersonalizationOrigin.CUSTOM && visibility == PersonalizationVisibility.PRIVATE) {
            if (price != null) {
                require(price.currency == Currency.PRIME) { "Private custom notification packs use PRIME only" }
            }
            return
        }

        if (price == null) {
            return
        }

        if (type == PersonalizationType.NOTIFICATION_PACK) {
            require(price.currency == Currency.PRIME) { "Notification packs use PRIME only" }
        }
    }

    private fun requireLifecycleState() {
        if (origin == PersonalizationOrigin.CUSTOM) {
            require(type == PersonalizationType.NOTIFICATION_PACK) {
                "Only notification packs can use custom origin"
            }
        }

        when (status) {
            PersonalizationStatus.DRAFT -> {
                require(origin == PersonalizationOrigin.CREATOR) {
                    "Draft personalization must use creator origin"
                }
                require(visibility == PersonalizationVisibility.PRIVATE) {
                    "Draft personalization must be private"
                }
                require(rejectionReason == null) {
                    "Draft personalization cannot have a rejection reason"
                }
            }

            PersonalizationStatus.PRIVATE -> {
                require(origin == PersonalizationOrigin.CUSTOM) {
                    "Private status is reserved for custom personalization"
                }
                require(visibility == PersonalizationVisibility.PRIVATE) {
                    "Private personalization must use private visibility"
                }
                require(rejectionReason == null) {
                    "Private personalization cannot have a rejection reason"
                }
            }

            PersonalizationStatus.IN_REVIEW -> {
                require(visibility == PersonalizationVisibility.PRIVATE) {
                    "Personalization in review must remain private"
                }
                require(rejectionReason == null) {
                    "Personalization in review cannot have a rejection reason"
                }
            }

            PersonalizationStatus.REJECTED -> {
                require(visibility == PersonalizationVisibility.PRIVATE) {
                    "Rejected personalization must remain private"
                }

                val reason =
                    requireNotNull(rejectionReason) {
                        "Rejected personalization requires a rejection reason"
                    }

                require(reason.isNotBlank()) {
                    "Rejected personalization requires a rejection reason"
                }
                require(reason.length <= MAX_REJECTION_REASON_LENGTH) {
                    "Rejection reason is too long"
                }
            }

            PersonalizationStatus.APPROVED,
            PersonalizationStatus.PUBLISHED,
            PersonalizationStatus.UNLISTED,
            -> {
                require(visibility == PersonalizationVisibility.PUBLIC) {
                    "Approved marketplace personalization must be public"
                }
                require(price != null) {
                    "Approved marketplace personalization requires a price"
                }
                require(rejectionReason == null) {
                    "Approved marketplace personalization cannot have a rejection reason"
                }
            }
        }
    }

    companion object {
        private const val MAX_REJECTION_REASON_LENGTH = 500

        fun createCreatorDraft(
            creatorUserId: UserId,
            type: PersonalizationType,
            name: String,
            content: PersonalizationContent,
            price: PersonalizationPrice,
            now: Instant,
        ): Personalization =
            Personalization(
                id = PersonalizationId.generate(),
                creatorUserId = creatorUserId,
                type = type,
                origin = PersonalizationOrigin.CREATOR,
                visibility = PersonalizationVisibility.PRIVATE,
                name = name,
                content = content,
                price = price,
                status = PersonalizationStatus.DRAFT,
                rejectionReason = null,
                createdAt = now,
                updatedAt = now,
            )

        fun createCustomNotificationPack(
            creatorUserId: UserId,
            name: String,
            content: PersonalizationContent.NotificationPack,
            price: PersonalizationPrice?,
            now: Instant,
        ): Personalization =
            Personalization(
                id = PersonalizationId.generate(),
                creatorUserId = creatorUserId,
                type = PersonalizationType.NOTIFICATION_PACK,
                origin = PersonalizationOrigin.CUSTOM,
                visibility = PersonalizationVisibility.PRIVATE,
                name = name,
                content = content,
                price = price,
                status = PersonalizationStatus.PRIVATE,
                rejectionReason = null,
                createdAt = now,
                updatedAt = now,
            )
    }
}
