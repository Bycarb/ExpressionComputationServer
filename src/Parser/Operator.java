package Parser;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Operator extends Node {

    public enum Type {
        SUM('+', a -> a.get(0) + a.get(1)),
        SUBTRACTION('-', a -> a.get(0) - a.get(1)),
        MULTIPLICATION('*', a -> a.get(0) * a.get(1)),
        DIVISION('/', a -> a.get(0) / a.get(1)),
        POWER('^', a -> Math.pow(a.get(0), a.get(1)));
        private final char symbol;
        private final Function<List<Double>, Double> function;

        Type(char symbol, Function<List<Double>, Double> function) {
            this.symbol = symbol;
            this.function = function;
        }

        public char getSymbol() {
            return symbol;
        }

        public Function<List<Double>, Double> getFunction() {
            return function;
        }
    }

    private final Type type;

    public Operator(Type type, List<Node> children) {
        super(children);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operator operator = (Operator) o;
        return type == operator.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        String sb = "(" +
                getChildren().stream()
                        .map(Node::toString)
                        .collect(Collectors.joining(" " + Character.toString(type.symbol) + " "))
                + ")";
        return sb;
    }
}
