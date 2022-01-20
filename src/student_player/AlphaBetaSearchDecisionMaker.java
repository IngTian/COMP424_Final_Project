package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoMove;

import java.util.*;

public class AlphaBetaSearchDecisionMaker implements DecisionMaker {

    private PentagoBoardState boardState;
    private static final Integer MAXIMUM_DEPTH = 3;

    private HashMap<String, Double> evalLoopUpTable;

    private static final Long MAX_TIME = 1000000L;

    private long startTime;

    public AlphaBetaSearchDecisionMaker(PentagoBoardState state) {
        this.boardState = state;
        this.evalLoopUpTable = new HashMap<>();
    }

    @Override
    public PentagoMove makeDecision() {
        this.startTime = System.currentTimeMillis();
        PentagoMove move = maxValue(this.boardState, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).move;
        System.out.println("TIME SPENT: " + (System.currentTimeMillis() - startTime));
        return move;
    }

    private Piece getMyColor(PentagoBoardState state) {
        return state.getTurnPlayer() == 0 ? Piece.WHITE : Piece.BLACK;
    }

    private Piece getOpponentColor(PentagoBoardState state) {
        return state.getTurnPlayer() == 0 ? Piece.BLACK : Piece.WHITE;
    }

    private Node maxValue(PentagoBoardState state, int depth, double alpha, double beta) {
        if (state.gameOver() || depth >= AlphaBetaSearchDecisionMaker.MAXIMUM_DEPTH)
            return new Node(
                    null,
                    null,
                    Evaluation.eval(state.getBoard(), getOpponentColor(this.boardState), getMyColor(this.boardState))
            );

        ArrayList<PentagoMove> moves = state.getAllLegalMoves();
        HashSet<String> visitedBoard = new HashSet<>();

        // Keep info.
        PentagoMove bestMove = null;
        PentagoBoardState bestState = null;
        double bestEval = Double.NEGATIVE_INFINITY;

        // Start checking.
        for (PentagoMove move : moves) {

            if (System.currentTimeMillis() - this.startTime > AlphaBetaSearchDecisionMaker.MAX_TIME)
                break;

            PentagoBoardState clonedState = (PentagoBoardState) state.clone();
            clonedState.processMove(move);

            String str = clonedState.toString();

            if (visitedBoard.contains(str))
                continue;
            visitedBoard.add(str);

            Node result;

            if (this.evalLoopUpTable.containsKey(str))
                result = new Node(null, null, this.evalLoopUpTable.get(str));
            else
                result = minValue(clonedState, depth + 1, alpha, beta);

            this.evalLoopUpTable.put(clonedState.toString(), result.stateEvaluation);

            if (result.stateEvaluation > bestEval) {
                bestEval = result.stateEvaluation;
                bestState = clonedState;
                bestMove = move;
            }

            if (bestEval >= beta)
                return new Node(bestState, bestMove, bestEval);

            alpha = Math.max(alpha, bestEval);
        }

        return new Node(bestState, bestMove, bestEval);
    }

    private Node minValue(PentagoBoardState state, int depth, double alpha, double beta) {

        long startTime = System.currentTimeMillis();

        if (state.gameOver() || depth >= AlphaBetaSearchDecisionMaker.MAXIMUM_DEPTH)
            return new Node(
                    null,
                    null,
                    Evaluation.eval(state.getBoard(), getOpponentColor(this.boardState), getMyColor(this.boardState))
            );

        ArrayList<PentagoMove> moves = state.getAllLegalMoves();
        HashSet<String> visitedBoard = new HashSet<>();

        // Keep info.
        PentagoMove bestMove = null;
        PentagoBoardState bestState = null;
        double worstEval = Double.POSITIVE_INFINITY;

        // Start checking.
        for (PentagoMove move : moves) {

            if (System.currentTimeMillis() - this.startTime > AlphaBetaSearchDecisionMaker.MAX_TIME)
                break;

            PentagoBoardState clonedState = (PentagoBoardState) state.clone();
            clonedState.processMove(move);

            String str = clonedState.toString();

            if (visitedBoard.contains(str))
                continue;
            visitedBoard.add(str);

            Node result;

            if (this.evalLoopUpTable.containsKey(str))
                result = new Node(null, null, this.evalLoopUpTable.get(str));
            else
                result = maxValue(clonedState, depth + 1, alpha, beta);

            this.evalLoopUpTable.put(clonedState.toString(), result.stateEvaluation);

            if (result.stateEvaluation < worstEval) {
                worstEval = result.stateEvaluation;
                bestState = clonedState;
                bestMove = move;
            }

            if (worstEval <= alpha)
                return new Node(bestState, bestMove, worstEval);

            beta = Math.max(beta, worstEval);
        }

        return new Node(bestState, bestMove, worstEval);
    }

    public class Node {
        PentagoBoardState resultState;
        PentagoMove move;
        Double stateEvaluation;

        public Node(PentagoBoardState state, PentagoMove move, Double eval) {
            this.resultState = state;
            this.move = move;
            this.stateEvaluation = eval;
        }
    }
}
