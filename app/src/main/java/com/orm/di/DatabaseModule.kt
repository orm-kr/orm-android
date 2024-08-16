package com.orm.di

import android.content.Context
import androidx.room.Room
import com.orm.data.local.AppDatabase
import com.orm.data.local.dao.ClubDao
import com.orm.data.local.dao.MemberDao
import com.orm.data.local.dao.NotificationDao
import com.orm.data.local.dao.PointDao
import com.orm.data.local.dao.RecordDao
import com.orm.data.local.dao.TraceDao
import com.orm.data.local.dao.TrailDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRoom(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    }

    @Provides
    fun provideClubDao(database: AppDatabase): ClubDao {
        return database.clubDao()
    }

    @Provides
    fun provideMemberDao(database: AppDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    fun provideTraceDao(database: AppDatabase): TraceDao {
        return database.traceDao()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun providePointDao(database: AppDatabase): PointDao {
        return database.pointDao()
    }

    @Provides
    fun provideTrailDao(database: AppDatabase): TrailDao {
        return database.trailDao()
    }

    @Provides
    fun provideRecordDao(database: AppDatabase): RecordDao {
        return database.recordDao()
    }
}