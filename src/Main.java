/*
Ashvin ganesan
Mr. Paige
Wednesday October 6th 2021
Artificial Intelligence
Project 1 checkpoint 3
 */

import java.util.ArrayList;

public class Main {

    public static abstract class ParameterException extends Exception {
        public ParameterException(String message) {
            super(message);
        }
    }

    public static class MissingOptionValue extends ParameterException {
        public MissingOptionValue(String name) {
            super("Missing value for option " + name);
        }
    }

    public static class InvalidOptionValue extends ParameterException {
        public InvalidOptionValue (String name, String value) {
            super("Invalid value for option " + name + ": " + value);
        }
    }

    public static class InvalidTileValue extends ParameterException {
        public InvalidTileValue(String tile) {
            super("Invalid tile value: " + tile);
        }
        public InvalidTileValue(int tile) {
            super("Invalid tile value: " + tile);
        }
    }

    public static class DuplicateTileValue extends ParameterException {
        public DuplicateTileValue(int tile) {
            super("Duplicate tile value: " + tile);
        }
    }
        

    public static int getOptionValue(String[] args, int index) throws ParameterException {
        String name = args[index];
        String value = "";
        try {
            value = args[index+1];
            if (value.charAt(0) == '-') {
                throw new MissingOptionValue(name);
            }
            return Integer.parseInt(value);
        } catch (IndexOutOfBoundsException e) {
            throw new MissingOptionValue(name);
        } catch (NumberFormatException e) {
            throw new InvalidOptionValue(name, value);
        }
    }

    public static int getTileValue(String[] args, int index, int size) throws ParameterException {
        String value = args[index];
        try {
            int tile = Integer.parseInt(value);
            if (tile < 0 || tile >= size) {
                throw new InvalidTileValue(tile);
            }
            return tile;
        } catch (NumberFormatException e) {
            throw new InvalidTileValue(value);
        }
    }

    public static void main(String[] args) {
        ArrayList<Board.Direction> moves = new ArrayList<>();
        ArrayList<Integer> tiles = new ArrayList<>();
        boolean error = false;
        int size = 4;
        int width = 4;
        int height = 4;
        int stepFromMax = -1;
        int heuristicWeight = 10;
        int movesWeight = 1;
        boolean verbose = false;
        boolean useStep = true;
        boolean stats = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            try {
                switch (arg) {
                    case "-hweight":
                        heuristicWeight = getOptionValue(args, i);
                        i++;
                        break;
                    case "-mweight":
                        movesWeight = getOptionValue(args,i);
                        i++;
                        break;
                    case "-nostep":
                        useStep = false;
                        i++;
                        break;
                    case "-stepfrommax":
                        stepFromMax = getOptionValue(args,i);
//                        System.out.println("StepFromMax: " + stepFromMax);
                        i++;
                        break;
                    case "-size":
                        size = getOptionValue(args, i);
                        height = size;
                        width = size;
                        i++;
                        break;
                    case "-ucs":
                        heuristicWeight = 0;
                        break;
                    case "-gbfs":
                        movesWeight = 0;
                        break;
                    case "-astar":
                        break;    
                    case "-verbose":
                        verbose = true;
                        break;
                    case "-width":
                        width = getOptionValue(args, i);
                        i++;
                        break;

                    case "-height":
                        height = getOptionValue(args, i);
                        i++;
                        break;

                    case "u":
                    case "up":
                        moves.add(Board.Direction.UP);
                        break;

                    case "d":
                    case "down":
                        moves.add(Board.Direction.DOWN);
                        break;

                    case "l":
                    case "left":
                        moves.add(Board.Direction.LEFT);
                        break;

                    case "r":
                    case "right":
                        moves.add(Board.Direction.RIGHT);
                        break;
                    case "-statistics":
                        stats = true;
                        break;
                        
                    default:
//                        System.out.println(arg);
                        int max = width * height;
                        int tile = getTileValue(args, i, max);
                        if (tiles.contains(tile)) {
                            throw new DuplicateTileValue(tile);
                        } else {
                            tiles.add(tile);
                        }
                        break;
                        
                }
            } catch (ParameterException e) {
                System.err.println(e.getMessage());
                error = true;
            }
        }

        size = width * height;
        int count = tiles.size();
        if (count < size) {
            System.err.println("Too few tiles: " + count);
        } else if (count > size) {
            System.err.println("Too many tiles: " + count);
        } else if (!error) {
            Board board;
            if(stepFromMax != -1) {
//                System.out.println(stepFromMax);
                board = new Board(width, height, tiles, 0, useStep, stepFromMax);
            }
            else {
                board = new Board(width, height, tiles, 0, useStep);
            }
            
//            System.out.println(board); //REMOVE THIS LATER
//            System.out.println("score" + board.score());
            A_star n = new A_star(board,heuristicWeight, movesWeight);
            for(Board.Direction d:n.printPath()) {
                moves.add(d);
            }
            
            if(verbose) {
                System.out.println(board);
            }

            for (Board.Direction direction : moves) {
                if(verbose) {
                    System.out.println(direction);
                }
                board = board.move(direction);
                if(verbose) {
                    System.out.println(board);
                }
            }
            if(stats) {
                n.statistics();
            }
        }
    }
}

