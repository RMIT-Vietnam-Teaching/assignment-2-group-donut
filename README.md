

## 💡 Why I Made This App
Traditional inspection workflows at Phuong Hai company involved **manual note-taking, delayed reporting, and scattered communication**.  
This app solves these issues by:
- Centralizing all inspection data in one place.
- Reducing paperwork and manual errors.
- Enabling supervisors to **monitor tasks, assign jobs, and review reports instantly**.



## 🎯 What I Learned
- Applying **Android Jetpack components** (ViewModel, LiveData, WorkManager).
- Handling **offline-first architecture** with background sync.
- Managing **team collaboration** with version control (Git & GitHub).
- Writing **clean, maintainable code** with dependency injection (Hilt).
- Understanding the challenges of building **real-world mobile apps** for businesses.

---

## 🚀 Features
- User authentication with role-based access (Supervisor / Inspector)
- Real-time messaging between Supervisor and Inspector
- Offline mode with background data synchronization
- Machine learning to auto-generate inspection descriptions from uploaded images
- Push notifications for new tasks and status updates 
- Inspection form with photo upload & notes
- Create inspection reports based on assigned tasks 
- Task assignment and management
- Review reports with option to accept or reject submissions
- Dashboard to monitor progress across branches
- Manage report history and export approved reports as PDF
- Compose UI and unit testing 
---

## 🛠 Tech Stack
- **Language**: Kotlin
- **Frameworks & Libraries**: Android Jetpack (ViewModel, LiveData, WorkManager, Room), Hilt, Coroutine
- **Backend / Database**: Firebase Realtime Database & Firebase Storage & Firebase Authentication && Firebase Storage
- **UI**: Jetpack Compose + Material Design
- **Tools**: GitHub, Android Studio

---

## 📂 How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/RMIT-Vietnam-Teaching/assignment-2-group-donut

## My demo video link: https://youtu.be/N1hbhho7Pu8



# FieldInspectionApp (Android • Jetpack Compose)

> An on-site inspection management app with **two roles** (Inspector & Supervisor). Supports **offline work**, **automatic sync**, **phone OTP login (Firebase Auth)**, **Google Maps integration**, **reports with photos/ML Kit auto-labeling**, **chat**, **notifications**, and **PDF export**.

🎥 **Demo video:** https://youtu.be/N1hbhho7Pu8

---

## 1) Quick Info
- **App name**: `FieldInspectionApp`
- **Package**: `com.phuonghai.inspection`
- **Min SDK**: 24
- **Target / Compile SDK**: 36
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material3)
- **DI**: Hilt
- **Local DB**: Room
- **Background sync**: WorkManager + Foreground Service (network listener)
- **Backend**: Firebase (Auth – Phone OTP, Firestore, Storage, Realtime DB for chat)
- **Maps**: Google Maps (Maps Compose + Play Services Location)
- **ML**: ML Kit (Image Labeling)
- **PDF export**: iText7
- **Networking**: OkHttp
- **Architecture**: Clean Architecture + MVVM + Repository + UseCase
- **Modules**: zip contains only the `app/` module (no Gradle root project).

> 🔐 Security note: the repo includes `google-services.json` and a **hard-coded Google Maps API key** inside `AndroidManifest.xml`. For public repos, remove these and load them from `local.properties` + `manifestPlaceholders`.


## 👥 Contributors

| Name                  | Student ID | Contribution |
|-----------------------|------------|--------------|
| Tran Thanh Lam        | s4038329   | Planned overall app architecture, designed workflows, implemented all Supervisor-related interfaces (task assignment, dashboard, report review), planned Firebase structure, and developed real-time messaging feature |
| Nguyen Dinh Lam       | s3990403   | Planned overall app architecture,Designed workflows, implemented all Inspector-related interfaces (inspection forms, task updates, photo uploads), handled work management module,sync worker and integrated machine learning feature |
| Truong Bien Hai Trong | s3872952   | Conducted end-to-end testing across user flows, identified and documented UI/UX issues, supported integration testing between Inspector and Supervisor modules, and collaborated with the team to refine requirements and improve usability |
| Cao Ngoc Son          | s3916151   | Executed QA testing, reported functional bugs, validated Supervisor/Inspector features, and assisted in requirement clarification and final verification |
---

---

## 2) Key Features
### For **Inspector**
- Login with **phone number + OTP (Firebase Phone Auth)**.
- Dashboard: view **assigned tasks**, filter by branch/priority.
- **Create Report**: title, description, inspection type (*Electrical, Fire Safety, Structural, Food Hygiene, Environmental, Machinery*), score, location (lat/lng + address), photos/videos.
- **ML Kit Image Labeling**: auto-suggests labels from attached images.
- **Offline support**: reports/tasks stored in Room → **auto-synced** to Firestore/Storage when online.
- **Report history**: view past reports with status (**PENDING/APPROVED/REJECTED**) and supervisor notes.
- **1–1 Chat** with Supervisor, plus **notifications** for updates.

### For **Supervisor**
- Dashboard + **Google Maps view** of report locations.
- Review reports: update status **ASSIGN / IN_PROGRESS / COMPLETED / CANCELLED / OVERDUE** (Task) and **PASSED/FAILED/NEEDS_ATTENTION** (Report).
- Manage **notifications** and **chat** with Inspectors.
- Filter by **Branch**, **Priority**, or time.

### System/Platform
- **Network monitor + Foreground service**: triggers **WorkManager sync**.
- **FileProvider** for external file sharing (PDF/images).
- **PDF export** with iText7 (note license restrictions).

---

## 3) Architecture & Folder Structure
### Model
- **Clean Architecture + MVVM**
  - **Presentation**: Compose screens + ViewModels
  - **Domain**: models, repositories, use cases
  - **Data**: repositories (Firebase/Room), local DB, remote Firestore/Storage/Realtime
  - **Core**: DI (Hilt), networking, sync, file storage

### Important Folders
```
app/src/main/java/com/phuonghai/inspection/
├─ InspectionApp.kt              # @HiltAndroidApp entry point, network listener
├─ MainActivity.kt / MainViewModel.kt
├─ core/ (di, network, storage, sync)
├─ data/ (local Room, repositories, remote Firebase)
├─ domain/ (model, repository interfaces, use cases)
├─ presentation/
│  ├─ auth (PhoneLogin, OTP, Splash)
│  ├─ navigation (NavHost + bottom bars for roles)
│  ├─ inspector (dashboard, report, history, notifications, tasks, chat)
│  ├─ supervisor (dashboard, map, history, report, tasks, chat)
│  └─ theme
└─ res/xml (backup rules, file_paths, etc.)
```

> ⚠️ One file (`SupervisorMapScreen.kt`) uses package `com.donut.assignment2...` instead of `com.phuonghai.inspection...`. Should be fixed to avoid build errors.

---

## 4) Environment Setup
Since only `app/` is provided, two options:

### Option A – Use Android Studio (recommended)
1. Install Android Studio **Ladybug 2024.2.1+**.
2. Create a new project → remove its `app/` → replace with this `app/` folder.
3. Add to `settings.gradle.kts`:
   ```kotlin
   include(":app")
   ```
4. Ensure root `build.gradle.kts` has correct plugin/versions (AGP, Kotlin, Hilt).
5. Sync Gradle → build.

### Option B – Minimal Gradle setup
Add root `settings.gradle.kts` and `build.gradle.kts` with proper plugin versions.

---

## 5) Secrets & Config
- **Firebase**: requires `google-services.json` in `app/`. Don’t commit this file.
- **Google Maps API key**: move from `AndroidManifest.xml` → `local.properties` + `manifestPlaceholders`.
- **Permissions**: INTERNET, NETWORK_STATE, FINE_LOCATION, CAMERA, STORAGE.

---

## 6) How to Run
- Run on device/emulator with **Google Play services**.
- Login flow: enter phone → receive OTP → verify → redirect to Inspector/Supervisor dashboard based on Firestore role.

  Account:
- Supervisor Account: Phone: 123456789, OTP: 123456
- Inspector Account: Phone: 223456789, OTP: 123456

---

## 7) Tech Stack
- **Compose**: Material3, icons, window-size-class.
- **DI**: Hilt.
- **Coroutines**.
- **Room**.
- **WorkManager**, **App Startup**.
- **Maps**: Maps Compose, Play Services Maps/Location.
- **Firebase**: Auth, Firestore, Storage, Realtime DB.
- **ML Kit**: image labeling.
- **Networking**: OkHttp.
- **PDF**: iText7.
- **Testing**: JUnit, Robolectric.

---

## 8) Data Models (simplified)
```kotlin
data class User(uId: String, fullName: String, phoneNumber: String, role: UserRole)

data class Task(taskId: String, title: String, description: String, priority: Priority, status: TaskStatus)

data class Report(reportId: String, inspectorId: String, taskId: String,
                  title: String, description: String, type: InspectionType,
                  lat: String, lng: String, address: String, score: Int?,
                  responseStatus: ResponseStatus, imageUrl: String?)
```

---

## 9) Navigation
- NavHost in `presentation/navigation/Navigation.kt`.
- After login → redirects to:
  - **InspectorNavigationBar**
  - **SupervisorNavigationBar**

---

## 10) Testing
- Run `./gradlew test` or via Android Studio.
- Example tests: `LocalReportDaoTest.kt`, `ReportMergeTest.kt`.

---

## 11) Build & Release
- Debug/release variants.
- Add ProGuard rules for Hilt/Room/Firebase.
- FileProvider config for external file sharing.

---

## 12) Common Issues
- Wrong package → fix `SupervisorMapScreen.kt`.
- AGP/Kotlin mismatch → adjust root `build.gradle.kts` or update Studio.
- Missing Compose BOM/`libs.versions.toml` → add or replace aliases with direct versions.
- Maps API key empty → app crashes → use manifestPlaceholders.
- Storage permissions (use Scoped Storage for Android 10+).

---

## 13) Roadmap (suggested)
- Push Notifications (FCM).
- Better role-based navigation (deeplink).
- Local DB/file encryption.
- Map clustering for reports.
- Crashlytics/Analytics.
- Accessibility & UI polish.

---

## 14) Authors & Contact
- **Team/Owner**: (fill names/roles)
- **Email**: (add contact email)
- **Links**: GitHub/LinkedIn

---

## 15) License
- **Default**: course project/private.
- **Third-party**:
  - iText7: **AGPL/commercial license**. Consider alternatives if commercial use.
  - Google Maps/ML Kit/Firebase: per Google terms.

---

### Notes
- Code is already structured with **Clean Architecture + MVVM**, ready to extend.
- Hide/change all API keys before making the repo public.
