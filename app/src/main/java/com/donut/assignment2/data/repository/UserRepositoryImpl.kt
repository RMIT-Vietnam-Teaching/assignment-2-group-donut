package com.donut.assignment2.data.repository

import android.util.Log
import com.donut.assignment2.data.local.dao.UserDao
import com.donut.assignment2.data.mapper.toDomain
import com.donut.assignment2.data.mapper.toEntity
import com.donut.assignment2.data.mapper.toUser
import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.model.UserRole
import com.donut.assignment2.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun getUserByPhone(phoneNumber: String): User? {
        return try {
            Log.d(TAG, "Getting user by phone: $phoneNumber")

            // Try local cache first for better performance
            val localUser = userDao.getUserByPhone(phoneNumber)?.toDomain()
            if (localUser != null) {
                Log.d(TAG, "User found in local cache: ${localUser.fullName}")
                return localUser
            }

            // Fallback to Firestore
            Log.d(TAG, "User not in cache, fetching from Firestore")
            val document = firestore.collection(USERS_COLLECTION)
                .document(phoneNumber)
                .get()
                .await()

            if (document.exists() && document.data != null) {
                val user = mapFirestoreDocumentToUser(document.data!!, phoneNumber)
                Log.d(TAG, "User found in Firestore: ${user.fullName}")

                // Cache locally for future use
                try {
                    userDao.insertUser(user.toEntity())
                    Log.d(TAG, "User cached locally")
                } catch (cacheException: Exception) {
                    Log.w(TAG, "Failed to cache user locally", cacheException)
                    // Continue anyway, cache failure shouldn't break the flow
                }

                user
            } else {
                Log.w(TAG, "User document not found: $phoneNumber")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by phone: $phoneNumber", e)

            // Try to return cached data if Firestore fails
            try {
                val cachedUser = userDao.getUserByPhone(phoneNumber)?.toDomain()
                if (cachedUser != null) {
                    Log.w(TAG, "Returning cached user due to Firestore error")
                    return cachedUser
                }
            } catch (cacheException: Exception) {
                Log.e(TAG, "Cache also failed", cacheException)
            }

            null
        }
    }

    override suspend fun getUsersByRole(role: UserRole): List<User> {
        return try {
            Log.d(TAG, "Getting users by role: $role")

            // Try cache first
            val cachedUsers = userDao.getUsersByRole(role.name).map { it.toDomain() }

            try {
                // Get fresh data from Firestore
                val querySnapshot = firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("role", role.name)
                    .get()
                    .await()

                val firestoreUsers = querySnapshot.documents.mapNotNull { document ->
                    try {
                        document.data?.let { data ->
                            mapFirestoreDocumentToUser(data, document.id)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to map user document: ${document.id}", e)
                        null
                    }
                }

                // Cache fresh data
                try {
                    val entities = firestoreUsers.map { it.toEntity() }
                    entities.forEach { userDao.insertUser(it) }
                    Log.d(TAG, "Cached ${entities.size} users with role: $role")
                } catch (cacheException: Exception) {
                    Log.w(TAG, "Failed to cache users", cacheException)
                }

                Log.d(TAG, "Found ${firestoreUsers.size} users with role: $role")
                firestoreUsers

            } catch (firestoreException: Exception) {
                Log.w(TAG, "Firestore failed, returning cached data", firestoreException)
                cachedUsers
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users by role: $role", e)
            emptyList()
        }
    }

    override suspend fun getInspectorsBySupervisor(supervisorPhone: String): List<User> {
        return try {
            Log.d(TAG, "Getting inspectors for supervisor: $supervisorPhone")

            // Try cache first
            val cachedInspectors = userDao.getInspectorsBySupervisor(supervisorPhone)
                .map { it.toDomain() }

            try {
                // Get fresh data from Firestore
                val querySnapshot = firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("role", "INSPECTOR")
                    .whereEqualTo("supervisorPhone", supervisorPhone)
                    .get()
                    .await()

                val firestoreInspectors = querySnapshot.documents.mapNotNull { document ->
                    try {
                        document.data?.let { data ->
                            mapFirestoreDocumentToUser(data, document.id)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to map inspector document: ${document.id}", e)
                        null
                    }
                }

                // Cache fresh data
                try {
                    val entities = firestoreInspectors.map { it.toEntity() }
                    entities.forEach { userDao.insertUser(it) }
                    Log.d(TAG, "Cached ${entities.size} inspectors for supervisor: $supervisorPhone")
                } catch (cacheException: Exception) {
                    Log.w(TAG, "Failed to cache inspectors", cacheException)
                }

                Log.d(TAG, "Found ${firestoreInspectors.size} inspectors for supervisor: $supervisorPhone")
                firestoreInspectors

            } catch (firestoreException: Exception) {
                Log.w(TAG, "Firestore failed, returning cached inspectors", firestoreException)
                cachedInspectors
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting inspectors for supervisor: $supervisorPhone", e)
            emptyList()
        }
    }

    override suspend fun getAllSupervisors(): List<User> {
        return getUsersByRole(UserRole.SUPERVISOR)
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return try {
            Log.d(TAG, "Saving user: ${user.phoneNumber}")

            // Prepare data for Firestore
            val userData = hashMapOf(
                "phoneNumber" to user.phoneNumber,
                "fullName" to user.fullName,
                "email" to user.email,
                "role" to user.role.name,
                "supervisorPhone" to user.supervisorPhone,
                "profileImageUrl" to user.profileImageUrl
            )

            // Save to Firestore using phone as document ID
            firestore.collection(USERS_COLLECTION)
                .document(user.phoneNumber)
                .set(userData)
                .await()

            Log.d(TAG, "User saved to Firestore: ${user.phoneNumber}")

            // Save to local cache
            try {
                userDao.insertUser(user.toEntity())
                Log.d(TAG, "User cached locally: ${user.phoneNumber}")
            } catch (cacheException: Exception) {
                Log.w(TAG, "Failed to cache user locally", cacheException)
                // Continue anyway, cache failure shouldn't break the save operation
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user: ${user.phoneNumber}", e)
            Result.failure(Exception("Không thể lưu thông tin người dùng: ${e.message}"))
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            Log.d(TAG, "Updating user: ${user.phoneNumber}")

            // Prepare update data
            val updates = hashMapOf<String, Any?>(
                "fullName" to user.fullName,
                "email" to user.email,
                "role" to user.role.name,
                "supervisorPhone" to user.supervisorPhone,
                "profileImageUrl" to user.profileImageUrl
            )

            // Update in Firestore
            firestore.collection(USERS_COLLECTION)
                .document(user.phoneNumber)
                .update(updates)
                .await()

            Log.d(TAG, "User updated in Firestore: ${user.phoneNumber}")

            // Update local cache
            try {
                userDao.updateUser(user.toEntity())
                Log.d(TAG, "User updated in local cache: ${user.phoneNumber}")
            } catch (cacheException: Exception) {
                Log.w(TAG, "Failed to update user in cache", cacheException)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${user.phoneNumber}", e)
            Result.failure(Exception("Không thể cập nhật thông tin người dùng: ${e.message}"))
        }
    }

    override suspend fun deleteUser(phoneNumber: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting user: $phoneNumber")

            // Delete from Firestore
            firestore.collection(USERS_COLLECTION)
                .document(phoneNumber)
                .delete()
                .await()

            Log.d(TAG, "User deleted from Firestore: $phoneNumber")

            // Delete from local cache
            try {
                userDao.deleteByPhone(phoneNumber)
                Log.d(TAG, "User deleted from local cache: $phoneNumber")
            } catch (cacheException: Exception) {
                Log.w(TAG, "Failed to delete user from cache", cacheException)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user: $phoneNumber", e)
            Result.failure(Exception("Không thể xóa người dùng: ${e.message}"))
        }
    }

    override suspend fun saveUserToLocal(user: User): Result<Unit> {
        return try {
            userDao.insertUser(user.toEntity())
            Log.d(TAG, "User saved to local cache: ${user.phoneNumber}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to local cache: ${user.phoneNumber}", e)
            Result.failure(Exception("Không thể lưu cache: ${e.message}"))
        }
    }

    override suspend fun clearLocalCache(): Result<Unit> {
        return try {
            userDao.deleteAllUsers()
            Log.d(TAG, "Local user cache cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local cache", e)
            Result.failure(Exception("Không thể xóa cache: ${e.message}"))
        }
    }

    override fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        Log.d(TAG, "Setting up users flow listener")

        val listener = firestore.collection(USERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Users flow error", error)
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.data?.let { data ->
                            mapFirestoreDocumentToUser(data, document.id)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to map user in flow: ${document.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "Users flow updated: ${users.size} users")
                trySend(users)
            }

        awaitClose {
            Log.d(TAG, "Removing users flow listener")
            listener.remove()
        }
    }.catch { e ->
        Log.e(TAG, "Users flow error", e)
        emit(emptyList()) // Emit empty list on error
    }

    override fun getInspectorsByRoleFlow(supervisorPhone: String): Flow<List<User>> = callbackFlow {
        Log.d(TAG, "Setting up inspectors flow for supervisor: $supervisorPhone")

        val listener = firestore.collection(USERS_COLLECTION)
            .whereEqualTo("role", "INSPECTOR")
            .whereEqualTo("supervisorPhone", supervisorPhone)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Inspectors flow error", error)
                    close(error)
                    return@addSnapshotListener
                }

                val inspectors = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.data?.let { data ->
                            mapFirestoreDocumentToUser(data, document.id)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to map inspector in flow: ${document.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "Inspectors flow updated: ${inspectors.size} inspectors for supervisor: $supervisorPhone")
                trySend(inspectors)
            }

        awaitClose {
            Log.d(TAG, "Removing inspectors flow listener")
            listener.remove()
        }
    }.catch { e ->
        Log.e(TAG, "Inspectors flow error", e)
        emit(emptyList())
    }

    override suspend fun countInspectorsBySupervisor(supervisorPhone: String): Int {
        return try {
            val querySnapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("role", "INSPECTOR")
                .whereEqualTo("supervisorPhone", supervisorPhone)
                .get(Source.CACHE) // Try cache first for count
                .await()

            val count = querySnapshot.size()
            Log.d(TAG, "Inspector count for supervisor $supervisorPhone: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error counting inspectors for supervisor: $supervisorPhone", e)

            // Fallback to local cache count
            try {
                userDao.countInspectorsBySupervisor(supervisorPhone)
            } catch (cacheException: Exception) {
                Log.e(TAG, "Cache count also failed", cacheException)
                0
            }
        }
    }

    // 🔧 Helper function to map Firestore document to User model
    private fun mapFirestoreDocumentToUser(data: Map<String, Any>, phoneNumber: String): User {
        return User(
            phoneNumber = data["phoneNumber"] as? String ?: phoneNumber,
            fullName = data["fullName"] as? String ?: "Unknown User",
            email = data["email"] as? String,
            role = try {
                UserRole.valueOf(data["role"] as? String ?: "INSPECTOR")
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Invalid role in Firestore for user $phoneNumber, defaulting to INSPECTOR", e)
                UserRole.INSPECTOR
            },
            supervisorPhone = data["supervisorPhone"] as? String,
            profileImageUrl = data["profileImageUrl"] as? String
        )
    }
}