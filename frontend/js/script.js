/* ===================================================
   SecureBank - Main JavaScript
   =================================================== */

const API_BASE_URL = 'http://localhost:8080';

/* ===== USER HELPERS ===== */
function getCurrentUser() {
  try {
    const data = localStorage.getItem('currentUser');
    return data ? JSON.parse(data) : null;
  } catch {
    return null;
  }
}

function setCurrentUser(user) {
  localStorage.setItem('currentUser', JSON.stringify(user));
}

function logout() {
  localStorage.removeItem('currentUser');
  window.location.href = 'index.html';
}

function requireAuth() {
  const user = getCurrentUser();
  if (!user) {
    window.location.href = 'index.html';
    return null;
  }
  return user;
}

function requireAdmin() {
  const user = getCurrentUser();
  if (!user) { window.location.href = 'index.html'; return null; }
  if (user.role !== 'ADMIN') { window.location.href = 'dashboard.html'; return null; }
  return user;
}

/* ===== FORMATTING HELPERS ===== */
function formatCurrency(amount) {
  if (amount === null || amount === undefined || isNaN(amount)) return '$0.00';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  if (isNaN(d)) return dateStr;
  return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatDateTime(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  if (isNaN(d)) return dateStr;
  return d.toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

function formatAccountNumber(num) {
  if (!num) return '—';
  const s = String(num);
  return s.replace(/(.{4})/g, '$1 ').trim();
}

function getInitials(firstName, lastName) {
  return ((firstName || '')[0] || '') + ((lastName || '')[0] || '');
}

function getTransactionIcon(type) {
  const map = {
    DEPOSIT: '⬇️',
    WITHDRAWAL: '⬆️',
    TRANSFER_IN: '↩️',
    TRANSFER_OUT: '↪️',
    LOAN_REPAYMENT: '💳',
    LOAN_DISBURSEMENT: '🏦'
  };
  return map[type] || '💱';
}

function getTransactionClass(type) {
  if (['DEPOSIT', 'TRANSFER_IN', 'LOAN_DISBURSEMENT'].includes(type)) return 'credit';
  return 'debit';
}

function getLoanStatusBadge(status) {
  const map = {
    PENDING: '<span class="badge badge-warning">Pending</span>',
    APPROVED: '<span class="badge badge-success">Approved</span>',
    REJECTED: '<span class="badge badge-danger">Rejected</span>',
    ACTIVE: '<span class="badge badge-primary">Active</span>',
    CLOSED: '<span class="badge badge-secondary">Closed</span>',
    DEFAULTED: '<span class="badge badge-danger">Defaulted</span>'
  };
  return map[status] || `<span class="badge badge-secondary">${status}</span>`;
}

function getAccountTypeBadge(type) {
  const map = {
    SAVINGS: '<span class="badge badge-success">Savings</span>',
    CHECKING: '<span class="badge badge-primary">Checking</span>',
    BUSINESS: '<span class="badge badge-info">Business</span>',
    FIXED_DEPOSIT: '<span class="badge badge-warning">Fixed Deposit</span>'
  };
  return map[type] || `<span class="badge badge-secondary">${type}</span>`;
}

/* ===== API HELPER ===== */
async function makeRequest(method, endpoint, data = null) {
  const url = `${API_BASE_URL}${endpoint}`;
  const options = {
    method: method.toUpperCase(),
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }
  };

  if (data && ['POST', 'PUT', 'PATCH'].includes(options.method)) {
    options.body = JSON.stringify(data);
  }

  const response = await fetch(url, options);

  let result;
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    result = await response.json();
  } else {
    result = await response.text();
  }

  if (!response.ok) {
    const msg = (typeof result === 'object' && result !== null)
      ? (result.message || result.error || JSON.stringify(result))
      : (result || `Request failed (${response.status})`);
    throw new Error(msg);
  }

  return result;
}

/* ===== ALERT HELPER ===== */
function showAlert(containerId, message, type = 'info') {
  const container = document.getElementById(containerId);
  if (!container) return;

  const icons = { success: '✅', danger: '❌', warning: '⚠️', info: 'ℹ️' };
  const icon = icons[type] || icons.info;

  container.innerHTML = `
    <div class="alert alert-${type}">
      <span class="alert-icon">${icon}</span>
      <span>${message}</span>
      <button class="alert-close" onclick="this.parentElement.remove()">✕</button>
    </div>`;
  container.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function clearAlert(containerId) {
  const container = document.getElementById(containerId);
  if (container) container.innerHTML = '';
}

/* ===== LOADING HELPERS ===== */
function setLoading(btnId, loading, text = 'Loading...') {
  const btn = document.getElementById(btnId);
  if (!btn) return;
  if (loading) {
    btn.dataset.originalText = btn.innerHTML;
    btn.innerHTML = `<span class="spinner"></span> ${text}`;
    btn.disabled = true;
  } else {
    btn.innerHTML = btn.dataset.originalText || btn.innerHTML;
    btn.disabled = false;
  }
}

/* ===== ACCOUNT LOADING ===== */
async function loadUserAccounts(selectId, userId) {
  const select = document.getElementById(selectId);
  if (!select) return [];

  try {
    const accounts = await makeRequest('GET', `/api/accounts/user/${userId}`);
    select.innerHTML = '<option value="">Select account</option>';
    (accounts || []).forEach(acc => {
      const opt = document.createElement('option');
      opt.value = acc.id;
      opt.dataset.balance = acc.balance;
      opt.dataset.accountNumber = acc.accountNumber;
      opt.textContent = `${acc.accountType} — ${formatAccountNumber(acc.accountNumber)} (${formatCurrency(acc.balance)})`;
      select.appendChild(opt);
    });
    return accounts || [];
  } catch (e) {
    select.innerHTML = '<option value="">Failed to load accounts</option>';
    return [];
  }
}

/* ===== NAV HELPERS ===== */
function initNav() {
  const user = getCurrentUser();

  // Hamburger toggle
  const hamburger = document.querySelector('.hamburger');
  const navLinks = document.querySelector('.nav-links');
  if (hamburger && navLinks) {
    hamburger.addEventListener('click', () => {
      hamburger.classList.toggle('open');
      navLinks.classList.toggle('nav-open');
    });
    // Close on link click
    navLinks.querySelectorAll('.nav-link').forEach(link => {
      link.addEventListener('click', () => {
        hamburger.classList.remove('open');
        navLinks.classList.remove('nav-open');
      });
    });
    // Close on outside click
    document.addEventListener('click', (e) => {
      if (!hamburger.contains(e.target) && !navLinks.contains(e.target)) {
        hamburger.classList.remove('open');
        navLinks.classList.remove('nav-open');
      }
    });
  }

  // Set active link
  const currentPage = window.location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.nav-link').forEach(link => {
    const href = link.getAttribute('href') || '';
    if (href === currentPage || href.endsWith(currentPage)) {
      link.classList.add('active');
    }
  });

  // Set user info in nav
  if (user) {
    const navAvatar = document.getElementById('navAvatar');
    const navUserName = document.getElementById('navUserName');
    if (navAvatar) navAvatar.textContent = getInitials(user.firstName, user.lastName);
    if (navUserName) navUserName.textContent = user.firstName || 'User';
  }

  // Logout buttons
  document.querySelectorAll('.logout-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      if (confirm('Are you sure you want to logout?')) logout();
    });
  });
}

/* ===== TABS HELPER ===== */
function initTabs(containerSelector) {
  const container = document.querySelector(containerSelector) || document;
  container.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const tabGroup = btn.dataset.tabGroup || 'default';
      const target = btn.dataset.tab;

      container.querySelectorAll(`.tab-btn[data-tab-group="${tabGroup}"]`).forEach(b => b.classList.remove('active'));
      container.querySelectorAll(`.tab-content[data-tab-group="${tabGroup}"]`).forEach(c => c.classList.remove('active'));

      btn.classList.add('active');
      const targetEl = container.querySelector(`.tab-content[data-tab="${target}"][data-tab-group="${tabGroup}"]`);
      if (targetEl) targetEl.classList.add('active');
    });
  });
}

/* ===== FORM VALIDATION ===== */
function validateForm(formId, rules) {
  const form = document.getElementById(formId);
  if (!form) return false;

  let valid = true;
  rules.forEach(rule => {
    const field = form.querySelector(`[name="${rule.name}"], #${rule.name}`);
    if (!field) return;

    const val = field.value.trim();
    let error = '';

    if (rule.required && !val) error = rule.requiredMsg || 'This field is required.';
    else if (rule.minLength && val.length < rule.minLength) error = `Minimum ${rule.minLength} characters required.`;
    else if (rule.maxLength && val.length > rule.maxLength) error = `Maximum ${rule.maxLength} characters allowed.`;
    else if (rule.pattern && !rule.pattern.test(val)) error = rule.patternMsg || 'Invalid format.';
    else if (rule.min && parseFloat(val) < rule.min) error = `Minimum value is ${rule.min}.`;
    else if (rule.match) {
      const matchField = form.querySelector(`#${rule.match}`);
      if (matchField && val !== matchField.value.trim()) error = rule.matchMsg || 'Values do not match.';
    } else if (rule.custom) {
      error = rule.custom(val, form) || '';
    }

    field.classList.toggle('is-invalid', !!error);
    field.classList.toggle('is-valid', !error && !!val);

    let feedback = field.parentElement.querySelector('.invalid-feedback');
    if (!feedback) {
      feedback = document.createElement('div');
      feedback.className = 'invalid-feedback';
      field.parentElement.appendChild(feedback);
    }
    feedback.textContent = error;
    if (error) valid = false;
  });

  return valid;
}

function clearValidation(formId) {
  const form = document.getElementById(formId);
  if (!form) return;
  form.querySelectorAll('.is-invalid, .is-valid').forEach(el => {
    el.classList.remove('is-invalid', 'is-valid');
  });
  form.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');
}

/* ===== MINI CHART (Pure CSS bars) ===== */
function renderMiniBar(containerId, data) {
  const container = document.getElementById(containerId);
  if (!container || !data || !data.length) return;

  const max = Math.max(...data.map(d => d.value || 0));
  container.innerHTML = data.map(d => {
    const pct = max > 0 ? Math.round((d.value / max) * 100) : 0;
    return `<div class="mini-bar-item" title="${d.label}: ${formatCurrency(d.value)}">
      <div class="mini-bar" style="height:${pct}%"></div>
    </div>`;
  }).join('');
}

/* ===== DOM READY ===== */
document.addEventListener('DOMContentLoaded', () => {
  initNav();
  initTabs('body');
});
