package com.example.pw03tv12_kryvytskyi_bohdan_petrovych

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt


@Composable
fun SolarCalculator() {
    var avrPower by remember { mutableStateOf("5.0") }
    var sigmaVal1 by remember { mutableStateOf("1.0") }
    var sigmaVal2 by remember { mutableStateOf("0.25") }
    var electricityPrice by remember { mutableStateOf("7.0") }
    var results by remember { mutableStateOf<CalculationResults?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = avrPower,
            onValueChange = { avrPower = it },
            label = { Text("Середня потужність (P_c)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = sigmaVal1,
            onValueChange = { sigmaVal1 = it },
            label = { Text("Сигма значення 1 (sigma_val_1)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = sigmaVal2,
            onValueChange = { sigmaVal2 = it },
            label = { Text("Сигма значення 2 (sigma_val_2)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = electricityPrice,
            onValueChange = { electricityPrice = it },
            label = { Text("Ціна електроенергії (price)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                results = calculateResults(
                    avrPower.toDoubleOrNull() ?: 5.0,
                    sigmaVal1.toDoubleOrNull() ?: 1.0,
                    sigmaVal2.toDoubleOrNull() ?: 1.0,
                    electricityPrice.toDoubleOrNull() ?: 7.0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Розрахувати")
        }

        Spacer(modifier = Modifier.height(16.dp))

        results?.let { res ->
            ResultsDisplay(res)
        }
    }
}

@Composable
fun ResultsDisplay(results: CalculationResults) {
    Column {
        Text("Нормальний закон: ${results.normLaw.round(2)}")
        Text("Частка енергії 1: ${results.energyPiece1.round(2)}%")
        Text("W_1: ${results.w1.round(0)}")
        Text("Доходи 1: ${results.income1.round(0)}")
        Text("W_2: ${results.w2.round(0)}")
        Text("Штраф 1: ${results.fine1.round(0)}")
        Text("Частка енергії 2: ${results.energyPiece2.round(2)}%")
        Text("W_3: ${results.w3.round(1)}")
        Text("Доходи 2: ${results.income2.round(1)}")
        Text("W_4: ${results.w4.round(1)}")
        Text("Штраф 2: ${results.fine2.round(1)}")
        Text("Кінцевий дохід: ${results.incomeFinale.round(1)}")
    }
}

data class CalculationResults(
    val normLaw: Double,
    val energyPiece1: Double,
    val w1: Double,
    val income1: Double,
    val w2: Double,
    val fine1: Double,
    val energyPiece2: Double,
    val w3: Double,
    val income2: Double,
    val w4: Double,
    val fine2: Double,
    val incomeFinale: Double
)

fun calculateResults(Pc: Double, sigmaVal1: Double, sigmaVal2: Double, price: Double): CalculationResults {
    val p = 5.0

    val deltaW1 = integrate(
        { x, pc, sigma -> calculatePdW1(x, pc, sigma) },
        4.75, 5.25, 1000, Pc, sigmaVal1
    )

    val w1 = Pc * 24 * deltaW1
    val income1 = w1 * price
    val w2 = Pc * 24 * (1 - deltaW1)
    val fine1 = w2 * price

    val deltaW2 = integrate(
        { x, pc, sigma -> calculatePdW2(x, pc, sigma) },
        4.75, 5.25, 1000, Pc, sigmaVal2
    )

    val w3 = Pc * 24 * deltaW2
    val income2 = w3 * price
    val w4 = Pc * 24 * (1 - deltaW2)
    val fine2 = w4 * price
    val incomeFinale = income2 - fine2

    val normLaw = calculatePdW1(p, Pc, sigmaVal1)
    val energyPiece1 = deltaW1 * 100
    val energyPiece2 = deltaW2 * 100

    return CalculationResults(
        normLaw, energyPiece1, w1, income1, w2, fine1,
        energyPiece2, w3, income2, w4, fine2, incomeFinale
    )
}

fun integrate(
    func: (Double, Double, Double) -> Double,
    start: Double,
    end: Double,
    steps: Int,
    Pc: Double,
    sigmaVal: Double
): Double {
    val step = (end - start) / steps
    var sum = 0.5 * (func(start, Pc, sigmaVal) + func(end, Pc, sigmaVal))

    var i = start + step
    while (i < end) {
        sum += func(i, Pc, sigmaVal)
        i += step
    }

    return sum * step
}

fun calculatePdW1(p: Double, Pc: Double, sigmaVal1: Double): Double {
    return (1 / (sigmaVal1 * sqrt(2 * PI))) *
            exp(-(p - Pc).pow(2) / (2 * sigmaVal1.pow(2)))
}

fun calculatePdW2(p: Double, Pc: Double, sigmaVal2: Double): Double {
    return (1 / (sigmaVal2 * sqrt(2 * PI))) *
            exp(-(p - Pc).pow(2) / (2 * sigmaVal2.pow(2)))
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}