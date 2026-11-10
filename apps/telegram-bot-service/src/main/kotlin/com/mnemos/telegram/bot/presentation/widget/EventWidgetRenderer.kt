package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.events.EventWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class EventWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: EventWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "filter" to view.filter.name,
                "search" to view.search,
                "cursor" to view.nextCursor?.toString(),
                "items" to view.items.map { item -> mapOf("type" to item.type, "label" to item.label, "time" to item.time) },
            ),
        )

    fun render(view: EventWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif}
              .heading{font-weight:800;font-size:18px;margin-bottom:9px}
              .tabs{display:flex;flex-wrap:wrap;gap:5px;margin-bottom:10px}
              .tabs button{padding:7px 10px;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              button{border:0;border-radius:9px;padding:9px 11px;background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color);font-weight:700}
              button.active{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              .event{display:flex;align-items:center;gap:8px;padding:8px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .title{flex:1;min-width:0;overflow-wrap:anywhere;font-size:12px;font-weight:600}
              .kind{font-size:11px;color:var(--tg-theme-hint-color)}
              .time{font-size:10px;color:var(--tg-theme-hint-color)}
              .actions{display:flex;gap:7px;margin-top:11px}
              .actions button{flex:1}
              #status{font-size:12px;color:var(--tg-theme-hint-color);margin-top:8px;min-height:15px}
            </style>
            <div class="heading">ПОДІЇ SAO</div>
            <div class="tabs" id="filters">
              <button data-filter="ALL">Усі</button>
              <button data-filter="ITEM_DROP">Дропи</button>
              <button data-filter="BOOSTER">Бустери</button>
              <button data-filter="GLOBAL">Глобальні</button>
              <button data-filter="RAID">Рейди</button>
            </div>
            <div class="kind" id="searchLabel"></div>
            <div id="events"></div>
            <div class="actions"><button id="search">Пошук</button><button id="clear">Скинути</button><button id="next">Наступні</button><button id="refresh">Оновити</button></div>
            <div id="status" aria-live="polite"></div>
            <script>
            (function(){
              var state={};
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('searchLabel').textContent=s.search?'Пошук: '+s.search:'';
                document.getElementById('clear').style.display=s.search?'block':'none';
                var root=document.getElementById('events');
                while(root.firstChild)root.removeChild(root.firstChild);
                if(!s.items||s.items.length===0){
                  var empty=document.createElement('div');empty.className='event kind';empty.textContent='Подій немає';
                  root.appendChild(empty);
                }else{
                  s.items.forEach(function(item){
                    var row=document.createElement('div');row.className='event';
                    var txt=document.createElement('div');txt.className='title';
                    var kind=document.createElement('div');kind.className='kind';kind.textContent=item.type;
                    var label=document.createElement('div');label.textContent=item.label;
                    txt.appendChild(kind);txt.appendChild(label);
                    var time=document.createElement('span');time.className='time';time.textContent=item.time;
                    row.appendChild(txt);row.appendChild(time);root.appendChild(row);
                  });
                }
                document.getElementById('next').style.display=s.cursor?'block':'none';
                var buttons=document.getElementById('filters').querySelectorAll('button');
                for(var i=0;i<buttons.length;i++){
                  buttons[i].className=buttons[i].getAttribute('data-filter')===s.filter?'active':'';
                }
              }
              function hydrate(){
                if(!window.TelegramWidget)return;
                try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}
              }
              function ask(action){
                if(!window.TelegramWidget)return;
                var status=document.getElementById('status');status.textContent='Завантаження…';
                TelegramWidget.request(action).then(function(answer){
                  try{var result=JSON.parse(answer.result||'{}');
                    if(result.state)draw(result.state);
                    status.textContent=result.message||'Готово';
                  }catch(e){status.textContent='Спробуйте оновити';}
                },function(){status.textContent='Не вдалося виконати дію';});
              }
              var buttons=document.getElementById('filters').querySelectorAll('button');
              for(var i=0;i<buttons.length;i++){
                buttons[i].addEventListener('click',function(){ask('w1:evt:filter:'+this.getAttribute('data-filter'));});
              }
              document.getElementById('search').addEventListener('click',function(){ask('w1:evt:search');});
              document.getElementById('clear').addEventListener('click',function(){ask('w1:evt:clear');});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:evt:refresh');});
              document.getElementById('next').addEventListener('click',function(){if(state.cursor)ask('w1:evt:next:'+state.cursor);});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · історія подій",
            chatData = state(view),
            allowQueries = true,
            actionRows =
                listOf(
                    listOf(
                        TelegramWidgetButton("Розширений пошук", "w:open:events"),
                        TelegramWidgetButton("Підписки", "w:open:notifications"),
                    ),
                ),
        )
    }
}
