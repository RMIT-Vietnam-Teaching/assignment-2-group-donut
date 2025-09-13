# 📱 Phuong Hai Field Inspection Application

An Android mobile application designed to enhance the workflow between **supervisors** and **inspectors** in Phuong Hai company, enabling faster and centralized data management for inspections.

---

## 📖 Overview
The **Field Inspection App** was developed as a group project for my **Mobile Development** course at **RMIT University**.  
It streamlines inspection reporting by providing inspectors with a mobile-first solution to collect, manage, and share data with supervisors in real time.

---

## 💡 Why I Made This App
Traditional inspection workflows at Phuong Hai company involved **manual note-taking, delayed reporting, and scattered communication**.  
This app solves these issues by:
- Centralizing all inspection data in one place.
- Reducing paperwork and manual errors.
- Enabling supervisors to **monitor tasks, assign jobs, and review reports instantly**.

---

## 👥 Contributors

| Name                  | Student ID | Contribution |
|-----------------------|------------|--------------|
| Tran Thanh Lam        | s4038329   | Planned overall app architecture, designed workflows, implemented all Supervisor-related interfaces (task assignment, dashboard, report review), planned Firebase structure, and developed real-time messaging feature |
| Nguyen Dinh Lam       | s3990403   | Designed workflows, implemented all Inspector-related interfaces (inspection forms, task updates, photo uploads), handled work management module, and integrated machine learning feature |
| Truong Bien Hai Trong | s3872952   | Tested user interface flows, identified UI/UX issues, and clarified requirements during development |
| Cao Ngoc Son          | s3916151   | Conducted QA testing, reported bugs, and supported requirement clarification for Supervisor and Inspector features |
---

## 🎯 What I Learned
- Applying **Android Jetpack components** (ViewModel, LiveData, WorkManager).
- Handling **offline-first architecture** with background sync.
- Managing **team collaboration** with version control (Git & GitHub).
- Writing **clean, maintainable code** with dependency injection (Hilt).
- Understanding the challenges of building **real-world mobile apps** for businesses.

---

## 🚀 Features
✔️ User authentication with role-based access (Supervisor / Inspector)  
✔️ Real-time messaging between Supervisor and Inspector  
✔️ Offline mode with background data synchronization  
✔️ Machine learning to auto-generate inspection descriptions from uploaded images  
✔️ Push notifications for new tasks and status updates
✔️ Inspection form with photo upload & notes  
✔️ Create inspection reports based on assigned tasks
✔️ Task assignment and management  
✔️ Review reports with option to accept or reject submissions  
✔️ Dashboard to monitor progress across branches  
✔️ Manage report history and export approved reports as PDF
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

## My demo video link: [Demo Video]()