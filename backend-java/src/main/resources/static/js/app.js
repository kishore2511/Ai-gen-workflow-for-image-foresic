const tokenKey = "forensic_jwt";

function setToken(token) {
  localStorage.setItem(tokenKey, token);
}

function getToken() {
  return localStorage.getItem(tokenKey);
}

function logout() {
  localStorage.removeItem(tokenKey);
  window.location.href = '/login';
}

async function register() {
  await authCall('/api/auth/register');
}

async function login() {
  await authCall('/api/auth/login');
}

async function authCall(url) {
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;
  const msg = document.getElementById('msg');
  msg.innerText = '';

  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  if (!res.ok) {
    msg.innerText = await res.text();
    return;
  }

  const data = await res.json();
  setToken(data.token);
  window.location.href = '/upload';
}

async function uploadImage() {
  const fileInput = document.getElementById('imageFile');
  const result = document.getElementById('result');
  result.innerText = 'Processing...';

  if (fileInput.files.length === 0) {
    result.innerText = 'Select an image first.';
    return;
  }

  const fd = new FormData();
  fd.append('file', fileInput.files[0]);

  const res = await fetch('/api/analysis/upload', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${getToken()}` },
    body: fd
  });

  if (!res.ok) {
    result.innerText = `Failed: ${await res.text()}`;
    return;
  }

  const data = await res.json();
  result.innerText = JSON.stringify(data, null, 2);
  await loadHistory();
}

async function loadHistory() {
  const history = document.getElementById('history');
  if (!history) return;

  const res = await fetch('/api/analysis/history', {
    headers: { 'Authorization': `Bearer ${getToken()}` }
  });

  if (!res.ok) {
    history.innerHTML = '<div class="text-danger">Login required.</div>';
    return;
  }

  const rows = await res.json();
  if (rows.length === 0) {
    history.innerHTML = '<div class="text-muted">No uploads yet.</div>';
    return;
  }

  history.innerHTML = `
<table class="table table-sm table-striped">
<thead><tr><th>ID</th><th>Label</th><th>Confidence</th><th>Stored Key</th><th>Created</th></tr></thead>
<tbody>
${rows.map(r => `<tr><td>${r.id}</td><td>${r.predictionLabel}</td><td>${r.confidence.toFixed(4)}</td><td>${r.storageKey}</td><td>${r.createdAt}</td></tr>`).join('')}
</tbody>
</table>`;
}
