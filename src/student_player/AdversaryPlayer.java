package student_player;

import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;
import pentago_twist.PentagoPlayer;

import java.util.Random;

public class AdversaryPlayer extends PentagoPlayer {

    private Random rand = new Random(2019);
    private MonteCarloDecisionMaker.Node root = null;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public AdversaryPlayer() {
        super("260917301");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {

        // Opening strategies.
        if (boardState.getTurnNumber() < 2) {
            int[][] midList = {
                    {1, 1},
                    {1, 4},
                    {4, 1},
                    {4, 4}
            };
            for (int[] point : midList)
                if (boardState.getPieceAt(point[0], point[1]) == PentagoBoardState.Piece.EMPTY)
                    return new PentagoMove(point[0], point[1], rand.nextInt(4), rand.nextInt(2), boardState.getTurnPlayer());
        }

        AlphaBetaSearchDecisionMaker maker = new AlphaBetaSearchDecisionMaker(boardState);
        PentagoMove decision = maker.makeDecision();
//        MonteCarloDecisionMaker maker = new MonteCarloDecisionMaker(boardState);
//
//        // Reuse the tree from last iteration.
//        Node newRoot = maker.getRoot();
//        boolean found = false;
//        if (this.root != null)
//            for (Node opponent : newRoot.children) {
//                for (Node candidate : opponent.children)
//                    if (boardEquals(candidate.state, boardState)) {
//                        newRoot = candidate;
//                        found = true;
//                        break;
//                    }
//                if (found)
//                    break;
//            }
//        maker.setRoot(newRoot);
//        PentagoMove decision = maker.makeDecision();
//
//        // Save root.
//        this.root = maker.getRoot();

        return decision;
    }

    /**
     * Check if two board is the same.
     *
     * @param b1 A game.
     * @param b2 A game.
     * @return True for same.
     */
    private boolean boardEquals(PentagoBoardState b1, PentagoBoardState b2) {
        PentagoBoardState.Piece[][] board1 = b1.getBoard(), board2 = b2.getBoard();
        int height = board1.length, width = board1[0].length;
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                if (board1[i][j] != board2[i][j])
                    return false;
        return true;
    }

}
