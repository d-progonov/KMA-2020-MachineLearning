import java.util.function.Function
import java.util.stream.Collectors

enum Distribution {
    GAUSS(0, "Gauss"),
    LAPLACE(1, "Laplace"),
    STUDENT(2, "Student"),
    BETA(3, "Beta");

    final int index
    final String name

    Distribution(int index, String name) {
        this.index = index
        this.name = name
    }

    private static Map<Integer, Distribution> indexToDistributionMap = Arrays
            .stream(values())
            .collect(Collectors.toMap(Distribution::getIndex, Function.identity()))

    static Distribution of(int index) {
        indexToDistributionMap.get(index)
    }
}
