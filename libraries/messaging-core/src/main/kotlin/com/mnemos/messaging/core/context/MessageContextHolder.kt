package com.mnemos.messaging.core.context

object MessageContextHolder {

    private val holder = ThreadLocal<MessageContext>()

    fun current(): MessageContext? = holder.get()

    fun requireCurrent(): MessageContext = requireNotNull(current()) {
        "No message context is currently active"
    }

    fun <T> withContext(context: MessageContext, block: () -> T): T {
        val previous = holder.get()
        holder.set(context)

        return try {
            block()
        } finally {
            if (previous == null) {
                holder.remove()
            } else {
                holder.set(previous)
            }
        }
    }
}