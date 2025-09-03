package com.phuonghai.inspection.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.phuonghai.inspection.data.repository.AuthRepositoryImpl
import com.phuonghai.inspection.data.repository.BranchRepositoryImpl
import com.phuonghai.inspection.data.repository.ReportRepositoryImpl
import com.phuonghai.inspection.data.repository.TaskRepositoryImpl
import com.phuonghai.inspection.data.repository.UserRepositoryImpl
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.IBranchRepository
import com.phuonghai.inspection.domain.repository.IReportRepository
import com.phuonghai.inspection.domain.repository.ITaskRepository
import com.phuonghai.inspection.domain.repository.IUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): IAuthRepository {
        return AuthRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideReportRepository(
        firestore: FirebaseFirestore
    ): IReportRepository {
        return ReportRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        firestore: FirebaseFirestore
    ): ITaskRepository {
        return TaskRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideBranchRepository(
        firestore: FirebaseFirestore
    ): IBranchRepository {
        return BranchRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore
    ): IUserRepository {
        return UserRepositoryImpl(firestore)
    }

}