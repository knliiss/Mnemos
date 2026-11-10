package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.wallet.WalletWidgetView
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class WalletWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: WalletWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "account" to (view.accountId != null),
                "coin" to view.coin,
                "prime" to view.prime,
                "page" to view.page,
                "pages" to view.totalPages,
                "total" to view.totalTransactions,
                "historyStatus" to view.historyStatus.name,
                "entries" to
                    view.entries.map { entry ->
                        mapOf(
                            "label" to entry.label,
                            "amount" to entry.amount,
                            "currency" to entry.currency,
                            "date" to entry.createdAt,
                        )
                    },
            ),
        )

    fun render(view: WalletWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif}
              .heading{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
              .title{font-size:18px;font-weight:800;letter-spacing:.5px}
              .hint{font-size:11px;color:var(--tg-theme-hint-color)}
              .tiles{display:flex;gap:8px;margin-bottom:12px}
              .tile{flex:1;min-width:0;padding:12px;background:var(--tg-theme-secondary-bg-color);border-radius:12px}
              .amount{font-weight:750;font-size:21px}
              .section{font-weight:700;font-size:13px;margin-bottom:3px}
              .transaction{display:flex;align-items:center;justify-content:space-between;gap:10px;padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .details{flex:1;min-width:0}
              .label{font-weight:650;font-size:12px;overflow-wrap:anywhere}
              .value{font-weight:750;font-size:12px;white-space:nowrap}
              .controls{display:flex;gap:7px;margin-top:12px}
              button{flex:1;border:0;border-radius:9px;padding:10px 7px;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color);font-weight:700}
              #refresh{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              #feedback{font-size:11px;min-height:15px;margin-top:8px;color:var(--tg-theme-hint-color)}
            </style>
            <div class="heading"><span class="title">ГАМАНЕЦЬ</span><span id="page" class="hint"></span></div>
            <div class="tiles">
              <div class="tile"><div id="coin" class="amount">—</div><div class="hint">COIN</div></div>
              <div class="tile"><div id="prime" class="amount">—</div><div class="hint">PRIME</div></div>
            </div>
            <div class="section">Останні транзакції</div>
            <div id="transactions"></div>
            <div class="controls">
              <button id="prev">Назад</button>
              <button id="next">Далі</button>
              <button id="refresh">Оновити</button>
            </div>
            <div id="feedback" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('coin').textContent=s.coin==null?'—':String(s.coin);
                document.getElementById('prime').textContent=s.prime==null?'—':String(s.prime);
                document.getElementById('page').textContent=String(s.page+1)+'/'+String(Math.max(1,s.pages));
                document.getElementById('prev').style.display=s.historyStatus==='AVAILABLE'&&s.page>0?'block':'none';
                document.getElementById('next').style.display=s.historyStatus==='AVAILABLE'&&s.page+1<s.pages?'block':'none';
                var root=document.getElementById('transactions');
                while(root.firstChild)root.removeChild(root.firstChild);
                if(s.historyStatus!=='AVAILABLE'){
                  var fallback=document.createElement('div');fallback.className='transaction hint';
                  fallback.textContent=s.historyStatus==='FORBIDDEN'?'Немає дозволу переглядати історію':s.historyStatus==='ACCOUNT_REQUIRED'?'Виберіть активний акаунт':'Історія тимчасово недоступна';
                  root.appendChild(fallback);return;
                }
                if(!s.entries||!s.entries.length){
                  var empty=document.createElement('div');empty.className='transaction hint';
                  empty.textContent='Транзакцій на цій сторінці немає';
                  root.appendChild(empty);return;
                }
                s.entries.forEach(function(entry){
                  var row=document.createElement('div');row.className='transaction';
                  var details=document.createElement('div');details.className='details';
                  var title=document.createElement('div');title.className='label';title.textContent=entry.label;
                  var time=document.createElement('div');time.className='hint';time.textContent=entry.date;
                  var amount=document.createElement('div');amount.className='value';
                  amount.textContent=String(entry.amount)+' '+entry.currency;
                  details.appendChild(title);details.appendChild(time);
                  row.appendChild(details);row.appendChild(amount);root.appendChild(row);
                });
              }
              function hydrate(){
                if(!window.TelegramWidget)return;
                try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}
              }
              function ask(code){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var feedback=document.getElementById('feedback');feedback.textContent='Завантаження…';
                TelegramWidget.request(code).then(function(answer){
                  try{
                    var result=JSON.parse(answer.result||'{}');
                    if(result.state)draw(result.state);
                    feedback.textContent=result.message||'Готово';
                  }catch(e){feedback.textContent='Оновіть картку';}
                  busy=false;
                },function(){busy=false;feedback.textContent='Не вдалося оновити гаманець';});
              }
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:wallet:refresh');});
              document.getElementById('prev').addEventListener('click',function(){ask('w1:wallet:prev:'+state.page);});
              document.getElementById('next').addEventListener('click',function(){ask('w1:wallet:next:'+state.page);});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · баланс та історія транзакцій",
            chatData = state(view),
            allowQueries = true,
            actionRows =
                listOf(
                    listOf(
                        TelegramWidgetButton("Поповнити PRIME", "w:open:prime"),
                        TelegramWidgetButton("Акаунти", "w:open:account"),
                    ),
                ),
        )
    }
}
