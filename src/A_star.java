
import java.util.HashMap;
import java.util.PriorityQueue;

/*
Ashvin ganesan
Mr. Paige
Wednesday October 6th 2021
Artificial Intelligence
Project 1 checkpoint 3
 */
public class A_star {
    HashMap<Board, Node> seen;
    PriorityQueue<Board> pq;
    Node goal;
    Board boardGoal;
    private int statesCreated;
    private int statesExpanded;
    private int maxFrontier;
    
    public int getScore(Board g) {
            
        return g.score();
    }
    public A_star(Board initial) {
        this(initial, 10, 1);
    }
    
    public A_star(Board initial, int heuristicWeight, int movesWeight) {
        boardGoal = initial.goal();
        initial.setWeight(heuristicWeight,movesWeight);
        pq = new PriorityQueue<>();
        seen = new HashMap<Board,Node>();
        maxFrontier = 0;
        statesCreated = 1;
        statesExpanded = 0;
        pq.add(initial);
        Board current = pq.peek();
        seen.put(current, new Node(0, null, null, getScore(current)));
        while(!pq.isEmpty()) {
            current = pq.poll();
            
//            if(current.moves() > 100) {
//                System.out.println(current);
//            }
//            System.out.println("current score: " + current.score());
//            System.out.println("current moves: " + current.moves());
//            System.out.println(current);
            statesExpanded++;
            if(current.isSolved()) {
                goal = seen.get(current);
//                System.out.println("solved");
                return;
            }
            Board.Direction[] validmoves = current.validMoves();
            for(Board.Direction d:validmoves) {
                Board temp = current.move(d);
                if(!seen.containsKey(temp)) {
                    pq.add(temp);
                    statesCreated++;
                    Node prev = seen.get(current);
                    temp.setWeight(heuristicWeight,movesWeight);
                    Node n = new Node(prev.moves+1, prev, d, getScore(current)+1); //Replace 0 with heuristic                   
                    seen.put(temp, n);
                }              
            }
            maxFrontier = Math.max(maxFrontier, pq.size());
        }
        System.out.println("No Goal possible");
    }
    public Board.Direction[] printPath() {
        Node n = goal;
        int cost = n.moves;
        //System.out.println("The number of moves made is " + cost);
        Board.Direction[] arr = new Board.Direction[cost];
        while(n.previous != null) {
            cost--;
            arr[cost] = n.direction;
            n = n.previous;
        }
        return arr;
    }
    public void statistics() {
        System.out.println(goal.moves+" Solution depth");
        System.out.println(statesCreated+" States created");
        System.out.println(statesExpanded+"  States expanded");
        System.out.println(maxFrontier+"  Maximum frontier size");
        double effectiveBranchingFactor = Math.pow(statesExpanded, (1.0/goal.moves));
        effectiveBranchingFactor = ((double)Math.round((effectiveBranchingFactor*10000)))/10000.0;
        System.out.println(effectiveBranchingFactor + " Effective branching factor");
    }
    
    private class Node {
        public Node previous; //This does not include the move to go back to a previous position.
        public int moves;
        public Board.Direction direction; 
        public int acost;

        public Node(int moves, Node previous, Board.Direction direction, int acost) {
            this.moves = moves;
            this.previous = previous;
            this.direction = direction;
            this.acost = acost;
        }

     
    }
}
