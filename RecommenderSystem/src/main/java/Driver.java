/**
 * Driver to run the recommender system on Hadoop
 */

public class Driver {
    public static void main(String[] args) throws Exception {
        DivideDataByUserID divideDataByUserID = new DivideDataByUserID();
        CooccurrenceMatrix cooccurrenceMatrix = new CooccurrenceMatrix();
        Normalization normalization = new Normalization();
        MatrixMultiplication matrixMultiplication = new MatrixMultiplication();
        MatrixSum matrixSum = new MatrixSum();

        // get the input and out path for each class
        String rawInputPath = args[0];
        String userMoveListPath = args[1];
        String cooccurrenceMatrixPath = args[2];
        String normalizedMatrixPath = args[3];
        String rawMatrixMultiplicationPath = args[4];
        String matrixMultiplicationSumPath = args[5];

        // build the input path
        String[] path1 = {rawInputPath, userMoveListPath};
        String[] path2 = {userMoveListPath, cooccurrenceMatrixPath};
        String[] path3 = {cooccurrenceMatrixPath, normalizedMatrixPath};
        String[] path4 = {normalizedMatrixPath, rawInputPath, rawMatrixMultiplicationPath};
        String[] path5 = {rawMatrixMultiplicationPath, matrixMultiplicationSumPath};

        // run recommender system
        divideDataByUserID.main(path1);
        cooccurrenceMatrix.main(path2);
        normalization.main(path3);
        matrixMultiplication.main(path4);
        matrixSum.main(path5);

    }

}
