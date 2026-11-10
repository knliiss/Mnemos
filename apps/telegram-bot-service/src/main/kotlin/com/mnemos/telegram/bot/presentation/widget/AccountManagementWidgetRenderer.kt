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
                "canKick" to view.canRemoveMembers,
                "canAssign" to view.canAssignRoles,
                "confirm" to view.pendingKickIndex,
                "members" to view.members.map { member ->
                    mapOf(
                        "n" to member.name,
                        "r" to member.role,
                        "o" to member.owner,
                        "s" to member.self,
                        "a" to view.roles.indices.filter { view.roles[it].id in member.roleIds },
                    )
                },
                "roles" to view.roles.map {
                    mapOf("n" to it.name, "rank" to it.rank, "p" to it.permissionCount)
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
              .tabs,.actions{display:flex;gap:6px;flex-wrap:wrap;margin:10px 0}
              button{border:0;border-radius:9px;padding:9px;font-weight:700;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              button.active,button.primary{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              button:disabled{opacity:.4}
              .tabs button,.actions button{flex:1}
              .item{padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .name{font-size:13px;font-weight:700;overflow-wrap:anywhere}
              .detail{font-size:11px;color:var(--tg-theme-hint-color);margin-top:3px}
              .rowActions{display:flex;gap:6px;margin-top:7px}
              .rowActions button{font-size:11px;padding:7px 9px}
              #feedback{min-height:16px;margin-top:8px}
            </style>
            <div class="title">УЧАСНИКИ ТА РОЛІ</div>
            <div class="hint" id="account"></div>
            <div class="tabs"><button id="membersTab" class="active">Учасники</button><button id="rolesTab">Ролі</button></div>
            <div id="selection" class="hint"></div>
            <div id="entries"></div>
            <div class="actions"><button id="prev">Назад</button><button id="next">Далі</button><button id="refresh" class="primary">Оновити</button></div>
            <div class="hint" id="feedback" aria-live="polite"></div>
            <script>
            (function(){
              var state={},tab='members',selected=null,busy=false;
              function el(tag,className,text){
                var node=document.createElement(tag);node.className=className;
                if(text!==undefined)node.textContent=text;
                return node;
              }
              function draw(s){
                if(!s)return;
                if(state.page!==undefined&&state.page!==s.page)selected=null;
                state=s;
                document.getElementById('account').textContent=s.name+' · '+s.membersCount+' учасників · '+s.rolesCount+' ролей · '+(s.page+1)+'/'+Math.max(1,s.pages);
                document.getElementById('prev').style.display=s.page>0?'block':'none';
                document.getElementById('next').style.display=s.page+1<s.pages?'block':'none';
                document.getElementById('membersTab').className=tab==='members'?'active':'';
                document.getElementById('rolesTab').className=tab==='roles'?'active':'';
                document.getElementById('selection').textContent=tab==='roles'&&selected!==null&&s.members[selected]?
                  'Ролі учасника: '+s.members[selected].n:'';
                var root=document.getElementById('entries');
                while(root.firstChild)root.removeChild(root.firstChild);
                var entries=tab==='members'?s.members:s.roles;
                if(!entries||!entries.length)root.appendChild(el('div','item hint','Немає записів на цій сторінці'));
                (entries||[]).forEach(function(entry,index){
                  var item=el('div','item');
                  item.appendChild(el('div','name',entry.n));
                  item.appendChild(el('div','detail',tab==='members'?
                    ((entry.o?'Власник':(entry.s?'Ви':'Учасник'))+' · '+(entry.r||'Без ролі')):
                    ('Позиція '+entry.rank+' · Дозволів: '+entry.p)));
                  var actions=el('div','rowActions');
                  if(tab==='members'){
                    if(s.canAssign&&!entry.o&&!entry.s){
                      var roles=el('button','','Ролі');
                      roles.addEventListener('click',function(){selected=index;tab='roles';draw(s);});
                      actions.appendChild(roles);
                    }
                    if(s.canKick&&!entry.o&&!entry.s){
                      var confirm=s.confirm===index;
                      var kick=el('button',confirm?'primary':'',confirm?'Підтвердити виключення':'Виключити');
                      kick.addEventListener('click',function(){
                        ask('w1:manage:'+(confirm?'kick-confirm':'kick')+':'+s.page+':'+index);
                      });
                      actions.appendChild(kick);
                      if(confirm){
                        var cancel=el('button','','Скасувати');
                        cancel.addEventListener('click',function(){ask('w1:manage:cancel');});
                        actions.appendChild(cancel);
                      }
                    }
                  }else if(s.canAssign&&selected!==null&&s.members[selected]&&!s.members[selected].o&&!s.members[selected].s){
                    var active=s.members[selected].a.indexOf(index)>=0;
                    var toggle=el('button',active?'':'primary',active?'Зняти роль':'Призначити роль');
                    toggle.addEventListener('click',function(){
                      ask('w1:manage:role:'+s.page+':'+selected+':'+index+':'+(active?'off':'on'));
                    });
                    actions.appendChild(toggle);
                  }
                  item.appendChild(actions);root.appendChild(item);
                });
              }
              function hydrate(){if(!window.TelegramWidget)return;try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}}
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var feedback=document.getElementById('feedback');feedback.textContent='Виконання…';
                TelegramWidget.request(action).then(function(answer){
                  try{
                    var result=JSON.parse(answer.result||'{}');if(result.state)draw(result.state);
                    feedback.textContent=result.message||'Готово';
                  }catch(e){feedback.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;feedback.textContent='Не вдалося виконати дію';});
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
