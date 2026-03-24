package com.simats.nutrisoul.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user_profile WHERE email = :email")
    fun getUserByEmail(email: String): Flow<User?>

    @Query("SELECT * FROM user_profile")
    fun getAllProfiles(): Flow<List<User>>

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
}
