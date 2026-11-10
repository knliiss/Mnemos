package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.daily.DailyWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class DailyWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: DailyWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "hasAccount" to (view.accountId != null),
                "streak" to view.streak,
                "claimed" to view.claimedToday,
                "recoverable" to view.recoverable,
                "recoverStreak" to view.recoverableStreak,
                "restores" to view.restoresRemaining,
                "min" to view.nextCoinMin,
                "max" to view.nextCoinMax,
                "chance" to view.primeChance,
                "date" to view.rewardDate,
            ),
        )

    fun render(view: DailyWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:14px;font-family:system-ui,sans-serif}
              .head{display:flex;align-items:center;justify-content:space-between}
              .title{font-size:17px;font-weight:800}
              .badge{border-radius:20px;padding:5px 9px;background:var(--tg-theme-secondary-bg-color);font-size:11px}
              .streak{font-size:35px;font-weight:800;margin-top:12px}
              .hint{font-size:12px;color:var(--tg-theme-hint-color);margin-top:4px}
              .tiles{display:flex;gap:8px;margin-top:13px}
              .tile{flex:1;min-width:0;border-radius:12px;background:var(--tg-theme-secondary-bg-color);padding:11px}
              .value{font-size:16px;font-weight:750}
              .section{margin-top:13px;padding:11px;border:1px solid var(--tg-theme-section-separator-color);border-radius:11px}
              .actions{display:flex;gap:8px;margin-top:12px}
              button{flex:1;padding:11px;border:0;border-radius:10px;background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color);font-weight:700}
              button.secondary{background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              #status{font-size:12px;margin-top:9px;min-height:17px;color:var(--tg-theme-hint-color)}
            </style>
            <div class="head"><div class="title">ЩОДЕННІ НАГОРОДИ</div><span class="badge" id="date">—</span></div>
            <div class="streak" id="streak">—</div><div class="hint">Поточна серія днів</div>
            <div class="tiles">
              <div class="tile"><div class="value" id="coins">—</div><div class="hint">Можлива нагорода COIN</div></div>
              <div class="tile"><div class="value" id="prime">—</div><div class="hint">Шанс PRIME</div></div>
            </div>
            <div class="section"><div id="claimInfo"></div><div class="hint" id="restoreInfo"></div></div>
            <div class="actions">
              <button id="claim">Отримати нагороду</button>
              <button id="restore" class="secondary">Відновити серію</button>
              <button id="refresh" class="secondary">Оновити</button>
            </div>
            <div id="status" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('date').textContent=s.date||'Немає акаунта';
                document.getElementById('streak').textContent=s.streak==null?'—':String(s.streak);
                document.getElementById('coins').textContent=s.min==null?'—':String(s.min)+' – '+String(s.max);
                document.getElementById('prime').textContent=s.chance==null?'—':String(Math.round(s.chance*1000)/10)+'%';
                document.getElementById('claimInfo').textContent=!s.hasAccount?'Спочатку виберіть акаунт':s.claimed?'Нагороду вже отримано сьогодні':'Сьогодні нагорода доступна';
                document.getElementById('restoreInfo').textContent=s.recoverable?'Можна відновити серію '+s.recoverStreak+' · залишилося: '+s.restores:'Відновлення серії зараз недоступне';
                document.getElementById('claim').style.display=s.hasAccount&&s.claimed===false?'block':'none';
                document.getElementById('restore').style.display=s.hasAccount&&s.recoverable&&s.restores>0?'block':'none';
              }
              function hydrate(){
                if(!window.TelegramWidget)return;
                try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}
              }
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var status=document.getElementById('status');status.textContent='Виконання…';
                TelegramWidget.request(action).then(function(answer){
                  try{
                    var result=JSON.parse(answer.result||'{}');
                    if(result.state)draw(result.state);
                    status.textContent=result.message||'Готово';
                  }catch(e){status.textContent='Оновіть картку';}
                  busy=false;
                },function(){busy=false;status.textContent='Не вдалося виконати дію';});
              }
              document.getElementById('claim').addEventListener('click',function(){ask('w1:daily:claim');});
              document.getElementById('restore').addEventListener('click',function(){ask('w1:daily:restore');});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:daily:refresh');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · щоденні нагороди",
            chatData = state(view),
            allowQueries = true,
            actionRows =
                listOf(
                    listOf(
                        TelegramWidgetButton("Акаунти", "w:open:account"),
                        TelegramWidgetButton("Текстове меню нагород", "w:open:daily"),
                    ),
                ),
        )
    }
}
