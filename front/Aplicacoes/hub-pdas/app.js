(function () {
  'use strict';

  var contexts = [];
  var conferenceId = null;
  var conference = null;

  function switchView(name) {
    document.querySelectorAll('.view').forEach(function (view) {
      view.classList.toggle('active', view.dataset.view === name);
    });
    document.querySelectorAll('[data-view-target]').forEach(function (button) {
      button.classList.toggle('active', button.dataset.viewTarget === name);
    });
  }

  function options(items, placeholder) {
    return '<option value="">' + placeholder + '</option>' + items.map(function (item) {
      return '<option value="' + GIT.escape(item.id) + '">' + GIT.escape(item.codigo + ' — ' + item.nome) + '</option>';
    }).join('');
  }

  async function loadContexts() {
    var list = document.getElementById('context-list');
    list.innerHTML = '<div class="empty-state">Carregando seus setores...</div>';
    try {
      contexts = await GIT.request('/v1/me/contextos-operacionais');
      var sectors = [];
      var shifts = [];
      contexts.forEach(function (item) {
        if (item.setor && !sectors.some(function (sector) { return sector.id === item.setor.id; })) sectors.push(item.setor);
        if (item.turno && !shifts.some(function (shift) { return shift.id === item.turno.id; })) shifts.push(item.turno);
      });
      document.getElementById('metric-sectors').textContent = sectors.length;
      document.getElementById('metric-quota').textContent = sectors.reduce(function (total, item) { return total + (item.cotaPdas || 0); }, 0);
      document.getElementById('sector-select').innerHTML = options(sectors, 'Selecione o setor');
      document.getElementById('shift-select').innerHTML = options(shifts, 'Selecione o turno');
      list.innerHTML = contexts.length ? contexts.map(function (item) {
        return '<article class="context-card"><h3>' + GIT.escape(item.setor.nome) + '</h3><p>' +
          GIT.escape(item.turno.nome) + ' · Cota de ' + item.setor.cotaPdas + ' PDAs</p><footer><span>' +
          GIT.escape(item.setor.codigo) + '</span><button class="primary-button" data-start-sector="' +
          item.setor.id + '" data-start-shift="' + item.turno.id + '">Conferir</button></footer></article>';
      }).join('') : '<div class="empty-state">Nenhum setor foi atribuído ao seu usuário.</div>';
    } catch (error) {
      list.innerHTML = '<div class="message message--error">' + GIT.escape(error.message) + '</div>';
    }
  }

  async function openConference(form, button) {
    var values = GIT.formData(form);
    GIT.loading(button, true);
    try {
      conference = await GIT.request('/v1/conferencias', {
        method: 'POST',
        body: { setorId: values.setorId, turnoId: values.turnoId, tipo: values.tipo }
      });
      conferenceId = conference.id;
      document.getElementById('conference-start').hidden = true;
      document.getElementById('conference-workspace').hidden = false;
      document.getElementById('conference-status').textContent = 'Em andamento';
      document.getElementById('conference-status').classList.add('active');
      document.getElementById('metric-conference').textContent = 'Ativa';
      GIT.toast('Conferência iniciada.');
      await refreshConference();
      document.getElementById('scan-input').focus();
    } catch (error) {
      GIT.toast(error.message, 'error');
    } finally {
      GIT.loading(button, false);
    }
  }

  async function refreshConference() {
    if (!conferenceId) return;
    try {
      var results = await Promise.all([
        GIT.request('/v1/conferencias/' + conferenceId),
        GIT.request('/v1/conferencias/' + conferenceId + '/itens'),
        GIT.request('/v1/conferencias/' + conferenceId + '/leituras')
      ]);
      conference = results[0];
      var items = results[1] || [];
      var reads = results[2] || [];
      var readAssetIds = new Set(reads.map(function (read) { return read.ativoId; }).filter(Boolean));
      document.getElementById('count-expected').textContent = items.length;
      document.getElementById('count-read').textContent = reads.length;
      document.getElementById('count-pending').textContent = Math.max(items.length - readAssetIds.size, 0);
      document.getElementById('equipment-list').innerHTML = items.length ? items.map(function (item) {
        var read = readAssetIds.has(item.ativoId) || String(item.status || '').indexOf('PRESENTE') >= 0;
        return '<article class="equipment-item"><span><strong>' +
          GIT.escape(item.patrimonio || item.numeroSerie || 'PDA') + '</strong><small>' +
          GIT.escape(item.numeroSerie || 'Sem número de série') + '</small></span><span class="tag ' +
          (read ? 'tag--success' : 'tag--warning') + '">' + (read ? 'Conferido' : 'Pendente') + '</span></article>';
      }).join('') : '<div class="empty-state">O setor não possui PDAs esperados.</div>';
    } catch (error) {
      GIT.toast(error.message, 'error');
    }
  }

  document.querySelectorAll('[data-view-target]').forEach(function (button) {
    button.addEventListener('click', function () { switchView(button.dataset.viewTarget); });
  });
  document.getElementById('refresh-contexts').addEventListener('click', loadContexts);
  document.getElementById('refresh-conference').addEventListener('click', refreshConference);
  document.getElementById('context-list').addEventListener('click', function (event) {
    var button = event.target.closest('[data-start-sector]');
    if (!button) return;
    document.getElementById('sector-select').value = button.dataset.startSector;
    document.getElementById('shift-select').value = button.dataset.startShift;
    switchView('conference');
  });
  document.getElementById('conference-form').addEventListener('submit', function (event) {
    event.preventDefault();
    openConference(event.currentTarget, event.submitter);
  });
  document.getElementById('scan-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    var values = GIT.formData(event.currentTarget);
    var feedback = document.getElementById('scan-feedback');
    GIT.loading(event.submitter, true);
    try {
      var result = await GIT.request('/v1/conferencias/' + conferenceId + '/leituras', {
        method: 'POST', body: { codigo: values.codigo }
      });
      feedback.className = 'scan-feedback success';
      feedback.textContent = result.leitura && result.leitura.resultado === 'DESCONHECIDO'
        ? 'Código não pertence ao pool.'
        : 'PDA registrado com sucesso.';
      event.currentTarget.reset();
      await refreshConference();
      document.getElementById('scan-input').focus();
    } catch (error) {
      feedback.className = 'scan-feedback error';
      feedback.textContent = error.message;
    } finally {
      GIT.loading(event.submitter, false);
    }
  });
  document.getElementById('finish-conference').addEventListener('click', async function () {
    if (!conferenceId || !confirm('Deseja concluir esta conferência?')) return;
    try {
      await GIT.request('/v1/conferencias/' + conferenceId + '/conclusoes', { method: 'POST' });
      GIT.toast('Conferência concluída.');
      conferenceId = null;
      document.getElementById('conference-start').hidden = false;
      document.getElementById('conference-workspace').hidden = true;
      document.getElementById('conference-status').textContent = 'Concluída';
      document.getElementById('conference-status').classList.remove('active');
      document.getElementById('metric-conference').textContent = 'Nenhuma';
      switchView('overview');
    } catch (error) {
      GIT.toast(error.message, 'error');
    }
  });

  GIT.auth({ origin: 'HUB_PDA', onReady: loadContexts });
})();
