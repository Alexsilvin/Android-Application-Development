# Student Grades Application

This project contains console-based Kotlin examples for student grade processing. A separate Excel-based calculator is available for importing student scores from a spreadsheet, previewing the computed grades in a terminal table, and exporting the result to a new Excel file.

## Project Structure

```text
student-grades-app
├── src
│   └── main
│       └── kotlin
│           ├── Main.kt
│           └── ExcelStudentGradeCalculator.kt
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Prerequisites

- Kotlin 1.5.31 or higher
- Gradle 7.0 or higher
- Apache POI library for reading and writing Excel files

## Setup Instructions

1. **Clone the Repository**: 
   Clone this repository to your local machine.

2. **Navigate to the Project Directory**: 
   Open a terminal and navigate to the `student-grades-app` directory.

3. **Build the Project**: 
   Run the following command to build the project:
   ```
   ./gradlew build
   ```

4. **Run the Excel Calculator**:
   Execute the Excel-based version using:
   ```
   gradle runExcelGradeCalculator
   ```

   You can also pass the input and output file paths directly:
   ```
   gradle runExcelGradeCalculator -PappArgs="students.xlsx graded-students.xlsx"
   ```

## Usage Instructions

1. Prepare an Excel sheet with a header row containing these columns:
   - Name
   - Score
2. Run the calculator and provide the input file path when prompted, or pass it with `-PappArgs`.
3. The application reads the spreadsheet, calculates grades using this scale:
   - A: 90 to 100
   - B: 80 to 89
   - C: 70 to 79
   - D: 60 to 69
   - F: below 60
4. A preview table is printed in the terminal with each student's name, score, and grade.
5. You can export the processed result to a new `.xlsx` file.

## Excel Format

The first row must contain headers. Example:

```text
| Name    | Score |
|---------|-------|
| Alice   | 95    |
| Bob     | 82    |
| Charlie | 67    |
```

The importer accepts `.xls` and `.xlsx` files. The exported result is written as `.xlsx`.

## Dependencies

This project uses the following dependencies:

- Apache POI for handling Excel files:
  - `org.apache.poi:poi:5.2.3`
  - `org.apache.poi:poi-ooxml:5.2.3`
  - `org.apache.poi:poi-ooxml-schemas:4.1.2`

## License

This project is licensed under the MIT License. See the LICENSE file for more details.