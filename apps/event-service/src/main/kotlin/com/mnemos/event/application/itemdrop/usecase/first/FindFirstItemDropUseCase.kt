package com.mnemos.event.application.itemdrop.usecase.first

import com.mnemos.event.application.itemdrop.model.FirstItemDropView

interface FindFirstItemDropUseCase {
    fun execute(query: FindFirstItemDropQuery): FirstItemDropView?
}
