# Student Grade Calculator - Enhanced Version

## Features Implemented

### 1. **Pass/Fail Statistics Display** ✅
   - Real-time percentage calculation of passed vs. failed students
   - Displays total student count
   - Color-coded stats panel (Green for passed, Red for failed, Orange for total)
   - Updates automatically after importing Excel data
   - Pass threshold: 50.0 or higher

### 2. **Light & Dark Mode** ✅
   - Theme toggle button (🌙) in the header
   - Persistent theme preference using SharedPreferences
   - Automatic theme switching on button press
   - Complete color scheme for both modes:
     - Light: Clean white backgrounds with purple accents
     - Dark: Dark backgrounds with light purple accents
   - Shared color palette for consistent UX across states

### 3. **Improved UI/UX** ✅
   - Modern Material Design 3 layout
   - Enhanced visual hierarchy with better spacing
   - Color-coded buttons with emojis for quick recognition:
     - 📁 Import Excel File
     - 💾 Save to Phone
     - 📤 Share Excel File
     - ⬆️ Export Graded Excel File
     - 🌙 Toggle Theme
   - Better scrollable preview with monospace font
   - Statistics panel visible only when data is loaded
   - Improved responsive layout using LinearLayout

### 4. **Save to Phone (Local Storage)** ✅
   - "Save to Phone" button generates and saves Excel file to phone's Download folder
   - Automatic filename with timestamp: `GradedStudents_YYYYMMDD_HHMMSS.xlsx`
   - Works on Android 10+ (scoped storage) and older versions
   - Complete Excel file with all student data:
     - Student Name
     - Matricule
     - CA Mark
     - Exam Mark
     - Total Score
     - Letter Grade
   - Toast notification confirms successful save
   - File path displayed in status text

### 5. **Share Excel File** ✅
   - "Share Excel File" button triggers Android share intent
   - Compatible with all available sharing services (email, cloud storage, messaging, etc.)
   - Supports offline sharing (via Bluetooth, NFC) and online services (Google Drive, Dropbox, OneDrive, etc.)
   - FileProvider implementation for secure file sharing
   - Auto-populated share with subject and description
   - Share URI properly configured in manifest

### 6. **Export Functionality (Enhanced)** ✅
   - Existing export flow maintained and improved
   - Now tracks exported file path for immediate sharing
   - Rich Excel format with headers and proper cell formatting
   - Data includes all student information and calculated grades

## Technical Implementation Details

### New/Modified Files:

1. **MainActivity.kt** - Complete rewrite with:
   - Theme toggle functionality
   - Statistics calculation (pass/fail percentages)
   - Save to phone functionality
   - Share intent implementation
   - SharedPreferences for theme persistence

2. **activity_main.xml** - Redesigned layout:
   - LinearLayout structure for better flexibility
   - Statistics panel with color-coded stats
   - Theme toggle button in header
   - Enhanced button styling
   - Improved preview scrollview

3. **colors.xml** - Complete color palette:
   - Light theme colors
   - Dark theme colors
   - Semantic colors (pass, fail, warning)

4. **themes.xml** - Theme definitions:
   - Light and dark theme styles
   - Material3 support
   - Custom text styles

5. **values-night/colors.xml** - Dark mode color mappings

6. **strings.xml** - All UI strings with resource IDs

7. **AndroidManifest.xml** - Updates:
   - Permissions for storage and internet
   - FileProvider configuration
   - Provider metadata

8. **file_paths.xml** - FileProvider paths configuration

## How to Use

### Importing:
1. Tap "📁 Import Excel File"
2. Select an Excel file with columns: Name, Matricule, CA (Max 30), Exam (Max 70)

### Viewing Stats:
1. Statistics display automatically after import
2. Shows passed %, failed %, and total count

### Saving:
1. Tap "💾 Save to Phone" to save Excel to Downloads folder
2. File auto-generated with timestamp

### Sharing:
1. Tap "📤 Share Excel File" to open share menu
2. Select destination (email, cloud storage, etc.)

### Theme Toggle:
1. Tap "🌙" button in header
2. Theme preference auto-saved

## Permissions Required
- `READ_EXTERNAL_STORAGE` - To import Excel files
- `WRITE_EXTERNAL_STORAGE` - To save Excel files
- `INTERNET` - For cloud sharing services

## Build & Run
```bash
./gradlew build
./gradlew install
```

## Version
- App Name: Student Grade Calculator
- Min SDK: 26 (Android 8.0+)
- Target SDK: 35
- Kotlin Version: 1.9+
