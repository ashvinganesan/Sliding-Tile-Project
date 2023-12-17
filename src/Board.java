/*
Ashvin ganesan
Mr. Paige
Wednesday October 6th 2021
Artificial Intelligence
Project 1 checkpoint 3
 */

import java.util.ArrayList;

public class Board implements Comparable<Board> {

    // An IMMUTABLE class to represent a configuration of the sliding tile puzzle.  
    private final int width;            // Number of squares wide
    private final int height;           // Number of squares tall
    private final Position empty;       // The location of the empty square
    private final int[] tiles;
    private int hash;
    private int score;
    private int moves;
    private int heuristicScore;
    private boolean useStep;
    private int stepFromMax;

    public static enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public class Position {

        private int row;
        private int column;
        private int index;

        // A class to represent the position of a tile in the puzzle.
        // You might wish to represent it as a row/column or as an
        // index in the range 0 .. width*height-1.
        public Position(int row, int column) {
            this.row = row;
            this.column = column;
            this.index = row * Board.this.width + column;
        }

        public Position(int index) {
            this.index = index;
            row = index / Board.this.width;
            column = index % Board.this.width;
        }

        public int row() {
            return row;
        }

        public int column() {
            return column;
        }

        public int index() {
            return index;
        }

        public boolean atTop() {
            if (row == 0) {
                return true;
            }
            return false;
        }

        public boolean atBottom() {
            if (row == height - 1) {
                return true;
            }
            return false;
        }

        public boolean atLeft() {
            if (column == 0) {
                return true;
            }
            return false;
        }

        public boolean atRight() {
            if (column == width - 1) {
                return true;
            }
            return false;
        }

        public Position up() {
            return new Position(row - 1, column);
        }

        public Position down() {
            return new Position(row + 1, column);
        }

        public Position left() {
            return new Position(row, column - 1);
        }

        public Position right() {
            return new Position(row, column + 1);
        }

        public Position move(Direction direction) {
            switch (direction) {
                case UP:
                    return up();
                case DOWN:
                    return down();
                case LEFT:
                    return left();
                case RIGHT:
                    return right();
                default:
                    return null;
            }
        }

        public Direction[] validMoves() {
            boolean up = !this.atTop();
            boolean down = !this.atBottom();
            boolean left = !this.atLeft();
            boolean right = !this.atRight();
            int count = 0;
            if (up) {
                count++;
            }
            if (down) {
                count++;
            }
            if (left) {
                count++;
            }
            if (right) {
                count++;
            }

            Direction[] valid = new Direction[count];
            count = 0;
            if (up) {
                valid[count] = Direction.UP;
                count++;
            }
            if (down) {
                valid[count] = Direction.DOWN;
                count++;
            }
            if (left) {
                valid[count] = Direction.LEFT;
                count++;
            }
            if (right) {
                valid[count] = Direction.RIGHT;
                count++;
            }
            return valid; // TODO
        }

        public boolean equals(Position other) {
            if (this.row == other.row && this.column == other.row) {
                return true;
            }
            return false; // TODO
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Position && this.equals((Position) other);
        }

        @Override
        public String toString() {
            return "(" + this.row() + "," + this.column() + ")";
        }
    }

    public Direction[] validMoves() {
        return empty.validMoves();
    }
    
    public Board(int size, int[] tiles, int moves, boolean useStep) {
        this(size, size, tiles, moves, useStep, 2);
    }
    public Board(int size, int[] tiles, int moves, int stepSize) {
        this(size, size, tiles, moves, true, stepSize);
    }
    public Board(int width, int height, int[] tiles, int moves, boolean useStep) {
        this(width, height, tiles, moves, useStep, 2);
    }
    public Board(int width, int height, int[] tiles, int moves, int stepSize) {
        this(width, height, tiles, moves, true, stepSize);
    }

    public Board(int width, int height, int[] tiles, int moves, boolean useStep, int stepFromMax) { // TODO
        // Make sure to make a copy of the tiles array.
        this.useStep = useStep;
        this.width = width;
        this.height = height;
        this.tiles = new int[tiles.length];
        this.hash = 0;
        this.stepFromMax = stepFromMax;
//        this.stepFromMax = 3;
        //System.out.println("");
        int emptyPos = -1;
        for (int i = 0; i < tiles.length; i++) {
            this.tiles[i] = tiles[i];
            if (tiles[i] == 0) {
                emptyPos = i;

            }
        }
        if (emptyPos != -1) {
            this.empty = new Position(emptyPos);
        } else {
            System.out.println("Invalid Empty initialized");
            this.empty = new Position(emptyPos);
        }
        this.moves = moves;
        this.heuristicScore = heuristic(useStep,stepFromMax);
        this.score = moves + 10 * heuristicScore;

    }

    public void setWeight(int heuristicWeight, int movesWeight) {
        this.score = movesWeight * moves + heuristicWeight * heuristicScore;
    }

    public int heuristic(boolean useStep) {
        return heuristic(useStep, 2);
    }

    public int heuristic(int stepFromMax) {
        return heuristic(true, stepFromMax);
    }

    public int heuristic(boolean useStep, int stepFromMax) {
        //System.out.println("StepFromMax: " + stepFromMax);
        boolean passes = true;
        int step = 0;
        int count = 0;
        int adder = 1000000;
        int numbPasses = 0;
        while (Math.min(width, height) > (step + stepFromMax) && useStep && passes) {

            for (int i = 0; i < tiles.length; i++) {

                if (tiles[i] - 1 + (width * step) < width * (step + 1) || (tiles[i] - 1) % width == step) {
                    int numb = tiles[i];
                    //System.out.println("number is " + numb);
                    if (numb != 0) {
                        int iX = ((i + 1) % width);
                        int iY = ((i + 1) / width);

                        int numbX = (numb % width);
                        int numbY = (numb / width);

                        count += (Math.abs(iX - numbX) + Math.abs(iY - numbY));
                    }

                }

                if (passes) {
                    if (i < width || i % width == 0) {
                        if (tiles[i] != i + 1) {
                            passes = false;
                        }
                    }
                }

            }
            if (!passes) {
                count += adder;
            } else {
                adder = adder / 10;
                step++;
            }

        }
        //System.out.println(step);
        return count;
    }

    public int score() {
        return score;
    }

    public int moves() {
        return moves;
    }

    @Override
    public int compareTo(Board other) {
        if (this.score > other.score) {
            return 1;
        } else if (this.score < other.score) {
            return -1;
        }
        return 0;
    }

    public Board(int width, int height, ArrayList<Integer> tiles, int moves, boolean useStep, int stepFromMax) {
        this(width, height, tiles.stream().mapToInt(i -> i).toArray(), moves, useStep, stepFromMax);
    }
    public Board(int width, int height, ArrayList<Integer> tiles, int moves, boolean useStep) {
        this(width, height, tiles.stream().mapToInt(i -> i).toArray(), moves, useStep,2);
    }
            

    public Board move(Direction direction) {
        int[] newTiles = new int[tiles.length];
        for (int i = 0; i < newTiles.length; i++) {
            newTiles[i] = tiles[i];
        }
        int emptyIndex = empty.index();
        //System.out.println("emptyIndex:" + emptyIndex);
        int newIndex = 0;
        if (direction == Direction.UP) {
            newIndex = empty.up().index();
        } else if (direction == Direction.DOWN) {
            newIndex = empty.down().index();
        } else if (direction == Direction.LEFT) {
            newIndex = empty.left().index();
        } else {
            newIndex = empty.right().index();
        }
        //System.out.println("newIndex:" + newIndex);
        newTiles[emptyIndex] = tiles[newIndex];
        newTiles[newIndex] = tiles[emptyIndex];
        return new Board(this.width, this.height, newTiles, this.moves + 1, useStep, stepFromMax);
    }

    public int get(Position position) {
        return get(position.row(), position.column()); // TODO
    }

    public int get(int row, int column) {
        // The value of the tile at the specified position.

        return tiles[row * width + column];
    }

    public Position empty() {
        return this.empty;
    }

    public boolean isSolved() {
        for (int i = 1; i < tiles.length; i++) {
            if (this.tiles[i - 1] != i) {
                return false;
            }
        }
        return true;

    }

    public int[] generateGoal() {
        int[] tile = new int[tiles.length];
        for (int i = 1; i < tiles.length; i++) {
            tile[i - 1] = i;
        }
        return tile;
    }

    public Board goal() {
        int[] tile = new int[tiles.length];
        for (int i = 1; i < tiles.length; i++) {
            tile[i - 1] = i;
        }
        return new Board(width, height, tile, 0, useStep, stepFromMax);
    }

    public static boolean isValid(int[] tiles) {
        boolean[] seen = new boolean[tiles.length];
        for (boolean k : seen) {
            k = false;
        }
        int max = tiles.length - 1;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] > 15 || tiles[i] < 0) {
                return false;
            } else {
                if (seen[tiles[i]]) {
                    return false;
                }
                seen[tiles[i]] = true;
            }
        }
        return true;
        // Is this array a permuation of the numbers 0 .. tiles.length-1 ?
    }

    public boolean equals(Board other) {
        if (this.tiles.length != other.tiles.length) {
            return false;
        }
        for (int i = 0; i < this.tiles.length; i++) {
            if (this.tiles[i] != other.tiles[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Board && this.equals((Board) other);
    }

    @Override
    public int hashCode() {
        // Yes, you do need to worry about this method !!!
        // Recall, if x.equals(y) then x.hashCode() == y.hashCode()
        if (this.hash != 0) {
            return this.hash;
        }
        this.hash = java.util.Arrays.hashCode(this.tiles);
        return this.hash; // Works - but a really bad idea.  TODO
    }

    @Override
    public String toString() {
        String result = "";
        for (int row = 0; row < this.height; row++) {
            if (row > 0) {
                result += rowSeparator();
            }
            String separator = "";
            for (int col = 0; col < this.width; col++) {
                int tile = this.get(row, col);
                result += separator;
                if (tile > 0) {
                    result += String.format(" %2d ", this.get(row, col));
                } else {
                    result += "    ";
                }
                separator = "|";
            }
            result += "\n"; // next line
        }
        return result;
    }

    private String rowSeparator() {
        String result = "";
        String separator = "";
        for (int col = 0; col < this.width; col++) {
            result += separator;
            result += "----";
            separator = "+";
        }
        result += "\n";
        return result;
    }
}
