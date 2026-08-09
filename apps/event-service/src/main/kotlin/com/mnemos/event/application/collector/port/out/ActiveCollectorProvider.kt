package com.mnemos.event.application.collector.port.out

interface ActiveCollectorProvider {
    fun activeCollectorCount(): Int
}
