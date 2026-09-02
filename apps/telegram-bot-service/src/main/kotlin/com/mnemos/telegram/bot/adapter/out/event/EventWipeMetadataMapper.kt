package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalWipeListResponse
import com.mnemos.event.contracts.internal.v1.InternalWipeResponse
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeOption
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeOptions

object EventWipeMetadataMapper {
    fun InternalWipeListResponse.toOptions(): EventHistoryWipeOptions {
        val options = wipes.map { wipe -> wipe.toOption() }
        val current = options.firstOrNull { option -> option.active }
        val previous =
            current?.let { currentOption ->
                options
                    .filter { option -> option.number < currentOption.number }
                    .maxByOrNull { option -> option.number }
            }

        return EventHistoryWipeOptions(
            current = current,
            previous = previous,
        )
    }

    private fun InternalWipeResponse.toOption(): EventHistoryWipeOption =
        EventHistoryWipeOption(
            id = id,
            number = number,
            name = name,
            active = active,
        )
}
