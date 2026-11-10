package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.eventdetail.EventDetailView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class EventDetailWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun render(view: EventDetailView): TelegramWidgetMessage {
        val state =
            json.writeValueAsString(
                mapOf(
                    "type" to view.type,
                    "title" to view.title,
                    "time" to view.occurredAt,
                    "facts" to view.facts.map { mapOf("title" to it.title, "value" to it.value) },
                ),
            )
        val html =
            """
            <style>
              body{margin:0;padding:14px;font-family:system-ui,sans-serif;color:var(--tg-theme-text-color)}
              .heading{font-weight:800;font-size:18px;margin-bottom:8px}
              .kind{font-size:11px;letter-spacing:.05em;color:var(--tg-theme-hint-color);text-transform:uppercase}
              .title{font-size:17px;font-weight:800;margin:8px 0;overflow-wrap:anywhere}
              .time{font-size:12px;color:var(--tg-theme-hint-color);padding:8px 0}
              .fact{padding:10px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .label{font-size:11px;color:var(--tg-theme-hint-color)}
              .value{font-size:13px;font-weight:700;overflow-wrap:anywhere}
            </style>
            <div class="heading">ДЕТАЛІ ПОДІЇ</div>
            <div class="kind" id="kind"></div>
            <div class="title" id="title"></div>
            <div class="time" id="time"></div>
            <div id="facts"></div>
            <script>
            (function(){
              if(!window.TelegramWidget)return;
              try{
                var state=JSON.parse(TelegramWidget.chatData||'{}');
                document.getElementById('kind').textContent=state.type||'';
                document.getElementById('title').textContent=state.title||'';
                document.getElementById('time').textContent=state.time||'';
                var root=document.getElementById('facts');
                (state.facts||[]).forEach(function(fact){
                  var row=document.createElement('div');row.className='fact';
                  var label=document.createElement('div');label.className='label';label.textContent=fact.title;
                  var value=document.createElement('div');value.className='value';value.textContent=fact.value;
                  row.appendChild(label);row.appendChild(value);root.appendChild(row);
                });
              }catch(e){}
              TelegramWidget.ready();
            })();
            </script>
            """.trimIndent()
        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · деталі події",
            chatData = state,
            allowQueries = false,
            actionRows = listOf(
                listOf(
                    TelegramWidgetButton("Усі події", "w:open:events"),
                    TelegramWidgetButton("Підписки", "w:open:notifications"),
                ),
            ),
        )
    }
}
