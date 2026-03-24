# Excel Student Grade Calculator - Terminal Version

A Kotlin console application that imports student scores from an Excel file, computes letter grades, displays a formatted table preview, and optionally exports results back to Excel.

## Quick Start

### Option 1: Run via Gradle (Recommended)

From the workspace root or `Class Exercise` folder:

```powershell
# From PowerShell
cd "Class Exercise"
powershell -ExecutionPolicy Bypass -File "RunExcelGradeCalculator.ps1"
```

Or with explicit file paths:

```powershell
powershell -ExecutionPolicy Bypass -File "RunExcelGradeCalculator.ps1" `
  -InputPath "C:\path\to\students.xlsx" `
  -OutputPath "C:\path\to\graded-students.xlsx"
```

### Option 2: Run Standalone (Direct Kotlin)

If Gradle is slow or unavailable:

```bat
cd "Class Exercise"
RunExcelGradeCalculatorStandalone.bat
```

Or with arguments:

```bat
RunExcelGradeCalculatorStandalone.bat "C:\path\to\students.xlsx"
```

## Input Format

The Excel file should have at least these two columns:
- **Name**: Student name (required)
- **Score**: Numeric score 0–100 (required)

Column names are flexible (case-insensitive). Alternative names recognized:
- Name: `name`, `student`, `studentname`, `fullname`
- Score: `score`, `mark`, `marks`, `total`, `totalscore`

### Example Input (students.xlsx)

```
| Name    | Score |
|---------|-------|
| Alice   | 95    |
| Bob     | 82    |
| Charlie | 67    |
| Diana   | 47    |
```

## Grade Scale

| Grade | Range   |
|-------|---------|
| A     | 90–100  |
| B     | 80–89   |
| C     | 70–79   |
| D     | 60–69   |
| F     | 0–59    |

## Output

The application displays a formatted table in the terminal:

```
+-----------+-------+-------+
| Name      | Score | Grade |
+-----------+-------+-------+
| Alice     | 95    | A     |
| Bob       | 82    | B     |
| Charlie   | 67    | C     |
| Diana     | 47    | F     |
+-----------+-------+-------+
```

You can then export this to a new `.xlsx` file with the same data plus the computed grades.

## Dependencies

- Kotlin 1.9+
- Apache POI 5.2.3 (for Excel file I/O)
- Java 21+ (JVM target)

The standalone batch script automatically locates POI jars from your Gradle cache.
