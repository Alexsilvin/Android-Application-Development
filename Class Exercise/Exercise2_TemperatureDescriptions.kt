// Exercise 2: Temperature Descriptions

// Function to describe temperature with null safety and when expression
fun describeTemperature(temp: Int?): String {
    return when {
        temp == null -> "No data"
        temp <= 0 -> "Freezing"
        temp in 1..15 -> "Cold"
        temp in 16..25 -> "Mild"
        temp in 26..35 -> "Warm"
        temp in 36..45 -> "Hot"
        temp > 45 -> "Extreme"
        else -> "Unknown"
    }
}

fun main() {
    // Create list of temperatures (some null)
    val temperatures = listOf(
        -5,      // Freezing
        8,       // Cold
        null,    // No data
        22,      // Mild
        32,      // Warm
        40,      // Hot
        50,      // Extreme
        0,       // Freezing (boundary)
        15,      // Cold (boundary)
        25,      // Mild (boundary)
        35,      // Warm (boundary)
        45       // Hot (boundary)
    )

    println("=== Temperature Description ===\n")

    // Loop through temperatures and print descriptions
    for ((index, temp) in temperatures.withIndex()) {
        val description = describeTemperature(temp)
        val tempDisplay = temp?.toString() ?: "null"
        println("Temperature $index: $tempDisplay°C → $description")
    }

    // Bonus: Summary statistics
    println("\n=== Temperature Statistics ===")
    val validTemps = temperatures.filterNotNull()
    val avgTemp = validTemps.average()
    println("Valid temperatures: ${validTemps.size}/${temperatures.size}")
    println("Average temperature: ${"%.1f".format(avgTemp)}°C")

    // Count each category
    println("\n=== Category Breakdown ===")
    val freezing = temperatures.count { describeTemperature(it) == "Freezing" }
    val cold = temperatures.count { describeTemperature(it) == "Cold" }
    val mild = temperatures.count { describeTemperature(it) == "Mild" }
    val warm = temperatures.count { describeTemperature(it) == "Warm" }
    val hot = temperatures.count { describeTemperature(it) == "Hot" }
    val extreme = temperatures.count { describeTemperature(it) == "Extreme" }
    val noData = temperatures.count { describeTemperature(it) == "No data" }

    println("Freezing (≤0): $freezing")
    println("Cold (1-15): $cold")
    println("Mild (16-25): $mild")
    println("Warm (26-35): $warm")
    println("Hot (36-45): $hot")
    println("Extreme (>45): $extreme")
    println("No data: $noData")
}
