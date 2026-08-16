package com.mnemos.social.adapter.out.persistence.spotlight

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "spotlight_queue_state")
class SpotlightQueueStateEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: Short,
    @Column(name = "next_sequence", nullable = false)
    var nextSequence: Long,
)
