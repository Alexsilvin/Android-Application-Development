package com.example.firstkot

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    data class StudentInput(
        val name: String,
        val matricule: String,
        val caMark: Double?,
        val examMark: Double?
    )

    data class StudentGrade(
        val name: String,
        val matricule: String,
        val caMark: Double,
        val examMark: Double,
        val total: Double,
        val grade: String
    )

    private lateinit var importButton: Button
    private lateinit var exportButton: Button
    private lateinit var saveToPhoneButton: Button
    private lateinit var shareButton: Button
    private lateinit var themeToggleButton: Button
    private lateinit var statusText: TextView
    private lateinit var previewText: TextView
    private lateinit var statsPanel: LinearLayout
    private lateinit var passPercentText: TextView
    private lateinit var failPercentText: TextView
    private lateinit var totalStudentsText: TextView
    private lateinit var studentsRecyclerView: RecyclerView
    private lateinit var studentsListAdapter: StudentAdapter
    private lateinit var studentsListHeader: TextView

    private var gradedStudents: MutableList<StudentGrade> = mutableListOf()
    private var lastExportedFilePath: String? = null
    private var lastExportedUri: Uri? = null
    private var isDarkMode = false

    private val importExcelLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            showStatus("No file selected.")
            return@registerForActivityResult
        }
        importAndPreviewExcel(uri)
    }

    private val exportExcelLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri: Uri? ->
        if (uri == null) {
            showStatus("Export cancelled.")
            return@registerForActivityResult
        }
        exportGradesToExcel(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load and apply saved theme preference
        loadThemePreference()
        
        setContentView(R.layout.activity_main)

        importButton = findViewById(R.id.importButton)
        exportButton = findViewById(R.id.exportButton)
        saveToPhoneButton = findViewById(R.id.saveToPhoneButton)
        shareButton = findViewById(R.id.shareButton)
        themeToggleButton = findViewById(R.id.themeToggleButton)
        statusText = findViewById(R.id.statusText)
        previewText = findViewById(R.id.previewText)
        statsPanel = findViewById(R.id.statsPanel)
        passPercentText = findViewById(R.id.passPercentText)
        failPercentText = findViewById(R.id.failPercentText)
        totalStudentsText = findViewById(R.id.totalStudentsText)
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView)
        studentsListHeader = findViewById(R.id.studentsListHeader)

        // Setup RecyclerView
        studentsListAdapter = StudentAdapter(gradedStudents) { position ->
            showEditStudentDialog(position)
        }
        studentsRecyclerView.layoutManager = LinearLayoutManager(this)
        studentsRecyclerView.adapter = studentsListAdapter

        importButton.setOnClickListener {
            importExcelLauncher.launch(
                arrayOf(
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
        }

        exportButton.setOnClickListener {
            if (gradedStudents.isEmpty()) {
                showStatus("Nothing to export. Import a file first.")
            } else {
                exportExcelLauncher.launch("graded_students.xlsx")
            }
        }

        saveToPhoneButton.setOnClickListener {
            if (gradedStudents.isEmpty()) {
                showStatus("Nothing to save. Import a file first.")
            } else {
                saveExcelToPhone()
            }
        }

        shareButton.setOnClickListener {
            if (lastExportedUri == null && lastExportedFilePath == null) {
                showStatus("No exported file to share. Export or save first.")
            } else {
                shareExcelFile()
            }
        }

        themeToggleButton.setOnClickListener {
            toggleTheme()
        }
    }

    private fun importAndPreviewExcel(uri: Uri) {
        runCatching {
            val parsedStudents = mutableListOf<StudentInput>()
            val sharedStrings = mutableListOf<String>()

            contentResolver.openInputStream(uri)?.use { inputStream ->
                // First pass: read sharedStrings.xml (if present) and store strings
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (entry.name == "xl/sharedStrings.xml") {
                            try {
                                val docFactory = DocumentBuilderFactory.newInstance()
                                docFactory.isNamespaceAware = true
                                val doc = docFactory.newDocumentBuilder().parse(zip)
                                val sis = doc.getElementsByTagName("si")
                                for (i in 0 until sis.length) {
                                    val si = sis.item(i) as Element
                                    val tNodes = si.getElementsByTagName("t")
                                    if (tNodes.length > 0) sharedStrings.add(tNodes.item(0).textContent)
                                    else sharedStrings.add("")
                                }
                            } catch (e: Exception) {
                                // ignore shared strings parsing errors
                            }
                            break
                        }
                        entry = zip.nextEntry
                    }
                }
            } ?: throw IllegalStateException("Could not read selected file.")

            // Second pass: read sheet XML and resolve cell values using sharedStrings
            contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (entry.name == "xl/worksheets/sheet1.xml") {
                            val docFactory = DocumentBuilderFactory.newInstance()
                            docFactory.isNamespaceAware = true
                            val doc = docFactory.newDocumentBuilder().parse(zip)

                            val rows = doc.getElementsByTagName("row")
                            for (i in 1 until rows.length) {
                                val row = rows.item(i) as Element
                                val cells = row.getElementsByTagName("c")

                                // prepare placeholders for first 4 columns
                                var name = ""
                                var matricule = ""
                                var caMark: Double? = null
                                var examMark: Double? = null

                                for (j in 0 until cells.length) {
                                    val cell = cells.item(j) as Element
                                    val rAttr = cell.getAttribute("r") // e.g., A2, B2
                                    val colLetters = rAttr.replace(Regex("\\d"), "")
                                    val colIndex = columnLettersToIndex(colLetters)

                                    val tAttr = cell.getAttribute("t")
                                    val value = when {
                                        tAttr == "s" -> {
                                            // shared string; <v> contains index
                                            val v = cell.getElementsByTagName("v").item(0)?.textContent
                                            v?.toIntOrNull()?.let { idx -> sharedStrings.getOrNull(idx) } ?: ""
                                        }
                                        tAttr == "inlineStr" -> {
                                            cell.getElementsByTagName("is").item(0)
                                                ?.let { isNode -> (isNode as Element).getElementsByTagName("t").item(0)?.textContent }
                                                ?: ""
                                        }
                                        else -> cell.getElementsByTagName("v").item(0)?.textContent ?: ""
                                    }

                                    when (colIndex) {
                                        0 -> name = value
                                        1 -> matricule = value
                                        2 -> caMark = value.toDoubleOrNull()
                                        3 -> examMark = value.toDoubleOrNull()
                                    }
                                }

                                if (name.isNotBlank() || matricule.isNotBlank()) {
                                    parsedStudents.add(
                                        StudentInput(
                                            name = name.ifBlank { "Unknown Student" },
                                            matricule = matricule.ifBlank { "N/A" },
                                            caMark = caMark,
                                            examMark = examMark
                                        )
                                    )
                                }
                            }
                            break
                        }
                        entry = zip.nextEntry
                    }
                }
            } ?: throw IllegalStateException("Could not read selected file.")

            gradedStudents = parsedStudents.mapNotNull { calculateStudentGrade(it) }.toMutableList()
        }.onSuccess {
            if (gradedStudents.isEmpty()) {
                exportButton.isEnabled = false
                saveToPhoneButton.isEnabled = false
                showStatus("No valid student rows found in the Excel file.")
                studentsListHeader.visibility = TextView.GONE
                previewText.visibility = TextView.VISIBLE
                previewText.text = "Preview is empty. Check columns: Name, Matricule, CA, Exam."
                return@onSuccess
            }

            exportButton.isEnabled = true
            saveToPhoneButton.isEnabled = true
            
            // Update RecyclerView
            studentsListAdapter.updateStudents(gradedStudents)
            studentsListHeader.visibility = TextView.VISIBLE
            previewText.visibility = TextView.GONE
            
            updateStatistics()
            showStatus("Imported ${gradedStudents.size} students successfully.")
        }.onFailure { error ->
            exportButton.isEnabled = false
            saveToPhoneButton.isEnabled = false
            shareButton.isEnabled = false
            gradedStudents = mutableListOf()
            studentsListAdapter.updateStudents(gradedStudents)
            showStatus("Import failed: ${error.message ?: "Unknown error"}")
            studentsListHeader.visibility = TextView.GONE
            previewText.visibility = TextView.VISIBLE
            previewText.text = "Preview will appear here..."
        }
    }

    private fun calculateStudentGrade(student: StudentInput): StudentGrade? {
        val ca = student.caMark?.coerceIn(0.0, 30.0) ?: 0.0
        val exam = student.examMark?.coerceIn(0.0, 70.0) ?: 0.0

        val total = ca + exam
        if (total !in 0.0..100.0) return null

        return StudentGrade(
            name = student.name,
            matricule = student.matricule,
            caMark = ca,
            examMark = exam,
            total = total,
            grade = gradeFromTotal(total)
        )
    }

    private fun gradeFromTotal(total: Double): String {
        val rounded = total.toInt()
        return when (rounded) {
            in 90..100 -> "A"
            in 80..89 -> "B+"
            in 60..79 -> "B"
            in 55..59 -> "C+"
            in 50..54 -> "C"
            in 45..49 -> "D+"
            in 40..44 -> "D"
            else -> "F"
        }
    }

    private fun columnLettersToIndex(letters: String): Int {
        if (letters.isBlank()) return -1
        var result = 0
        letters.uppercase().forEach { ch ->
            if (ch in 'A'..'Z') {
                result = result * 26 + (ch - 'A' + 1)
            }
        }
        return result - 1
    }

    private fun exportGradesToExcel(uri: Uri) {
        runCatching {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val zipOut = java.util.zip.ZipOutputStream(outputStream)
                
                zipOut.putNextEntry(java.util.zip.ZipEntry("_rels/.rels"))
                zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".toByteArray())
                zipOut.closeEntry()

                zipOut.putNextEntry(java.util.zip.ZipEntry("xl/_rels/workbook.xml.rels"))
                zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""".toByteArray())
                zipOut.closeEntry()

                zipOut.putNextEntry(java.util.zip.ZipEntry("xl/workbook.xml"))
                zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheets>
<sheet name="Grades" sheetId="1" r:id="rId1" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/>
</sheets>
</workbook>""".toByteArray())
                zipOut.closeEntry()

                zipOut.putNextEntry(java.util.zip.ZipEntry("xl/styles.xml"))
                zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="0"/>
<fonts count="1"><font><sz val="11"/></font></fonts>
<fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
<borders count="1"><border><left/><right/><top/><bottom/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
</styleSheet>""".toByteArray())
                zipOut.closeEntry()

                zipOut.putNextEntry(java.util.zip.ZipEntry("xl/worksheets/sheet1.xml"))
                val sheetXml = buildString {
                    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1"><c r="A1" t="inlineStr"><is><t>Student Name</t></is></c><c r="B1" t="inlineStr"><is><t>Student Grade</t></is></c></row>
""")
                    gradedStudents.forEachIndexed { idx, student ->
                        val rowNum = idx + 2
                        append("""<row r="$rowNum"><c r="A$rowNum" t="inlineStr"><is><t>${student.name}</t></is></c><c r="B$rowNum" t="inlineStr"><is><t>${student.grade}</t></is></c></row>
""")
                    }
                    append("</sheetData>\n</worksheet>")
                }
                zipOut.write(sheetXml.toByteArray())
                zipOut.closeEntry()

                zipOut.putNextEntry(java.util.zip.ZipEntry("[Content_Types].xml"))
                zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>""".toByteArray())
                zipOut.closeEntry()

                zipOut.close()
            } ?: throw IllegalStateException("Could not open export destination.")
        }.onSuccess {
            lastExportedUri = uri
            showStatus("Export completed successfully.")
            Toast.makeText(this, "Excel exported.", Toast.LENGTH_SHORT).show()
            shareButton.isEnabled = true
        }.onFailure { error ->
            showStatus("Export failed: ${error.message ?: "Unknown error"}")
            shareButton.isEnabled = false
        }
    }

    private fun showStatus(message: String) {
        statusText.text = message
    }

    private fun updateStatistics() {
        if (gradedStudents.isEmpty()) {
            statsPanel.visibility = LinearLayout.GONE
            return
        }

        statsPanel.visibility = LinearLayout.VISIBLE
        
        val passCount = gradedStudents.count { it.total >= 50.0 }
        val failCount = gradedStudents.size - passCount
        val passPercent = if (gradedStudents.isNotEmpty()) {
            (passCount * 100) / gradedStudents.size
        } else {
            0
        }
        val failPercent = 100 - passPercent

        passPercentText.text = "$passPercent%"
        failPercentText.text = "$failPercent%"
        totalStudentsText.text = gradedStudents.size.toString()
    }

    private fun loadThemePreference() {
        val sharedPref = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        applyTheme()
    }

    private fun toggleTheme() {
        isDarkMode = !isDarkMode
        saveThemePreference()
        applyTheme()
        recreate()
    }

    private fun saveThemePreference() {
        val sharedPref = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isDarkMode", isDarkMode)
            apply()
        }
    }

    private fun applyTheme() {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun saveExcelToPhone() {
        runCatching {
            val fileName = "GradedStudents_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.xlsx"
            val file = createExcelFile(fileName)
            lastExportedFilePath = file.absolutePath
            lastExportedUri = FileProvider.getUriForFile(this, "com.example.firstkot.fileprovider", file)
            
            showStatus("File saved to: ${file.absolutePath}")
            saveToPhoneButton.isEnabled = true
            shareButton.isEnabled = true
            Toast.makeText(this, "Saved to downloads folder", Toast.LENGTH_SHORT).show()
        }.onFailure { error ->
            showStatus("Save failed: ${error.message ?: "Unknown error"}")
        }
    }

    private fun createExcelFile(fileName: String): File {
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        }

        file.parentFile?.mkdirs()

        file.outputStream().use { outputStream ->
            val zipOut = java.util.zip.ZipOutputStream(outputStream)
            
            zipOut.putNextEntry(java.util.zip.ZipEntry("_rels/.rels"))
            zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(java.util.zip.ZipEntry("xl/_rels/workbook.xml.rels"))
            zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""".toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(java.util.zip.ZipEntry("xl/workbook.xml"))
            zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheets>
<sheet name="Grades" sheetId="1" r:id="rId1" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/>
</sheets>
</workbook>""".toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(java.util.zip.ZipEntry("xl/styles.xml"))
            zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="0"/>
<fonts count="1"><font><sz val="11"/></font></fonts>
<fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
<borders count="1"><border><left/><right/><top/><bottom/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
</styleSheet>""".toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(java.util.zip.ZipEntry("xl/worksheets/sheet1.xml"))
            val sheetXml = buildString {
                append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1"><c r="A1" t="inlineStr"><is><t>Student Name</t></is></c><c r="B1" t="inlineStr"><is><t>Matricule</t></is></c><c r="C1" t="inlineStr"><is><t>CA Mark</t></is></c><c r="D1" t="inlineStr"><is><t>Exam Mark</t></is></c><c r="E1" t="inlineStr"><is><t>Total</t></is></c><c r="F1" t="inlineStr"><is><t>Grade</t></is></c></row>
""")
                gradedStudents.forEachIndexed { idx, student ->
                    val rowNum = idx + 2
                    append("""<row r="$rowNum"><c r="A$rowNum" t="inlineStr"><is><t>${student.name}</t></is></c><c r="B$rowNum" t="inlineStr"><is><t>${student.matricule}</t></is></c><c r="C$rowNum"><v>${"%.2f".format(student.caMark)}</v></c><c r="D$rowNum"><v>${"%.2f".format(student.examMark)}</v></c><c r="E$rowNum"><v>${"%.2f".format(student.total)}</v></c><c r="F$rowNum" t="inlineStr"><is><t>${student.grade}</t></is></c></row>
""")
                }
                append("</sheetData>\n</worksheet>")
            }
            zipOut.write(sheetXml.toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(java.util.zip.ZipEntry("[Content_Types].xml"))
            zipOut.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>""".toByteArray())
            zipOut.closeEntry()

            zipOut.close()
        }
        return file
    }

    private fun shareExcelFile() {
        runCatching {
            val uri = lastExportedUri ?: run {
                val filePath = lastExportedFilePath ?: throw IllegalStateException("No file available to share.")
                val file = File(filePath)
                if (!file.exists()) {
                    throw IllegalStateException("File not found at: $filePath")
                }
                FileProvider.getUriForFile(this, "com.example.firstkot.fileprovider", file)
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Student Grades Report")
                putExtra(Intent.EXTRA_TEXT, "Please find the graded student report attached.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share Excel File"))
        }.onFailure { error ->
            showStatus("Share failed: ${error.message ?: "Unknown error"}")
        }
    }

    private fun showEditStudentDialog(position: Int) {
        val student = gradedStudents[position]
        val dialogView = layoutInflater.inflate(R.layout.edit_student_dialog, null)
        
        val nameView = dialogView.findViewById<TextView>(R.id.editStudentName)
        val matriculeView = dialogView.findViewById<TextView>(R.id.editStudentMatricule)
        val caMarkEdit = dialogView.findViewById<EditText>(R.id.editCaMark)
        val examMarkEdit = dialogView.findViewById<EditText>(R.id.editExamMark)
        val deleteBtn = dialogView.findViewById<Button>(R.id.buttonDeleteStudent)
        val cancelBtn = dialogView.findViewById<Button>(R.id.buttonCancel)
        val saveBtn = dialogView.findViewById<Button>(R.id.buttonSave)

        nameView.text = student.name
        matriculeView.text = student.matricule
        caMarkEdit.setText(String.format("%.2f", student.caMark))
        examMarkEdit.setText(String.format("%.2f", student.examMark))

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Student Grade")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        deleteBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete ${student.name}?")
                .setPositiveButton("DELETE") { _, _ ->
                    deleteStudent(position)
                    dialog.dismiss()
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }

        saveBtn.setOnClickListener {
            val newCaMark = caMarkEdit.text.toString().toDoubleOrNull()
            val newExamMark = examMarkEdit.text.toString().toDoubleOrNull()

            if (newCaMark == null || newExamMark == null) {
                Toast.makeText(this, "Please enter valid marks", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newCaMark !in 0.0..30.0 || newExamMark !in 0.0..70.0) {
                Toast.makeText(this, "Marks out of range: CA (0-30), Exam (0-70)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateStudentGrade(position, newCaMark, newExamMark)
            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateStudentGrade(position: Int, newCaMark: Double, newExamMark: Double) {
        val oldStudent = gradedStudents[position]
        val total = newCaMark + newExamMark
        val newGrade = gradeFromTotal(total)

        val updatedStudent = StudentGrade(
            name = oldStudent.name,
            matricule = oldStudent.matricule,
            caMark = newCaMark,
            examMark = newExamMark,
            total = total,
            grade = newGrade
        )

        gradedStudents[position] = updatedStudent
        studentsListAdapter.notifyItemChanged(position)
        updateStatistics()
        shareButton.isEnabled = false
        showStatus("Student grade updated successfully")
    }

    private fun deleteStudent(position: Int) {
        val studentName = gradedStudents[position].name
        gradedStudents.removeAt(position)
        studentsListAdapter.notifyItemRemoved(position)
        updateStatistics()
        shareButton.isEnabled = false
        showStatus("Student \'$studentName\' deleted successfully")

        if (gradedStudents.isEmpty()) {
            exportButton.isEnabled = false
            saveToPhoneButton.isEnabled = false
            studentsListHeader.visibility = TextView.GONE
            previewText.visibility = TextView.VISIBLE
            previewText.text = "No students in the list. Import a file to continue."
        }
    }
}
