(function () {
  'use strict';

  var STORAGE = {
    apiUrl: 'gerenciamentoit.apiUrl',
    token: 'gerenciamentoit.token',
    session: 'gerenciamentoit.session'
  };

  var memory = {};

  function storageGet(key) {
    try {
      return window.localStorage.getItem(key);
    } catch (_) {
      return memory[key] || null;
    }
  }

  function storageSet(key, value) {
    memory[key] = value;
    try {
      window.localStorage.setItem(key, value);
    } catch (_) {
      // O modo file:// pode restringir armazenamento em alguns navegadores.
    }
  }

  function storageRemove(key) {
    delete memory[key];
    try {
      window.localStorage.removeItem(key);
    } catch (_) {
      // Mantém o fallback em memória quando o armazenamento não está disponível.
    }
  }

  function normalizeBaseUrl(value) {
    return String(value || 'http://localhost:8080/api').trim().replace(/\/+$/, '');
  }

  function getBaseUrl() {
    return normalizeBaseUrl(storageGet(STORAGE.apiUrl));
  }

  function setBaseUrl(value) {
    storageSet(STORAGE.apiUrl, normalizeBaseUrl(value));
  }

  function getToken() {
    return storageGet(STORAGE.token) || '';
  }

  function getSession() {
    var raw = storageGet(STORAGE.session);
    if (!raw) return null;

    try {
      return JSON.parse(raw);
    } catch (_) {
      return null;
    }
  }

  function saveSession(payload) {
    storageSet(STORAGE.token, payload.token);
    storageSet(STORAGE.session, JSON.stringify(payload));
    window.dispatchEvent(new CustomEvent('git:session-changed'));
  }

  function clearSession() {
    storageRemove(STORAGE.token);
    storageRemove(STORAGE.session);
    window.dispatchEvent(new CustomEvent('git:session-changed'));
  }

  function navigationHash() {
    var session = getSession();
    var token = getToken();
    if (!session || !token) return '';

    return (
      '#git-session=' +
      encodeURIComponent(
        JSON.stringify({
          apiUrl: getBaseUrl(),
          token: token,
          session: session
        })
      )
    );
  }

  function importNavigationHash() {
    var prefix = '#git-session=';
    if (window.location.hash.indexOf(prefix) !== 0) return;

    try {
      var payload = JSON.parse(
        decodeURIComponent(window.location.hash.slice(prefix.length))
      );
      if (payload.apiUrl) setBaseUrl(payload.apiUrl);
      if (payload.token && payload.session) {
        storageSet(STORAGE.token, payload.token);
        storageSet(STORAGE.session, JSON.stringify(payload.session));
      }
      try {
        window.history.replaceState(
          null,
          document.title,
          window.location.pathname + window.location.search
        );
      } catch (_) {
        // Alguns navegadores não permitem replaceState em file://.
      }
    } catch (_) {
      // Um hash inválido é ignorado e a página continua utilizável.
    }
  }

  function createCorrelationId() {
    if (window.crypto && window.crypto.randomUUID) {
      return window.crypto.randomUUID();
    }
    return 'portal-' + Date.now() + '-' + Math.random().toString(16).slice(2);
  }

  function parseResponseBody(text, contentType) {
    if (!text) return null;
    if (contentType && contentType.indexOf('application/json') >= 0) {
      try {
        return JSON.parse(text);
      } catch (_) {
        return text;
      }
    }

    try {
      return JSON.parse(text);
    } catch (_) {
      return text;
    }
  }

  async function request(path, options) {
    var config = options || {};
    var url = /^https?:\/\//i.test(path)
      ? path
      : getBaseUrl() + '/' + String(path || '').replace(/^\/+/, '');
    var headers = Object.assign(
      {
        Accept: 'application/json',
        'X-Correlation-Id': createCorrelationId()
      },
      config.headers || {}
    );

    if (config.auth !== false && getToken()) {
      headers.Authorization = 'Bearer ' + getToken();
    }

    var body = config.body;
    if (body !== undefined && body !== null && typeof body !== 'string') {
      headers['Content-Type'] = 'application/json';
      body = JSON.stringify(body);
    }

    var startedAt = performance.now();
    var response;

    try {
      response = await fetch(url, {
        method: config.method || 'GET',
        headers: headers,
        body: body
      });
    } catch (error) {
      var networkError = new Error(
        'Não foi possível acessar a API. Confirme se ela está executando e se o endereço está correto.'
      );
      networkError.cause = error;
      networkError.network = true;
      networkError.url = url;
      throw networkError;
    }

    var text = await response.text();
    var data = parseResponseBody(text, response.headers.get('content-type'));
    var result = {
      ok: response.ok,
      status: response.status,
      statusText: response.statusText,
      data: data,
      elapsed: Math.round(performance.now() - startedAt),
      url: url,
      method: config.method || 'GET',
      headers: {
        correlationId: response.headers.get('X-Correlation-Id'),
        location: response.headers.get('Location')
      }
    };

    if (!response.ok) {
      var message =
        data && (data.mensagem || data.message || data.error)
          ? data.mensagem || data.message || data.error
          : 'A API retornou o status ' + response.status + '.';
      var apiError = new Error(message);
      apiError.result = result;
      throw apiError;
    }

    return result;
  }

  async function login(matricula, origemAplicacao) {
    var result = await request('/v1/sessoes', {
      method: 'POST',
      auth: false,
      body: {
        matricula: matricula,
        origemAplicacao: origemAplicacao || 'API'
      }
    });
    saveSession(result.data);
    return result;
  }

  async function logout() {
    try {
      if (getToken()) {
        await request('/v1/sessoes/atual', { method: 'DELETE' });
      }
    } finally {
      clearSession();
    }
  }

  async function health() {
    var base = getBaseUrl();
    var root = base.replace(/\/api\/?$/, '');
    return request(root + '/api/actuator/health', { auth: false });
  }

  function query(params) {
    var search = new URLSearchParams();
    Object.keys(params || {}).forEach(function (key) {
      var value = params[key];
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        search.set(key, value);
      }
    });
    var serialized = search.toString();
    return serialized ? '?' + serialized : '';
  }

  window.GerenciamentoITApi = {
    request: request,
    login: login,
    logout: logout,
    health: health,
    query: query,
    getBaseUrl: getBaseUrl,
    setBaseUrl: setBaseUrl,
    getToken: getToken,
    getSession: getSession,
    clearSession: clearSession,
    navigationHash: navigationHash
  };

  importNavigationHash();
})();
