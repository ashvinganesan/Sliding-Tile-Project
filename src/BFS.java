
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/*
Ashvin ganesan
Mr. Paige
Tuesday September 28th 2021
Artificial Intelligence
Project 1 checkpoint 2
 */
public class BFS {
    HashMap<Board, Node> seen;
    Queue<Board> pq;
    Node goal;
    private int statesCreated;
    private int statesExpanded;
    private int maxFrontier;
    
    public BFS(Board initial) {
        pq = new LinkedList<>();
        seen = new HashMap<Board,Node>();
        maxFrontier = 0;
        statesCreated = 1;
        statesExpanded = 0;
        pq.add(initial);
        Board current = pq.peek(); 
        seen.put(current, new Node(0, null, null));
        while(!pq.isEmpty()) {
            current = pq.poll();
            statesExpanded++;
            if(current.isSolved()) {
                goal = seen.get(current);
                //System.out.println("solved");
                return;
            }
            Board.Direction[] validmoves = current.validMoves();
            for(Board.Direction d:validmoves) {
                Board temp = current.move(d);
                if(!seen.containsKey(temp)) {
                    pq.add(temp);
                    statesCreated++;
                    Node prev = seen.get(current);
                    Node n = new Node(prev.cost+1, prev, d); 
                    seen.put(temp, n);
                }              
            }
            maxFrontier = Math.max(maxFrontier, pq.size());
        }
        System.out.println("No Goal possible");
    }
    public Board.Direction[] printPath() {
        Node n = goal;
        int cost = n.cost;
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
        System.out.println(goal.cost+" Solution depth");
        System.out.println(statesCreated+" States created");
        System.out.println(statesExpanded+"  States expanded");
        System.out.println(maxFrontier+"  Maximum frontier size");
        double effectiveBranchingFactor = Math.pow(statesExpanded, (1.0/goal.cost));
        effectiveBranchingFactor = ((double)Math.round((effectiveBranchingFactor*10000)))/10000.0;
        System.out.println(effectiveBranchingFactor + " Effective branching factor");
    }
    
    private class Node {
        public Node previous; //This does not include the move to go back to a previous position.
        public int cost;
        public Board.Direction direction; 

        public Node(int cost, Node previous, Board.Direction direction) {
            this.cost = cost;
            this.previous = previous;
            this.direction = direction;
        }

     
    }
}
