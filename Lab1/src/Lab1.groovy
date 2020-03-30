import org.apache.commons.math3.random.MersenneTwister

import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.IntStream

class Lab1 implements Runnable {

    private static final String COLLECTION_PATH = "/Users/igor/Downloads/mirflickr"
    private static final String OUTPUT_PATH = "/Users/igor/Downloads/output"
    private static final String IMAGE_PREFIX = "im"
    private static final String OUTPUT_FILE_PREFIX = "out_"
    private static final String IMAGE_EXTENSION = ".jpg"
    private static final String OUTPUT_FILE_EXTENSION = ".txt"
    private static final int COLLECTION_SIZE = 25_000
    private static final int SELECTION_SIZE = 250

    private final String[] imageFileNames

    Lab1() {
        def randomGenerator = new MersenneTwister(4)
        imageFileNames = IntStream
                .range(0, SELECTION_SIZE)
                .map(i -> randomGenerator.nextInt(COLLECTION_SIZE) + 1)
                .mapToObj(i -> IMAGE_PREFIX + i)
                .toArray() as String[]

        new File(OUTPUT_PATH).mkdirs()
    }

    @Override
    void run() {
        def randomVariables = Arrays
                .stream(imageFileNames)
                .map(imageFileName -> {
                    def pixels = ImageReader.readGreenChannel(new File(COLLECTION_PATH, imageFileName + IMAGE_EXTENSION))
                    def randomVariable = new RandomVariable(
                            Arrays
                                    .stream(pixels)
                                    .flatMapToInt(Arrays::stream)
                                    .toArray(),
                            0, 255
                    )

                    def outputFile = new File(OUTPUT_PATH, OUTPUT_FILE_PREFIX + imageFileName + OUTPUT_FILE_EXTENSION)
                    outputFile.delete()
                    outputFile.createNewFile()
                    outputFile << randomVariable

                    randomVariable
                })
                .collect(Collectors.toList())

        def distributionCounts = randomVariables
                .stream()
                .map(RandomVariable::getBestDistribution)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))

        def outputFile = new File(OUTPUT_PATH, OUTPUT_FILE_PREFIX + "best" + OUTPUT_FILE_EXTENSION)
        outputFile.delete()
        outputFile.createNewFile()
        outputFile << Arrays
                .stream(Distribution.values())
                .map(
                        distribution ->
                                "$distribution.name: ${distributionCounts.getOrDefault(distribution, 0)}"
                )
                .toArray()
                .join('\n')

        def vector1RandomVariable = new VectorRandomVariable(
                randomVariables
                        .stream()
                        .map(randomVariable -> new double[]{randomVariable.expectedValue})
                        .toArray() as double[][]
        )

        outputFile = new File(OUTPUT_PATH, OUTPUT_FILE_PREFIX + "vector1" + OUTPUT_FILE_EXTENSION)
        outputFile.delete()
        outputFile.createNewFile()
        outputFile << vector1RandomVariable

        def vector2RandomVariable = new VectorRandomVariable(
                randomVariables
                        .stream()
                        .map(randomVariable -> new double[]{
                                randomVariable.expectedValue, randomVariable.variance
                        })
                        .toArray() as double[][]
        )

        outputFile = new File(OUTPUT_PATH, OUTPUT_FILE_PREFIX + "vector2" + OUTPUT_FILE_EXTENSION)
        outputFile.delete()
        outputFile.createNewFile()
        outputFile << vector2RandomVariable

        def vector3RandomVariable = new VectorRandomVariable(
                randomVariables
                        .stream()
                        .map(randomVariable -> new double[]{
                                randomVariable.expectedValue, randomVariable.variance, randomVariable.skewness
                        })
                        .toArray() as double[][]
        )

        outputFile = new File(OUTPUT_PATH, OUTPUT_FILE_PREFIX + "vector3" + OUTPUT_FILE_EXTENSION)
        outputFile.delete()
        outputFile.createNewFile()
        outputFile << vector3RandomVariable

        def vector4RandomVariable = new VectorRandomVariable(
                randomVariables
                        .stream()
                        .map(randomVariable -> new double[]{
                                randomVariable.expectedValue, randomVariable.variance,
                                randomVariable.skewness, randomVariable.kurtosis
                        })
                        .toArray() as double[][]
        )

        outputFile = new File(OUTPUT_PATH, OUTPUT_FILE_PREFIX + "vector4" + OUTPUT_FILE_EXTENSION)
        outputFile.delete()
        outputFile.createNewFile()
        outputFile << vector4RandomVariable
    }
}
