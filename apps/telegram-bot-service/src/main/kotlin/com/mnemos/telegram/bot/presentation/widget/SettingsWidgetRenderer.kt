package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.settings.SettingsWidgetView
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class SettingsWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: SettingsWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "language" to view.language?.name,
                "active" to view.activePackId?.toString(),
                "activeName" to view.activePackTitle,
                "page" to view.page,
                "pages" to view.totalPages,
                "total" to view.totalPacks,
                "packs" to view.packs.map { pack ->
                    mapOf("id" to pack.id.toString(), "name" to pack.title, "active" to pack.active)
                },
            ),
        )

    fun render(view: SettingsWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif}
              .head{font-size:18px;font-weight:800;margin-bottom:9px}
              .section{font-weight:750;font-size:13px;margin-top:12px;margin-bottom:6px}
              .hint{font-size:11px;color:var(--tg-theme-hint-color)}
              .row{display:flex;align-items:center;justify-content:space-between;gap:9px;padding:8px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .name{font-size:13px;flex:1;overflow-wrap:anywhere;min-width:0}
              button{border:0;border-radius:9px;padding:9px;background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color);font-weight:700}
              button.secondary{background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              button.selected{opacity:.6}
              .actions{display:flex;gap:7px;margin:7px 0}
              .actions button{flex:1}
              #feedback{min-height:16px;margin-top:8px}
            </style>
            <div class="head">НАЛАШТУВАННЯ</div>
            <div class="section">Мова</div>
            <div class="actions"><button id="UK">UA</button><button id="RU">RU</button><button id="EN">EN</button></div>
            <div class="section">Пакет сповіщень</div>
            <div class="hint" id="selectedPack"></div>
            <div id="packs"></div>
            <div class="actions"><button id="previous" class="secondary">Назад</button><button id="next" class="secondary">Далі</button><button id="refresh">Оновити</button></div>
            <div id="feedback" class="hint" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function draw(s){
                if(!s)return;
                state=s;
                ['UK','RU','EN'].forEach(function(lang){
                  var btn=document.getElementById(lang);
                  btn.className=lang===s.language?'selected':'secondary';
                  btn.disabled=lang===s.language;
                });
                document.getElementById('selectedPack').textContent=s.activeName?'Активний: '+s.activeName:'Активний пакет не вибрано';
                document.getElementById('previous').style.display=s.page>0?'block':'none';
                document.getElementById('next').style.display=s.page+1<s.pages?'block':'none';
                var root=document.getElementById('packs');
                while(root.firstChild)root.removeChild(root.firstChild);
                if(!s.packs||!s.packs.length){
                  var empty=document.createElement('div');empty.className='row hint';
                  empty.textContent='Пакетів немає';root.appendChild(empty);
                }else s.packs.forEach(function(pack){
                  var row=document.createElement('div');row.className='row';
                  var name=document.createElement('span');name.className='name';name.textContent=pack.name;
                  var btn=document.createElement('button');
                  btn.className=pack.active?'selected':'secondary';
                  btn.textContent=pack.active?'Активний':'Застосувати';
                  btn.disabled=pack.active;
                  btn.addEventListener('click',function(){ask('w1:settings:pack:'+pack.id);});
                  row.appendChild(name);row.appendChild(btn);root.appendChild(row);
                });
              }
              function hydrate(){if(!window.TelegramWidget)return;try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}}
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var feedback=document.getElementById('feedback');feedback.textContent='Завантаження…';
                TelegramWidget.request(action).then(function(answer){
                  try{var result=JSON.parse(answer.result||'{}');if(result.state)draw(result.state);
                    feedback.textContent=result.message||'Готово';
                  }catch(e){feedback.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;feedback.textContent='Не вдалося виконати дію';});
              }
              ['UK','RU','EN'].forEach(function(lang){document.getElementById(lang).addEventListener('click',function(){ask('w1:settings:lang:'+lang);});});
              document.getElementById('previous').addEventListener('click',function(){ask('w1:settings:prev:'+state.page);});
              document.getElementById('next').addEventListener('click',function(){ask('w1:settings:next:'+state.page);});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:settings:refresh');});
              if(window.TelegramWidget){TelegramWidget.onEvent('chatDataChanged',hydrate);hydrate();TelegramWidget.ready();}
            })();
            </script>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · налаштування",
            chatData = state(view),
            allowQueries = true,
            actionRows = listOf(listOf(TelegramWidgetButton("Розширені налаштування", "w:open:settings"))),
        )
    }
}
