package dev.drawethree.xprison.utils.expression;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Map;

public class ExpressionUtils {

    public static Expression createExpression(String expression, Map<String, Double> variables) {
        ExpressionBuilder builder = new ExpressionBuilder(expression);

        builder.variables(variables.keySet());

        Expression expr = builder.build();

        for (Map.Entry<String, Double> entry : variables.entrySet()) {
            expr.setVariable(entry.getKey(), entry.getValue());
        }

        return expr;
    }

    public static Expression createExpressionWithSingleVariable(String expression, String variableName, double value) {
        return new ExpressionBuilder(expression)
                .variable(variableName)
                .build()
                .setVariable(variableName, value);
    }
}