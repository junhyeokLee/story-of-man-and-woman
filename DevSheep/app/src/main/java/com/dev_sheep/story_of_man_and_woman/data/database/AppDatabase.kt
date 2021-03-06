package com.dev_sheep.story_of_man_and_woman.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev_sheep.story_of_man_and_woman.data.database.converter.Converters
import com.dev_sheep.story_of_man_and_woman.data.database.dao.SearchDAO
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Feed
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Search
import com.leopold.mvvm.data.db.converter.DateConverter


@Database(version = 5, entities = [Feed::class, Search::class],exportSchema = false )
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun searchDAO(): SearchDAO

}
