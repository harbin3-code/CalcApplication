package com.example.testprogram.calcapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Stack;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText etEquation;
    TextView tvResult;
    EquationCalculator equationCalculator;

    public void btnCalculateClick(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        try {
            equationCalculator.parse(etEquation.getText().toString());
            double res = equationCalculator.calc();
            tvResult.setText(String.valueOf(res));
        } catch (ArithmeticException e) {
            tvResult.setText("Ошибка!");
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static class EquationCalculator {
        final static String U_MINUS = "~";
        final static String OPERATORS = "+-*/" + U_MINUS;
        final static String DELIMITERS = "()" + OPERATORS;
        final static String ALLOWED_CHARS = "0123456789.";

        private java.util.List<String> lstParsed;

        private boolean isDelimiter(String token) {
            return token.length() == 1 && DELIMITERS.indexOf(token) >= 0;
        }

        private boolean isOperator(String token) {
            return token.length() == 1 && OPERATORS.indexOf(token) >= 0;
        }

        private boolean isAllowedChar(String token) {
            return token.length() == 1 && ALLOWED_CHARS.indexOf(token) >= 0;
        }

        private int priority(String token) {
            if (token.equals("("))
                return 1;
            else if (token.equals("+") || token.equals("-"))
                return 2;
            else if (token.equals("*") || token.equals("/"))
                return 3;
            else
                return 4;
        }

        public void parse(String equationStr) {
            Stack<String> 	stack = new Stack<String>();

            String	prev = "",
                    curr = "",
                    sourceStr = equationStr.replace(" ", "");
            Integer i = 0,
                    maxI = sourceStr.length();

            lstParsed = new ArrayList<String>();

            while (i < maxI) {

                curr = "";
                if ( isDelimiter(sourceStr.substring(i, i+1)) ) {
                    curr = sourceStr.substring(i, i+1);
                    i++;
                } else {
                    while ( !isDelimiter(sourceStr.substring(i, i+1)) ) {

                        if ( isAllowedChar(sourceStr.substring(i, i+1)) ) {
                            curr = curr + sourceStr.substring(i, i+1);
                            i++;
                        } else {
                            throw new ArithmeticException("Недопустимый символ: " + sourceStr.substring(i, i+1));
                        }

                        if (i == maxI)
                            break;
                    }
                }

                if (i >= maxI && isOperator(curr))
                    throw new ArithmeticException("Ошибка в формуле");


                if (isDelimiter(curr)) {
                    if (curr.equals("(")) {
                        if (prev.equals("") || prev.equals("(") || isOperator(prev) || prev.equals(U_MINUS))
                            stack.push(curr);
                        else
                            throw new ArithmeticException("Левая скобка допускается вначале формулы, после оператора или левой скобки.");
                    } else if (curr.equals(")")) {
                        while (!stack.peek().equals("(")) {
                            lstParsed.add(stack.pop());
                            if (stack.isEmpty())
                                throw new ArithmeticException("Пропущена левая скобка.");
                        }
                        stack.pop();
                    } else {
                        if (curr.equals("-") && (prev.equals("") || prev.equals("("))) {
                            // unary minus
                            curr = U_MINUS;
                        } else if (curr.equals("-") && isOperator(prev)) {
                            throw new ArithmeticException("Нужны скобки.");
                        } else if (isOperator(curr) && (isOperator(prev) || prev.equals("("))) {
                            throw new ArithmeticException("Ошибка в формуле.");
                        } else {
                            while (!stack.isEmpty() && (priority(curr) <= priority(stack.peek())))
                                lstParsed.add(stack.pop());
                        }
                        stack.push(curr);
                    }

                } else {
                    lstParsed.add(curr);
                }
                prev = curr;
            }

            while (!stack.isEmpty()) {
                if (isOperator(stack.peek()))
                    lstParsed.add(stack.pop());
                else
                    throw new ArithmeticException("Пропущена правая скобка.");

            }
        }

        public Double calc() {
            Stack<Double> stack = new Stack<Double>();
            for (String x : lstParsed) {
                if (x.equals("+")) {
                    stack.push(stack.pop() + stack.pop());
                } else if (x.equals("-")) {
                    Double b = stack.pop(), a = stack.pop();
                    stack.push(a - b);
                } else if (x.equals("*")) {
                    stack.push(stack.pop() * stack.pop());
                } else if (x.equals("/")) {
                    Double b = stack.pop(), a = stack.pop();

                    if (b == 0.0)
                        throw new ArithmeticException("Деление на ноль.");

                    stack.push(a / b);
                } else if (x.equals(EquationCalculator.U_MINUS)) {
                    stack.push(-stack.pop());
                } else {
                    stack.push(Double.parseDouble(x));
                }
            }

            return stack.pop();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEquation = findViewById(R.id.etEquation);
        tvResult = findViewById(R.id.tvResult);

        equationCalculator = new EquationCalculator();
    }
}
