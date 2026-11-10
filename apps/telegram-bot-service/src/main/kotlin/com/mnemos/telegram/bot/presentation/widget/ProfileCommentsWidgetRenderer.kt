package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.profilecomments.ProfileCommentsWidgetView
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class ProfileCommentsWidgetRenderer(
    private val json: ObjectMapper,
) {
    fun state(view: ProfileCommentsWidgetView): String =
        json.writeValueAsString(
            mapOf(
                "n" to view.nickname,
                "page" to view.page,
                "pages" to view.totalPages,
                "total" to view.totalItems,
                "thread" to (view.rootCommentId != null),
                "feedback" to view.feedback,
                "confirmDelete" to view.pendingDeleteIndex,
                "comments" to view.comments.map {
                    mapOf(
                        "a" to it.author,
                        "t" to it.id.toString().replace("-", "").take(12),
                        "c" to it.content,
                        "d" to it.deleted,
                        "l" to it.likes,
                        "r" to it.replies,
                        "at" to it.createdAt,
                        "e" to it.canEdit,
                        "x" to it.canDelete,
                    )
                },
            ),
        )

    fun render(view: ProfileCommentsWidgetView): TelegramWidgetMessage {
        val html =
            """
            <style>
              body{margin:0;padding:13px;font-family:system-ui,sans-serif;color:var(--tg-theme-text-color)}
              .head{font-size:17px;font-weight:800}
              .hint{font-size:11px;color:var(--tg-theme-hint-color)}
              .row{padding:10px 0;border-bottom:1px solid var(--tg-theme-section-separator-color)}
              .author{font-size:13px;font-weight:700}
              .content{font-size:12px;white-space:pre-wrap;overflow-wrap:anywhere;margin:6px 0}
              .actions{display:flex;gap:7px;margin:10px 0}
              button{border:0;border-radius:9px;padding:9px 11px;font-weight:700;background:var(--tg-theme-secondary-bg-color);color:var(--tg-theme-text-color)}
              button.primary{background:var(--tg-theme-button-color);color:var(--tg-theme-button-text-color)}
              .actions button{flex:1}
              #feedback{min-height:15px}
            </style>
            <div class="head">КОМЕНТАРІ ПРОФІЛЮ</div>
            <div id="subtitle" class="hint"></div>
            <div id="list"></div>
            <div class="actions">
              <button id="compose" class="primary">Написати коментар</button>
              <button id="back">До коментарів</button>
              <button id="prev">Назад</button>
              <button id="next">Далі</button>
              <button id="refresh" class="primary">Оновити</button>
            </div>
            <div id="feedback" class="hint" aria-live="polite"></div>
            <script>
            (function(){
              var state={},busy=false;
              function elem(tag,className,content){
                var e=document.createElement(tag);e.className=className;
                if(content!==undefined)e.textContent=content;
                return e;
              }
              function draw(s){
                if(!s)return;
                state=s;
                document.getElementById('compose').textContent=s.thread?'Відповісти':'Написати коментар';
                if(s.feedback)document.getElementById('feedback').textContent=s.feedback;
                document.getElementById('subtitle').textContent=
                  (s.thread?'Відповіді · ':'Коментарі · ')+s.n+' · '+s.total+
                  ' · '+(s.page+1)+'/'+Math.max(1,s.pages);
                document.getElementById('back').style.display=s.thread?'block':'none';
                document.getElementById('prev').style.display=s.page>0?'block':'none';
                document.getElementById('next').style.display=s.page+1<s.pages?'block':'none';
                var container=document.getElementById('list');
                while(container.firstChild)container.removeChild(container.firstChild);
                if(!s.comments||!s.comments.length){
                  container.appendChild(elem('div','row hint','Коментарів поки немає'));
                }else s.comments.forEach(function(comment,index){
                  var row=elem('div','row');
                  row.appendChild(elem('div','author',comment.a));
                  row.appendChild(elem('div','content',comment.c));
                  row.appendChild(elem('div','hint',
                    '👍 '+comment.l+' · Відповідей: '+comment.r+' · '+comment.at+' UTC'));
                  if(comment.e){
                    var edit=elem('button','','Редагувати');
                    edit.addEventListener('click',function(){
                      ask('w1:comments:edit:'+index+':'+comment.t);
                    });
                    row.appendChild(edit);
                  }
                  if(comment.x){
                    var confirming=s.confirmDelete===index;
                    var remove=elem('button','',confirming?'Підтвердити видалення':'Видалити');
                    remove.addEventListener('click',function(){
                      ask('w1:comments:'+(confirming?'delete-confirm':'delete')+':'+index+':'+comment.t);
                    });
                    row.appendChild(remove);
                    if(confirming){
                      var cancel=elem('button','','Скасувати');
                      cancel.addEventListener('click',function(){ask('w1:comments:delete-cancel');});
                      row.appendChild(cancel);
                    }
                  }
                  if(!s.thread&&!comment.d&&comment.r>0){
                    var replies=elem('button','','Відповіді');
                    replies.addEventListener('click',function(){
                      ask('w1:comments:thread:'+index+':'+comment.t);
                    });
                    row.appendChild(replies);
                  }
                  container.appendChild(row);
                });
              }
              function hydrate(){
                if(!window.TelegramWidget)return;
                try{draw(JSON.parse(TelegramWidget.chatData||'{}'));}catch(e){}
              }
              function ask(action){
                if(!window.TelegramWidget||busy)return;
                busy=true;
                var feedback=document.getElementById('feedback');feedback.textContent='Оновлення…';
                TelegramWidget.request(action).then(function(answer){
                  try{
                    var result=JSON.parse(answer.result||'{}');
                    if(result.state)draw(result.state);
                    feedback.textContent=result.message||'Готово';
                  }catch(e){feedback.textContent='Оновіть віджет';}
                  busy=false;
                },function(){busy=false;feedback.textContent='Не вдалося виконати запит';});
              }
              document.getElementById('compose').addEventListener('click',function(){ask('w1:comments:compose');});
              document.getElementById('back').addEventListener('click',function(){ask('w1:comments:back');});
              document.getElementById('prev').addEventListener('click',function(){ask('w1:comments:prev:'+state.page);});
              document.getElementById('next').addEventListener('click',function(){ask('w1:comments:next:'+state.page);});
              document.getElementById('refresh').addEventListener('click',function(){ask('w1:comments:refresh');});
              if(window.TelegramWidget){
                TelegramWidget.onEvent('chatDataChanged',hydrate);
                hydrate();TelegramWidget.ready();
              }
            })();
            </script>
            """.trimIndent()
        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · коментарі профілю",
            chatData = state(view),
            allowQueries = true,
            actionRows = listOf(
                listOf(TelegramWidgetButton("Пошук профілів", "w:open:profiles")),
            ),
        )
    }
}
