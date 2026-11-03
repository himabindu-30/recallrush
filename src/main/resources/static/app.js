/* =====================================================
   app.js - frontend logic.
   Handles button clicks, calls the backend API with fetch(), and renders the results.
   ===================================================== */

const API = '/api';

// The login token and username are kept in localStorage so the user stays logged in after a refresh
let token = localStorage.getItem('token');
let username = localStorage.getItem('username');

// ---------- Small helpers ----------

// Shortcut: document.getElementById('x') -> $('x')
const $ = (id) => document.getElementById(id);

// SECURITY (XSS protection): converts user-supplied text to safe HTML,
// so a value like <script>...</script> is displayed as text and never executed.
function esc(text) {
  const div = document.createElement('div');
  div.textContent = text == null ? '' : text; // textContent never interprets HTML
  return div.innerHTML;
}

// Formats a Date as "YYYY-MM-DD" (matches the heatmap keys from the backend)
function fmtDate(d) {
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}

/**
 * api() is the single function used for every backend call.
 *  - Adds the "Authorization: Bearer <token>" header when a token exists
 *  - On 401 (expired or invalid token) it logs the user out
 *  - On errors it throws an Error carrying the backend's {"message": "..."} text
 */
async function api(path, method = 'GET', body = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const res = await fetch(API + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  if (res.status === 401 && token) {   // a token was sent but rejected: the session expired
    logout();
    throw new Error('Session expired, please login again');
  }
  if (res.status === 204) return null; // 204 = success with no body

  const data = await res.json().catch(() => ({})); // fall back to an empty object if the body is not JSON
  if (!res.ok) throw new Error(data.message || 'Something went wrong');
  return data;
}

// =====================================================
// AUTH (login / register)
// =====================================================
let isRegister = false;

// Switches the form between Login and Register mode
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
    // Save the token, then open the app
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

// After login: hide the auth form and show the app
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
  // Hide every tab, then show only the selected one
  document.querySelectorAll('.tabview').forEach(t => t.classList.add('hidden'));
  $('tab-' + name).classList.remove('hidden');

  // Highlight the active tab button
  document.querySelectorAll('.tab').forEach(b =>
    b.classList.toggle('active', b.dataset.tab === name));

  // Load the data for that tab
  if (name === 'decks') { closeDeck(); loadDecks(); }
  if (name === 'study') prepareStudy();
  if (name === 'stats') loadStats();
  if (name === 'board') loadBoard();
}

// Updates the Level / XP / Streak chips in the top bar
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
let decks = [];          // local copy of the user's decks
let currentDeck = null;  // the deck currently open

async function loadDecks() {
  decks = await api('/decks');
  const list = $('deckList');

  if (decks.length === 0) {
    list.innerHTML = '<p class="muted center">No decks yet. Create one above 👆</p>';
    return;
  }

  // map builds one HTML block per deck; join concatenates them
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
let queue = [];        // cards still to study in this session
let totalToStudy = 0;  // number of cards when the session started
let doneCount = 0;     // cards completed (recalled) so far
let sessionXp = 0;     // XP earned in this session

// Runs when the Study tab opens: fills the deck dropdown
async function prepareStudy() {
  decks = await api('/decks');
  $('studyDeck').innerHTML = '<option value="">All decks (due cards)</option>' +
    decks.map(d => `<option value="${d.id}">${esc(d.name)} (${d.dueCount} due)</option>`).join('');
  $('studyArea').classList.add('hidden');
  $('studyDone').classList.add('hidden');
}

// "Study" button on a deck in the deck list
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

// Shows the first card in the queue
function showCard() {
  if (queue.length === 0) {
    finishStudy();
    return;
  }
  const card = queue[0];
  $('cardFrontText').textContent = card.front;  // textContent is safe against XSS
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

// Sends the rating to the backend (SM-2, XP and streak are all computed there)
async function rate(quality) {
  const card = queue[0];
  const res = await api('/study/review/' + card.id, 'POST', { quality });

  sessionXp += res.xpGained;
  setChips(res.level, res.totalXp, res.streak); // update the top bar immediately

  if (quality < 3) {
    // Forgotten: show it again later in this same session (move to the end of the queue)
    queue.push(queue.shift());
  } else {
    queue.shift();  // card completed
    doneCount++;
  }

  $('feedback').textContent = `+${res.xpGained} XP   •   next review in ${res.intervalDays} day(s)`;
  // Wait 0.7 seconds so the feedback is readable, then show the next card
  setTimeout(showCard, 700);
}

function finishStudy() {
  $('studyArea').classList.add('hidden');
  $('studyDone').classList.remove('hidden');
  $('studySummary').textContent = `You reviewed ${totalToStudy} card(s) and earned ${sessionXp} XP. Keep the streak alive! 🔥`;
}

// =====================================================
// STATS (tiles, XP bar, heatmap)
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

  // XP progress bar: percentage of the current level completed
  const pct = ((s.xp - s.levelStartXp) / (s.nextLevelXp - s.levelStartXp)) * 100;
  $('levelTitle').textContent = `Level ${s.level}`;
  $('xpBar').style.width = pct + '%';
  $('xpText').textContent = `${s.xp} / ${s.nextLevelXp} XP to Level ${s.level + 1}`;

  drawHeatmap(s.heatmap);
}

// GitHub-style heatmap: one box per day for 90 days; more reviews = darker color
function drawHeatmap(data) {
  const cells = [];
  const today = new Date();

  // Leading empty boxes so the first day lines up with its weekday (getDay: 0 = Sunday ... 6 = Saturday)
  const start = new Date(today);
  start.setDate(today.getDate() - 89);
  for (let i = 0; i < start.getDay(); i++) {
    cells.push('<div class="cell" style="background:transparent"></div>');
  }

  // Loop over the 90 days
  for (let i = 89; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i);
    const key = fmtDate(d);
    const count = data[key] || 0;

    // Map the review count to a color level from 0 to 4
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
// PAGE LOAD: if a token exists, open the app directly; otherwise show the login screen
// =====================================================
if (token) {
  showApp();
}
