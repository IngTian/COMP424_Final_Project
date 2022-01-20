package student_player;

import java.util.ArrayList;

public class MyMath {

    /**
     * Find the dot product between two vectors.
     *
     * @param a A vector.
     * @param b A vector.
     * @return Their dot product.
     */
    public static int dotProduct(int[] a, int[] b) {
        int total = 0, size = Math.min(a.length, b.length);
        for (int i = 0; i < size; i++)
            total += a[i] * b[i];
        return total;
    }

    /**
     * Fetch a column of data from
     * a matrix.
     *
     * @param matrix A matrix.
     * @param column Column Number.
     * @param <E>    A generic type.
     * @return A column.
     */
    public static <E> E[] fetchColumn(E[][] matrix, int column) {
        int height = matrix.length;
        ArrayList<E> result = new ArrayList<>();
        for (E[] es : matrix) result.add(es[column]);
        return (E[]) result.toArray();
    }

    /**
     * Fetch a row of data
     * from a matrix.
     *
     * @param matrix A matrix.
     * @param row    Row number.
     * @param <E>    A generic type.
     * @return A row.
     */
    public static <E> E[] fetchRow(E[][] matrix, int row) {
        return matrix[row];
    }

    public static <E> E[] fetchRightDiagonal(E[][] matrix, int startRow, int startCol) {
        int r = startRow, c = startCol, width = getWidth(matrix), height = getHeight(matrix);
        ArrayList<E> result = new ArrayList<>();
        while (c < width && r < height)
            result.add(matrix[r++][c++]);
        return (E[]) result.toArray();
    }

    /**
     * Fetch the left diagonal
     * from a matrix.
     *
     * @param matrix   A matrix.
     * @param startRow Row coordinate.
     * @param startCol Column coordinate.
     * @param <E>      A generic type.
     * @return An array of data.
     */
    public static <E> E[] fetchLeftDiagonal(E[][] matrix, int startRow, int startCol) {
        int r = startRow, c = startCol, width = getWidth(matrix), height = getHeight(matrix);
        ArrayList<E> result = new ArrayList<>();
        while (c < width && r >= 0)
            result.add(matrix[r--][c++]);
        return (E[]) result.toArray();
    }

    public static <E> int getWidth(E[][] matrix) {
        return matrix[0].length;
    }

    public static <E> int getHeight(E[][] matrix) {
        return matrix.length;
    }
}
