package student_player;

import pentago_twist.PentagoBoardState.Piece;

import java.util.HashMap;
import java.util.Iterator;

public class Evaluation implements EvaluationTool {

    private static final int[] WEIGHT = {1000, 300, 100, 10, 1, 0, 0};

    public static double eval(Piece[][] board, Piece opponentColor, Piece myColor) {
        return MyMath.dotProduct(crudeCountTotalMTW(board, opponentColor), Evaluation.WEIGHT);
    }

    /**
     * Count how many pieces need to win in a row.
     *
     * @param row           A row.
     * @param opponentColor Opponent's Color.
     * @return Minimum steps to win.
     */
    private static int countRowMTW(Object[] row, Piece opponentColor) {
        int minMTW = 5, numOfWhite = 0, prev = -1, numOfMyColor = 0;

        // Fetch my color.
        Piece myColor = null;
        switch (opponentColor) {
            case WHITE:
                myColor = Piece.BLACK;
                break;
            case BLACK:
                myColor = Piece.WHITE;
                break;
            default:
                myColor = Piece.EMPTY;
        }

        for (int i = 0; i < row.length; i++) {
            Piece piece = (Piece) row[i];
            if (piece.equals(opponentColor)) {
                if (prev == -1 && i >= 5)
                    minMTW = Math.min(minMTW, numOfWhite);
                else if (prev != -1 && i - prev - 1 >= 5)
                    minMTW = Math.min(minMTW, numOfWhite);
                prev = i;
                numOfWhite = 0;
            } else if (piece.equals(Piece.EMPTY))
                numOfWhite++;
            else
                numOfMyColor++;
        }

        if (prev == -1)
            return Math.min(minMTW, row.length - numOfMyColor);

        return minMTW;
    }

    /**
     * Get the distribution of moves to win
     * in the board game.
     *
     * @param board         A board.
     * @param opponentColor Opponent color.
     * @return A distribution.
     */
    private static int[] crudeCountTotalMTW(Piece[][] board, Piece opponentColor) {
        int[] total = new int[7];

        // Row.
        for (int i = 0; i < board.length; i++)
            total[countRowMTW(MyMath.fetchRow(board, i), opponentColor)]++;

        // Column.
        for (int i = 0; i < board[0].length; i++)
            total[countRowMTW(MyMath.fetchColumn(board, i), opponentColor)]++;

        // Right diagonal.
        total[countRowMTW(MyMath.fetchRightDiagonal(board, 1, 0), opponentColor)]++;
        total[countRowMTW(MyMath.fetchRightDiagonal(board, 0, 0), opponentColor)]++;
        total[countRowMTW(MyMath.fetchRightDiagonal(board, 0, 1), opponentColor)]++;

        // Left diagonal.
        total[countRowMTW(MyMath.fetchLeftDiagonal(board, 4, 0), opponentColor)]++;
        total[countRowMTW(MyMath.fetchLeftDiagonal(board, 5, 0), opponentColor)]++;
        total[countRowMTW(MyMath.fetchLeftDiagonal(board, 5, 1), opponentColor)]++;

        return total;
    }
}
