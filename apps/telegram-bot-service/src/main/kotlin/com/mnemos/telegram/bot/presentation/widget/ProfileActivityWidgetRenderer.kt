package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.profileactivity.ProfileActivityView
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class ProfileActivityWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: ProfileActivityView): String =
        json.writeValueAsString(
            mapOf(
                "nickname" to view.nickname,
                "after" to view.after?.toString(),
                "next" to view.nextCursor?.toString(),
                "drops" to view.items.map {
                    mapOf("n" to it.name, "r" to it.rarity, "t" to it.occurredAt)
                },
            ),
        )

    fun render(view: ProfileActivityView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif;color:var(--tg-theme-text-color)}
              .title{font-size:18px;font-weight:800}
              .subtitle,.meta{color:var(--tg-theme-hint-color);font-size:11px}
              .subtitle{margin:7px 0 13px}
              .item{display:flex;align-items:center;gap:9px;border-bottom:1px solid var(--tg-theme-section-separator-color);padding:10px 0}
              .itemText{font-weight:700;font-size:12px;min-width:0;flex:1;overflow-wrap:anywhere}
              .date{font-size:10px;color:var(--tg-theme-hint-color)}
              .actions{display:flex;gap:7px;margin:11px 0}
              button{border:0;border-radius:8px;padding:10px 11px;font-weight:700;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              button.primary{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              #feedback{min-height:14px}
            </style>
            <div class="title">АКТИВНІСТЬ ГРАВЦЯ</div>
            <div class="subtitle" id="nickname"></div>
            <div id="events"></div>
            <div class="actions">
              <button id="first">Початок</button>
              <button id="next" class="primary">Далі</button>
              <button id="refresh">Оновити</button>
            </div>
            <div class="meta" id="feedback" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('nickname').textContent='Дропи · '+s.nickname;
                document.getElementById('first').style.display=s.after?'block':'none';
                document.getElementById('next').style.display=s.next?'block':'none';
                var root=document.getElementById('events');
                while(root.firstChild)root.removeChild(root.firstChild);
                if(!s.drops||!s.drops.length){
                  var empty=document.createElement('div');empty.className='meta';
                  empty.textContent='Дропів на цій сторінці немає';root.appendChild(empty);
                }else s.drops.forEach(function(drop){
                  var r=document.createElement('div');r.className='item';
                  var title=document.createElement('div');title.className='itemText';
                  title.textContent=drop.n;
                  var meta=document.createElement('div');meta.className='meta';
                  meta.textContent=drop.r+' · '+drop.t+' UTC';
                  r.appendChild(title);r.appendChild(meta);root.appendChild(r);
                });
              }
              function hydrate(){if(!window.TelegramWidget)return;try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}}
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;var feedback=document.getElementById('feedback');feedback.textContent='Завантаження…';
                TelegramWidget.request(action).then(function(answer){
                  try{var result=JSON.parse(answer.result||'{}');if(result.state)draw(result.state);
                    feedback.textContent=result.message||'Готово';
                  }catch(e){feedback.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;feedback.textContent='Помилка запиту';});
              }
              document.getElementById('first').addEventListener('click',function(){ask('w1:activity:first');});
              document.getElementById('next').addEventListener('click',function(){if(state.next)ask('w1:activity:next:'+state.next);});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:activity:refresh');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · активність профілю",
            chatData = state(view),
            allowQueries = true,
            actionRows = listOf(
                listOf(TelegramWidgetButton("Пошук профілів", "w:open:profiles")),
            ),
        )
    }
}
