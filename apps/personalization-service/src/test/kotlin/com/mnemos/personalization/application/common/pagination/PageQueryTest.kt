package com.mnemos.personalization.application.common.pagination

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PageQueryTest {
    @Test
    fun `page query accepts maximum supported size`() {
        assertDoesNotThrow {
            PageQuery(
                page = 0,
                size = PageQuery.MAX_PAGE_SIZE,
            )
        }
    }

    @Test
    fun `page query rejects size above supported maximum`() {
        assertThrows(IllegalArgumentException::class.java) {
            PageQuery(
                page = 0,
                size = PageQuery.MAX_PAGE_SIZE + 1,
            )
        }
    }

    @Test
    fun `page query rejects negative page`() {
        assertThrows(IllegalArgumentException::class.java) {
            PageQuery(
                page = -1,
                size = 20,
            )
        }
    }
}
