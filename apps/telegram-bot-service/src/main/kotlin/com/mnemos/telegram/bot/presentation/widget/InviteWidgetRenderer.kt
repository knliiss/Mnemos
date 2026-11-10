package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteStatus
import com.mnemos.telegram.bot.application.widget.invites.InviteWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class InviteWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: InviteWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "account" to view.accountName,
                "count" to view.totalInvites,
                "page" to view.page,
                "pages" to view.totalPages,
                "confirm" to view.pendingRevokeId?.toString(),
                "invites" to view.invites.map {
                    mapOf(
                        "id" to it.id.toString(),
                        "code" to it.code,
                        "status" to it.status.name,
                        "expires" to it.expiresAt?.toString()?.take(16),
                    )
                },
            ),
        )

    fun render(view: InviteWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif;color:var(--tg-theme-text-color)}
              .head{font-weight:800;font-size:18px}
              .hint{font-size:11px;color:var(--tg-theme-hint-color)}
              .section{font-weight:750;font-size:13px;margin:14px 0 7px}
              .invite{padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .line{display:flex;gap:7px;align-items:center;justify-content:space-between}
              .code{font-size:13px;font-weight:700;overflow-wrap:anywhere;user-select:text}
              .actions{display:flex;flex-wrap:wrap;gap:6px;margin:9px 0}
              button{border:0;border-radius:8px;padding:8px 9px;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color);font-size:12px;font-weight:700}
              button.primary{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              button:disabled{opacity:.45}
              #feedback{min-height:16px;padding-top:6px}
            </style>
            <div class="head">ЗАПРОШЕННЯ</div>
            <div class="hint" id="account"></div>
            <div class="section">Створити запрошення</div>
            <div class="actions">
              <button data-expiry="ONE_HOUR">1 год</button>
              <button data-expiry="ONE_DAY" class="primary">1 день</button>
              <button data-expiry="SEVEN_DAYS">7 днів</button>
              <button data-expiry="NONE">Без терміну</button>
            </div>
            <div class="section">Мої запрошення</div>
            <div id="invites"></div>
            <div class="actions">
              <button id="prev">Назад</button>
              <button id="next">Далі</button>
              <button id="refresh" class="primary">Оновити</button>
            </div>
            <div id="feedback" class="hint" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('account').textContent=s.account+' · '+s.count+' запрошень · '+(s.page+1)+'/'+Math.max(1,s.pages);
                document.getElementById('prev').style.display=s.page>0?'block':'none';
                document.getElementById('next').style.display=s.page+1<s.pages?'block':'none';
                var list=document.getElementById('invites');
                while(list.firstChild)list.removeChild(list.firstChild);
                if(!s.invites||!s.invites.length){
                  var empty=document.createElement('div');empty.className='hint';empty.textContent='Запрошень поки немає';list.appendChild(empty);
                }else s.invites.forEach(function(inv){
                  var row=document.createElement('div');row.className='invite';
                  var line=document.createElement('div');line.className='line';
                  var code=document.createElement('span');code.className='code';code.textContent=inv.code;
                  var label=document.createElement('span');label.className='hint';
                  label.textContent=inv.status==='ACTIVE'?'Активне':(inv.status==='CONSUMED'?'Використане':(inv.status==='REVOKED'?'Відкликане':'Прострочене'));
                  line.appendChild(code);line.appendChild(label);row.appendChild(line);
                  var expiry=document.createElement('div');expiry.className='hint';
                  expiry.textContent=inv.expires?'Діє до '+inv.expires+' UTC':'Без обмеження терміну';
                  row.appendChild(expiry);
                  if(inv.status==='ACTIVE'){
                    var buttons=document.createElement('div');buttons.className='actions';
                    var b=document.createElement('button');b.textContent=s.confirm===inv.id?'Підтвердити відкликання':'Відкликати';
                    b.className=s.confirm===inv.id?'primary':'';
                    b.addEventListener('click',function(){ask((s.confirm===inv.id?'w1:inv:revoke:':'w1:inv:confirm:')+inv.id);});
                    buttons.appendChild(b);
                    if(s.confirm===inv.id){
                      var cancel=document.createElement('button');cancel.textContent='Скасувати';
                      cancel.addEventListener('click',function(){ask('w1:inv:cancel');});
                      buttons.appendChild(cancel);
                    }
                    row.appendChild(buttons);
                  }
                  list.appendChild(row);
                });
              }
              function hydrate(){if(!window.TelegramWidget)return;try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}}
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var label=document.getElementById('feedback');label.textContent='Виконання…';
                TelegramWidget.request(action).then(function(answer){
                  try{var result=JSON.parse(answer.result||'{}');if(result.state)draw(result.state);
                    label.textContent=result.message||'Готово';
                  }catch(e){label.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;label.textContent='Не вдалося виконати дію';});
              }
              document.querySelectorAll('[data-expiry]').forEach(function(b){
                b.addEventListener('click',function(){ask('w1:inv:create:'+this.getAttribute('data-expiry'));});
              });
              document.getElementById('prev').addEventListener('click',function(){ask('w1:inv:prev:'+state.page);});
              document.getElementById('next').addEventListener('click',function(){ask('w1:inv:next:'+state.page);});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:inv:refresh');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · запрошення",
            chatData = state(view),
            allowQueries = true,
            actionRows = listOf(
                listOf(TelegramWidgetButton("Текстове меню запрошень", "w:open:account-invites")),
            ),
        )
    }
}
