package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.management.AccountManagementWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class AccountManagementWidgetRenderer(private val json: ObjectMapper) {
    fun state(view: AccountManagementWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "name" to view.nickname,
                "page" to view.page,
                "pages" to view.pages,
                "membersTotal" to view.totalMembers,
                "rolesTotal" to view.totalRoles,
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
              .head{font-size:17px;font-weight:800}
              .small{font-size:11px;color:var(--tg-theme-hint-color);margin-top:6px}
              .subhead{font-size:13px;font-weight:750;margin:12px 0 5px}
              .row{display:flex;justify-content:space-between;gap:8px;padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .name{font-size:12px;font-weight:700;min-width:0;overflow-wrap:anywhere}
              .meta{font-size:11px;color:var(--tg-theme-hint-color)}
              button{border:0;border-radius:8px;padding:9px 12px;font-weight:700;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              .primary{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              .actions{display:flex;gap:7px;margin-top:11px}
              #status{min-height:14px;margin:7px 0}
            </style>
            <div class="head">УЧАСНИКИ ТА РОЛІ</div>
            <div class="small" id="summary"></div>
            <div class="subhead">Учасники</div>
            <div id="members"></div>
            <div class="subhead">Ролі</div>
            <div id="roles"></div>
            <div class="actions">
              <button id="previous">Назад</button>
              <button id="next">Далі</button>
              <button id="refresh" class="primary">Оновити</button>
            </div>
            <div class="small" id="status" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function line(root,name,info){
                var row=document.createElement('div');row.className='row';
                var label=document.createElement('span');label.className='name';label.textContent=name;
                var meta=document.createElement('span');meta.className='meta';meta.textContent=info;
                row.appendChild(label);row.appendChild(meta);root.appendChild(row);
              }
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('summary').textContent=
                  s.name+' · '+s.membersTotal+' учасників · '+s.rolesTotal+' ролей · '+(s.page+1)+'/'+Math.max(1,s.pages);
                document.getElementById('previous').style.display=s.page>0?'block':'none';
                document.getElementById('next').style.display=s.page+1<s.pages?'block':'none';
                var members=document.getElementById('members');
                var roles=document.getElementById('roles');
                while(members.firstChild)members.removeChild(members.firstChild);
                while(roles.firstChild)roles.removeChild(roles.firstChild);
                if(!s.members||!s.members.length)line(members,'Немає учасників на сторінці','');
                else s.members.forEach(function(m){line(members,m.name,(m.owner?'Власник':'')+(m.role?' · '+m.role:''));});
                if(!s.roles||!s.roles.length)line(roles,'Немає ролей на сторінці','');
                else s.roles.forEach(function(r){line(roles,r.name,'Ранг '+r.rank+' · '+r.permissions+' дозволів');});
              }
              function hydrate(){if(!window.TelegramWidget)return;try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}}
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var status=document.getElementById('status');status.textContent='Завантаження…';
                TelegramWidget.request(action).then(function(answer){
                  try{var result=JSON.parse(answer.result||'{}');if(result.state)draw(result.state);
                    status.textContent=result.message||'Готово';
                  }catch(e){status.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;status.textContent='Не вдалося виконати дію';});
              }
              document.getElementById('previous').addEventListener('click',function(){ask('w1:manage:prev:'+state.page);});
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
            actionRows = listOf(listOf(
                TelegramWidgetButton("Редагувати ролі й учасників", "w:open:account-manage"),
                TelegramWidgetButton("Акаунти", "w:open:account"),
            )),
        )
    }
}
