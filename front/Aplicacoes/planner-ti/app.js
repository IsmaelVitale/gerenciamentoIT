(function () {
  'use strict';
  var KEY = 'gerenciamentoit.final.planner';
  function tasks() { try { return JSON.parse(localStorage.getItem(KEY) || '[]'); } catch (_) { return []; } }
  function save(items) { try { localStorage.setItem(KEY, JSON.stringify(items)); } catch (_) {} renderAll(); }
  function switchView(name) {
    document.querySelectorAll('.view').forEach(function (view) { view.classList.toggle('active', view.dataset.view === name); });
    document.querySelectorAll('[data-view-target]').forEach(function (button) { button.classList.toggle('active', button.dataset.viewTarget === name); });
  }
  function taskCard(task, status) {
    return '<article class="task-card"><span class="task-priority ' + task.prioridade.toLowerCase() + '">' + GIT.escape(task.prioridade) +
      '</span><h3>' + GIT.escape(task.titulo) + '</h3><p>' + GIT.escape(task.descricao || 'Sem descrição') +
      '</p><div class="task-meta"><span>' + GIT.escape(task.responsavel || 'Sem responsável') + '</span><span>' +
      GIT.escape(task.prazo || 'Sem prazo') + '</span></div><div class="task-actions">' +
      (status !== 'A_FAZER' ? '<button data-task-action="back" data-id="' + task.id + '">← Voltar</button>' : '<span></span>') +
      '<button data-task-action="delete" data-id="' + task.id + '">Excluir</button>' +
      (status !== 'CONCLUIDO' ? '<button data-task-action="next" data-id="' + task.id + '">Avançar →</button>' : '<span></span>') + '</div></article>';
  }
  function renderAll() {
    var items = tasks(), statuses = [{ id: 'A_FAZER', label: 'A fazer' }, { id: 'EM_ANDAMENTO', label: 'Em andamento' }, { id: 'CONCLUIDO', label: 'Concluído' }];
    document.getElementById('kanban-board').innerHTML = statuses.map(function (column) {
      var filtered = items.filter(function (item) { return item.status === column.id; });
      return '<section class="kanban-column"><header><h2>' + column.label + '</h2><span>' + filtered.length + '</span></header>' +
        (filtered.length ? filtered.map(function (task) { return taskCard(task, column.id); }).join('') : '<div class="empty-state">Nenhuma tarefa</div>') + '</section>';
    }).join('');
    var backlog = items.filter(function (item) { return item.status === 'BACKLOG'; }).sort(function (a, b) {
      return ['URGENTE', 'ALTA', 'MEDIA', 'BAIXA'].indexOf(a.prioridade) - ['URGENTE', 'ALTA', 'MEDIA', 'BAIXA'].indexOf(b.prioridade);
    });
    document.getElementById('backlog-list').innerHTML = backlog.length ? backlog.map(function (task) {
      return '<article class="backlog-item"><span class="task-priority ' + task.prioridade.toLowerCase() + '">' + task.prioridade +
        '</span><span><strong>' + GIT.escape(task.titulo) + '</strong><small>' + GIT.escape(task.descricao || 'Sem descrição') +
        '</small></span><span>' + GIT.escape(task.responsavel || 'Sem responsável') + '</span><span>' + GIT.escape(task.prazo || 'Sem prazo') +
        '</span><button class="secondary-button" data-task-action="start" data-id="' + task.id + '">Planejar</button></article>';
    }).join('') : '<div class="empty-state">O backlog está vazio.</div>';
    document.getElementById('metric-total').textContent = items.length;
    document.getElementById('metric-progress').textContent = items.filter(function (item) { return item.status === 'EM_ANDAMENTO'; }).length;
    document.getElementById('metric-done').textContent = items.filter(function (item) { return item.status === 'CONCLUIDO'; }).length;
    document.getElementById('metric-urgent').textContent = items.filter(function (item) { return item.prioridade === 'URGENTE'; }).length;
    var max = Math.max(items.length, 1);
    document.getElementById('progress-chart').innerHTML = [{ id: 'BACKLOG', label: 'Backlog' }].concat(statuses).map(function (status) {
      var count = items.filter(function (item) { return item.status === status.id; }).length;
      return '<div class="progress-row"><span>' + status.label + '</span><div class="progress-track"><span style="width:' + Math.round(count / max * 100) +
        '%"></span></div><strong>' + count + '</strong></div>';
    }).join('');
  }
  function taskAction(event) {
    var button = event.target.closest('[data-task-action]'); if (!button) return;
    var items = tasks(), task = items.find(function (item) { return item.id === button.dataset.id; }); if (!task) return;
    var action = button.dataset.taskAction, order = ['A_FAZER', 'EM_ANDAMENTO', 'CONCLUIDO'];
    if (action === 'delete') items = items.filter(function (item) { return item.id !== task.id; });
    if (action === 'start') task.status = 'A_FAZER';
    if (action === 'next') task.status = order[Math.min(order.indexOf(task.status) + 1, 2)];
    if (action === 'back') task.status = order[Math.max(order.indexOf(task.status) - 1, 0)];
    save(items);
  }
  document.querySelectorAll('[data-view-target]').forEach(function (button) { button.addEventListener('click', function () { switchView(button.dataset.viewTarget); }); });
  document.getElementById('kanban-board').addEventListener('click', taskAction);
  document.getElementById('backlog-list').addEventListener('click', taskAction);
  document.getElementById('new-task-button').addEventListener('click', function () { document.getElementById('task-dialog').showModal(); });
  document.getElementById('new-backlog-button').addEventListener('click', function () { document.querySelector('#task-form [name=status]').value = 'BACKLOG'; document.getElementById('task-dialog').showModal(); });
  document.getElementById('close-task-dialog').addEventListener('click', function () { document.getElementById('task-dialog').close(); });
  document.getElementById('task-form').addEventListener('submit', function (event) {
    event.preventDefault(); var values = GIT.formData(event.currentTarget), items = tasks();
    values.id = String(Date.now()); items.push(values); save(items); event.currentTarget.reset(); document.getElementById('task-dialog').close(); GIT.toast('Tarefa criada.');
  });
  GIT.auth({ origin: 'PLANNER', onReady: renderAll });
})();
