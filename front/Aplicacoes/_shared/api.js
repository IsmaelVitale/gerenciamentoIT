(function () {
  'use strict';

  var PREFIX = 'git.final.';
  var memory = {};

  function get(key) {
    try {
      return localStorage.getItem(PREFIX + key);
    } catch (_) {
      return memory[key] || null;
    }
  }

  function set(key, value) {
    memory[key] = value;
    try {
      localStorage.setItem(PREFIX + key, value);
    } catch (_) {
      // Alguns navegadores restringem armazenamento em file://.
    }
  }

  function remove(key) {
    delete memory[key];
    try {
      localStorage.removeItem(PREFIX + key);
    } catch (_) {
      // Mantém o fallback em memória.
    }
  }

  function apiUrl() {
    return (get('apiUrl') || 'http://localhost:8080/api').replace(/\/+$/, '');
  }

  function token() {
    return get('token') || '';
  }

  function session() {
    try {
      return JSON.parse(get('session') || 'null');
    } catch (_) {
      return null;
    }
  }

  function roles() {
    var current = session();
    return current && current.usuario && Array.isArray(current.usuario.papeis)
      ? current.usuario.papeis
      : [];
  }

  function permissions() {
    var current = session();
    return current && current.usuario && Array.isArray(current.usuario.permissoes)
      ? current.usuario.permissoes
      : [];
  }

  function hasRole(role) {
    return roles().indexOf(role) >= 0;
  }

  function hasPermission(permission) {
    return permissions().indexOf(permission) >= 0;
  }

  function roleLabel(user) {
    var userRoles = user && Array.isArray(user.papeis) ? user.papeis : [];
    if (userRoles.indexOf('GESTOR_TI') >= 0) return 'T.I. · Gestor';
    if (userRoles.indexOf('ANALISTA_TI') >= 0) return 'T.I. · Analista';
    if (userRoles.indexOf('SUPERVISOR') >= 0) return 'Supervisor';
    if (userRoles.indexOf('LIDER') >= 0) return 'Líder';
    return 'Usuário';
  }

  function applyAccessRules(user) {
    var userPermissions = user && Array.isArray(user.permissoes) ? user.permissoes : [];
    var userRoles = user && Array.isArray(user.papeis) ? user.papeis : [];

    document.querySelectorAll('[data-permission]').forEach(function (element) {
      var required = element.dataset.permission.split(',').map(function (item) { return item.trim(); });
      var allowed = required.some(function (permission) { return userPermissions.indexOf(permission) >= 0; });
      element.hidden = !allowed;
    });

    document.querySelectorAll('[data-roles]').forEach(function (element) {
      var required = element.dataset.roles.split(',').map(function (item) { return item.trim(); });
      var allowed = required.some(function (role) { return userRoles.indexOf(role) >= 0; });
      element.hidden = !allowed;
    });
  }

  function correlationId() {
    return window.crypto && window.crypto.randomUUID
      ? window.crypto.randomUUID()
      : 'web-' + Date.now() + '-' + Math.random().toString(16).slice(2);
  }

  async function request(path, options) {
    var config = options || {};
    var headers = Object.assign(
      { Accept: 'application/json', 'X-Correlation-Id': correlationId() },
      config.headers || {}
    );
    if (config.auth !== false && token()) headers.Authorization = 'Bearer ' + token();

    var body = config.body;
    if (body !== undefined && body !== null && typeof body !== 'string') {
      headers['Content-Type'] = 'application/json';
      body = JSON.stringify(body);
    }

    var response;
    try {
      response = await fetch(
        /^https?:/i.test(path) ? path : apiUrl() + '/' + path.replace(/^\/+/, ''),
        { method: config.method || 'GET', headers: headers, body: body }
      );
    } catch (error) {
      var networkError = new Error('Não foi possível acessar a API local.');
      networkError.network = true;
      throw networkError;
    }

    var text = await response.text();
    var data = null;
    if (text) {
      try { data = JSON.parse(text); } catch (_) { data = text; }
    }
    if (!response.ok) {
      var apiError = new Error(
        data && (data.mensagem || data.message)
          ? data.mensagem || data.message
          : 'Não foi possível concluir esta ação.'
      );
      apiError.status = response.status;
      apiError.data = data;
      throw apiError;
    }
    return data;
  }

  async function login(matricula, origem) {
    var data = await request('/v1/sessoes', {
      method: 'POST',
      auth: false,
      body: { matricula: matricula, origemAplicacao: origem }
    });
    set('token', data.token);
    set('session', JSON.stringify(data));
    return data;
  }

  async function logout() {
    try {
      if (token()) await request('/v1/sessoes/atual', { method: 'DELETE' });
    } catch (_) {
      // A sessão local será removida mesmo com a API indisponível.
    }
    remove('token');
    remove('session');
  }

  function query(values) {
    var params = new URLSearchParams();
    Object.keys(values || {}).forEach(function (key) {
      var value = values[key];
      if (value !== '' && value !== null && value !== undefined) params.set(key, value);
    });
    return params.toString() ? '?' + params.toString() : '';
  }

  function formData(form) {
    var values = {};
    new FormData(form).forEach(function (value, key) {
      values[key] = typeof value === 'string' ? value.trim() : value;
    });
    form.querySelectorAll('input[type=checkbox]').forEach(function (input) {
      values[input.name] = input.checked;
    });
    return values;
  }

  function escape(value) {
    return String(value === undefined || value === null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function date(value) {
    if (!value) return '—';
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date(value));
  }

  function toast(message, kind) {
    var region = document.getElementById('toast-region');
    if (!region) return;
    var item = document.createElement('div');
    item.className = 'toast toast--' + (kind || 'success');
    item.textContent = message;
    region.appendChild(item);
    setTimeout(function () { item.remove(); }, 3800);
  }

  function loading(button, enabled) {
    if (!button) return;
    if (enabled) {
      button.dataset.label = button.textContent;
      button.textContent = 'Aguarde...';
      button.disabled = true;
    } else {
      button.textContent = button.dataset.label || button.textContent;
      button.disabled = false;
    }
  }

  function auth(options) {
    var config = options || {};
    var dialog = document.getElementById('login-dialog');
    var form = document.getElementById('login-form');
    var userArea = document.getElementById('current-user');
    var logoutButton = document.getElementById('logout-button');

    function paint() {
      var current = session();
      var user = current && current.usuario;
      if (userArea) {
        userArea.innerHTML = user
          ? '<strong>' + escape(user.nome) + '</strong><small>' + escape(roleLabel(user)) + ' · ' + escape(user.matricula) + '</small>'
          : '<strong>Sem sessão</strong><small>Entre para continuar</small>';
      }
      document.body.dataset.authenticated = user ? 'true' : 'false';
      applyAccessRules(user);
      if (!user && dialog && !dialog.open) dialog.showModal();
      if (user && config.onReady) config.onReady(user);
    }

    if (form) {
      form.addEventListener('submit', async function (event) {
        event.preventDefault();
        var values = formData(form);
        var button = form.querySelector('button[type=submit]');
        loading(button, true);
        try {
          set('apiUrl', values.apiUrl || 'http://localhost:8080/api');
          await login(values.matricula, config.origin || 'API');
          dialog.close();
          toast('Bem-vindo ao GerenciamentoIT.');
          paint();
        } catch (error) {
          toast(error.message, 'error');
        } finally {
          loading(button, false);
        }
      });
    }

    if (logoutButton) {
      logoutButton.addEventListener('click', async function () {
        await logout();
        paint();
      });
    }

    paint();
  }

  window.GIT = {
    request: request,
    login: login,
    logout: logout,
    query: query,
    formData: formData,
    escape: escape,
    date: date,
    toast: toast,
    loading: loading,
    auth: auth,
    session: session,
    token: token,
    apiUrl: apiUrl,
    roles: roles,
    permissions: permissions,
    hasRole: hasRole,
    hasPermission: hasPermission,
    roleLabel: roleLabel
  };
})();
