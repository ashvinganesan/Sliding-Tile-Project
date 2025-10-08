public class SolverBridge {

    // Returns moves as a string of characters 'U','D','L','R'
    public static String solveCSV(int size, String csvTiles) {
        String[] parts = csvTiles.split(",");
        int[] tiles = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            tiles[i] = Integer.parseInt(parts[i].trim());
        }
        // Use step heuristic. For 5x5, bias toward greedy best-first (movesWeight=0)
        Board initial = new Board(size, size, tiles, 0, true, 2);
        if (initial.isSolved()) return "";
        Board.Direction[] path;
        if (size >= 5) {
            // Use greedy layer-by-layer for larger boards
            path = GreedyLayerSolver.solve(size, tiles);
        } else {
            A_star solver = new A_star(initial, 10, 1);
            path = solver.printPath();
        }
        StringBuilder sb = new StringBuilder(path.length);
        for (Board.Direction d : path) {
            switch (d) {
                case UP: sb.append('U'); break;
                case DOWN: sb.append('D'); break;
                case LEFT: sb.append('L'); break;
                case RIGHT: sb.append('R'); break;
            }
        }
        return sb.toString();
    }
}


