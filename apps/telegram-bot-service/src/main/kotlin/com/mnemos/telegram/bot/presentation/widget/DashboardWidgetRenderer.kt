package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class DashboardWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: DashboardWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "name" to view.accountName,
                "accounts" to view.accountCount,
                "coin" to view.coin,
                "prime" to view.prime,
                "streak" to view.dailyStreak,
                "claimed" to view.dailyClaimed,
            ),
        )

    fun render(view: DashboardWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:14px;font-family:system-ui,sans-serif}
              .head{display:flex;justify-content:space-between;align-items:center}
              .brand{font-weight:800;font-size:18px;letter-spacing:1px}
              .tag{padding:5px 9px;border-radius:20px;background:var(--tg-theme-secondary-bg-color);font-size:11px}
              .account{font-size:17px;font-weight:700;margin-top:16px;overflow-wrap:anywhere}
              .hint{font-size:12px;color:var(--tg-theme-hint-color);margin-top:5px}
              .stats{display:flex;gap:8px;margin:15px 0}
              .stat{flex:1;background:var(--tg-theme-secondary-bg-color);border-radius:12px;padding:10px}
              .num{font-weight:750;font-size:20px}
              .label{font-size:11px;color:var(--tg-theme-hint-color)}
              .daily{border:1px solid var(--tg-theme-section-separator-color);border-radius:12px;padding:12px}
              .daily-title{font-weight:700;font-size:13px}
              .actions{display:flex;gap:8px;margin-top:12px}
              button{flex:1;border:0;border-radius:10px;padding:10px;background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color);font-weight:700}
              button.secondary{background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              #feedback{margin-top:7px;font-size:12px;color:var(--tg-theme-hint-color)}
            </style>
            <div class="head"><span class="brand">MNEMOS</span><span class="tag">SAO</span></div>
            <div class="account" id="account"></div><div class="hint" id="accountHint"></div>
            <div class="stats">
              <div class="stat"><div class="num" id="coin">—</div><div class="label">COIN</div></div>
              <div class="stat"><div class="num" id="prime">—</div><div class="label">PRIME</div></div>
            </div>
            <div class="daily"><div class="daily-title" id="daily"></div><div class="hint" id="dailyHint"></div></div>
            <div class="actions">
              <button id="claim">Отримати</button>
              <button id="refresh" class="secondary">Оновити</button>
            </div>
            <div class="actions"><button id="subscriptions" class="secondary">Мої підписки</button><button id="events" class="secondary">Події</button></div>
            <div class="actions"><button id="accounts" class="secondary">Акаунти</button><button id="daily" class="secondary">Нагороди</button></div>
            <div class="actions"><button id="bets" class="secondary">Ставки</button><button id="wallet" class="secondary">Гаманець</button></div>
            <div id="feedback" aria-live="polite"></div>
            <script>
            (function(){
              var state={};
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('account').textContent=s.name||'Акаунт не вибрано';
                document.getElementById('accountHint').textContent=s.name?'Доступних акаунтів: '+s.accounts:'Створіть акаунт або приєднайтеся до команди';
                document.getElementById('coin').textContent=s.coin==null?'—':String(s.coin);
                document.getElementById('prime').textContent=s.prime==null?'—':String(s.prime);
                document.getElementById('daily').textContent='Щоденна нагорода · серія '+(s.streak==null?'—':s.streak);
                document.getElementById('dailyHint').textContent=s.claimed===true?'Отримано сьогодні':s.claimed===false?'Нагорода доступна':'Відкрийте меню нагород';
                document.getElementById('claim').style.display=s.name&&s.claimed===false?'block':'none';
              }
              function hydrate(){
                if(!window.TelegramWidget)return;
                try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}
              }
              function request(code){
                if(!window.TelegramWidget)return;
                var feedback=document.getElementById('feedback');
                feedback.textContent='Виконання…';
                TelegramWidget.request(code).then(function(answer){
                  try{
                    var result=JSON.parse(answer.result||'{}');
                    if(result.state)draw(result.state);
                    feedback.textContent=result.message||'Готово';
                  }catch(e){feedback.textContent='Оновіть картку';}
                },function(){feedback.textContent='Не вдалося виконати дію';});
              }
              document.getElementById('refresh').addEventListener('click',function(){request('w1:refresh');});
              document.getElementById('claim').addEventListener('click',function(){request('w1:claim');});
              document.getElementById('subscriptions').addEventListener('click',function(){request('w1:sub:open');});
              document.getElementById('events').addEventListener('click',function(){request('w1:evt:open');});
              document.getElementById('accounts').addEventListener('click',function(){request('w1:acct:open');});
              document.getElementById('daily').addEventListener('click',function(){request('w1:daily:open');});
              document.getElementById('bets').addEventListener('click',function(){request('w1:bet:open');});
              document.getElementById('wallet').addEventListener('click',function(){request('w1:wallet:open');});
              if(window.TelegramWidget){
                TelegramWidget.onEvent('chatDataChanged',hydrate);
                hydrate();
                TelegramWidget.ready();
              }
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · головна панель",
            chatData = state(view),
            allowQueries = true,
            actionRows =
                listOf(
                    listOf(
                        TelegramWidgetButton("Події", "w:open:events"),
                        TelegramWidgetButton("Підписки", "w:open:notifications"),
                    ),
                    listOf(
                        TelegramWidgetButton("Акаунт", "w:open:account"),
                        TelegramWidgetButton("Нагороди", "w:open:daily"),
                    ),
                ),
        )
    }
}
