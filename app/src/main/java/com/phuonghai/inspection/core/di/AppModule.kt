package com.phuonghai.inspection.core.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.core.storage.OfflineFileManager
import com.phuonghai.inspection.core.sync.ReportSyncService
import com.phuonghai.inspection.core.sync.SyncManager
import com.phuonghai.inspection.core.sync.TaskSyncService
import com.phuonghai.inspection.data.local.dao.LocalReportDao
import com.phuonghai.inspection.data.local.dao.LocalTaskDao
import com.phuonghai.inspection.data.local.database.AppDatabase
import com.phuonghai.inspection.data.repository.AuthRepositoryImpl
import com.phuonghai.inspection.data.repository.BranchRepositoryImpl
import com.phuonghai.inspection.data.repository.ChatMessageRepositoryImpl
import com.phuonghai.inspection.data.repository.OfflineReportRepository
import com.phuonghai.inspection.data.repository.OfflineTaskRepository
import com.phuonghai.inspection.data.repository.ReportRepositoryImpl
import com.phuonghai.inspection.data.repository.TaskRepositoryImpl
import com.phuonghai.inspection.data.repository.UserRepositoryImpl
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.IBranchRepository
import com.phuonghai.inspection.domain.repository.IChatMessageRepository
import com.phuonghai.inspection.domain.repository.IReportRepository
import com.phuonghai.inspection.domain.repository.ITaskRepository
import com.phuonghai.inspection.domain.repository.IUserRepository
import com.phuonghai.inspection.domain.usecase.GetFirebaseReportsByInspectorUseCase
import com.phuonghai.inspection.domain.usecase.GetPendingReportsBySupervisorUseCase
import com.phuonghai.inspection.domain.usecase.auth.SignOutUseCase
import com.phuonghai.inspection.domain.usecase.GetInspectorTasksUseCase
import com.phuonghai.inspection.domain.usecase.GetReportsByInspectorUseCase
import com.phuonghai.inspection.domain.usecase.GetTodayTasksUseCase
import com.phuonghai.inspection.domain.usecase.UpdateTaskStatusUseCase
import com.phuonghai.inspection.presentation.inspector.historyreport.InspectorHistoryViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ✅ Firebase Core Services
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance("https://field-reporting-app-15810-default-rtdb.asia-southeast1.firebasedatabase.app/")

    // ✅ Room Database & DAOs
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideLocalReportDao(database: AppDatabase): LocalReportDao {
        return database.localReportDao()
    }

    @Provides
    fun provideLocalTaskDao(database: AppDatabase): LocalTaskDao {
        return database.localTaskDao()
    }

    // ✅ Core System Services
    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideOfflineFileManager(@ApplicationContext context: Context): OfflineFileManager {
        return OfflineFileManager(context)
    }

    // ✅ Online Repository Implementations (for delegation)
    @Provides
    @Singleton
    fun provideOnlineReportRepository(
        firestore: FirebaseFirestore
    ): ReportRepositoryImpl {
        return ReportRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideOnlineTaskRepository(
        firestore: FirebaseFirestore
    ): TaskRepositoryImpl {
        return TaskRepositoryImpl(firestore)
    }

    // ✅ Auth Repository
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): IAuthRepository {
        return AuthRepositoryImpl(auth, firestore)
    }

    // ✅ Main Repository Implementations (with offline support)
    @Provides
    @Singleton
    fun provideReportRepository(
        localReportDao: LocalReportDao,
        onlineReportRepository: ReportRepositoryImpl,
        networkMonitor: NetworkMonitor,
        fileManager: OfflineFileManager,
        @ApplicationContext context: Context
    ): IReportRepository {
        return OfflineReportRepository(
            localReportDao,
            onlineReportRepository,
            networkMonitor,
            fileManager,
            context
        )
    }

    // 🆕 UPDATED: Use improved OfflineTaskRepository
    @Provides
    @Singleton
    fun provideTaskRepository(
        localTaskDao: LocalTaskDao,
        onlineTaskRepository: TaskRepositoryImpl,
        networkMonitor: NetworkMonitor,
        @ApplicationContext context: Context
    ): ITaskRepository {
        return OfflineTaskRepository(
            localTaskDao,
            onlineTaskRepository,
            networkMonitor,
            context
        )
    }

    // ✅ Other Repository Implementations
    @Provides
    @Singleton
    fun provideBranchRepository(firestore: FirebaseFirestore): IBranchRepository {
        return BranchRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(firestore: FirebaseFirestore): IUserRepository {
        return UserRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideChatMessageRepository(
        firebaseDatabase: FirebaseDatabase
    ): IChatMessageRepository {
        return ChatMessageRepositoryImpl(firebaseDatabase)
    }

    // 🆕 UPDATED: Improved Sync Services
    @Provides
    @Singleton
    fun provideReportSyncService(
        localReportDao: LocalReportDao,
        reportRepository: IReportRepository,
        networkMonitor: NetworkMonitor,
        fileManager: OfflineFileManager
    ): ReportSyncService {
        return ReportSyncService(
            localReportDao,
            reportRepository,
            networkMonitor,
            fileManager
        )
    }

    @Provides
    @Singleton
    fun provideTaskSyncService(
        taskRepository: ITaskRepository,
        authRepository: IAuthRepository,
        networkMonitor: NetworkMonitor
    ): TaskSyncService {
        // Cast to OfflineTaskRepository to access sync methods
        val offlineTaskRepository = taskRepository as OfflineTaskRepository
        return TaskSyncService(
            offlineTaskRepository,
            authRepository,
            networkMonitor
        )
    }

    // 🆕 UPDATED: SyncManager with improved services
    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        networkMonitor: NetworkMonitor,
        authRepository: IAuthRepository,
        taskSyncService: TaskSyncService,
        reportSyncService: ReportSyncService
    ): SyncManager {
        return SyncManager(
            context,
            networkMonitor,
            authRepository,
            taskSyncService,
            reportSyncService
        )
    }

    // ✅ Use Cases
    @Provides
    @Singleton
    fun provideSignOutUseCase(authRepository: IAuthRepository): SignOutUseCase {
        return SignOutUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideGetInspectorTasksUseCase(taskRepository: ITaskRepository): GetInspectorTasksUseCase {
        return GetInspectorTasksUseCase(taskRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateTaskStatusUseCase(taskRepository: ITaskRepository): UpdateTaskStatusUseCase {
        return UpdateTaskStatusUseCase(taskRepository)
    }

    // 🆕 Updated History ViewModel
    @Provides
    @Singleton
    fun provideInspectorHistoryViewModel(
        getFirebaseReportsByInspectorUseCase: GetFirebaseReportsByInspectorUseCase,
        localReportDao: LocalReportDao,
        networkMonitor: NetworkMonitor
    ): InspectorHistoryViewModel {
        return InspectorHistoryViewModel(
            getFirebaseReportsByInspectorUseCase,
            localReportDao,
            networkMonitor
        )
    }
    @Provides
    @Singleton
    fun provideGetTodayTasksUseCase(
        taskRepository: ITaskRepository
    ): GetTodayTasksUseCase {
        return GetTodayTasksUseCase(taskRepository)
    }

    @Provides
    @Singleton
    fun provideGetReportsByInspectorUseCase(
        reportRepository: IReportRepository
    ): GetReportsByInspectorUseCase {
        return GetReportsByInspectorUseCase(reportRepository)
    }
    @Provides
    @Singleton
    fun provideGetFirebaseReportsByInspectorUseCase(
        firebaseReportRepository: ReportRepositoryImpl
    ): GetFirebaseReportsByInspectorUseCase {
        return GetFirebaseReportsByInspectorUseCase(firebaseReportRepository)
    }

    @Provides
    @Singleton
    fun provideGetPendingReportsBySupervisorUseCase(
        reportRepository: IReportRepository
    ): GetPendingReportsBySupervisorUseCase {
        return GetPendingReportsBySupervisorUseCase(reportRepository)
    }
}