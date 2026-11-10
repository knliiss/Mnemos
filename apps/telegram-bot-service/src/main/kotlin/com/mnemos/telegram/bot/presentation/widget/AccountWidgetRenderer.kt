package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class AccountWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: AccountWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "active" to view.activeAccountId?.toString(),
                "count" to view.totalAccounts,
                "page" to view.page,
                "pages" to view.totalPages,
                "coin" to view.coin,
                "prime" to view.prime,
                "feedback" to view.feedback,
                "accounts" to
                    view.accounts.map { account ->
                        mapOf(
                            "id" to account.accountId.toString(),
                            "name" to account.nickname,
                            "owner" to account.owner,
                            "members" to account.memberCount,
                            "max" to account.maxMembers,
                            "active" to account.active,
                            "enabled" to account.enabled,
                        )
                    },
            ),
        )

    fun render(view: AccountWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:12px;font-family:system-ui,sans-serif}
              .top{display:flex;align-items:center;justify-content:space-between;gap:8px}
              .title{font-size:17px;font-weight:800;letter-spacing:.5px}
              .hint{color:var(--tg-theme-hint-color);font-size:11px}
              .balance{display:flex;gap:8px;margin:12px 0}
              .tile{flex:1;border-radius:12px;background:var(--tg-theme-secondary-bg-color);padding:10px}
              .num{font-size:20px;font-weight:750}
              .account{display:flex;align-items:center;gap:8px;padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .details{flex:1;min-width:0}
              .name{font-weight:700;font-size:13px;overflow-wrap:anywhere}
              button{border:0;border-radius:9px;padding:9px;background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color);font-weight:700}
              button.secondary{background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              .actions{display:flex;gap:7px;margin-top:10px}
              .actions button{flex:1}
              #status{font-size:12px;color:var(--tg-theme-hint-color);min-height:16px;margin-top:8px}
            </style>
            <div class="top"><span class="title">МОЇ АКАУНТИ</span><span class="hint" id="count"></span></div>
            <div class="balance">
              <div class="tile"><div class="num" id="coin">—</div><div class="hint">COIN</div></div>
              <div class="tile"><div class="num" id="prime">—</div><div class="hint">PRIME</div></div>
            </div>
            <div id="accounts"></div>
            <div class="actions">
              <button id="prev" class="secondary">Назад</button>
              <button id="next" class="secondary">Далі</button>
              <button id="refresh">Оновити</button>
            </div>
            <div class="actions"><button id="create">Створити</button><button id="join" class="secondary">Приєднатися</button><button id="invites" class="secondary">Запрошення</button></div>
            <div id="status" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function draw(s){
                if(!s)return;
                state=s;
                if(s.feedback)document.getElementById('status').textContent=s.feedback;
                document.getElementById('count').textContent=String(s.count)+' · '+String(s.page+1)+'/'+String(Math.max(1,s.pages));
                document.getElementById('coin').textContent=s.coin==null?'—':String(s.coin);
                document.getElementById('prime').textContent=s.prime==null?'—':String(s.prime);
                document.getElementById('prev').style.display=s.page>0?'block':'none';
                document.getElementById('next').style.display=s.page+1<s.pages?'block':'none';
                var root=document.getElementById('accounts');
                while(root.firstChild)root.removeChild(root.firstChild);
                if(!s.accounts||!s.accounts.length){
                  var empty=document.createElement('div');empty.className='account hint';
                  empty.textContent='Створіть акаунт або приєднайтеся до команди';
                  root.appendChild(empty);
                  return;
                }
                s.accounts.forEach(function(a){
                  var row=document.createElement('div');row.className='account';
                  var details=document.createElement('div');details.className='details';
                  var name=document.createElement('div');name.className='name';name.textContent=a.name;
                  var info=document.createElement('div');info.className='hint';
                  info.textContent=String(a.members)+'/'+String(a.max)+' учасників'+(a.owner?' · Власник':'')+(a.enabled?'':' · Деактивовано');
                  details.appendChild(name);details.appendChild(info);row.appendChild(details);
                  var button=document.createElement('button');
                  button.className=a.active?'secondary':'';
                  button.textContent=a.active?'Активний':(a.enabled?'Вибрати':'Недоступний');
                  button.disabled=a.active||!a.enabled;
                  button.addEventListener('click',function(){ask('w1:acct:select:'+a.id);});
                  row.appendChild(button);root.appendChild(row);
                });
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
                  }catch(e){status.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;status.textContent='Не вдалося виконати дію';});
              }
              document.getElementById('prev').addEventListener('click',function(){ask('w1:acct:prev:'+state.page);});
              document.getElementById('next').addEventListener('click',function(){ask('w1:acct:next:'+state.page);});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:acct:refresh');});
              document.getElementById('create').addEventListener('click',function(){ask('w1:acct:create');});
              document.getElementById('join').addEventListener('click',function(){ask('w1:acct:join');});
              document.getElementById('invites').addEventListener('click',function(){ask('w1:acct:invites');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · акаунти",
            chatData = state(view),
            allowQueries = true,
            actionRows =
                listOf(
                    listOf(
                        TelegramWidgetButton("Розширені дії", "w:open:account"),
                        TelegramWidgetButton("Керування", "w:open:account-manage"),
                    ),
                ),
        )
    }
}
