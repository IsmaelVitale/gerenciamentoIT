// ==========================================
// CONFIGURAÇÕES GLOBAIS
// ==========================================
const API_BASE_URL = '/api';

// ==========================================
// UTILITÁRIO DE ALERTAS (TOAST)
// ==========================================
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return; // Segurança caso a página não tenha o container

    const toast = document.createElement('div');
    let bgClass = type === 'success' ? 'bg-green-600' : (type === 'error' ? 'bg-red-600' : (type === 'warning' ? 'bg-yellow-500' : 'bg-blue-600'));

    toast.className = `${bgClass} text-white px-4 py-3 rounded shadow-lg flex items-center gap-2 transform transition-all duration-300 translate-x-full opacity-0 z-50`;
    toast.innerHTML = `<i class="ph ph-info text-xl"></i> <span>${message}</span>`;

    container.appendChild(toast);
    requestAnimationFrame(() => toast.classList.remove('translate-x-full', 'opacity-0'));
    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-x-full');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ==========================================
// SEGURANÇA E SESSÃO
// ==========================================
function verificarSessao() {
    const token = sessionStorage.getItem('admin_token_id');
    const nome = sessionStorage.getItem('admin_token_nome');
    const overlay = document.getElementById('security-overlay');
    const loginScanner = document.getElementById('login-scanner');

    if (token && nome) {
        // Usuário logado: libera a interface e esconde o lock screen
        if (overlay) overlay.classList.add('hidden');
        const nameDisplay = document.getElementById('admin-logged-name');
        if (nameDisplay) nameDisplay.innerText = nome;

        // Dispara um evento customizado para avisar que a página pode carregar os seus dados específicos
        document.dispatchEvent(new Event('AdminSessionValid'));
    } else {
        // Não está logado: garante que o lock screen aparece
        if (overlay) {
            overlay.classList.remove('hidden');
            if (loginScanner) loginScanner.focus();
        } else {
            // Se a página não tiver lock screen (ex: acessou inventario.html direto), expulsa para o index do admin
            window.location.href = 'chamados.html';
        }
    }
}

// ==========================================
// LOGIN (LOCK SCREEN)
// ==========================================
async function autenticarAdmin(matricula) {
    const loginScanner = document.getElementById('login-scanner');
    const loginLoading = document.getElementById('login-loading');

    try {
        const response = await fetch(`${API_BASE_URL}/usuarios/${matricula}`);
        if (!response.ok) throw new Error("Crachá inválido ou não encontrado.");

        const usuario = await response.json();
        if (usuario.role !== 'ADMIN') throw new Error(`Acesso negado. Perfil: ${usuario.role}. Requisito: ADMIN.`);

        sessionStorage.setItem('admin_token_id', usuario.matricula);
        sessionStorage.setItem('admin_token_nome', usuario.nome);

        showToast(`Bem-vindo, ${usuario.nome}!`, "success");
        setTimeout(() => window.location.reload(), 1000); // Recarrega para aplicar a sessão
    } catch (error) {
        showToast(error.message, "error");
        if (loginScanner) {
            loginScanner.classList.add('ring-4', 'ring-red-500');
            setTimeout(() => loginScanner.classList.remove('ring-4', 'ring-red-500'), 500);
        }
    } finally {
        if (loginScanner) loginScanner.disabled = false;
        if (loginLoading) loginLoading.classList.add('hidden');
        if (loginScanner) loginScanner.focus();
    }
}

// Event Listener do Scanner de Login
document.addEventListener('DOMContentLoaded', () => {
    verificarSessao();

    const loginScanner = document.getElementById('login-scanner');
    if (loginScanner) {
        loginScanner.addEventListener('keypress', async function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const matricula = this.value.trim().toUpperCase();
                if (matricula !== '') {
                    this.value = '';
                    this.disabled = true;
                    document.getElementById('login-loading').classList.remove('hidden');
                    await autenticarAdmin(matricula);
                }
            }
        });
    }
});

function efetuarLogout() {
    sessionStorage.clear();
    showToast("Sessão encerrada. Redirecionando...", "info");
    setTimeout(() => { window.location.href = '../index.html'; }, 800);
}
