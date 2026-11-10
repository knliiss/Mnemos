package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryRouter
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import com.pengrad.telegrambot.BotUtils
import com.pengrad.telegrambot.UpdatesListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class WidgetAwareTelegramPollingConsumer(
    private val telegram: TelegramBotProperties,
    private val widgets: TelegramWidgetProperties,
    private val legacy: TelegramLongPollingConsumer,
    private val queries: TelegramWidgetQueryRouter,
) : ApplicationRunner,
    DisposableBean {
    private val active = AtomicBoolean(false)
    private var worker: Thread? = null

    private val http =
        RestClient
            .builder()
            .baseUrl("https://api.telegram.org/bot" + telegram.token)
            .requestFactory(
                JdkClientHttpRequestFactory().apply {
                    setReadTimeout(Duration.ofSeconds(40))
                },
            ).build()

    override fun run(args: ApplicationArguments) {
        if (!widgets.enabled) return

        active.set(true)
        worker =
            Thread(::poll, "mnemos-widget-updates").apply {
                isDaemon = true
                start()
            }
    }

    override fun destroy() {
        active.set(false)
        worker?.interrupt()
    }

    private fun poll() {
        var offset = 0

        while (active.get() && !Thread.currentThread().isInterrupted) {
            try {
                val response =
                    http
                        .post()
                        .uri("/getUpdates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            mapOf(
                                "offset" to offset,
                                "timeout" to 25,
                                "limit" to 100,
                                "allowed_updates" to emptyList<String>(),
                            ),
                        ).retrieve()
                        .body(JsonNode::class.java) ?: error("Empty getUpdates response")

                check(response.path("ok").asBoolean(false)) { "Telegram rejected getUpdates" }

                val updates = response.path("result")
                check(updates.isArray) { "Telegram returned an invalid update list" }

                for (update in updates) {
                    val updateId = update.path("update_id").asInt(0)
                    if (updateId <= 0) continue

                    val delivered = process(update, updateId)
                    if (!delivered) break
                    offset = updateId + 1
                }
            } catch (exception: Exception) {
                if (!active.get() || Thread.currentThread().isInterrupted) break
                log.warn("Widget-aware Telegram polling failed: {}", exception.javaClass.simpleName)
                pause()
            }
        }
    }

    internal fun process(
        update: JsonNode,
        updateId: Int,
    ): Boolean {
        val result =
            try {
                val widget = update.path("widget_query")
                if (!widget.isMissingNode && !widget.isNull) {
                    val context = TelegramWidgetQueryDecoder.parse(widget)
                    if (context == null) {
                        TelegramDeliveryResult.Delivered
                    } else {
                        queries.route(context)
                    }
                } else {
                    val legacyUpdate = BotUtils.parseUpdate(update.toString())
                    val confirmed = legacy.processUpdates(listOf(legacyUpdate))
                    if (confirmed == UpdatesListener.CONFIRMED_UPDATES_NONE) {
                        TelegramDeliveryResult.Retryable
                    } else {
                        TelegramDeliveryResult.Delivered
                    }
                }
            } catch (exception: Exception) {
                log.warn(
                    "Telegram update failed: updateId={}, errorType={}",
                    updateId,
                    exception.javaClass.simpleName,
                )
                TelegramDeliveryResult.Retryable
            }

        if (result == TelegramDeliveryResult.Retryable) {
            pause()
            return false
        }
        return true
    }

    private fun pause() {
        try {
            Thread.sleep(500)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(WidgetAwareTelegramPollingConsumer::class.java)
    }
}
