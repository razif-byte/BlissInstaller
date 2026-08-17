package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BackupRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallationLogDao {
    @Query("SELECT * FROM installation_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<InstallationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: InstallationLogEntity): Long

    @Query("DELETE FROM installation_logs")
    suspend fun clearLogs()
}

@Dao
interface BackupRecordDao {
    @Query("SELECT * FROM backup_records ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<BackupRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupRecord): Long

    @Query("DELETE FROM backup_records WHERE id = :id")
    suspend fun deleteBackup(id: Long)
}
