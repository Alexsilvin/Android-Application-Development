import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.Locale
import java.util.Scanner

data class Student2(val name: String, val score: Double, val grade: Char)

private data class ImportResult(
    val students: List<Student2>,
    val skippedRows: List<String>
)

private val scoreFormatter = DecimalFormat("0.##")

fun getLetterGrade(score: Double): Char = when {
    score in 90.0..100.0 -> 'A'
    score >= 80.0 -> 'B'
    score >= 70.0 -> 'C'
    score >= 60.0 -> 'D'
    else -> 'F'
}

fun main(args: Array<String>) {
    Locale.setDefault(Locale.US)
    val scanner = Scanner(System.`in`)
    val inputFile = resolveInputFile(args.getOrNull(0), scanner)
    val importResult = readStudentsFromExcel(inputFile)

    if (importResult.students.isEmpty()) {
        println("No valid student records were found in ${inputFile.absolutePath}.")
        importResult.skippedRows.forEach(::println)
        return
    }

    println()
    println("Preview of computed grades")
    printStudentTable(importResult.students)

    if (importResult.skippedRows.isNotEmpty()) {
        println()
        println("Rows skipped during import:")
        importResult.skippedRows.forEach(::println)
    }

    val shouldExport = args.getOrNull(1) != null || askYesNo(scanner, "\nExport the previewed result to Excel? (y/n): ")
    if (!shouldExport) {
        return
    }

    val outputFile = resolveOutputFile(args.getOrNull(1), inputFile, scanner)
    exportStudentsToExcel(importResult.students, outputFile)
    println("Exported graded results to ${outputFile.absolutePath}")
}

private fun resolveInputFile(argumentPath: String?, scanner: Scanner): File {
    val isInteractive = System.console() != null
    while (true) {
        val candidatePath = argumentPath ?: try {
            prompt(scanner, "Enter the path to the Excel file with student scores: ")
        } catch (e: NoSuchElementException) {
            if (isInteractive) throw e
            println()
            throw IllegalStateException("No input file provided and stdin is not available.")
        }
        val inputFile = File(candidatePath.trim().trim('"'))
        if (inputFile.exists() && inputFile.isFile) {
            return inputFile
        }
        println("File not found: ${inputFile.absolutePath}")
        if (argumentPath != null) {
            throw IllegalArgumentException("The input file does not exist: ${inputFile.absolutePath}")
        }
        if (!isInteractive) {
            throw IllegalStateException("Cannot continue: input file not found and stdin reached EOF.")
        }
    }
}

private fun resolveOutputFile(argumentPath: String?, inputFile: File, scanner: Scanner): File {
    val suggestedName = inputFile.nameWithoutExtension + "-graded.xlsx"
    val defaultOutput = File(inputFile.parentFile ?: File("."), suggestedName)
    val chosenPath = argumentPath ?: prompt(scanner, "Enter export path [$suggestedName]: ").ifBlank { suggestedName }
    val normalizedPath = chosenPath.trim().trim('"')
    val outputFile = if (File(normalizedPath).isAbsolute) {
        File(normalizedPath)
    } else {
        File(inputFile.parentFile ?: File("."), normalizedPath)
    }
    return if (outputFile.extension.equals("xlsx", ignoreCase = true)) {
        outputFile
    } else {
        File(outputFile.parentFile ?: File("."), outputFile.name + ".xlsx")
    }
}

private fun prompt(scanner: Scanner, message: String): String {
    print(message)
    return scanner.nextLine().trim()
}

private fun askYesNo(scanner: Scanner, message: String): Boolean {
    while (true) {
        when (prompt(scanner, message).lowercase()) {
            "y", "yes" -> return true
            "n", "no" -> return false
        }
        println("Please answer with y or n.")
    }
}

private fun readStudentsFromExcel(file: File): ImportResult {
    FileInputStream(file).use { inputStream ->
        WorkbookFactory.create(inputStream).use { workbook ->
            val formatter = DataFormatter()
            val evaluator = workbook.creationHelper.createFormulaEvaluator()
            val sheet = workbook.getSheetAt(0)
            val headerRow = sheet.getRow(sheet.firstRowNum)
                ?: return ImportResult(emptyList(), listOf("The spreadsheet is empty."))

            val headers = headerRow.associate { cell ->
                val headerText = formatter.formatCellValue(cell, evaluator)
                normalizeHeader(headerText) to cell.columnIndex
            }

            val nameColumn = findColumnIndex(headers, "name", "student", "studentname", "fullname")
            val scoreColumn = findColumnIndex(headers, "score", "mark", "marks", "total", "totalscore")

            if (nameColumn == null || scoreColumn == null) {
                val missingColumns = buildList {
                    if (nameColumn == null) add("Name")
                    if (scoreColumn == null) add("Score")
                }
                return ImportResult(emptyList(), listOf("Missing required column(s): ${missingColumns.joinToString(", ")}."))
            }

            val students = mutableListOf<Student2>()
            val skippedRows = mutableListOf<String>()

            for (rowIndex in (sheet.firstRowNum + 1)..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                val name = getCellText(row, nameColumn, formatter, evaluator)
                val scoreText = getCellText(row, scoreColumn, formatter, evaluator)
                val displayRowNumber = row.rowNum + 1

                if (name.isBlank() && scoreText.isBlank()) {
                    continue
                }
                if (name.isBlank()) {
                    skippedRows += "Row $displayRowNumber skipped: missing student name."
                    continue
                }
                val score = scoreText.toDoubleOrNull()
                if (score == null) {
                    skippedRows += "Row $displayRowNumber skipped: invalid score '$scoreText'."
                    continue
                }
                if (score !in 0.0..100.0) {
                    skippedRows += "Row $displayRowNumber skipped: score $score is outside 0 to 100."
                    continue
                }

                students += Student2(name, score, getLetterGrade(score))
            }

            return ImportResult(students, skippedRows)
        }
    }
}

private fun findColumnIndex(headers: Map<String, Int>, vararg acceptedNames: String): Int? {
    return acceptedNames.firstNotNullOfOrNull(headers::get)
}

private fun normalizeHeader(value: String): String {
    return value.lowercase().filter { it.isLetterOrDigit() }
}

private fun getCellText(
    row: Row,
    columnIndex: Int,
    formatter: DataFormatter,
    evaluator: org.apache.poi.ss.usermodel.FormulaEvaluator
): String {
    val cell = row.getCell(columnIndex) ?: return ""
    return formatter.formatCellValue(cell, evaluator).trim()
}

private fun printStudentTable(students: List<Student2>) {
    val headers = listOf("Name", "Score", "Grade")
    val rows = students.map { student ->
        listOf(student.name, scoreFormatter.format(student.score), student.grade.toString())
    }
    val widths = headers.indices.map { columnIndex ->
        (listOf(headers[columnIndex]) + rows.map { it[columnIndex] }).maxOf(String::length)
    }

    fun border(): String = "+" + widths.joinToString("+") { "-".repeat(it + 2) } + "+"
    fun formatRow(values: List<String>): String {
        return "| " + values.mapIndexed { index, value -> value.padEnd(widths[index]) }.joinToString(" | ") + " |"
    }

    println(border())
    println(formatRow(headers))
    println(border())
    rows.forEach { println(formatRow(it)) }
    println(border())
}

private fun exportStudentsToExcel(students: List<Student2>, outputFile: File) {
    outputFile.parentFile?.mkdirs()
    XSSFWorkbook().use { workbook ->
        val sheet = workbook.createSheet("Grades")
        val headerStyle = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { bold = true })
        }

        val headerRow = sheet.createRow(0)
        listOf("Name", "Score", "Grade").forEachIndexed { index, title ->
            headerRow.createCell(index).apply {
                setCellValue(title)
                cellStyle = headerStyle
            }
        }

        students.forEachIndexed { index, student ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(student.name)
            row.createCell(1).setCellValue(student.score)
            row.createCell(2).setCellValue(student.grade.toString())
        }

        repeat(3) { columnIndex -> sheet.autoSizeColumn(columnIndex) }

        FileOutputStream(outputFile).use(workbook::write)
    }
}
