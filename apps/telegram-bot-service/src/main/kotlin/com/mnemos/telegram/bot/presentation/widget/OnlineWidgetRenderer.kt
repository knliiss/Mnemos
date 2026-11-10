package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetView
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class OnlineWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: OnlineWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "total" to view.trackedOnline,
                "realmCount" to view.totalRealms,
                "selected" to view.selectedRealmNumber,
                "search" to view.search,
                "name" to view.selectedRealmName,
                "playersTotal" to view.selectedRealmPlayerCount,
                "time" to view.generatedAt,
                "realms" to view.realms.map { mapOf("n" to it.number, "name" to it.name, "count" to it.playerCount) },
                "players" to view.players.map { it.nickname },
            ),
        )

    fun render(view: OnlineWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif}
              .head{display:flex;justify-content:space-between;align-items:center;gap:8px}
              .title{font-weight:800;font-size:18px}
              .hint{font-size:11px;color:var(--tg-theme-hint-color)}
              .big{font-size:26px;font-weight:800;margin-top:10px}
              .section{font-size:13px;font-weight:750;margin:13px 0 4px}
              .row{display:flex;gap:8px;align-items:center;padding:8px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .name{font-size:13px;font-weight:650;flex:1;min-width:0;overflow-wrap:anywhere}
              .count{font-size:12px;color:var(--tg-theme-hint-color);font-weight:700}
              button{border:0;border-radius:9px;padding:9px;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color);font-weight:700}
              .actions{display:flex;gap:7px;margin-top:11px}
              .actions button{flex:1}
              #refresh{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              #feedback{min-height:15px;margin-top:8px;font-size:11px;color:var(--tg-theme-hint-color)}
            </style>
            <div class="head"><span class="title">SAO ONLINE</span><span class="hint" id="time"></span></div>
            <div class="big" id="online">—</div>
            <div class="hint" id="summary"></div>
            <div class="section" id="section">Реалми</div>
            <div id="items"></div>
            <div class="actions"><button id="search">Пошук</button><button id="back">Усі реалми</button><button id="refresh">Оновити</button></div>
            <div id="feedback" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('time').textContent=s.time||'';
                document.getElementById('online').textContent=String(s.total);
                document.getElementById('summary').textContent=s.search?'Пошук «'+s.search+'» · знайдено '+s.playersTotal:(s.selected==null?'Гравців онлайн · реалмів '+s.realmCount:'Гравців у '+s.name+': '+s.playersTotal);
                document.getElementById('section').textContent=s.search?'Результати пошуку':(s.selected==null?'Реалми':'Гравці');
                document.getElementById('back').style.display=s.selected==null&&!s.search?'none':'block';
                var root=document.getElementById('items');
                while(root.firstChild)root.removeChild(root.firstChild);
                if(s.selected==null&&!s.search){
                  if(!s.realms||!s.realms.length){
                    var empty=document.createElement('div');empty.className='row hint';empty.textContent='Реалмів немає';root.appendChild(empty);
                  }else s.realms.forEach(function(realm){
                    var row=document.createElement('div');row.className='row';
                    var name=document.createElement('span');name.className='name';name.textContent=realm.name;
                    var count=document.createElement('span');count.className='count';count.textContent=String(realm.count);
                    var button=document.createElement('button');button.textContent='Переглянути';
                    button.addEventListener('click',function(){ask('w1:on:realm:'+realm.n);});
                    row.appendChild(name);row.appendChild(count);row.appendChild(button);root.appendChild(row);
                  });
                }else{
                  if(!s.players||!s.players.length){
                    var empty=document.createElement('div');empty.className='row hint';empty.textContent='Немає гравців';root.appendChild(empty);
                  }else s.players.forEach(function(nickname){
                    var row=document.createElement('div');row.className='row';
                    var name=document.createElement('span');name.className='name';name.textContent=nickname;
                    row.appendChild(name);root.appendChild(row);
                  });
                }
              }
              function hydrate(){
                if(!window.TelegramWidget)return;
                try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}
              }
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var feedback=document.getElementById('feedback');feedback.textContent='Завантаження…';
                TelegramWidget.request(action).then(function(answer){
                  try{var result=JSON.parse(answer.result||'{}');if(result.state)draw(result.state);
                    feedback.textContent=result.message||'Готово';
                  }catch(e){feedback.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;feedback.textContent='Не вдалося оновити дані';});
              }
              document.getElementById('search').addEventListener('click',function(){ask('w1:on:search');});
              document.getElementById('back').addEventListener('click',function(){ask('w1:on:list');});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:on:refresh');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · SAO онлайн",
            chatData = state(view),
            allowQueries = true,
            actionRows =
                listOf(
                    listOf(
                        TelegramWidgetButton("Пошук гравця", "w:open:online"),
                    ),
                ),
        )
    }
}
