package com.mnemos.event.application.itemdrop.port.out

import com.mnemos.event.application.itemdrop.model.FirstItemDropView
import com.mnemos.event.application.itemdrop.usecase.first.FindFirstItemDropQuery

interface FirstItemDropReader {
    fun findFirst(query: FindFirstItemDropQuery): FirstItemDropView?
}
