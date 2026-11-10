package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetEntry
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetView
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class ProfileWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: ProfileWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "query" to view.query,
                "page" to view.page,
                "pages" to view.totalPages,
                "total" to view.totalItems,
                "profiles" to view.profiles.map { it.toListPayload() },
                "selected" to view.selected?.toPayload(),
            ),
        )

    private fun ProfileWidgetEntry.toListPayload(): Map<String, Any?> =
        mapOf(
            "id" to accountId.toString(),
            "nickname" to nickname,
            "likes" to likes,
            "comments" to comments,
        )

    private fun ProfileWidgetEntry.toPayload(): Map<String, Any?> =
        mapOf(
            "id" to accountId.toString(),
            "nickname" to nickname,
            "likes" to likes,
            "dislikes" to dislikes,
            "comments" to comments,
            "presence" to presenceStatus.name,
            "realm" to realm,
            "lastSeen" to lastSeenOnline,
            "joined" to joinedAt,
        )

    fun render(view: ProfileWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif;color:var(--tg-theme-text-color)}
              .head{font-weight:800;font-size:18px}
              .hint{font-size:11px;color:var(--tg-theme-hint-color)}
              .subtitle{font-size:12px;margin:8px 0;color:var(--tg-theme-hint-color)}
              .row{display:flex;align-items:center;gap:8px;padding:9px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .name{font-size:13px;font-weight:700;flex:1;min-width:0;overflow-wrap:anywhere}
              .stats{font-size:11px;color:var(--tg-theme-hint-color)}
              .actions{display:flex;gap:7px;flex-wrap:wrap;margin:10px 0}
              button{border:0;border-radius:9px;padding:9px 10px;font-weight:700;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              button.primary{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              #details{padding:5px 0}
              #feedback{min-height:15px;font-size:11px;margin-top:9px}
            </style>
            <div class="head">ПРОФІЛІ MNEMOS</div>
            <div class="subtitle" id="summary"></div>
            <div id="list"></div>
            <div id="details"></div>
            <div class="actions">
              <button id="search" class="primary">Пошук</button>
              <button id="clear">Скинути</button>
              <button id="back">Результати</button>
              <button id="prev">Назад</button>
              <button id="next">Далі</button>
              <button id="refresh">Оновити</button>
            </div>
            <div id="feedback" class="hint" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function row(text,secondary){
                var r=document.createElement('div');r.className='row';
                var v=document.createElement('span');v.className=secondary?'stats':'name';
                v.textContent=text;r.appendChild(v);return r;
              }
              function draw(s){
                if(!s)return;
                state=s;
                var summary=document.getElementById('summary');
                var list=document.getElementById('list');
                var details=document.getElementById('details');
                while(list.firstChild)list.removeChild(list.firstChild);
                while(details.firstChild)details.removeChild(details.firstChild);
                var selected=s.selected;
                document.getElementById('search').style.display=selected?'none':'block';
                document.getElementById('clear').style.display=selected||!s.query?'none':'block';
                document.getElementById('back').style.display=selected?'block':'none';
                document.getElementById('prev').style.display=!selected&&s.query&&s.page>0?'block':'none';
                document.getElementById('next').style.display=!selected&&s.query&&s.page+1<s.pages?'block':'none';
                if(selected){
                  summary.textContent='Публічний профіль';
                  details.appendChild(row(selected.nickname,false));
                  details.appendChild(row('👍 '+selected.likes+' · 👎 '+selected.dislikes+' · Коментарів: '+selected.comments,true));
                  var label=selected.presence==='HIDDEN'?'Прихований онлайн':(
                    selected.presence==='ONLINE'?'Онлайн':(selected.presence==='OFFLINE'?'Офлайн':'Невідомий статус')
                  );
                  details.appendChild(row(label+(selected.realm?' · '+selected.realm:''),true));
                  if(selected.lastSeen)details.appendChild(row('Останній онлайн: '+selected.lastSeen,true));
                  details.appendChild(row('У Mnemos із '+selected.joined,true));
                  var activity=document.createElement('button');activity.textContent='Історія дропів';
                  activity.className='primary';
                  activity.addEventListener('click',function(){ask('w1:prof:activity');});
                  details.appendChild(activity);
                }else{
                  summary.textContent=s.query?'«'+s.query+'» · '+s.total+' результатів · '+(s.page+1)+'/'+Math.max(1,s.pages):'Знайдіть користувача за нікнеймом';
                  if(!s.profiles||!s.profiles.length){
                    details.appendChild(row(s.query?'За запитом нічого не знайдено':'Натисніть «Пошук»',true));
                  }else s.profiles.forEach(function(profile){
                    var entry=document.createElement('div');entry.className='row';
                    var info=document.createElement('span');info.className='name';
                    info.textContent=profile.nickname;
                    var numbers=document.createElement('span');numbers.className='stats';
                    numbers.textContent='👍 '+profile.likes+' · '+profile.comments+' коментарів';
                    var button=document.createElement('button');button.textContent='Переглянути';
                    button.addEventListener('click',function(){ask('w1:prof:open:'+profile.id);});
                    entry.appendChild(info);entry.appendChild(numbers);entry.appendChild(button);
                    list.appendChild(entry);
                  });
                }
              }
              function hydrate(){if(!window.TelegramWidget)return;try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}}
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var feedback=document.getElementById('feedback');
                feedback.textContent='Завантаження…';
                TelegramWidget.request(action).then(function(answer){
                  try{
                    var result=JSON.parse(answer.result||'{}');
                    if(result.state)draw(result.state);
                    feedback.textContent=result.message||'Готово';
                  }catch(e){feedback.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;feedback.textContent='Помилка запиту';});
              }
              document.getElementById('search').addEventListener('click',function(){ask('w1:prof:search');});
              document.getElementById('clear').addEventListener('click',function(){ask('w1:prof:clear');});
              document.getElementById('back').addEventListener('click',function(){ask('w1:prof:back');});
              document.getElementById('prev').addEventListener('click',function(){ask('w1:prof:prev:'+state.page);});
              document.getElementById('next').addEventListener('click',function(){ask('w1:prof:next:'+state.page);});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:prof:refresh');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · публічні профілі",
            chatData = state(view),
            allowQueries = true,
            actionRows = listOf(
                listOf(
                    TelegramWidgetButton("Акаунти", "w:open:account"),
                    TelegramWidgetButton("SAO Online", "w:open:online"),
                ),
            ),
        )
    }
}
