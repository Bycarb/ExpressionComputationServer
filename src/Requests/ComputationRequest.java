package Requests;

import Requests.Computation.ComputationKind;
import Requests.Computation.Expression.*;
import Requests.Computation.ValuesKind;
import Requests.Computation.ValuesParser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComputationRequest implements Request {

    private final ComputationKind computationKind;
    private final List<Node> expressions;
    private final ValuesParser tuplesParser; //differently from expressions, the tuples list is not evaluated in the constructor because it can be computationally heavy

    public ComputationRequest(String request) throws IllegalArgumentException {

        String[] tokens = request.split(";");
        if (tokens.length < 3) {
            throw new IllegalArgumentException("Invalid number of arguments: missing computation request, variable values, or Expressions. " +
                    "Usage: CamputationRequest; [VariableValuesFunction]; Expression1; Expression2;...");
        }

        String[] computationRequestType = tokens[0].split("_");
        if (computationRequestType.length != 2) {
            throw new IllegalArgumentException("Invalid number of arguments for computation request. " +
                    "Usage: ComputationKind_ValuesKind");
        }

        this.computationKind = switch (computationRequestType[0]) {
            case "MIN" -> ComputationKind.MIN;
            case "MAX" -> ComputationKind.MAX;
            case "AVG" -> ComputationKind.AVG;
            case "COUNT" -> ComputationKind.COUNT;
            default ->
                    throw new IllegalArgumentException("Invalid computation kind. Accepted computation kinds are: MIN,MAX,AVG,COUNT");
        };

        ValuesKind valuesKind = switch (computationRequestType[1]) {
            case "GRID" -> ValuesKind.GRID;
            case "LIST" -> ValuesKind.LIST;
            default -> throw new IllegalArgumentException("Invalid value kind. Accepted value kinds are: LIST, GRID");
        };

        tuplesParser = new ValuesParser(tokens[1], valuesKind);

        expressions = Arrays.stream(tokens, 2, tokens.length)
                .map(token -> (new ExpressionParser(token)).parseExpression())
                .toList();
    }


    @Override
    public String call() {
        List<Map<String, Double>> tuples = (tuplesParser).getTuples();

        if (computationKind == ComputationKind.MIN) {
            if (tuples.size() == 0) {
                throw new IllegalArgumentException("Impossible to calculate MIN. The provided intervals generated zero valid tuples of values.");
            }
            Double result = null;
            for (Map<String, Double> tuple : tuples) {
                for (Node expression : expressions) {
                    Double tmpResult = compute(expression, tuple);
                    if (result == null || tmpResult < result) {
                        result = tmpResult;
                    }
                }
            }
            return String.format("%.6f", result);
        }
        if (computationKind == ComputationKind.MAX) {
            if (tuples.size() == 0) {
                throw new IllegalArgumentException("Impossible to calculate MAX. The provided intervals generated zero valid tuples of values.");
            }
            Double result = null;
            for (Map<String, Double> tuple : tuples) {
                for (Node expression : expressions) {
                    Double tmpResult = compute(expression, tuple);
                    if (result == null || tmpResult > result) {
                        result = tmpResult;
                    }
                }
            }
            return String.format("%.6f", result);
        }
        if (computationKind == ComputationKind.AVG) {
            //NOTE: following specifications, AVG calculates the average only for the first expression
            if (expressions.size() == 0) {
                throw new IllegalArgumentException("Impossible to calculate AVG. No valid expression provided.");
            }
            if (tuples.size() == 0) {
                throw new IllegalArgumentException("Impossible to calculate AVG. The provided intervals generated zero valid tuples of values.");
            }
            double result = 0d;
            for (Map<String, Double> tuple : tuples) {
                Double tmpResult = compute(expressions.get(0), tuple);
                result += tmpResult / tuples.size();
            }
            return String.format("%.6f", result);
        }
        if (computationKind == ComputationKind.COUNT) {
            return String.format("%.6f", (double) tuples.size());
        }

        throw new EnumConstantNotPresentException(computationKind.getClass(), computationKind.toString());
    }

    private static Double compute(Node expression, Map<String, Double> tuple) throws IllegalArgumentException {
        if (expression instanceof Variable) {
            String variableName = ((Variable) expression).getName();
            Double result = tuple.get(variableName);
            if (result == null) {
                throw new IllegalArgumentException(String.format("Variable %s is not defined in the variables list.", variableName));
            } else {
                return result;
            }
        }

        if (expression instanceof Constant) {
            return ((Constant) expression).getValue();
        }

        if (expression instanceof Operator) {
            Function<List<Double>, Double> function = ((Operator) expression).getType().getFunction();
            List<Double> childrenValue = expression.getChildren()
                    .stream()
                    .map(child -> compute(child, tuple))
                    .toList();
            Double result = function.apply(childrenValue);
            if (result.isNaN()
                    || result.isInfinite()
                    || result.equals(Double.MIN_VALUE)
                    || result.equals(Double.MAX_VALUE)) {
                throw new ArithmeticException("Arithmetic error computing " + expression
                        + " for values:" + tuple.entrySet().stream().map(e -> "<" + e.getKey() + " = " + e.getValue() + ">").collect(Collectors.joining()));
            }
            return result;
        }
        //Should never reach this point
        throw new UnsupportedOperationException("Unknown Node instance");
    }
}
