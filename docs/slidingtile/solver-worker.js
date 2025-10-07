// IDA* solver for n-puzzle using Manhattan distance with linear conflict

self.onmessage = (e) => {
  const { type, payload } = e.data || {};
  if (type === 'solve') {
    try {
      const { n, tiles } = payload;
      const res = idaStar(n, tiles);
      postMessage({ type: 'solution', payload: res || [] });
    } catch (err) {
      postMessage({ type: 'error', payload: (err && err.message) || String(err) });
    }
  }
};

function idaStar(n, start) {
  const startKey = key(start);
  const goal = goalState(n);
  const goalKey = key(goal);
  if (startKey === goalKey) return [];
  const blankStart = start.indexOf(0);
  const startH = heuristic(n, start);
  let bound = startH;
  const path = [];
  const visited = new Set();

  while (true) {
    const t = search(n, start, blankStart, 0, bound, -1, path, visited);
    if (Array.isArray(t)) return t; // found solution (path of moves)
    if (t === Infinity) return null; // not found
    bound = t;
    postMessage({ type: 'status', payload: `Deepening to bound ${bound}...` });
  }
}

function search(n, state, blankIndex, g, bound, prevMove, path, visited) {
  const h = heuristic(n, state);
  const f = g + h;
  if (f > bound) return f;
  if (isGoal(state)) return [...path];
  const keyStr = key(state);
  if (visited.has(keyStr)) return Infinity;
  visited.add(keyStr);

  let min = Infinity;
  const br = Math.floor(blankIndex / n), bc = blankIndex % n;
  // moves encode opposite to avoid undo: 0=U,1=D,2=L,3=R
  const moves = [];
  if (br > 0 && prevMove !== 1) moves.push(0);
  if (br < n - 1 && prevMove !== 0) moves.push(1);
  if (bc > 0 && prevMove !== 3) moves.push(2);
  if (bc < n - 1 && prevMove !== 2) moves.push(3);

  for (const m of moves) {
    const ni = neighborIndex(n, blankIndex, m);
    swap(state, blankIndex, ni);
    path.push(moveChar(m));
    const t = search(n, state, ni, g + 1, bound, m, path, visited);
    if (Array.isArray(t)) return t;
    if (t < min) min = t;
    path.pop();
    swap(state, blankIndex, ni);
  }
  visited.delete(keyStr);
  return min;
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

function heuristic(n, state) {
  let dist = 0;
  for (let i = 0; i < state.length; i++) {
    const v = state[i];
    if (v === 0) continue;
    const tr = Math.floor((v - 1) / n), tc = (v - 1) % n;
    const r = Math.floor(i / n), c = i % n;
    dist += Math.abs(tr - r) + Math.abs(tc - c);
  }
  // Linear conflict (simple version)
  dist += linearConflict(n, state);
  return dist;
}

function linearConflict(n, state) {
  let conflicts = 0;
  // rows
  for (let r = 0; r < n; r++) {
    let maxSeen = -1;
    for (let c = 0; c < n; c++) {
      const v = state[r * n + c];
      if (v !== 0 && Math.floor((v - 1) / n) === r) {
        const targetCol = (v - 1) % n;
        if (targetCol > maxSeen) maxSeen = targetCol; else conflicts += 2;
      }
    }
  }
  // cols
  for (let c = 0; c < n; c++) {
    let maxSeen = -1;
    for (let r = 0; r < n; r++) {
      const v = state[r * n + c];
      if (v !== 0 && ((v - 1) % n) === c) {
        const targetRow = Math.floor((v - 1) / n);
        if (targetRow > maxSeen) maxSeen = targetRow; else conflicts += 2;
      }
    }
  }
  return conflicts;
}


