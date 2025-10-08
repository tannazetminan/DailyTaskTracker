# 📱 Daily Task Tracker

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)

A sophisticated Android task management application designed to help users organize their daily activities, track time spent on tasks, and improve productivity through intelligent task prioritization and real-time progress monitoring.

## ✨ Features

### Core Functionality
- **📝 Task Management**
  - Create, edit, and delete tasks with detailed descriptions
  - Set estimated duration for better time management
  - Schedule tasks with specific dates and times
  - Mark tasks as complete with visual feedback

- **⏱️ Time Tracking**
  - Start/stop timer for individual tasks
  - Track actual vs. estimated time
  - Visual indicators for overtime tasks
  - Cumulative time tracking across sessions
  - Automatic time saving when task is paused or completed

- **🎯 Priority System**
  - Three-level priority system (Low, Medium, High)
  - Visual priority indicators with colored icons
  - Priority-based task sorting options

- **⭐ Immediate Tasks**
  - Mark up to 5 tasks as "immediate" for urgent attention
  - Quick-access starred tasks
  - Automatic removal from immediate list when completed

### User Interface
- **Modern Material Design**
  - Clean, intuitive interface following Material Design 3 guidelines
  - Smooth animations and transitions
  - Dark/light theme support (system default)
  - Responsive layout for different screen sizes

- **Interactive Elements**
  - Swipe to delete with confirmation dialog
  - Long press for quick task completion
  - Bottom sheet options menu for advanced actions
  - Floating Action Button for quick task creation

- **Visual Feedback**
  - Color-coded task status (overdue, immediate, completed)
  - Progress bars showing task completion percentage
  - Real-time counters for immediate and completed tasks
  - Task completion celebration dialog with statistics

### Analytics Dashboard
- **📊 Progress Tracking**
  - Daily completion percentage
  - Total tasks vs. completed tasks
  - Time spent statistics
  - Circular progress visualization

## 📸 Screenshots

| Home Screen | Add Task | Task Options | Dashboard |
|------------|----------|--------------|-----------|
| ![Home](screenshots/home.png) | ![Add Task](screenshots/add_task.png) | ![Options](screenshots/options.png) | ![Dashboard](screenshots/dashboard.png) |

*Note: Add your app screenshots to a `screenshots/` folder in the project root*

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- Java 11 or higher

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

## 📁 Project Structure

```
DailyTaskTracker/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/tannazetm/dailytasktracker/
│           │   ├── MainActivity.java
│           │   ├── Task.java                 # Task model
│           │   ├── TaskAdapter.java          # RecyclerView adapter
│           │   ├── TaskDao.java              # Database DAO
│           │   ├── TaskDatabase.java         # Room database
│           │   ├── TaskRepository.java       # Repository pattern
│           │   └── ui/
│           │       ├── home/
│           │       │   ├── HomeFragment.java
│           │       │   └── HomeViewModel.java
│           │       ├── dashboard/
│           │       │   ├── DashboardFragment.java
│           │       │   └── DashboardViewModel.java
│           │       └── notifications/
│           │           └── NotificationsFragment.java
│           └── res/
│               ├── layout/
│               │   ├── activity_main.xml
│               │   ├── fragment_home.xml
│               │   ├── fragment_dashboard.xml
│               │   ├── item_task_enhanced.xml
│               │   ├── dialog_add_task_enhanced.xml
│               │   ├── dialog_task_completion.xml
│               │   └── bottom_sheet_task_options.xml
│               ├── drawable/
│               │   ├── ic_priority_low.xml
│               │   ├── ic_priority_medium.xml
│               │   ├── ic_priority_high.xml
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
   - Estimated duration (hours/minutes or use quick chips)
   - Due date and time
   - Priority level (Low/Medium/High)
3. Tap **"Create Task"** to save

### Managing Tasks
- **Start Time Tracking:** Tap the play button on any task
- **Mark as Complete:** Check the checkbox or long-press the task
- **Mark as Immediate:** Tap the star icon (max 5 tasks)
- **Edit/Delete/Reschedule:** Tap the three-dot menu for options

### Viewing Analytics
- Navigate to the Dashboard tab to see:
  - Overall completion percentage
  - Total tasks created vs completed
  - Time spent on tasks
  - Visual progress indicators

## 🔄 Recent Updates

### Version 1.2.0 (Latest)
- ✅ Fixed time tracking persistence across sessions
- ✅ Added priority indicators for all tasks
- ✅ Improved real-time UI updates without tab switching
- ✅ Enhanced time comparison display (actual vs estimated)
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

### Code Style
- Follow Android/Java naming conventions
- Comment complex logic
- Keep methods under 50 lines when possible
- Write unit tests for new features

## 📝 Known Issues

- Chart visualization in Dashboard is currently a placeholder
- Notification system not yet implemented
- No data export functionality yet

## 🚧 Roadmap

- [ ] Add notification reminders for tasks
- [ ] Implement data backup/restore
- [ ] Add task categories and tags
- [ ] Create weekly/monthly analytics views
- [ ] Add dark mode toggle
- [ ] Implement task templates
- [ ] Add collaboration features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Tannaz Etminan**
- GitHub: [@tannazetminan](https://github.com/tannazetminan)
- Email: info@stegroup.ir

## 🙏 Acknowledgments

- Material Design Icons by Google
- Android Development Documentation
- Stack Overflow community for troubleshooting help

---

<p align="center">Made with ❤️ for better productivity</p>