package student_player;

import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.*;

public class MonteCarloDecisionMaker implements DecisionMaker {

    private Node root;
    private static final Integer SIM_LIMIT = Integer.MAX_VALUE;
    private static final Long TIME_LIMIT = 1950L;
    private static final Integer REPETITIVE_SIM_RATE = 20;
    private static final Double UCT_CONST = Math.sqrt(2);
    private static final boolean SHOW_STAT = false;
    private static final Integer WIN_LEVER = 4;
    private static final Integer LOSE_LEVER = 4;

    public MonteCarloDecisionMaker(PentagoBoardState state) {
        this.root = new Node(null, state, null, 0, state.getTurnPlayer());
        this.root.initializeChildren();
    }

    @Override
    public PentagoMove makeDecision() {

        // If we can win in one round, return.
        for (Node child : this.root.children)
            if (child.state.gameOver() && child.state.getWinner() == this.root.state.getTurnPlayer())
                return child.move;

        return monteCarloTreeSearch();
    }

    /**
     * Get root.
     *
     * @return Root node.
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Set root.
     *
     * @param root Root node.
     */
    public void setRoot(Node root) {
        this.root = root;
    }

    /**
     * Monte Carlo Tree Search Algorithm.
     *
     * @return A move.
     */
    private PentagoMove monteCarloTreeSearch() {

        long startTime = System.currentTimeMillis();

        // Run simulations.
        int i;
        for (i = 0; i < MonteCarloDecisionMaker.SIM_LIMIT; i++) {
            Node node = root;

            // A manual time limit.
            if (System.currentTimeMillis() - startTime >= MonteCarloDecisionMaker.TIME_LIMIT)
                break;

            while (!node.isLeaf())
                node = node.getNextPossibleNode();

            if (node.timeVisited != 0 && !node.isOver()) {
                node.initializeChildren();
                node = node.getNextPossibleNode();
            }
            node.simulate();
        }

        // Retrieve result.
        if (MonteCarloDecisionMaker.SHOW_STAT)
            System.out.print(
                    "TREE SIZE: " + getTreeSize(this.root) +
                            " SIM TURN: " + i +
                            " TREE HEIGHT: " + getTreeHeight(this.root) +
                            "\n");
        return this.root.getMoveWithHighestWinRate();
    }

    /**
     * Find the size of the tree.
     *
     * @param node A starting node.
     * @return Size.
     */
    private static int getTreeSize(Node node) {
        if (node == null)
            return 0;
        int total = 1;
        for (Node child : node.children)
            total += getTreeSize(child);
        return total;
    }

    /**
     * Get the tree height of the node.
     *
     * @param node A node.
     * @return The height of the tree.
     */
    private static int getTreeHeight(Node node) {
        if (node == null)
            return 0;

        int maxSubTreeHeight = 0;
        for (Node child : node.children)
            maxSubTreeHeight = Math.max(maxSubTreeHeight, getTreeHeight(child));

        return maxSubTreeHeight + 1;
    }

    class Node {
        // Node value.
        PentagoBoardState state;
        PentagoMove move;
        Integer timeVisited;
        Integer winFrequency;
        Integer depth;
        Integer player;

        // Node topology.
        Node parent;
        ArrayList<Node> children;

        // Utility.
        Random rand;

        public Node(PentagoMove move, PentagoBoardState state, Node parent, Integer depth, Integer player) {
            this.move = move;
            this.state = state;
            this.timeVisited = 0;
            this.winFrequency = 0;
            this.parent = parent;
            this.depth = depth;
            this.player = player;
            this.rand = new Random(2019);
            this.children = new ArrayList<>();
        }

        boolean isLeaf() {
            return this.children.isEmpty();
        }

        /**
         * Expand this node.
         */
        void initializeChildren() {
            if (isLeaf()) {
                HashSet<String> recordedChildren = new HashSet<>();
                ArrayList<PentagoMove> moves = state.getAllLegalMoves();

                while (!moves.isEmpty()) {
                    PentagoMove move = moves.remove(rand.nextInt(moves.size()));

                    PentagoBoardState clonedState = (PentagoBoardState) state.clone();
                    clonedState.processMove(move);
                    if (recordedChildren.contains(clonedState.toString())) continue;
                    recordedChildren.add(clonedState.toString());

                    Node newNode = new Node(
                            move,
                            clonedState,
                            this,
                            this.depth + 1,
                            player
                    );

                    // If already win, just return.
                    if (newNode.isOver()) {
                        newNode.timeVisited = MonteCarloDecisionMaker.REPETITIVE_SIM_RATE;
                        newNode.winFrequency = newNode.state.getWinner() == this.player ? newNode.timeVisited : 0;
                    }

                    this.children.add(newNode);
                }
            }
        }

        /**
         * Fetch the best node according to their UCT.
         *
         * @return The best node.
         */
        Node getNextPossibleNode() {
            Node bestNode = null;
            double bestUCT = Double.NEGATIVE_INFINITY;
            for (Node n : this.children)
                if (UCT(n, this.timeVisited) > bestUCT) {
                    bestNode = n;
                    bestUCT = UCT(n, this.timeVisited);
                }
            return bestNode;
        }

        /**
         * Return if the state needless to search.
         *
         * @return Is the state over or not.
         */
        boolean isOver() {
            return state.gameOver();
        }

        /**
         * Run a simulation if this is a leaf.
         * Then backtrack to the root and update
         * all relevant information.
         */
        void simulate() {

            int dTimeVisited = MonteCarloDecisionMaker.REPETITIVE_SIM_RATE, dWinFrequency = 0;

            if (!this.isOver()) {
                // Run simulation.
                for (int i = 0; i < MonteCarloDecisionMaker.REPETITIVE_SIM_RATE; i++) {
                    PentagoBoardState clonedState = (PentagoBoardState) state.clone();
                    while (!clonedState.gameOver())
                        clonedState.processMove(getRandomMove(clonedState));
                    dWinFrequency += clonedState.getWinner() == this.player ? 1 : 0;
                }
//                dWinFrequency *= MonteCarloDecisionMaker.WIN_LEVER;
            } else {
                dTimeVisited *= this.state.getWinner() == this.player ? 1 : MonteCarloDecisionMaker.LOSE_LEVER;
                dWinFrequency = this.state.getWinner() == this.player ? dTimeVisited * MonteCarloDecisionMaker.WIN_LEVER : 0;
            }

            // Update tree.
            updateTree(dTimeVisited, dWinFrequency, this);
        }

        /**
         * Update the tree from a specified location.
         *
         * @param dTimeVisited  New time visited.
         * @param dWinFrequency New win frequency.
         * @param node          Node of interest.
         */
        private void updateTree(int dTimeVisited, int dWinFrequency, Node node) {
            Node parent = node.parent, child = node;
            while (parent != null) {
                child.timeVisited += dTimeVisited;
                child.winFrequency += dWinFrequency;
                child = parent;
                parent = parent.parent;
            }
            child.timeVisited += dTimeVisited;
            child.winFrequency += dWinFrequency;
        }

        /**
         * Get a pseudo-random move.
         *
         * @param state A state.
         * @return A random move.
         */
        PentagoMove getRandomMove(PentagoBoardState state) {
            ArrayList<int[]> emptySlots = new ArrayList<>();
            Piece[][] board = state.getBoard();
            for (int i = 0; i < board.length; i++)
                for (int j = 0; j < board[i].length; j++)
                    if (board[i][j] == Piece.EMPTY)
                        emptySlots.add(new int[]{i, j});

            int[] randomCoordinate = emptySlots.get(rand.nextInt(emptySlots.size()));
            return new PentagoMove(
                    randomCoordinate[0],
                    randomCoordinate[1],
                    rand.nextInt(4),
                    rand.nextInt(2),
                    state.getTurnPlayer()
            );
        }

        /**
         * Find the best move according to win ratio.
         *
         * @return A move.
         */
        PentagoMove getMoveWithHighestWinRate() {
            double highestWinRate = Double.NEGATIVE_INFINITY;
            PentagoMove bestMove = null;

            for (Node child : this.children)
                if (child.timeVisited != 0 && (double) child.winFrequency / child.timeVisited > highestWinRate) {
                    bestMove = child.move;
                    highestWinRate = (double) child.winFrequency / child.timeVisited;
                }

            return bestMove;
        }

        double UCT(Node children, Integer parentTimeVisited) {
            double Q_sa = children.timeVisited == 0 ? 0.0 : ((double) children.winFrequency) / children.timeVisited;
            double N_sa = children.timeVisited == 0 ? 0.000001 : children.timeVisited;
            return Q_sa + MonteCarloDecisionMaker.UCT_CONST * Math.sqrt((Math.log1p(parentTimeVisited) / N_sa));
        }
    }
}
