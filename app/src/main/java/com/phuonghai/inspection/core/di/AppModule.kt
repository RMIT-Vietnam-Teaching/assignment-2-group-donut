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
import com.phuonghai.inspection.domain.usecase.auth.SignOutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): IAuthRepository {
        return AuthRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance("https://field-reporting-app-15810-default-rtdb.asia-southeast1.firebasedatabase.app/")

    // ✅ Room Database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideLocalReportDao(database: AppDatabase): LocalReportDao {
        return database.localReportDao()
    }

    // ✅ NEW: Local Task DAO
    @Provides
    fun provideLocalTaskDao(database: AppDatabase): LocalTaskDao {
        return database.localTaskDao()
    }

    // ✅ Network Monitor
    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

    // ✅ Offline File Manager
    @Provides
    @Singleton
    fun provideOfflineFileManager(@ApplicationContext context: Context): OfflineFileManager {
        return OfflineFileManager(context)
    }

    // ✅ Online Report Repository (for delegation)
    @Provides
    @Singleton
    fun provideOnlineReportRepository(
        firestore: FirebaseFirestore
    ): ReportRepositoryImpl {
        return ReportRepositoryImpl(firestore)
    }

    // ✅ Online Task Repository (for delegation)
    @Provides
    @Singleton
    fun provideOnlineTaskRepository(
        firestore: FirebaseFirestore
    ): TaskRepositoryImpl {
        return TaskRepositoryImpl(firestore)
    }

    // ✅ Offline Report Repository (main implementation)
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

    // ✅ NEW: Offline Task Repository (main implementation)
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

    // ✅ NEW: Task Sync Service
    @Provides
    @Singleton
    fun provideTaskSyncService(
        offlineTaskRepository: OfflineTaskRepository,
        authRepository: IAuthRepository,
        networkMonitor: NetworkMonitor
    ): TaskSyncService {
        return TaskSyncService(
            offlineTaskRepository,
            authRepository,
            networkMonitor
        )
    }

    // ✅ Report Sync Service
    @Provides
    @Singleton
    fun provideReportSyncService(
        localReportDao: LocalReportDao,
        reportRepository: IReportRepository,
        fileManager: OfflineFileManager
    ): ReportSyncService {
        return ReportSyncService(localReportDao, reportRepository, fileManager)
    }

    // Other existing providers...
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

    @Provides
    @Singleton
    fun provideSignOutUseCase(authRepository: IAuthRepository): SignOutUseCase {
        return SignOutUseCase(authRepository)
    }
}