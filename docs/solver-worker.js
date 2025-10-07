// A* solver for n-puzzle mirroring Java implementation (Board + A_star)

self.onmessage = (e) => {
  const { type, payload } = e.data || {};
  if (type === 'solve') {
    try {
      const { n, tiles } = payload;
      const res = aStar(n, tiles);
      if (res == null) {
        postMessage({ type: 'no-solution' });
      } else {
        postMessage({ type: 'solution', payload: res });
      }
    } catch (err) {
      postMessage({ type: 'error', payload: (err && err.message) || String(err) });
    }
  }
};

function aStar(n, startTiles) {
  const goal = goalState(n);
  if (arraysEqual(startTiles, goal)) return [];

  const startKey = key(startTiles);
  const open = new MinHeap();
  const gScore = new Map();
  const parent = new Map(); // key -> {prevKey, move}

  const startH = heuristicJavaStyle(n, startTiles, true, 2);
  gScore.set(startKey, 0);
  open.push({ f: weightScore(startH, 0), key: startKey, blank: startTiles.indexOf(0), tiles: startTiles });

  let expanded = 0;
  let maxFrontier = 0;

  while (!open.isEmpty()) {
    const node = open.pop();
    const currentTiles = node.tiles;
    const currentKey = node.key;
    const currentG = gScore.get(currentKey) ?? Infinity;

    if (arraysEqual(currentTiles, goal)) {
      return reconstructPath(parent, currentKey);
    }

    expanded++;
    if (expanded % 1000 === 0) {
      postMessage({ type: 'status', payload: `A*: expanded ${expanded}, frontier ${open.size()}...` });
    }

    const blank = currentTiles.indexOf(0);
    const r = Math.floor(blank / n), c = blank % n;
    const moves = [];
    if (r > 0) moves.push(0); // U
    if (r < n - 1) moves.push(1); // D
    if (c > 0) moves.push(2); // L
    if (c < n - 1) moves.push(3); // R

    for (const m of moves) {
      const ni = neighborIndex(n, blank, m);
      const next = currentTiles.slice();
      swap(next, blank, ni);
      const nextKey = key(next);
      const tentativeG = currentG + 1;
      const prevBest = gScore.get(nextKey);
      if (prevBest !== undefined && tentativeG >= prevBest) continue;
      gScore.set(nextKey, tentativeG);
      parent.set(nextKey, { prevKey: currentKey, move: moveChar(m) });
      const h = heuristicJavaStyle(n, next, true, 2);
      open.push({ f: weightScore(h, tentativeG), key: nextKey, tiles: next });
    }

    maxFrontier = Math.max(maxFrontier, open.size());
  }
  return null;
}

function neighborIndex(n, blank, move) {
  // 0=U (blank up means tile moves down), 1=D, 2=L, 3=R
  const r = Math.floor(blank / n), c = blank % n;
  if (move === 0) return (r - 1) * n + c;
  if (move === 1) return (r + 1) * n + c;
  if (move === 2) return r * n + (c - 1);
  return r * n + (c + 1);
}

function moveChar(m) { return m === 0 ? 'U' : m === 1 ? 'D' : m === 2 ? 'L' : 'R'; }

function swap(arr, i, j) { const t = arr[i]; arr[i] = arr[j]; arr[j] = t; }

function key(arr) { return arr.join(','); }

function goalState(n) {
  return Array.from({ length: n * n }, (_, i) => (i + 1) % (n * n));
}

function isGoal(state) {
  for (let i = 0; i < state.length - 1; i++) if (state[i] !== i + 1) return false;
  return state[state.length - 1] === 0;
}

// Java Board.heuristic(useStep=true, stepFromMax=2) style
function heuristicJavaStyle(n, state, useStep, stepFromMax) {
  let passes = true;
  let step = 0;
  let count = 0;
  let adder = 1000000;
  while (Math.min(n, n) > (step + stepFromMax) && useStep && passes) {
    for (let i = 0; i < state.length; i++) {
      const v = state[i];
      if ((v - 1 + (n * step)) < n * (step + 1) || ((v - 1) % n) === step) {
        if (v !== 0) {
          const iX = (i + 1) % n;
          const iY = Math.floor((i + 1) / n);
          const numbX = v % n;
          const numbY = Math.floor(v / n);
          count += Math.abs(iX - numbX) + Math.abs(iY - numbY);
        }
      }
      if (passes) {
        if (i < n || i % n === 0) {
          if (state[i] !== i + 1) {
            passes = false;
          }
        }
      }
    }
    if (!passes) {
      count += adder;
    } else {
      adder = Math.floor(adder / 10);
      step++;
    }
  }
  return count;
}

function weightScore(h, g) {
  // mirror Java: score = 10*heuristic + 1*moves
  return 10 * h + g;
}

function arraysEqual(a, b) {
  if (a.length !== b.length) return false;
  for (let i = 0; i < a.length; i++) if (a[i] !== b[i]) return false;
  return true;
}

// Simple binary min-heap for {f, key, tiles}
class MinHeap {
  constructor() { this.h = []; }
  size() { return this.h.length; }
  isEmpty() { return this.h.length === 0; }
  push(x) { this.h.push(x); this._siftUp(this.h.length - 1); }
  pop() {
    const a = this.h;
    const top = a[0];
    const last = a.pop();
    if (a.length) { a[0] = last; this._siftDown(0); }
    return top;
  }
  _siftUp(i) { const a = this.h; while (i > 0) { const p = (i - 1) >> 1; if (a[p].f <= a[i].f) break; [a[p], a[i]] = [a[i], a[p]]; i = p; } }
  _siftDown(i) {
    const a = this.h; const n = a.length;
    while (true) {
      let l = i * 2 + 1, r = l + 1, s = i;
      if (l < n && a[l].f < a[s].f) s = l;
      if (r < n && a[r].f < a[s].f) s = r;
      if (s === i) break; [a[i], a[s]] = [a[s], a[i]]; i = s;
    }
  }
}

function reconstructPath(parent, goalKey) {
  const moves = [];
  let k = goalKey;
  while (parent.has(k)) {
    const { prevKey, move } = parent.get(k);
    moves.push(move);
    k = prevKey;
  }
  moves.reverse();
  return moves;
}


