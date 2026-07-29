(function () {
  'use strict';
  var tickets = [];

  function switchView(name) {
    document.querySelectorAll('.view').forEach(function (view) { view.classList.toggle('active', view.dataset.view === name); });
    document.querySelectorAll('[data-view-target]').forEach(function (button) { button.classList.toggle('active', button.dataset.viewTarget === name); });
    if (name === 'my-tickets') loadTickets();
  }
  function statusClass(status) {
    if (status === 'RESOLVIDO' || status === 'FECHADO') return 'tag--success';
    if (status === 'ABERTO' || status === 'EM_TRIAGEM') return 'tag--warning';
    return '';
  }
  function statusLabel(status) { return String(status || '').replaceAll('_', ' '); }
  function ticketCards(items) {
    if (!items.length) return '<div class="empty-state">Você ainda não possui chamados.</div>';
    return items.map(function (item) {
      return '<article class="ticket-card"><button data-ticket-id="' + item.id + '"><strong>' + GIT.escape(item.protocolo) + '</strong><small>' +
        GIT.date(item.criadoEm) + '</small></button><p>' + GIT.escape(item.descricao) + '</p><span class="tag ' +
        statusClass(item.status) + '">' + GIT.escape(statusLabel(item.status)) + '</span></article>';
    }).join('');
  }
  function paintMetrics() {
    document.getElementById('ticket-total').textContent = tickets.length;
    document.getElementById('ticket-open').textContent = tickets.filter(function (item) { return item.status === 'ABERTO' || item.status === 'EM_TRIAGEM'; }).length;
    document.getElementById('ticket-progress').textContent = tickets.filter(function (item) { return item.status === 'EM_ATENDIMENTO' || item.status === 'AGUARDANDO_SOLICITANTE'; }).length;
    document.getElementById('ticket-done').textContent = tickets.filter(function (item) { return item.status === 'RESOLVIDO' || item.status === 'FECHADO'; }).length;
    document.getElementById('recent-tickets').innerHTML = ticketCards(tickets.slice(0, 4));
  }
  async function loadTickets() {
    var list = document.getElementById('ticket-list');
    list.innerHTML = '<div class="empty-state">Carregando seus chamados...</div>';
    try {
      var page = await GIT.request('/v1/chamados/meus?tamanho=50');
      tickets = page.conteudo || [];
      list.innerHTML = ticketCards(tickets);
      paintMetrics();
    } catch (error) { list.innerHTML = '<div class="message message--error">' + GIT.escape(error.message) + '</div>'; }
  }
  async function showTicket(id) {
    var detail = document.getElementById('ticket-detail');
    try {
      var ticket = await GIT.request('/v1/chamados/meus/' + id);
      detail.hidden = false;
      detail.innerHTML = '<span class="kicker">DETALHES DO CHAMADO</span><h2>' + GIT.escape(ticket.protocolo) + '</h2><span class="tag ' +
        statusClass(ticket.status) + '">' + GIT.escape(statusLabel(ticket.status)) + '</span><p>' + GIT.escape(ticket.descricao) +
        '</p><div class="message">Aberto em ' + GIT.date(ticket.criadoEm) + (ticket.telefoneContato ? ' · Contato: ' + GIT.escape(ticket.telefoneContato) : '') + '</div>';
      detail.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) { GIT.toast(error.message, 'error'); }
  }

  document.querySelectorAll('[data-view-target]').forEach(function (button) { button.addEventListener('click', function () { switchView(button.dataset.viewTarget); }); });
  document.querySelectorAll('[data-open-ticket]').forEach(function (button) { button.addEventListener('click', function () { switchView('new-ticket'); }); });
  document.getElementById('refresh-tickets').addEventListener('click', loadTickets);
  document.getElementById('ticket-list').addEventListener('click', function (event) { var button = event.target.closest('[data-ticket-id]'); if (button) showTicket(button.dataset.ticketId); });
  document.getElementById('recent-tickets').addEventListener('click', function (event) { var button = event.target.closest('[data-ticket-id]'); if (button) { switchView('my-tickets'); showTicket(button.dataset.ticketId); } });
  document.getElementById('ticket-search').addEventListener('input', function (event) {
    var term = event.target.value.toLowerCase();
    document.getElementById('ticket-list').innerHTML = ticketCards(tickets.filter(function (item) {
      return item.protocolo.toLowerCase().includes(term) || item.descricao.toLowerCase().includes(term);
    }));
  });
  document.getElementById('ticket-form').addEventListener('submit', async function (event) {
    event.preventDefault(); var values = GIT.formData(event.currentTarget), headers = {};
    if (values.identificadorExterno) headers['Idempotency-Key'] = values.identificadorExterno;
    GIT.loading(event.submitter, true);
    try {
      var ticket = await GIT.request('/v1/chamados', { method: 'POST', headers: headers, body: values });
      document.getElementById('created-protocol').textContent = ticket.protocolo;
      document.getElementById('ticket-success').hidden = false;
      event.currentTarget.reset(); await loadTickets(); GIT.toast('Chamado enviado à T.I.');
    } catch (error) { GIT.toast(error.message, 'error'); }
    finally { GIT.loading(event.submitter, false); }
  });

  GIT.auth({ origin: 'ITSM', onReady: loadTickets });
})();
