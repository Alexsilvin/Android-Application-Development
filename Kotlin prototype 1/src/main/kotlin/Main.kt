// Main.kt
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

data class Student(val name: String, val matricule: String, val total: Double) {
    val grade: String
        get() = when (total) {
            in 90.0..100.0 -> "A"
            in 80.0..89.99 -> "B+"
            in 70.0..79.99 -> "B"
            in 60.0..69.99 -> "C+"
            in 50.0..59.99 -> "C"
            in 45.0..49.99 -> "D+"
            in 40.0..44.99 -> "D"
            else -> "F"
        }
}

fun main() {
    val scanner = Scanner(System.`in`)
    println("Welcome to the student‑grade calculator")
    print("Enter path to Excel file: ")
    val path = scanner.nextLine().trim()
    val file = File(path)
    if (!file.exists()) {
        println("File not found.")
        return
    }

    val students = mutableListOf<Student>()
    FileInputStream(file).use { fis ->
        val workbook = WorkbookFactory.create(fis)
        val sheet = workbook.getSheetAt(0)
        for (row in sheet.drop(1)) {              // skip header row
            val name = row.getCell(0).stringCellValue
            val mat = row.getCell(1).stringCellValue
            val ca = row.getCell(2).numericCellValue
            val exam = row.getCell(3).numericCellValue
            val total = ca * 0.3 + exam * 0.7
            students += Student(name, mat, total)
        }
    }

    println("\nName\t\tMatricule\tGrade")
    println("----\t\t---------\t-----")
    students.forEach { println("${it.name}\t${it.matricule}\t${it.grade}") }

    print("\nExport results to Excel? (y/n): ")
    if (scanner.nextLine().equals("y", true)) {
        val outWb = XSSFWorkbook()
        val outSheet = outWb.createSheet("Grades")
        val header = outSheet.createRow(0)
        header.createCell(0).setCellValue("Name")
        header.createCell(1).setCellValue("Matricule")
        header.createCell(2).setCellValue("Grade")
        students.forEachIndexed { i, s ->
            val r = outSheet.createRow(i + 1)
            r.createCell(0).setCellValue(s.name)
            r.createCell(1).setCellValue(s.matricule)
            r.createCell(2).setCellValue(s.grade)
        }
        FileOutputStream("Student_Grades_Export.xlsx").use { fos ->
            outWb.write(fos)
        }
        outWb.close()
        println("Exported to Student_Grades_Export.xlsx")
    }

    println("Done.")
}
