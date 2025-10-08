import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GreedyLayerSolver {

    public static Board.Direction[] solve(int size, int[] tiles) {
        Board board = new Board(size, size, tiles, 0, true, 2);
        if (board.isSolved()) return new Board.Direction[0];

        int n = size;
        boolean[] locked = new boolean[n * n];
        List<Board.Direction> moves = new ArrayList<>();

        for (int layer = 0; layer < n - 2; layer++) {
            // Place top row of current layer (cols layer .. n-2)
            int row = layer;
            for (int col = layer; col < n - 1; col++) {
                int idx = row * n + col;
                int targetVal = goalValueAt(n, idx);
                if (board.get(row, col) == targetVal) {
                    locked[idx] = true; // already correct
                    continue;
                }
                Board.Direction[] step = bfsPlace(board, idx, targetVal, locked, n);
                board = apply(board, moves, step);
                locked[idx] = true;
            }

            // Place left column of current layer (rows layer+1 .. n-2)
            int col = layer;
            for (int r = layer + 1; r < n - 1; r++) {
                int idx = r * n + col;
                int targetVal = goalValueAt(n, idx);
                if (board.get(r, col) == targetVal) {
                    locked[idx] = true;
                    continue;
                }
                Board.Direction[] step = bfsPlace(board, idx, targetVal, locked, n);
                board = apply(board, moves, step);
                locked[idx] = true;
            }
        }

        // Finish remaining 2x2 with BFS to solved
        Board.Direction[] finish = bfsToSolved(board, locked, n);
        board = apply(board, moves, finish);

        return moves.toArray(new Board.Direction[0]);
    }

    private static Board apply(Board start, List<Board.Direction> acc, Board.Direction[] seq) {
        Board cur = start;
        for (Board.Direction d : seq) {
            acc.add(d);
            cur = cur.move(d);
        }
        return cur;
    }

    private static Board.Direction[] bfsToSolved(Board start, boolean[] locked, int n) {
        Map<Board, Node> prev = new HashMap<>();
        Queue<Board> q = new ArrayDeque<>();
        q.add(start);
        prev.put(start, new Node(null, null));
        while (!q.isEmpty()) {
            Board cur = q.poll();
            if (cur.isSolved()) {
                return reconstruct(prev, cur);
            }
            for (Board.Direction d : cur.validMoves()) {
                Board nxt = cur.move(d);
                if (!respectsLocked(nxt, locked, n)) continue;
                if (prev.containsKey(nxt)) continue;
                prev.put(nxt, new Node(cur, d));
                q.add(nxt);
            }
        }
        return new Board.Direction[0];
    }

    private static Board.Direction[] bfsPlace(Board start, int targetIndex, int targetValue, boolean[] locked, int n) {
        int tr = targetIndex / n, tc = targetIndex % n;
        if (start.get(tr, tc) == targetValue) return new Board.Direction[0];
        Map<Board, Node> prev = new HashMap<>();
        Queue<Board> q = new ArrayDeque<>();
        q.add(start);
        prev.put(start, new Node(null, null));
        while (!q.isEmpty()) {
            Board cur = q.poll();
            if (cur.get(tr, tc) == targetValue) {
                return reconstruct(prev, cur);
            }
            for (Board.Direction d : cur.validMoves()) {
                Board nxt = cur.move(d);
                if (!respectsLocked(nxt, locked, n)) continue;
                if (prev.containsKey(nxt)) continue;
                prev.put(nxt, new Node(cur, d));
                q.add(nxt);
            }
        }
        return new Board.Direction[0];
    }

    private static boolean respectsLocked(Board b, boolean[] locked, int n) {
        for (int i = 0; i < locked.length; i++) {
            if (!locked[i]) continue;
            int r = i / n, c = i % n;
            if (b.get(r, c) != goalValueAt(n, i)) return false;
        }
        return true;
    }

    private static int goalValueAt(int n, int index) {
        int last = n * n - 1;
        if (index == last) return 0;
        return index + 1;
    }

    private static Board.Direction[] reconstruct(Map<Board, Node> prev, Board goal) {
        Deque<Board.Direction> stack = new ArrayDeque<>();
        Board cur = goal;
        while (true) {
            Node p = prev.get(cur);
            if (p == null || p.prev == null) break;
            stack.push(p.move);
            cur = p.prev;
        }
        Board.Direction[] arr = new Board.Direction[stack.size()];
        int i = 0;
        while (!stack.isEmpty()) arr[i++] = stack.pop();
        return arr;
    }

    private static class Node {
        final Board prev;
        final Board.Direction move;
        Node(Board prev, Board.Direction move) { this.prev = prev; this.move = move; }
    }
}


