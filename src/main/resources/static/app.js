/* =====================================================
   app.js = frontend logic.
   Pani: buttons click cheste backend API ki fetch() request pampi, result ni screen meeda chupinchadam.
   ===================================================== */

const API = '/api';

// Login token & username browser lo (localStorage) save chestam, refresh chesina login alaage undali
let token = localStorage.getItem('token');
let username = localStorage.getItem('username');

// ---------- Small helpers ----------

// Shortcut: document.getElementById('x') -> $('x')
const $ = (id) => document.getElementById(id);

// SECURITY (XSS protection): user rasina text lo <script> unte, HTML ga kakunda plain text ga marchadam
function esc(text) {
  const div = document.createElement('div');
  div.textContent = text == null ? '' : text; // textContent HTML ni run cheyyadu
  return div.innerHTML;
}

// Date ni "YYYY-MM-DD" ga marchadam (heatmap keys tho match avvadaniki)
function fmtDate(d) {
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}

/**
 * api() = anni backend calls ki oke function.
 *  - token unte "Authorization: Bearer <token>" header add chestundi
 *  - 401 vaste (token expire) -> logout
 *  - error aithe backend {"message": "..."} ni Error ga throw chestundi
 */
async function api(path, method = 'GET', body = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const res = await fetch(API + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  if (res.status === 401 && token) {   // token undi kani reject = session expired
    logout();
    throw new Error('Session expired, please login again');
  }
  if (res.status === 204) return null; // 204 = success, body ledu

  const data = await res.json().catch(() => ({})); // body JSON kaakapothe empty object
  if (!res.ok) throw new Error(data.message || 'Something went wrong');
  return data;
}

// =====================================================
// AUTH (login / register)
// =====================================================
let isRegister = false;

// Login <-> Register form toggle
function toggleAuthMode() {
  isRegister = !isRegister;
  $('authTitle').textContent = isRegister ? 'Create your account 🚀' : 'Welcome back 👋';
  $('authBtn').textContent = isRegister ? 'Register' : 'Login';
  $('authSwitchText').textContent = isRegister ? 'Already have an account?' : 'New here?';
  $('authSwitchLink').textContent = isRegister ? 'Login' : 'Create account';
  $('authError').textContent = '';
}

async function submitAuth() {
  try {
    const data = await api('/auth/' + (isRegister ? 'register' : 'login'), 'POST', {
      username: $('authUser').value,
      password: $('authPass').value
    });
    // Token save chesi app open
    token = data.token;
    username = data.username;
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);
    $('authPass').value = '';
    showApp();
  } catch (e) {
    $('authError').textContent = e.message;
  }
}

function logout() {
  token = null;
  username = null;
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  $('appView').classList.add('hidden');
  $('userBar').classList.add('hidden');
  $('authView').classList.remove('hidden');
}

// Login ayyaka: auth form daachi, app chupinchu
function showApp() {
  $('authView').classList.add('hidden');
  $('appView').classList.remove('hidden');
  $('userBar').classList.remove('hidden');
  refreshChips();
  switchTab('decks');
}

// =====================================================
// TABS
// =====================================================
function switchTab(name) {
  // Anni tabs daachu, kavalsindi matrame chupinchu
  document.querySelectorAll('.tabview').forEach(t => t.classList.add('hidden'));
  $('tab-' + name).classList.remove('hidden');

  // Active tab highlight
  document.querySelectorAll('.tab').forEach(b =>
    b.classList.toggle('active', b.dataset.tab === name));

  // Aa tab ki data load
  if (name === 'decks') { closeDeck(); loadDecks(); }
  if (name === 'study') prepareStudy();
  if (name === 'stats') loadStats();
  if (name === 'board') loadBoard();
}

// Top bar lo Level / XP / Streak chips update
function setChips(level, xp, streak) {
  $('chipLevel').textContent = 'Lv ' + level;
  $('chipXp').textContent = xp + ' XP';
  $('chipStreak').textContent = '🔥 ' + streak;
}

async function refreshChips() {
  try {
    const s = await api('/stats');
    setChips(s.level, s.xp, s.streak);
  } catch (e) { /* ignore */ }
}

// =====================================================
// DECKS
// =====================================================
let decks = [];          // local copy of decks
let currentDeck = null;  // ippudu open chesina deck

async function loadDecks() {
  decks = await api('/decks');
  const list = $('deckList');

  if (decks.length === 0) {
    list.innerHTML = '<p class="muted center">No decks yet. Create one above 👆</p>';
    return;
  }

  // map = prathi deck ki oka HTML block, join = anni kalipi
  list.innerHTML = decks.map(d => `
    <div class="card item">
      <div>
        <h3>${esc(d.name)}</h3>
        <p>${esc(d.description)}</p>
        <p style="margin-top:6px">
          <span class="badge">${d.cardCount} cards</span>
          ${d.dueCount > 0 ? `<span class="badge due">${d.dueCount} due</span>` : '<span class="badge">all caught up ✅</span>'}
        </p>
      </div>
      <div class="btns">
        <button class="small" onclick="studyDeck(${d.id})">Study</button>
        <button class="small ghost" onclick="openDeck(${d.id})">Cards</button>
        <button class="small danger" onclick="deleteDeck(${d.id})">Delete</button>
      </div>
    </div>`).join('');
}

async function createDeck() {
  try {
    await api('/decks', 'POST', { name: $('deckName').value, description: $('deckDesc').value });
    $('deckName').value = '';
    $('deckDesc').value = '';
    $('deckError').textContent = '';
    loadDecks();
  } catch (e) {
    $('deckError').textContent = e.message;
  }
}

async function deleteDeck(id) {
  if (!confirm('Delete this deck and all its cards?')) return;
  await api('/decks/' + id, 'DELETE');
  loadDecks();
}

// ---------- Cards inside a deck ----------
async function openDeck(id) {
  currentDeck = decks.find(d => d.id === id);
  $('deckListView').classList.add('hidden');
  $('cardView').classList.remove('hidden');
  $('cardViewTitle').textContent = currentDeck.name;
  loadCards();
}

function closeDeck() {
  currentDeck = null;
  $('cardView').classList.add('hidden');
  $('deckListView').classList.remove('hidden');
}

async function loadCards() {
  const cards = await api(`/decks/${currentDeck.id}/cards`);
  $('cardList').innerHTML = cards.length === 0
    ? '<p class="muted center">No cards yet. Add your first one 👆</p>'
    : cards.map(c => `
      <div class="card item">
        <div>
          <h3>${esc(c.front)}</h3>
          <p>${esc(c.back)}</p>
          <p style="margin-top:6px">
            <span class="badge">next: ${c.dueDate}</span>
            <span class="badge">interval: ${c.intervalDays}d</span>
            <span class="badge">ease: ${c.easeFactor.toFixed(2)}</span>
          </p>
        </div>
        <button class="small danger" onclick="deleteCard(${c.id})">Delete</button>
      </div>`).join('');
}

async function addCard() {
  try {
    await api(`/decks/${currentDeck.id}/cards`, 'POST', {
      front: $('cardFront').value,
      back: $('cardBack').value
    });
    $('cardFront').value = '';
    $('cardBack').value = '';
    $('cardError').textContent = '';
    loadCards();
  } catch (e) {
    $('cardError').textContent = e.message;
  }
}

async function deleteCard(id) {
  await api('/cards/' + id, 'DELETE');
  loadCards();
}

// =====================================================
// STUDY SESSION
// =====================================================
let queue = [];        // study cheyyalsina cards (varusaga)
let totalToStudy = 0;  // session start lo entha cards
let doneCount = 0;     // correct ga complete chesina cards
let sessionXp = 0;     // e session lo vachina XP

// Study tab open chesinappudu: deck dropdown fill
async function prepareStudy() {
  decks = await api('/decks');
  $('studyDeck').innerHTML = '<option value="">All decks (due cards)</option>' +
    decks.map(d => `<option value="${d.id}">${esc(d.name)} (${d.dueCount} due)</option>`).join('');
  $('studyArea').classList.add('hidden');
  $('studyDone').classList.add('hidden');
}

// Deck list nundi "Study" button
function studyDeck(id) {
  switchTab('study');
  prepareStudy().then(() => {
    $('studyDeck').value = String(id);
    startStudy();
  });
}

async function startStudy() {
  const deckId = $('studyDeck').value;
  queue = await api('/study/due' + (deckId ? '?deckId=' + deckId : ''));
  totalToStudy = queue.length;
  doneCount = 0;
  sessionXp = 0;
  $('studyDone').classList.add('hidden');

  if (queue.length === 0) {
    $('studyArea').classList.add('hidden');
    $('studyDone').classList.remove('hidden');
    $('studySummary').textContent = 'Nothing due right now. Come back later! 🎯';
    return;
  }
  $('studyArea').classList.remove('hidden');
  showCard();
}

// Queue lo first card chupinchu
function showCard() {
  if (queue.length === 0) {
    finishStudy();
    return;
  }
  const card = queue[0];
  $('cardFrontText').textContent = card.front;  // textContent = safe (XSS ledu)
  $('cardBackText').textContent = card.back;
  $('answerBox').classList.add('hidden');
  $('rateBox').classList.add('hidden');
  $('showBtn').classList.remove('hidden');
  $('feedback').textContent = '';

  $('studyCount').textContent = `${doneCount} / ${totalToStudy} done`;
  $('studyBar').style.width = (totalToStudy ? (doneCount / totalToStudy) * 100 : 0) + '%';
}

function showAnswer() {
  $('answerBox').classList.remove('hidden');
  $('rateBox').classList.remove('hidden');
  $('showBtn').classList.add('hidden');
}

// User rating ichhaka: backend ki pampu (SM-2 + XP + streak backend lo)
async function rate(quality) {
  const card = queue[0];
  const res = await api('/study/review/' + card.id, 'POST', { quality });

  sessionXp += res.xpGained;
  setChips(res.level, res.totalXp, res.streak); // top bar live update

  if (quality < 3) {
    // Marchipoyav: ee session lone malli chupistam (queue last ki)
    queue.push(queue.shift());
  } else {
    queue.shift();  // card complete
    doneCount++;
  }

  $('feedback').textContent = `+${res.xpGained} XP   •   next review in ${res.intervalDays} day(s)`;
  // 0.7 sec aagi next card (feedback chudadaniki time)
  setTimeout(showCard, 700);
}

function finishStudy() {
  $('studyArea').classList.add('hidden');
  $('studyDone').classList.remove('hidden');
  $('studySummary').textContent = `You reviewed ${totalToStudy} card(s) and earned ${sessionXp} XP. Keep the streak alive! 🔥`;
}

// =====================================================
// STATS (tiles + XP bar + heatmap)
// =====================================================
async function loadStats() {
  const s = await api('/stats');
  setChips(s.level, s.xp, s.streak);

  $('statTiles').innerHTML = [
    [s.totalCards, 'Total cards'],
    [s.dueToday, 'Due today'],
    [s.totalReviews, 'Total reviews'],
    [s.accuracy + '%', 'Accuracy'],
    ['🔥 ' + s.streak, 'Day streak']
  ].map(t => `<div class="tile"><div class="num">${t[0]}</div><div class="lbl">${t[1]}</div></div>`).join('');

  // XP progress bar: ee level lo entha % complete
  const pct = ((s.xp - s.levelStartXp) / (s.nextLevelXp - s.levelStartXp)) * 100;
  $('levelTitle').textContent = `Level ${s.level}`;
  $('xpBar').style.width = pct + '%';
  $('xpText').textContent = `${s.xp} / ${s.nextLevelXp} XP to Level ${s.level + 1}`;

  drawHeatmap(s.heatmap);
}

// GitHub-style heatmap: 90 days, prathi day oka box. Ekkuva reviews = dark colour.
function drawHeatmap(data) {
  const cells = [];
  const today = new Date();

  // Mundu empty boxes: first day weekday ki align avvadaniki (getDay: 0=Sun ... 6=Sat)
  const start = new Date(today);
  start.setDate(today.getDate() - 89);
  for (let i = 0; i < start.getDay(); i++) {
    cells.push('<div class="cell" style="background:transparent"></div>');
  }

  // 90 days loop
  for (let i = 89; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i);
    const key = fmtDate(d);
    const count = data[key] || 0;

    // count -> colour level (0 to 4)
    const level = count === 0 ? 0 : count < 3 ? 1 : count < 6 ? 2 : count < 10 ? 3 : 4;
    cells.push(`<div class="cell l${level}" title="${key}: ${count} review(s)"></div>`);
  }
  $('heatmap').innerHTML = cells.join('');
}

// =====================================================
// LEADERBOARD
// =====================================================
async function loadBoard() {
  const rows = await api('/leaderboard');
  const medals = ['🥇', '🥈', '🥉'];

  $('board').innerHTML = rows.length === 0 ? '<p class="muted center">No players yet</p>' :
    rows.map((r, i) => `
      <div class="board-row ${r.username === username ? 'me' : ''}">
        <div class="rank">${medals[i] || (i + 1)}</div>
        <div class="name">${esc(r.username)} ${r.username === username ? '(you)' : ''}</div>
        <span class="chip">Lv ${r.level}</span>
        <span class="chip">${r.xp} XP</span>
        <span class="chip">🔥 ${r.streak}</span>
      </div>`).join('');
}

// =====================================================
// PAGE LOAD: token undi ante direct ga app open, lekapothe login screen
// =====================================================
if (token) {
  showApp();
}
