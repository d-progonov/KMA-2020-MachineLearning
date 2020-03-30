import org.apache.commons.math3.distribution.BetaDistribution
import org.apache.commons.math3.distribution.LaplaceDistribution
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.TDistribution

import java.util.stream.IntStream

import static Distribution.*

class RandomVariable {

    final double expectedValue
    final double variance
    final double median
    final double interquartileRange
    final double skewness
    final double kurtosis
    final double[] histogram
    final double[] gaussHistogram
    final double[] laplaceHistogram
    final double[] studentHistogram
    final double[] betaHistogram
    final Distribution bestDistribution

    RandomVariable(int[] values, int min, int max) {
        Arrays.sort(values)
        expectedValue = computeExpectedValue(values)
        variance = computeVariance(values, expectedValue)
        median = computeMedian(values)
        interquartileRange = computeInterquartileRange(values)
        skewness = computeSkewness(values, expectedValue, variance)
        kurtosis = computeKurtosis(values, expectedValue, variance)
        histogram = computeHistogram(values, min, max)
        gaussHistogram = computeGaussHistogram(expectedValue, variance, min, max)
        laplaceHistogram = computeLaplaceHistogram(expectedValue, variance, min, max)
        studentHistogram = computeStudentHistogram(variance, min, max)
        betaHistogram = computeBetaHistogram(expectedValue, variance, min, max)
        bestDistribution = computeBestDistribution(
                histogram, gaussHistogram, laplaceHistogram, studentHistogram, betaHistogram
        )
    }

    private static double computeExpectedValue(int[] values) {
        Arrays
                .stream(values)
                .average()
                .asDouble
    }

    private static double computeVariance(int[] values, double expectedValue) {
        Arrays
                .stream(values)
                .mapToDouble(value -> value - expectedValue)
                .map(diff -> diff * diff)
                .average()
                .asDouble
    }

    private static double computeMedian(int[] values) {
        if (values.length % 2 == 0) {
            (values[values.length / 2 as int] + values[values.length / 2 - 1 as int]) / 2
        }

        values[values.length / 2 as int]
    }

    private static double computeInterquartileRange(int[] values) {
        if (values.length % 2 == 0) {
            (
                    values[values.length * 0.75 as int] + values[values.length * 0.75 - 1 as int] -
                            (values[values.length / 4 as int] + values[values.length / 4 - 1 as int])
            ) / 2
        }

        values[values.length * 0.75 as int] - values[values.length / 4 as int]
    }

    private static double computeSkewness(int[] values, double expectedValue, double variance) {
        Arrays
                .stream(values)
                .mapToDouble(value -> value - expectedValue)
                .map(value -> value * value * value)
                .average()
                .asDouble / Math.pow(variance, 1.5)
    }

    private static double computeKurtosis(int[] values, double expectedValue, double variance) {
        Arrays
                .stream(values)
                .mapToDouble(value -> value - expectedValue)
                .map(value -> value * value)
                .map(value -> value * value)
                .average()
                .asDouble / (variance * variance) - 3
    }

    private static double[] computeHistogram(int[] values, int min, int max) {
        double[] result = new double[max - min + 1]
        for (int value : values) {
            result[value - min]++
        }

        for (int i = 0; i < result.length; i++) {
            result[i] /= values.length
        }

        result
    }

    private static double[] computeGaussHistogram(double expectedValue, double variance, int min, int max) {
        double[] result = new double[max - min + 1]
        def distribution = new NormalDistribution(expectedValue, Math.sqrt(variance))
        for (int i = 0; i < result.length; i++) {
            result[i] = distribution.cumulativeProbability(i + min) - distribution.cumulativeProbability(i + min - 1)
        }

        result
    }

    private static double[] computeLaplaceHistogram(double expectedValue, double variance, int min, int max) {
        double[] result = new double[max - min + 1]
        def b = Math.sqrt(variance / 2)
        def distribution = new LaplaceDistribution(expectedValue, b)
        for (int i = 0; i < result.length; i++) {
            result[i] = distribution.cumulativeProbability(i + min) - distribution.cumulativeProbability(i + min - 1)
        }

        result
    }

    private static double[] computeStudentHistogram(double variance, int min, int max) {
        double[] result = new double[max - min + 1]
        def v = variance * 2 / (variance - 1)
        def distribution = new TDistribution(v)
        for (int i = 0; i < result.length; i++) {
            result[i] = distribution.cumulativeProbability(i + min) - distribution.cumulativeProbability(i + min - 1)
        }

        result
    }

    private static double[] computeBetaHistogram(double expectedValue, double variance, int min, int max) {
        double[] result = new double[max - min + 1]
        expectedValue /= 256
        variance /= 256 * 256
        def a = expectedValue * (expectedValue * (1 - expectedValue) / variance - 1)
        def b = a * (1 - expectedValue) / expectedValue
        def distribution = new BetaDistribution(a, b)
        for (int i = 0; i < result.length; i++) {
            result[i] = distribution.cumulativeProbability((i + min) / 256) -
                    distribution.cumulativeProbability((i + min - 1) / 256)
        }

        result
    }

    private static Distribution computeBestDistribution(
            double[] sourceHistogram,
            double[] gaussHistogram,
            double[] laplaceHistogram,
            double[] studentHistogram,
            double[] betaHistogram
    ) {
        def variances = [
                computeVariance(sourceHistogram, gaussHistogram),
                computeVariance(sourceHistogram, laplaceHistogram),
                computeVariance(sourceHistogram, studentHistogram),
                computeVariance(sourceHistogram, betaHistogram)
        ]

        of(variances.indexOf(variances.min()))
    }

    private static double computeVariance(double[] sourceHistogram, double[] approximatedHistogram) {
        IntStream
                .range(0, sourceHistogram.length)
                .mapToDouble(i -> sourceHistogram[i] - approximatedHistogram[i])
                .map(diff -> diff * diff)
                .average()
                .asDouble
    }

    @Override
    String toString() {
        "Expected value: $expectedValue\n" +
                "Variance: $variance\n" +
                "Median: $median\n" +
                "Interquartile Range: $interquartileRange\n" +
                "Skewness: $skewness\n" +
                "Kurtosis: $kurtosis\n" +
                "Histogram: $histogram\n" +
                "$GAUSS.name Histogram: $gaussHistogram\n" +
                "$LAPLACE.name Histogram: $laplaceHistogram\n" +
                "$STUDENT.name Histogram: $studentHistogram\n" +
                "$BETA.name Histogram: $betaHistogram"
    }
}
