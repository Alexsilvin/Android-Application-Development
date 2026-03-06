# Student Grades Application

This Kotlin console application calculates student grades from an Excel file. The application reads student data, computes grades based on specified criteria, and allows for exporting results back to an Excel file.

## Project Structure

```
student-grades-app
├── src
│   └── main
│       └── kotlin
│           └── Main.kt
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

4. **Run the Application**: 
   Execute the application using:
   ```
   ./gradlew run
   ```

## Usage Instructions

1. Upon running the application, you will be welcomed and prompted to enter the directory of the Excel file containing student data.
2. The Excel file should have the following columns:
   - Name
   - Matricule
   - CA (Continuous Assessment) - 30% of the total mark
   - Exam Mark - 70% of the total mark
3. The application will calculate the total marks and assign grades based on the following scale:
   - A: 90 and above
   - B+: 80 to 89
   - B: 70 to 79
   - C+: 60 to 69
   - C: 50 to 59
   - D+: 45 to 49
   - D: 40 to 44
   - F: Below 40
4. A preview of student names and their corresponding grades will be displayed in the terminal.
5. You will be asked if you want to export the results to a new Excel file. If you choose to do so, the application will create the file in the specified directory.

## Dependencies

This project uses the following dependencies:

- Apache POI for handling Excel files:
  - `org.apache.poi:poi:5.2.3`
  - `org.apache.poi:poi-ooxml:5.2.3`
  - `org.apache.poi:poi-ooxml-schemas:4.1.2`

## License

This project is licensed under the MIT License. See the LICENSE file for more details.