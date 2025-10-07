const boardEl = document.getElementById('board');
const sizeEl = document.getElementById('size');
const newGameEl = document.getElementById('newGame');
const solveEl = document.getElementById('solve');
const statusEl = document.getElementById('status');
const stateInputEl = document.getElementById('stateInput');
const loadStateEl = document.getElementById('loadState');

let n = 4;
let tiles = []; // 0 represents blank
let animating = false;
let worker = null;

function setGrid(n) {
  boardEl.style.gridTemplateColumns = `repeat(${n}, 1fr)`;
  boardEl.style.gridTemplateRows = `repeat(${n}, 1fr)`;
}

function goalState(n) {
  const arr = Array.from({ length: n * n }, (_, i) => (i + 1) % (n * n));
  return arr;
}

function isSolvable(state, n) {
  const inv = inversions(state.filter(x => x !== 0));
  if (n % 2 === 1) return inv % 2 === 0;
  const blankIndex = state.indexOf(0);
  const blankRowFromBottom = n - Math.floor(blankIndex / n);
  if (blankRowFromBottom % 2 === 0) return inv % 2 === 1; // blank on even row from bottom
  return inv % 2 === 0; // blank on odd row from bottom
}

function inversions(arr) {
  let inv = 0;
  for (let i = 0; i < arr.length; i++) {
    for (let j = i + 1; j < arr.length; j++) {
      if (arr[i] > arr[j]) inv++;
    }
  }
  return inv;
}

function shuffleSolvable(n) {
  const g = goalState(n);
  let s;
  do {
    s = [...g];
    for (let i = s.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [s[i], s[j]] = [s[j], s[i]];
    }
  } while (!isSolvable(s, n) || isGoal(s));
  return s;
}

function isGoal(state) {
  for (let i = 0; i < state.length - 1; i++) {
    if (state[i] !== i + 1) return false;
  }
  return state[state.length - 1] === 0;
}

function render() {
  boardEl.innerHTML = '';
  setGrid(n);
  tiles.forEach((v, idx) => {
    const tile = document.createElement('div');
    tile.className = 'tile' + (v === 0 ? ' blank' : '');
    if (v !== 0) tile.textContent = String(v);
    const [r, c] = [Math.floor(idx / n), idx % n];
    tile.style.gridColumn = String(c + 1);
    tile.style.gridRow = String(r + 1);
    if (isMovableIndex(idx)) tile.classList.add('movable');
    tile.addEventListener('click', () => tryMoveIndex(idx));
    boardEl.appendChild(tile);
  });
}

function isMovableIndex(idx) {
  const blank = tiles.indexOf(0);
  const br = Math.floor(blank / n), bc = blank % n;
  const r = Math.floor(idx / n), c = idx % n;
  return (r === br && Math.abs(c - bc) === 1) || (c === bc && Math.abs(r - br) === 1);
}

function tryMoveIndex(idx) {
  if (animating) return;
  if (!isMovableIndex(idx)) return;
  const blank = tiles.indexOf(0);
  [tiles[idx], tiles[blank]] = [tiles[blank], tiles[idx]];
  render();
  if (isGoal(tiles)) statusEl.textContent = 'Solved!'; else statusEl.textContent = '';
}

function newGame() {
  tiles = shuffleSolvable(n);
  statusEl.textContent = '';
  render();
}

function ensureWorker() {
  if (worker) return;
  try {
    worker = new Worker('solver-worker.js', { type: 'module' });
  } catch (e) {
    worker = new Worker('solver-worker.js');
  }
  worker.onmessage = (e) => {
    const { type, payload } = e.data || {};
    if (type === 'status') statusEl.textContent = payload;
    if (type === 'solution') {
      playSolution(payload);
    }
    if (type === 'no-solution') {
      animating = false;
      statusEl.textContent = 'No solution found';
    }
    if (type === 'error') {
      statusEl.textContent = payload || 'Solver error';
    }
  };
}

async function playSolution(moves) {
  if (!Array.isArray(moves) || moves.length === 0) {
    statusEl.textContent = isGoal(tiles) ? 'Already solved' : 'No solution found';
    return;
  }
  animating = true;
  statusEl.textContent = `Playing ${moves.length} moves...`;
  for (const move of moves) {
    applyMove(move);
    render();
    await sleep(140);
  }
  animating = false;
  statusEl.textContent = isGoal(tiles) ? 'Solved!' : 'Unexpected: not solved';
}

function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

// Moves are strings: 'U','D','L','R' indicating blank moves
function applyMove(m) {
  const blank = tiles.indexOf(0);
  const br = Math.floor(blank / n), bc = blank % n;
  let nr = br, nc = bc;
  // Moves indicate the BLANK's direction
  if (m === 'U') nr = br - 1;
  if (m === 'D') nr = br + 1;
  if (m === 'L') nc = bc - 1;
  if (m === 'R') nc = bc + 1;
  if (nr < 0 || nr >= n || nc < 0 || nc >= n) return;
  const ni = nr * n + nc;
  [tiles[blank], tiles[ni]] = [tiles[ni], tiles[blank]];
}

function solve() {
  if (isGoal(tiles)) { statusEl.textContent = 'Already solved'; return; }
  ensureWorker();
  statusEl.textContent = 'Solving...';
  worker.postMessage({ type: 'solve', payload: { n, tiles } });
}

sizeEl.addEventListener('change', () => {
  n = Number(sizeEl.value);
  newGame();
});
newGameEl.addEventListener('click', newGame);
solveEl.addEventListener('click', solve);
loadStateEl.addEventListener('click', () => {
  const raw = (stateInputEl.value || '').trim();
  if (!raw) return;
  const parts = raw.split(/\s*,\s*/).map(Number);
  if (parts.length !== n * n || parts.some(v => !Number.isInteger(v))) {
    statusEl.textContent = `Invalid state. Expected ${n * n} integers`;
    return;
  }
  const seen = new Set(parts);
  if (seen.size !== parts.length || !seen.has(0)) {
    statusEl.textContent = 'State must contain all tiles including 0';
    return;
  }
  if (!isSolvable(parts, n)) {
    statusEl.textContent = 'Unsolvable state for this size';
    return;
  }
  tiles = parts;
  render();
  statusEl.textContent = '';
});

// init
n = Number(sizeEl.value);
newGame();


