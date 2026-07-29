(function () {
  'use strict';
  var types = [], sectors = [], selectedAsset = null;

  function switchView(name) {
    document.querySelectorAll('.view').forEach(function (view) { view.classList.toggle('active', view.dataset.view === name); });
    document.querySelectorAll('[data-view-target]').forEach(function (button) { button.classList.toggle('active', button.dataset.viewTarget === name); });
  }
  function options(items, empty) {
    return '<option value="">' + empty + '</option>' + items.map(function (item) { return '<option value="' + item.id + '">' + GIT.escape(item.codigo + ' — ' + item.nome) + '</option>'; }).join('');
  }
  function clean(values) { Object.keys(values).forEach(function (key) { if (values[key] === '') delete values[key]; }); return values; }

  async function loadReferences() {
    try {
      var result = await Promise.all([GIT.request('/v1/tipos-ativo'), GIT.request('/v1/setores')]);
      types = result[0] || []; sectors = result[1] || [];
      document.getElementById('type-count').textContent = types.length;
      document.getElementById('asset-type-select').innerHTML = options(types, 'Selecione o tipo');
      document.getElementById('operation-sector').innerHTML = options(sectors, 'Selecione o setor');
      renderTypes();
    } catch (error) { GIT.toast(error.message, 'error'); }
  }
  function renderTypes() {
    document.getElementById('type-list').innerHTML = types.length ? types.map(function (item) {
      return '<article class="type-card"><strong>' + GIT.escape(item.nome) + '</strong><small>' + GIT.escape(item.codigo) +
        (item.controlaPool ? ' · Controla pool' : '') + '</small></article>';
    }).join('') : '<div class="empty-state">Nenhum tipo cadastrado.</div>';
  }
  async function loadAssets(filters) {
    var container = document.getElementById('asset-table');
    container.innerHTML = '<div class="empty-state">Carregando inventário...</div>';
    try {
      var page = await GIT.request('/v1/ativos' + GIT.query(Object.assign({ tamanho: 50 }, filters || {})));
      var assets = page.conteudo || [];
      document.getElementById('asset-total').textContent = page.totalElementos !== undefined ? page.totalElementos : assets.length;
      document.getElementById('asset-available').textContent = assets.filter(function (item) { return item.disponibilidade === 'DISPONIVEL'; }).length;
      document.getElementById('asset-active').textContent = assets.filter(function (item) { return item.situacaoPatrimonial === 'ATIVO'; }).length;
      container.innerHTML = assets.length ? '<table class="data-table"><thead><tr><th>Equipamento</th><th>Tipo</th><th>Situação</th><th>Local</th><th></th></tr></thead><tbody>' +
        assets.map(function (item) {
          return '<tr><td class="asset-name"><strong>' + GIT.escape(item.patrimonio || item.numeroSerie) + '</strong><small>' +
            GIT.escape(item.numeroSerie) + '</small></td><td>' + GIT.escape(item.tipoNome) + '</td><td><span class="tag ' +
            (item.situacaoPatrimonial === 'ATIVO' ? 'tag--success' : 'tag--warning') + '">' + GIT.escape(item.situacaoPatrimonial.replaceAll('_', ' ')) +
            '</span></td><td>' + GIT.escape(item.setorPermanenteNome || item.localizacaoAtual || 'Sem alocação') + '</td><td><button class="table-action" data-select-asset="' +
            item.id + '">Movimentar</button></td></tr>';
        }).join('') + '</tbody></table>' : '<div class="empty-state">Nenhum ativo encontrado.</div>';
    } catch (error) { container.innerHTML = '<div class="message message--error">' + GIT.escape(error.message) + '</div>'; }
  }
  function renderDetail(asset) {
    selectedAsset = asset;
    document.getElementById('operation-asset-id').value = asset.id;
    document.getElementById('asset-detail').innerHTML = '<article class="asset-detail-card"><h3>' + GIT.escape(asset.patrimonio || asset.numeroSerie) +
      '</h3><p>' + GIT.escape(asset.tipoNome + ' · ' + asset.numeroSerie) + '</p><p>Situação: ' +
      GIT.escape(asset.situacaoPatrimonial.replaceAll('_', ' ')) + '</p><p>Local: ' + GIT.escape(asset.setorPermanenteNome || 'Sem setor permanente') + '</p></article>';
  }

  document.querySelectorAll('[data-view-target]').forEach(function (button) { button.addEventListener('click', function () { switchView(button.dataset.viewTarget); }); });
  document.querySelector('[data-go-register]').addEventListener('click', function () { switchView('register'); });
  document.getElementById('inventory-filter').addEventListener('submit', function (event) { event.preventDefault(); loadAssets(clean(GIT.formData(event.currentTarget))); });
  document.getElementById('refresh-assets').addEventListener('click', function () { loadAssets({}); });
  document.getElementById('asset-table').addEventListener('click', async function (event) {
    var button = event.target.closest('[data-select-asset]'); if (!button) return;
    try { renderDetail(await GIT.request('/v1/ativos/' + button.dataset.selectAsset)); switchView('operations'); }
    catch (error) { GIT.toast(error.message, 'error'); }
  });
  document.getElementById('asset-form').addEventListener('submit', async function (event) {
    event.preventDefault(); GIT.loading(event.submitter, true);
    try { var asset = await GIT.request('/v1/ativos', { method: 'POST', body: clean(GIT.formData(event.currentTarget)) }); event.currentTarget.reset(); renderDetail(asset); switchView('operations'); GIT.toast('Ativo cadastrado.'); }
    catch (error) { GIT.toast(error.message, 'error'); } finally { GIT.loading(event.submitter, false); }
  });
  document.getElementById('type-form').addEventListener('submit', async function (event) {
    event.preventDefault(); GIT.loading(event.submitter, true);
    try { await GIT.request('/v1/tipos-ativo', { method: 'POST', body: GIT.formData(event.currentTarget) }); event.currentTarget.reset(); await loadReferences(); GIT.toast('Tipo criado.'); }
    catch (error) { GIT.toast(error.message, 'error'); } finally { GIT.loading(event.submitter, false); }
  });
  document.getElementById('asset-detail-form').addEventListener('submit', async function (event) {
    event.preventDefault(); GIT.loading(event.submitter, true);
    try { renderDetail(await GIT.request('/v1/ativos/' + GIT.formData(event.currentTarget).assetId)); }
    catch (error) { GIT.toast(error.message, 'error'); } finally { GIT.loading(event.submitter, false); }
  });
  document.getElementById('operation-form').addEventListener('submit', async function (event) {
    event.preventDefault(); if (!selectedAsset) { GIT.toast('Selecione um ativo primeiro.', 'error'); return; }
    var values = GIT.formData(event.currentTarget), path = '/v1/ativos/' + selectedAsset.id, config = { method: 'POST' };
    if (values.operation === 'release') path += '/liberacoes';
    if (values.operation === 'allocate') { path += '/alocacoes-setor'; config.body = { setorId: values.setorId, motivo: values.motivo }; }
    if (values.operation === 'correct') { path += '/correcoes-identificacao'; config.body = { numeroSerie: values.numeroSerie, patrimonio: values.patrimonio || null, removerPatrimonio: false, motivo: values.motivo }; }
    GIT.loading(event.submitter, true);
    try { renderDetail(await GIT.request(path, config)); await loadAssets({}); GIT.toast('Movimentação registrada.'); }
    catch (error) { GIT.toast(error.message, 'error'); } finally { GIT.loading(event.submitter, false); }
  });

  GIT.auth({ origin: 'GESTAO_ATIVOS', onReady: function () { loadReferences(); loadAssets({}); } });
})();
