package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.bets.BetWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class BetWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: BetWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "mode" to view.mode.name,
                "hasAccount" to (view.accountId != null),
                "total" to view.totalItems,
                "entries" to
                    view.entries.map { entry ->
                        mapOf(
                            "title" to entry.title,
                            "details" to entry.details,
                            "status" to entry.status,
                        )
                    },
            ),
        )

    fun render(view: BetWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif}
              .header{display:flex;align-items:center;justify-content:space-between;margin-bottom:11px}
              .title{font-size:18px;font-weight:800}
              .hint{font-size:11px;color:var(--tg-theme-hint-color)}
              .tabs{display:flex;gap:6px;margin-bottom:11px}
              button{border:0;border-radius:9px;padding:9px;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color);font-weight:700}
              button.active,#refresh{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              .tabs button{flex:1}
              .entry{padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .entryTitle{font-size:13px;font-weight:750;overflow-wrap:anywhere}
              .entryMeta{font-size:11px;color:var(--tg-theme-hint-color);margin-top:4px;overflow-wrap:anywhere}
              .actions{display:flex;gap:8px;margin-top:12px}
              .actions button{flex:1}
              #status{margin-top:8px;min-height:16px;color:var(--tg-theme-hint-color);font-size:11px}
            </style>
            <div class="header"><div class="title">СТАВКИ</div><span id="count" class="hint"></span></div>
            <div class="tabs">
              <button data-mode="ROUNDS">Раунди</button>
              <button data-mode="RACES">Перегони</button>
              <button data-mode="HISTORY">Історія</button>
            </div>
            <div id="list"></div>
            <div class="actions"><button id="refresh">Оновити</button></div>
            <div id="status" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('count').textContent='Показано '+String((s.entries||[]).length)+' із '+String(s.total);
                var tabs=document.querySelectorAll('[data-mode]');
                for(var i=0;i<tabs.length;i++)tabs[i].className=tabs[i].getAttribute('data-mode')===s.mode?'active':'';
                var root=document.getElementById('list');
                while(root.firstChild)root.removeChild(root.firstChild);
                if(!s.hasAccount){
                  var message=document.createElement('div');message.className='entry hint';
                  message.textContent='Спочатку виберіть активний акаунт';
                  root.appendChild(message);
                  return;
                }
                if(!s.entries||s.entries.length===0){
                  var empty=document.createElement('div');empty.className='entry hint';
                  empty.textContent='Записів немає';
                  root.appendChild(empty);
                }else{
                  s.entries.forEach(function(entry){
                    var row=document.createElement('div');row.className='entry';
                    var title=document.createElement('div');title.className='entryTitle';title.textContent=entry.title;
                    var details=document.createElement('div');details.className='entryMeta';details.textContent=entry.details;
                    var status=document.createElement('div');status.className='entryMeta';status.textContent=entry.status;
                    row.appendChild(title);row.appendChild(details);row.appendChild(status);root.appendChild(row);
                  });
                }
              }
              function hydrate(){
                if(!window.TelegramWidget)return;
                try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}
              }
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var status=document.getElementById('status');status.textContent='Завантаження…';
                TelegramWidget.request(action).then(function(answer){
                  try{
                    var result=JSON.parse(answer.result||'{}');
                    if(result.state)draw(result.state);
                    status.textContent=result.message||'Готово';
                  }catch(e){status.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;status.textContent='Не вдалося завантажити дані';});
              }
              var tabs=document.querySelectorAll('[data-mode]');
              for(var i=0;i<tabs.length;i++){
                tabs[i].addEventListener('click',function(){ask('w1:bet:mode:'+this.getAttribute('data-mode'));});
              }
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:bet:refresh');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · ставки та прогнози",
            chatData = state(view),
            allowQueries = true,
            actionRows =
                listOf(
                    listOf(
                        TelegramWidgetButton("Зробити ставку", "w:open:bets"),
                        TelegramWidgetButton("Акаунти", "w:open:account"),
                    ),
                ),
        )
    }
}
