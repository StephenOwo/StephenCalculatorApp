package com.sen104.stephencalculatorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay, tvHistory;
    private final StringBuilder expression = new StringBuilder();
    private boolean lastIsResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);

        setClickListeners();
    }

    private void setClickListeners() {
        // Digits
        int[] digitIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int i = 0; i <= 9; i++) {
            final String digit = String.valueOf(i);
            findViewById(digitIds[i]).setOnClickListener(v -> appendToExpression(digit));
        }

        findViewById(R.id.btnDecimal).setOnClickListener(v -> appendToExpression("."));
        findViewById(R.id.btnClear).setOnClickListener(v -> onClear());
        findViewById(R.id.btnDelete).setOnClickListener(v -> onDelete());
        findViewById(R.id.btnPercent).setOnClickListener(v -> appendToExpression("%"));
        findViewById(R.id.btnPlusMinus).setOnClickListener(v -> onPlusMinus());

        findViewById(R.id.btnAdd).setOnClickListener(v -> appendToExpression("+"));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> appendToExpression("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> appendToExpression("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> appendToExpression("÷"));

        findViewById(R.id.btnSin).setOnClickListener(v -> appendToExpression("sin("));
        findViewById(R.id.btnCos).setOnClickListener(v -> appendToExpression("cos("));
        findViewById(R.id.btnTan).setOnClickListener(v -> appendToExpression("tan("));
        findViewById(R.id.btnLog).setOnClickListener(v -> appendToExpression("log("));
        findViewById(R.id.btnLn).setOnClickListener(v -> appendToExpression("ln("));
        findViewById(R.id.btnSqrt).setOnClickListener(v -> appendToExpression("√("));
        findViewById(R.id.btnSq).setOnClickListener(v -> appendToExpression("^2"));
        findViewById(R.id.btnPow).setOnClickListener(v -> appendToExpression("^"));
        findViewById(R.id.btnOpenParen).setOnClickListener(v -> appendToExpression("("));
        findViewById(R.id.btnCloseParen).setOnClickListener(v -> appendToExpression(")"));
        findViewById(R.id.btnPi).setOnClickListener(v -> appendToExpression("π"));
        findViewById(R.id.btnE).setOnClickListener(v -> appendToExpression("e"));
        findViewById(R.id.btnInv).setOnClickListener(v -> appendToExpression("1/("));
        findViewById(R.id.btnFact).setOnClickListener(v -> appendToExpression("!"));

        findViewById(R.id.btnEquals).setOnClickListener(v -> onEquals());
    }

    private void updateDisplay() {
        String currentExpr = expression.toString();
        tvDisplay.setText(currentExpr.isEmpty() ? "0" : currentExpr);
        
        // Optional: show real-time result in history view
        if (!currentExpr.isEmpty() && !lastIsResult) {
            try {
                double result = evaluate(currentExpr);
                if (!Double.isNaN(result)) {
                    tvHistory.setText(formatResult(result));
                } else {
                    tvHistory.setText("");
                }
            } catch (Exception e) {
                tvHistory.setText("");
            }
        } else if (currentExpr.isEmpty()) {
            tvHistory.setText("");
        }
    }

    private void appendToExpression(String str) {
        if (lastIsResult) {
            if (isOperator(str) || str.equals("%") || str.equals("!")) {
                // Continue with result
            } else {
                expression.setLength(0);
            }
            lastIsResult = false;
        }

        if (expression.length() == 0) {
            if (str.equals("×") || str.equals("÷") || str.equals("%") || str.equals("!") || str.equals("^")) {
                return;
            }
        } else {
            String current = expression.toString();
            char last = current.charAt(current.length() - 1);
            if (isOperator(str) && isOperator(String.valueOf(last))) {
                expression.deleteCharAt(expression.length() - 1);
            }
        }

        expression.append(str);
        updateDisplay();
    }

    private boolean isOperator(String s) {
        return s.equals("+") || s.equals("-") || s.equals("×") || s.equals("÷") || s.equals("^");
    }

    private void onClear() {
        expression.setLength(0);
        tvHistory.setText("");
        lastIsResult = false;
        updateDisplay();
    }

    private void onDelete() {
        int len = expression.length();
        if (len > 0) {
            String s = expression.toString();
            if (s.endsWith("sin(") || s.endsWith("cos(") || s.endsWith("tan(") || s.endsWith("log(")) {
                expression.delete(len - 4, len);
            } else if (s.endsWith("ln(") || s.endsWith("√(")) {
                expression.delete(len - 3, len);
            } else {
                expression.deleteCharAt(len - 1);
            }
        }
        lastIsResult = false;
        updateDisplay();
    }

    private void onPlusMinus() {
        if (expression.length() > 0) {
            String s = expression.toString();
            if (s.startsWith("-(")) {
                expression.delete(0, 2);
                if (expression.length() > 0 && expression.charAt(expression.length() - 1) == ')') {
                    expression.deleteCharAt(expression.length() - 1);
                }
            } else {
                expression.insert(0, "-(");
                expression.append(")");
            }
        }
        updateDisplay();
    }

    private void onEquals() {
        if (expression.length() == 0) return;
        try {
            double result = evaluate(expression.toString());
            if (Double.isNaN(result)) {
                tvDisplay.setText(R.string.error_msg);
                expression.setLength(0);
            } else {
                tvHistory.setText(String.format(Locale.US, "%s =", expression));
                String resStr = formatResult(result);
                expression.setLength(0);
                expression.append(resStr);
                tvDisplay.setText(resStr);
                lastIsResult = true;
            }
        } catch (Exception e) {
            tvDisplay.setText(R.string.error_msg);
            expression.setLength(0);
            lastIsResult = true;
        }
    }

    private String formatResult(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) return "Error";
        if (d == (long) d) {
            return String.valueOf((long) d);
        } else {
            String s = String.format(Locale.US, "%.10f", d);
            return s.contains(".") ? s.replaceAll("0*$", "").replaceAll("\\.$", "") : s;
        }
    }

    private double evaluate(String expr) {
        String cleanExpr = expr.replace("×", "*")
                .replace("÷", "/")
                .replace("π", String.valueOf(Math.PI))
                .replace("e", String.valueOf(Math.E))
                .replace("√", "sqrt");
        
        int openCount = 0;
        for (char c : cleanExpr.toCharArray()) {
            if (c == '(') openCount++;
            else if (c == ')') openCount--;
        }
        StringBuilder sb = new StringBuilder(cleanExpr);
        while (openCount > 0) {
            sb.append(")");
            openCount--;
        }
        return eval(sb.toString());
    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;
            void nextChar() { ch = (++pos < str.length()) ? str.charAt(pos) : -1; }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) return Double.NaN;
                return x;
            }
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) return Double.NaN;
                        x /= divisor;
                    } else return x;
                }
            }
            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();
                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sqrt": x = Math.sqrt(x); break;
                        case "sin": x = Math.sin(Math.toRadians(x)); break;
                        case "cos": x = Math.cos(Math.toRadians(x)); break;
                        case "tan": x = Math.tan(Math.toRadians(x)); break;
                        case "log": x = Math.log10(x); break;
                        case "ln": x = Math.log(x); break;
                        default: return Double.NaN;
                    }
                } else { return Double.NaN; }
                if (eat('^')) x = Math.pow(x, parseFactor());
                if (eat('%')) x = x / 100;
                if (eat('!')) x = factorial((int) x);
                return x;
            }
            double factorial(int n) {
                if (n < 0 || n > 170) return Double.NaN;
                double fact = 1;
                for (int i = 2; i <= n; i++) fact *= i;
                return fact;
            }
        }.parse();
    }
}