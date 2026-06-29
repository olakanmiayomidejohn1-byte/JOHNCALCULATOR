package com.john.johncalculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var tvExpression: TextView
    private lateinit var tvResult: TextView
    private var canAddOperation = false
    private var canAddDecimal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvExpression = findViewById(R.id.tv_expression)
        tvResult = findViewById(R.id.tv_result)

        setupButtons()
    }

    private fun setupButtons() {
        // Numbers
        val numberButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        )
        for (id in numberButtons) {
            findViewById<Button>(id).setOnClickListener { appendText((it as Button).text.toString()) }
        }

        // Basic Operators
        findViewById<Button>(R.id.btn_add).setOnClickListener { appendText("+") }
        findViewById<Button>(R.id.btn_sub).setOnClickListener { appendText("-") }
        findViewById<Button>(R.id.btn_mul).setOnClickListener { appendText("×") }
        findViewById<Button>(R.id.btn_div).setOnClickListener { appendText("÷") }

        // Parentheses and Decimals
        findViewById<Button>(R.id.btn_open).setOnClickListener { appendText("(") }
        findViewById<Button>(R.id.btn_close).setOnClickListener { appendText(")") }
        findViewById<Button>(R.id.btn_dot).setOnClickListener { appendText(".") }

        // Scientific Operators
        findViewById<Button>(R.id.btn_sin).setOnClickListener { appendText("sin(") }
        findViewById<Button>(R.id.btn_cos).setOnClickListener { appendText("cos(") }
        findViewById<Button>(R.id.btn_tan).setOnClickListener { appendText("tan(") }
        findViewById<Button>(R.id.btn_log).setOnClickListener { appendText("log(") }
        findViewById<Button>(R.id.btn_ln).setOnClickListener { appendText("ln(") }
        findViewById<Button>(R.id.btn_sqrt).setOnClickListener { appendText("sqrt(") }
        findViewById<Button>(R.id.btn_power).setOnClickListener { appendText("^") }
        findViewById<Button>(R.id.btn_fact).setOnClickListener { appendText("!") }
        findViewById<Button>(R.id.btn_npr).setOnClickListener { appendText("P") }
        findViewById<Button>(R.id.btn_ncr).setOnClickListener { appendText("C") }

        // Actions
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            tvExpression.text = ""
            tvResult.text = "0"
        }

        findViewById<Button>(R.id.btn_del).setOnClickListener {
            val currentText = tvExpression.text.toString()
            if (currentText.isNotEmpty()) {
                tvExpression.text = currentText.substring(0, currentText.length - 1)
            }
        }

        findViewById<Button>(R.id.btn_eq).setOnClickListener {
            try {
                val expression = tvExpression.text.toString()
                val result = evaluate(expression)

                // Format the result to remove trailing zeros (e.g., 5.0 becomes 5)
                val format = DecimalFormat("0.######")
                tvResult.text = format.format(result)
            } catch (e: Exception) {
                tvResult.text = "Error"
            }
        }
    }

    private fun appendText(str: String) {
        tvExpression.append(str)
    }

    // ==========================================
    // THE MATH ENGINE (Recursive Descent Parser)
    // ==========================================
    private fun evaluate(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code) || eat('×'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code) || eat('÷'.code)) x /= parseFactor() // division
                    else if (eat('P'.code)) x = nPr(x, parseFactor()) // Permutation
                    else if (eat('C'.code)) x = nCr(x, parseFactor()) // Combination
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = str.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> Math.sqrt(x)
                        "sin" -> Math.sin(Math.toRadians(x)) // Handled in degrees for standard calculators
                        "cos" -> Math.cos(Math.toRadians(x))
                        "tan" -> Math.tan(Math.toRadians(x))
                        "log" -> Math.log10(x)
                        "ln" -> Math.log(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = Math.pow(x, parseFactor()) // exponentiation
                if (eat('!'.code)) x = factorial(x) // factorial

                return x
            }
        }.parse()
    }

    // Helper Functions for Advanced Math
    private fun factorial(n: Double): Double {
        if (n < 0 || n % 1 != 0.0) throw IllegalArgumentException("Invalid Factorial")
        var res = 1.0
        for (i in 2..n.toInt()) res *= i
        return res
    }

    private fun nPr(n: Double, r: Double): Double {
        return factorial(n) / factorial(n - r)
    }

    private fun nCr(n: Double, r: Double): Double {
        return factorial(n) / (factorial(r) * factorial(n - r))
    }
}