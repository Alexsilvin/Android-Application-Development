data class Student(val name: String, val score: Int?)

val students = mutableListOf<Student>()
val nextId = 1

fun getGrade(score: Int): Char = when (score) {
    in 90..100 -> 'A'
    in 80..89 -> 'B'
    in 70..79 -> 'C'
    in 60..69 -> 'D'
    else -> 'F'
}

fun printGrades(students: List<Student>) {
    for (student in students) {
        val gradeMessage = student.score?.let { score ->
        val grade = getGrade(score)
        "${student.name} scored $score : Grade $grade"
    } ?: "No score for ${student.name}"
    println(gradeMessage)
    }
}

fun main() {
    println("Welcome User..")
    println("choose an option")
    println("1.Enter new student")
    println("2.View existing students")
    println("3.Exit")

    println("Enter your choice (from 1 to 4): ")

    val input = readln().toIntOrNull()

    when (input) {
        1 -> 
        2 -> println(gradeMessage)
        3 -> println("Exiting...")
        else -> println("invalide choice")
    }
}