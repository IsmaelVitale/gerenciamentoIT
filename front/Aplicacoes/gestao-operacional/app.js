(function () {
  'use strict';
  var sectors = [];
  var shifts = [];
  var selectedUser = null;

  function switchView(name) {
    document.querySelectorAll('.view').forEach(function (view) { view.classList.toggle('active', view.dataset.view === name); });
    document.querySelectorAll('[data-view-target]').forEach(function (button) { button.classList.toggle('active', button.dataset.viewTarget === name); });
  }

  function optionList(items, empty) {
    return '<option value="">' + empty + '</option>' + items.map(function (item) {
      return '<option value="' + item.id + '">' + GIT.escape(item.codigo + ' — ' + item.nome) + '</option>';
    }).join('');
  }

  function renderStructure() {
    document.getElementById('sector-count').textContent = sectors.filter(function (item) { return item.ativo; }).length;
    document.getElementById('shift-count').textContent = shifts.filter(function (item) { return item.ativo; }).length;
    document.getElementById('quota-count').textContent = sectors.reduce(function (sum, item) { return sum + item.cotaPdas; }, 0);
    document.getElementById('dashboard-sectors').innerHTML = sectors.length ? sectors.slice(0, 6).map(function (item) {
      return '<article class="list-card"><span><strong>' + GIT.escape(item.nome) + '</strong><small>' + GIT.escape(item.codigo) + '</small></span><span class="tag">' + item.cotaPdas + ' PDAs</span></article>';
    }).join('') : '<div class="empty-state">Nenhum setor cadastrado.</div>';
    document.getElementById('dashboard-shifts').innerHTML = shifts.length ? shifts.slice(0, 6).map(function (item) {
      return '<article class="list-card"><span><strong>' + GIT.escape(item.nome) + '</strong><small>' + GIT.escape(item.codigo) + '</small></span><span class="tag">' + GIT.escape((item.horaInicio || '—') + '–' + (item.horaFim || '—')) + '</span></article>';
    }).join('') : '<div class="empty-state">Nenhum turno cadastrado.</div>';
    document.getElementById('assign-sector').innerHTML = optionList(sectors, 'Sem setor');
    document.getElementById('assign-shift').innerHTML = optionList(shifts, 'Sem turno');
    document.getElementById('sector-table').innerHTML = table(sectors, 'sector');
    document.getElementById('shift-table').innerHTML = table(shifts, 'shift');
  }

  function table(items, type) {
    if (!items.length) return '<div class="empty-state">Nenhum cadastro.</div>';
    return '<table class="data-table"><thead><tr><th>Código</th><th>Nome</th><th>' + (type === 'sector' ? 'Cota' : 'Horário') + '</th></tr></thead><tbody>' +
      items.map(function (item) { return '<tr><td>' + GIT.escape(item.codigo) + '</td><td>' + GIT.escape(item.nome) + '</td><td>' +
        (type === 'sector' ? item.cotaPdas : GIT.escape((item.horaInicio || '—') + '–' + (item.horaFim || '—'))) + '</td></tr>'; }).join('') + '</tbody></table>';
  }

  async function loadStructure() {
    try {
      var result = await Promise.all([GIT.request('/v1/setores'), GIT.request('/v1/turnos')]);
      sectors = result[0] || []; shifts = result[1] || []; renderStructure();
      var current = GIT.session();
      document.getElementById('role-name').textContent = current && current.usuario ? (current.usuario.papeis[0] || 'Usuário').replace('_', ' ') : '—';
    } catch (error) { GIT.toast(error.message, 'error'); }
  }

  function renderUser(user) {
    selectedUser = user;
    document.getElementById('selected-user-id').value = user.id;
    document.getElementById('selected-user-name').value = user.nome + ' · ' + user.matricula;
    document.getElementById('user-result').innerHTML = '<article class="person-card"><header><span><h3>' + GIT.escape(user.nome) + '</h3><p>' +
      GIT.escape(user.matricula) + '</p></span><span class="tag ' + (user.ativo ? 'tag--success' : 'tag--danger') + '">' +
      (user.ativo ? 'Ativo' : 'Inativo') + '</span></header><div class="access-list">' +
      ((user.atribuicoes || []).length ? user.atribuicoes.map(function (access) {
        return '<div class="access-item"><strong>' + GIT.escape(access.papel.replace('_', ' ')) + '</strong> · ' +
          GIT.escape(access.setor || 'Acesso global') + (access.turno ? ' · ' + GIT.escape(access.turno) : '') + '</div>';
      }).join('') : '<div class="access-item">Ainda sem atribuições.</div>') + '</div></article>';
  }

  function clean(values) {
    Object.keys(values).forEach(function (key) { if (values[key] === '') delete values[key]; });
    return values;
  }

  document.querySelectorAll('[data-view-target]').forEach(function (button) { button.addEventListener('click', function () { switchView(button.dataset.viewTarget); }); });
  document.getElementById('refresh-dashboard').addEventListener('click', loadStructure);
  document.getElementById('refresh-structure').addEventListener('click', loadStructure);
  document.getElementById('search-user-form').addEventListener('submit', async function (event) {
    event.preventDefault(); GIT.loading(event.submitter, true);
    try { renderUser(await GIT.request('/v1/usuarios' + GIT.query(GIT.formData(event.currentTarget)))); }
    catch (error) { document.getElementById('user-result').innerHTML = '<div class="message message--error">' + GIT.escape(error.message) + '</div>'; }
    finally { GIT.loading(event.submitter, false); }
  });
  document.getElementById('create-user-form').addEventListener('submit', async function (event) {
    event.preventDefault(); GIT.loading(event.submitter, true);
    try {
      var values = GIT.formData(event.currentTarget);
      var created = await GIT.request('/v1/usuarios', { method: 'POST', body: values });
      renderUser(Object.assign({}, created, { atribuicoes: [] })); event.currentTarget.reset(); GIT.toast('Colaborador cadastrado.');
    } catch (error) { GIT.toast(error.message, 'error'); }
    finally { GIT.loading(event.submitter, false); }
  });
  document.getElementById('assign-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    if (!selectedUser) { GIT.toast('Selecione um colaborador primeiro.', 'error'); return; }
    var values = GIT.formData(event.currentTarget); GIT.loading(event.submitter, true);
    try {
      await GIT.request('/v1/usuarios/' + values.userId + '/atribuicoes', {
        method: 'POST',
        body: {
          papel: values.papel, setorId: values.setorId || null, turnoId: values.turnoId || null,
          inicioVigencia: values.inicioVigencia ? new Date(values.inicioVigencia).toISOString() : null,
          fimVigencia: values.fimVigencia ? new Date(values.fimVigencia).toISOString() : null
        }
      });
      renderUser(await GIT.request('/v1/usuarios/' + values.userId)); GIT.toast('Acesso atribuído.');
    } catch (error) { GIT.toast(error.message, 'error'); }
    finally { GIT.loading(event.submitter, false); }
  });
  document.getElementById('role-select').addEventListener('change', function (event) {
    var hints = { USUARIO: 'Usuário exige setor.', LIDER: 'Líder exige setor e turno.', SUPERVISOR: 'Supervisor usa acesso global.', ANALISTA_TI: 'Analista de T.I. usa acesso global.', GESTOR_TI: 'Gestor de T.I. usa acesso global.' };
    document.getElementById('scope-hint').textContent = hints[event.target.value];
  });
  document.getElementById('sector-form').addEventListener('submit', async function (event) {
    event.preventDefault(); var values = GIT.formData(event.currentTarget); values.cotaPdas = Number(values.cotaPdas); GIT.loading(event.submitter, true);
    try { await GIT.request('/v1/setores', { method: 'POST', body: values }); event.currentTarget.reset(); await loadStructure(); GIT.toast('Setor criado.'); }
    catch (error) { GIT.toast(error.message, 'error'); } finally { GIT.loading(event.submitter, false); }
  });
  document.getElementById('shift-form').addEventListener('submit', async function (event) {
    event.preventDefault(); var values = clean(GIT.formData(event.currentTarget)); GIT.loading(event.submitter, true);
    try { await GIT.request('/v1/turnos', { method: 'POST', body: values }); event.currentTarget.reset(); await loadStructure(); GIT.toast('Turno criado.'); }
    catch (error) { GIT.toast(error.message, 'error'); } finally { GIT.loading(event.submitter, false); }
  });

  GIT.auth({ origin: 'GESTAO_OPERACIONAL', onReady: loadStructure });
})();
