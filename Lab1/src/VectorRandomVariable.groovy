import java.util.stream.IntStream

class VectorRandomVariable {

    final double[] expectedValue
    final double[][] variance

    VectorRandomVariable(double[][] values) {
        expectedValue = computeExpectedValue(values)
        variance = computeVariance(values, expectedValue)
    }

    private static double[] computeExpectedValue(double[][] values) {
        IntStream
                .range(0, values[0].length)
                .mapToDouble(i -> {
                    IntStream
                            .range(0, values.length)
                            .mapToDouble(j -> values[j][i])
                            .average()
                            .asDouble
                })
                .toArray()
    }

    private static double[][] computeVariance(double[][] values, double[] expectedValue) {
        double[][][] squares = Arrays
                .stream(values)
                .map(
                        vector -> IntStream
                                .range(0, expectedValue.length)
                                .mapToDouble(i -> vector[i] - expectedValue[i])
                                .toArray()
                )
                .map(diff -> {
                    double[][] square = new double[expectedValue.length][expectedValue.length]
                    for (int i = 0; i < expectedValue.length; i++) {
                        for (int j = i; j < expectedValue.length; j++) {
                            square[i][j] = square[j][i] = diff[i] * diff[j]
                        }
                    }

                    square
                })
                .toArray() as double[][][]

        double[][] result = new double[expectedValue.length][expectedValue.length]
        for (int i = 0; i < expectedValue.length; i++) {
            for (int j = i; j < expectedValue.length; j++) {
                result[i][j] = result[j][i] = Arrays
                        .stream(squares)
                        .mapToDouble(square -> square[i][j])
                        .average()
                        .asDouble
            }
        }

        result
    }

    @Override
    String toString() {
        "Expected value: $expectedValue\nVariance: $variance"
    }
}
