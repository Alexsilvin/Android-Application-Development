// Step 1 – Define a data class
data class Student(val name: String, val score: Int?)

// Step 2 – Create a function to get grade
fun getGrade(score: Int): Char = when (score) {
    in 90..100 -> 'A'
    in 80..89 -> 'B'
    in 70..79 -> 'C'
    in 60..69 -> 'D'
    else -> 'F'
}

// Step 3 – Process a list of students
fun printGrades(students: List<Student>) {
    for (student in students) {
        val gradeMessage = student.score?.let { score ->
            val grade = getGrade(score)
            "${student.name} scored $score : Grade $grade"
        } ?: "No score for ${student.name}"
        println(gradeMessage)
    }
}

// Step 4 – Test with sample data
fun main() {
    val students = listOf(
        Student("Alice", 95),
        Student("Bob", 82),
        Student("Charlie", null),
        Student("Diana", 47)
    )
    printGrades(students)
}
