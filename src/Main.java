import Parser.Node;
import Parser.Parser;
import Parser.Operator;
import Parser.Variable;
import Parser.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Parser parser = new Parser("(2.0^x1)");
        Node node = parser.parse();
        System.out.println(((Operator) node).getType());
        for (Node nod : node.getChildren()) {
            System.out.println(nod);
        }
        Map<String, Double> values = new HashMap<>();
        values.put("x1", 4.0);
        try {
            System.out.println(compute(node, values));
        } catch (InvalidVariableException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Double compute(Node expression, Map<String, Double> values) throws InvalidVariableException {
        if (expression instanceof Variable) {
            String variableName = ((Variable) expression).getName();
            Double result = values.get(variableName);
            if (result == null) {
                throw new InvalidVariableException(String.format("Variable %s is not defined in the variables list.", variableName));
            } else {
                return result;
            }
        }

        if (expression instanceof Constant) {
            return ((Constant) expression).getValue();
        }

        if (expression instanceof Operator) {
            Function<List<Double>, Double> function = ((Operator) expression).getType().getFunction();
            List<Double> children = new ArrayList<>();
            for (Node child : expression.getChildren()) {
                children.add(compute(child, values));
            }
            return function.apply(children);
        }

        throw new RuntimeException("Unknown Node instance"); //TODO: fix this shit
    }
}