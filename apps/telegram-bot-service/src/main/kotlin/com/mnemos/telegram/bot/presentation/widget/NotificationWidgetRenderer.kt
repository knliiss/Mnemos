package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.notifications.NotificationWidgetView
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class NotificationWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: NotificationWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "ping" to view.pingEnabled,
                "page" to view.page,
                "pages" to view.totalPages,
                "total" to view.totalDrops,
                "boosters" to view.boosters,
                "dungeons" to view.dungeons,
                "globals" to view.globalEvents,
                "raid" to view.raidEnabled,
                "items" to view.itemDrops.map { entry -> mapOf("id" to entry.id.toString(), "label" to entry.label) },
            ),
        )

    fun render(view: NotificationWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif}
              .head{display:flex;justify-content:space-between;align-items:center}
              .brand{font-weight:800;font-size:17px;letter-spacing:.5px}
              .small{color:var(--tg-theme-hint-color);font-size:12px}
              .stats{display:flex;gap:7px;margin:13px 0}
              .tile{flex:1;min-width:0;padding:9px;border-radius:11px;background:var(--tg-theme-secondary-bg-color)}
              .num{font-size:19px;font-weight:750}
              .row{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .title{font-weight:700;overflow-wrap:anywhere;font-size:13px;flex:1}
              button{border:0;border-radius:9px;background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color);padding:9px;font-weight:700}
              button.secondary{background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              .actions{display:flex;gap:8px;margin-top:12px}
              .actions button{flex:1}
              #status{min-height:16px;margin-top:9px;color:var(--tg-theme-hint-color);font-size:12px}
            </style>
            <div class="head"><div class="brand">СПОВІЩЕННЯ</div><span class="small" id="pager">1/1</span></div>
            <div class="stats">
              <div class="tile"><div class="num" id="drops">0</div><div class="small">Дропи</div></div>
              <div class="tile"><div class="num" id="boosts">0</div><div class="small">Бустери</div></div>
              <div class="tile"><div class="num" id="globals">0</div><div class="small">Глобальні</div></div>
            </div>
            <div class="row"><span class="title" id="pingLabel"></span><button id="ping" class="secondary">Змінити</button></div>
            <div id="items"></div>
            <div class="actions">
              <button id="previous" class="secondary">Назад</button>
              <button id="next" class="secondary">Далі</button>
              <button id="refresh">Оновити</button>
            </div>
            <div id="status" aria-live="polite"></div>
            <script>
            (function(){
              var state={};
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('pager').textContent=String(s.page+1)+'/'+String(Math.max(1,s.pages));
                document.getElementById('drops').textContent=String(s.total);
                document.getElementById('boosts').textContent=String(s.boosters);
                document.getElementById('globals').textContent=String(s.globals);
                document.getElementById('pingLabel').textContent=s.ping?'Ping-сповіщення ввімкнені':'Ping-сповіщення вимкнені';
                document.getElementById('previous').style.display=s.page>0?'block':'none';
                document.getElementById('next').style.display=s.page+1<s.pages?'block':'none';
                var root=document.getElementById('items');
                while(root.firstChild)root.removeChild(root.firstChild);
                if(!s.items||s.items.length===0){
                  var empty=document.createElement('div');empty.className='row small';
                  empty.textContent='На цій сторінці немає правил дропів';
                  root.appendChild(empty);
                }else{
                  s.items.forEach(function(item){
                    var row=document.createElement('div');row.className='row';
                    var label=document.createElement('span');label.className='title';label.textContent=item.label;
                    var button=document.createElement('button');button.className='secondary';button.textContent='Видалити';
                    button.addEventListener('click',function(){act('w1:del:'+item.id);});
                    row.appendChild(label);row.appendChild(button);root.appendChild(row);
                  });
                }
              }
              function hydrate(){
                if(!window.TelegramWidget)return;
                try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}
              }
              function act(action){
                if(!window.TelegramWidget)return;
                var status=document.getElementById('status');status.textContent='Виконання…';
                TelegramWidget.request(action).then(function(answer){
                  try{var result=JSON.parse(answer.result||'{}');if(result.state)draw(result.state);
                    status.textContent=result.message||'Готово';
                  }catch(e){status.textContent='Оновіть віджет';}
                },function(){status.textContent='Не вдалося виконати дію';});
              }
              document.getElementById('ping').addEventListener('click',function(){act(state.ping?'w1:ping:off':'w1:ping:on');});
              document.getElementById('refresh').addEventListener('click',function(){act('w1:sub:refresh');});
              document.getElementById('previous').addEventListener('click',function(){act('w1:prev:'+state.page);});
              document.getElementById('next').addEventListener('click',function(){act('w1:next:'+state.page);});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · керування сповіщеннями",
            chatData = state(view),
            allowQueries = true,
            actionRows =
                listOf(
                    listOf(TelegramWidgetButton("Додати правило", "w:open:notifications")),
                    listOf(TelegramWidgetButton("Головна", "w:open:account")),
                ),
        )
    }
}
