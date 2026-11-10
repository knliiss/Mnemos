package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.management.AccountManagementWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class AccountManagementWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: AccountManagementWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "name" to view.nickname,
                "page" to view.page,
                "pages" to view.pages,
                "membersCount" to view.totalMembers,
                "rolesCount" to view.totalRoles,
                "members" to view.members.map {
                    mapOf("name" to it.name, "role" to it.role, "owner" to it.owner)
                },
                "roles" to view.roles.map {
                    mapOf("name" to it.name, "rank" to it.rank, "permissions" to it.permissionCount)
                },
            ),
        )

    fun render(view: AccountManagementWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif;color:var(--tg-theme-text-color)}
              .title{font-size:17px;font-weight:800}
              .hint{font-size:11px;color:var(--tg-theme-hint-color)}
              .tabs,.actions{display:flex;gap:6px;margin:10px 0}
              button{border:0;border-radius:9px;padding:9px;font-weight:700;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              button.active,button.primary{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              button:disabled{opacity:.45}
              .tabs button,.actions button{flex:1}
              .item{padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .name{font-size:13px;font-weight:700;overflow-wrap:anywhere}
              .detail{font-size:11px;color:var(--tg-theme-hint-color);margin-top:3px}
              #feedback{min-height:16px;margin-top:8px}
            </style>
            <div class="title">УЧАСНИКИ ТА РОЛІ</div>
            <div class="hint" id="account"></div>
            <div class="tabs"><button id="membersTab" class="active">Учасники</button><button id="rolesTab">Ролі</button></div>
            <div id="entries"></div>
            <div class="actions"><button id="prev">Назад</button><button id="next">Далі</button><button id="refresh" class="primary">Оновити</button></div>
            <div class="hint" id="feedback" aria-live="polite"></div>
            <script>
            (function(){
              var state={},tab='members',busy=false;
              function draw(s){
                if(!s)return;state=s;
                document.getElementById('account').textContent=s.name+' · '+s.membersCount+' учасників · '+s.rolesCount+' ролей · '+(s.page+1)+'/'+Math.max(1,s.pages);
                document.getElementById('prev').style.display=s.page>0?'block':'none';
                document.getElementById('next').style.display=s.page+1<s.pages?'block':'none';
                document.getElementById('membersTab').className=tab==='members'?'active':'';
                document.getElementById('rolesTab').className=tab==='roles'?'active':'';
                var root=document.getElementById('entries');
                while(root.firstChild)root.removeChild(root.firstChild);
                var entries=s[tab]||[];
                if(!entries.length){
                  var empty=document.createElement('div');empty.className='item hint';empty.textContent='Немає записів на цій сторінці';root.appendChild(empty);
                }
                entries.forEach(function(entry){
                  var item=document.createElement('div');item.className='item';
                  var name=document.createElement('div');name.className='name';name.textContent=entry.name;
                  var detail=document.createElement('div');detail.className='detail';
                  detail.textContent=tab==='members'?((entry.owner?'Власник':'Учасник')+' · '+(entry.role||'Без ролі')):
                    ('Позиція '+entry.rank+' · Дозволів: '+entry.permissions);
                  item.appendChild(name);item.appendChild(detail);root.appendChild(item);
                });
              }
              function hydrate(){if(!window.TelegramWidget)return;try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}}
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var feedback=document.getElementById('feedback');feedback.textContent='Завантаження…';
                TelegramWidget.request(action).then(function(answer){
                  try{var result=JSON.parse(answer.result||'{}');if(result.state)draw(result.state);feedback.textContent=result.message||'Готово';}
                  catch(e){feedback.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;feedback.textContent='Не вдалося завантажити';});
              }
              document.getElementById('membersTab').addEventListener('click',function(){tab='members';draw(state);});
              document.getElementById('rolesTab').addEventListener('click',function(){tab='roles';draw(state);});
              document.getElementById('prev').addEventListener('click',function(){ask('w1:manage:prev:'+state.page);});
              document.getElementById('next').addEventListener('click',function(){ask('w1:manage:next:'+state.page);});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:manage:refresh');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()
        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · учасники та ролі",
            chatData = state(view),
            allowQueries = true,
            actionRows = listOf(
                listOf(TelegramWidgetButton("Розширене керування", "w:open:account-manage")),
            ),
        )
    }
}
