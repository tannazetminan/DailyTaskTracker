# 📱 Daily Task Tracker

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)

A sophisticated Android task management application designed to help users organize their daily activities, track time spent on tasks, and improve productivity through intelligent task prioritization, advanced filtering, and real-time progress monitoring.

## ✨ Features

### Core Functionality
- **📝 Task Management**
  - Create, edit, and delete tasks with detailed descriptions
  - Set estimated duration for better time management
  - Schedule tasks with specific dates and times
  - Mark tasks as complete with visual feedback
  - Swipe gestures for quick task deletion

- **⏱️ Real-Time Time Tracking**
  - Start/stop timer for individual tasks with live updates
  - Track actual vs. estimated time with visual comparison
  - Real-time progress updates without tab switching (updates every second)
  - Visual indicators for overtime tasks with color coding
  - Cumulative time tracking across multiple sessions
  - Automatic time saving when task is paused or completed
  - In-task progress bars showing time percentage

- **🎯 Priority System**
  - Three-level priority system (Low, Medium, High)
  - Visual priority indicators with colored icons for all priority levels
  - Priority-based task sorting and filtering
  - Time distribution analytics by priority

- **⭐ Immediate Tasks**
  - Mark up to 5 tasks as "immediate" for urgent attention
  - Quick-access starred tasks with limit enforcement
  - Automatic removal from immediate list when completed

### Advanced Filtering System 🔍
- **Multiple Filter Categories**
  - **Date Filters:** Today, Tomorrow, This Week, All Tasks, Custom Date (with Material Date Picker)
  - **Status Filters:** All, Active, Completed, Overdue
  - **Priority Filters:** All, High, Medium, Low
  
- **Smart Filtering**
  - Combine multiple filters simultaneously
  - Active filter summary display
  - Collapsible filter panel with smooth animations
  - Empty state with helpful messages
  - Clear all filters with one tap
  - Filter icon with rotation animation

### User Interface
- **Modern Material Design 3**
  - Clean, intuitive interface following latest Material Design guidelines
  - Smooth animations and transitions throughout
  - Professional chip-based filter UI
  - Dark/light theme support (system default)
  - Responsive layout for different screen sizes

- **Interactive Elements**
  - Swipe to delete with confirmation dialog
  - Long press for quick task completion
  - Bottom sheet options menu for advanced actions
  - Floating Action Button for quick task creation
  - Collapsible filter panels
  - Material date picker integration

- **Visual Feedback**
  - Color-coded task status (overdue, immediate, completed, active)
  - Real-time progress bars showing task completion percentage
  - Live counters for immediate and completed tasks
  - Task completion celebration dialog with time statistics
  - Overtime indicators with dynamic color changes
  - Empty states with helpful illustrations

### Analytics Dashboard 📊
- **Progress Tracking**
  - Daily completion percentage with circular progress
  - Total tasks vs. completed tasks statistics
  - Time spent statistics with detailed formatting
  - Visual progress visualization

- **Time Distribution Chart**
  - Custom bar chart showing time spent by priority
  - Color-coded bars (Low=green, Medium=orange, High=red)
  - Proportional bar heights based on actual time
  - Time labels on each bar for clarity
  - Empty state when no time tracked

### Notifications & Reminders 🔔
- **Smart Notifications Tab**
  - Overdue tasks alert (red warning)
  - Tasks due today (blue indicator)
  - Tasks due tomorrow (orange indicator)
  - Recent activity feed (last 5 completions)
  - Quick statistics dashboard
  - Empty state when all caught up
  - Auto-refresh on tab open

- **Quick Stats**
  - Total active tasks count
  - Total completed tasks count
  - Overall completion rate percentage

## 📸 Screenshots

| Home Screen | Advanced Filters | Time Tracking | Task Options |
|------------|------------------|---------------|--------------|
| ![Home](screenshots/home.png) | ![Filters](screenshots/filters.png) | ![Tracking](screenshots/tracking.png) | ![Options](screenshots/options.png) |

| Dashboard | Time Chart | Notifications | Task Completion |
|-----------|------------|---------------|-----------------|
| ![Dashboard](screenshots/dashboard.png) | ![Chart](screenshots/chart.png) | ![Notifications](screenshots/notifications.png) | ![Completion](screenshots/completion.png) |

*Note: Add your app screenshots to a `screenshots/` folder in the project root*

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later (Recommended: Latest stable version)
- Android SDK 24 or higher
- Java 11 or higher
- Gradle 7.0 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/tannazetminan/DailyTaskTracker.git
   cd DailyTaskTracker
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Run the application**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press `Shift + F10`
   - Select your target device

## 🛠️ Tech Stack

### Core Technologies
- **Language:** Java
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 33 (Android 13)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (SQLite)

### Libraries & Dependencies
```gradle
dependencies {
    // AndroidX Core
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    
    // UI Components
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.0'
    
    // Navigation
    implementation 'androidx.navigation:navigation-fragment:2.6.0'
    implementation 'androidx.navigation:navigation-ui:2.6.0'
    
    // Database
    implementation 'androidx.room:room-runtime:2.5.2'
    annotationProcessor 'androidx.room:room-compiler:2.5.2'
    
    // Lifecycle Components
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
}
```

### Key Architectural Patterns
- **Repository Pattern:** Abstraction layer between ViewModel and data sources
- **LiveData:** Lifecycle-aware observable data holder
- **Event Wrapper Pattern:** One-time event consumption (prevents dialog re-triggering)
- **ViewBinding:** Type-safe view access
- **DiffUtil:** Efficient RecyclerView updates

## 📁 Project Structure

```
DailyTaskTracker/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/tannazetm/dailytasktracker/
│           │   ├── MainActivity.java
│           │   ├── Task.java                      # Task model with tracking
│           │   ├── TaskAdapter.java               # RecyclerView adapter with real-time updates
│           │   ├── TaskDao.java                   # Database DAO with filter queries
│           │   ├── TaskDatabase.java              # Room database
│           │   ├── TaskRepository.java            # Repository pattern implementation
│           │   ├── ui/
│           │   │   ├── home/
│           │   │   │   ├── HomeFragment.java      # Main task list with filters
│           │   │   │   └── HomeViewModel.java     # Filter logic & event handling
│           │   │   ├── dashboard/
│           │   │   │   ├── DashboardFragment.java # Analytics & statistics
│           │   │   │   ├── DashboardViewModel.java
│           │   │   │   └── TimeDistributionChart.java  # Custom chart view
│           │   │   └── notifications/
│           │   │       ├── NotificationsFragment.java  # Smart notifications
│           │   │       └── NotificationsViewModel.java
│           │   └── util/
│           │       └── Event.java                 # Event wrapper for one-time consumption
│           └── res/
│               ├── layout/
│               │   ├── activity_main.xml
│               │   ├── fragment_home_enhanced.xml        # With filter panel
│               │   ├── fragment_dashboard.xml
│               │   ├── fragment_notifications.xml        # Enhanced notifications
│               │   ├── item_task_enhanced.xml            # Task card with tracking
│               │   ├── dialog_add_task_enhanced.xml
│               │   ├── dialog_task_completion.xml
│               │   └── bottom_sheet_task_options.xml
│               ├── drawable/
│               │   ├── ic_filter.xml              # Filter icon
│               │   ├── ic_notification.xml        # Notification bell icon
│               │   ├── ic_priority_low.xml
│               │   ├── ic_priority_medium.xml
│               │   ├── ic_priority_high.xml
│               │   ├── ic_play.xml
│               │   ├── ic_pause.xml
│               │   └── [other icons]
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── navigation/
│                   └── mobile_navigation.xml
└── README.md
```

## 💡 Usage

### Creating a Task
1. Tap the **"Add Task"** floating action button
2. Fill in the task details:
   - Task name (required)
   - Description (optional)
   - Estimated duration (hours/minutes or use quick chips: 15min, 30min, 1h, 2h)
   - Due date and time (tap to select)
   - Priority level (Low/Medium/High)
3. Tap **"Create Task"** to save

### Using Filters 🔍
1. Tap the **filter icon** in the top right of Home screen
2. Select your desired filters:
   - **Date:** Choose from preset options or pick a custom date
   - **Status:** Filter by Active, Completed, or Overdue tasks
   - **Priority:** Show only High, Medium, or Low priority tasks
3. Combine multiple filters for precise results
4. View active filter summary below the filter panel
5. Tap **"Clear All Filters"** to reset

### Managing Tasks
- **Start Time Tracking:** Tap the play button on any task (updates in real-time)
- **Pause Tracking:** Tap pause button (time is automatically saved)
- **Mark as Complete:** Check the checkbox or long-press the task
- **Mark as Immediate:** Tap the star icon (maximum 5 tasks allowed)
- **Edit/Delete/Reschedule:** Tap the three-dot menu for options
- **Swipe to Delete:** Swipe left or right on a task card

### Viewing Analytics
Navigate to the **Dashboard** tab to see:
- Overall completion percentage with circular progress
- Total tasks created vs completed
- Time spent on tasks (formatted as hours and minutes)
- Time distribution chart by priority level
- Visual progress indicators

### Checking Notifications
Navigate to the **Notifications** tab to view:
- Overdue tasks requiring immediate attention
- Tasks due today
- Tasks scheduled for tomorrow
- Recent activity (last 5 completed tasks)
- Quick statistics (Active, Completed, Completion Rate)

## 📄 Recent Updates

### Version 2.0.0 (Latest - Major Update) 🎉
- ✅ **NEW:** Advanced filtering system with date, status, and priority filters
- ✅ **NEW:** Material date picker for custom date selection
- ✅ **NEW:** Active filter summary display
- ✅ **NEW:** Empty state handling with helpful messages
- ✅ **FIXED:** Real-time tracking updates without tab switching (1-second intervals)
- ✅ **FIXED:** Time distribution chart now displays actual data
- ✅ **FIXED:** Dialog re-appearing bug after tab switches (Event wrapper pattern)
- ✅ **NEW:** Comprehensive notifications tab with overdue alerts
- ✅ **NEW:** Recent activity feed showing last 5 completions
- ✅ **NEW:** Custom TimeDistributionChart view with color-coded bars
- ✅ **IMPROVED:** Priority indicators now show for all priority levels
- ✅ **IMPROVED:** Enhanced time comparison display (actual vs estimated)
- ✅ **IMPROVED:** Overtime indicators with dynamic color coding
- ✅ **IMPROVED:** Better lifecycle management for tracking updates
- ✅ **IMPROVED:** Professional UI/UX throughout the app

### Version 1.2.0
- ✅ Fixed time tracking persistence across sessions
- ✅ Added priority indicators for all tasks
- ✅ Improved real-time UI updates
- ✅ Enhanced time comparison display
- ✅ Added overtime indicators with color coding

### Version 1.1.0
- Added swipe-to-delete functionality
- Implemented task completion celebration dialog
- Added analytics dashboard
- Improved Material Design implementation

### Version 1.0.0
- Initial release with basic task management
- Time tracking functionality
- Priority system implementation

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style Guidelines
- Follow Android/Java naming conventions
- Use meaningful variable and method names
- Comment complex logic and algorithms
- Keep methods under 50 lines when possible
- Write unit tests for new features
- Follow MVVM architecture pattern
- Use LiveData for reactive UI updates
- Implement proper lifecycle management

### Development Setup
```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/DailyTaskTracker.git

# Create a feature branch
git checkout -b feature/your-feature-name

# Make your changes and commit
git add .
git commit -m "Description of your changes"

# Push to your fork
git push origin feature/your-feature-name
```

## 📝 Known Issues

Currently, there are no known critical issues. The app is stable and fully functional.

### Minor Notes
- App requires Android 7.0 (API 24) or higher
- Some animations may vary based on device performance
- Time tracking accuracy depends on device's system clock

## 🚧 Roadmap

### Planned Features
- [ ] Push notification reminders for upcoming tasks
- [ ] Data backup/restore to cloud storage
- [ ] Task categories and custom tags
- [ ] Weekly/monthly analytics views with charts
- [ ] Manual dark mode toggle (currently system-based)
- [ ] Task templates for recurring tasks
- [ ] Collaboration features (share tasks)
- [ ] Widget for home screen quick access
- [ ] Export tasks to CSV/PDF
- [ ] Voice input for task creation
- [ ] Integration with calendar apps
- [ ] Pomodoro timer integration
- [ ] Task dependencies (task A must complete before B)
- [ ] Subtasks/checklist within tasks

### Performance Improvements
- [ ] Implement pagination for large task lists
- [ ] Add caching for better offline experience
- [ ] Optimize database queries
- [ ] Add animations for filter transitions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 Tannaz Etminan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 👨‍💻 Author

**Tannaz Etminan**
- GitHub: [@tannazetminan](https://github.com/tannazetminan)
- Email: info@stegroup.ir
- LinkedIn: [TannazEtminan](https://linkedin.com/in/tannazetminan)

## 🙏 Acknowledgments

- **Material Design 3** guidelines and components by Google
- **Android Jetpack** libraries for modern Android development
- **Room Database** for efficient local data persistence
- **Stack Overflow** community for troubleshooting help
- All contributors and users who provided feedback

## 📊 Project Statistics

- **Lines of Code:** ~5,000+
- **Number of Classes:** 20+
- **Database Tables:** 1 (Tasks with 15+ fields)
- **Supported Languages:** English (easily extensible)
- **App Size:** ~5MB

## 🔒 Privacy & Data

- **No Internet Permission:** App works completely offline
- **No User Tracking:** Your data never leaves your device
- **Local Storage Only:** All tasks stored locally in SQLite database
- **No Ads:** Clean, ad-free experience

## 🐛 Bug Reports

Found a bug? Please open an issue with:
- Device model and Android version
- Steps to reproduce the bug
- Expected vs actual behavior
- Screenshots if applicable

## 💬 Support

Need help? You can:
- Open an issue on GitHub
- Email: info@stegroup.ir
- Check existing issues for solutions

---

<p align="center">
  <strong>Made with ❤️ for better productivity</strong><br>
  <sub>⭐ Star this repo if you find it helpful!</sub>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#-usage">Usage</a> •
  <a href="#-contributing">Contributing</a> •
  <a href="#-license">License</a>
</p>