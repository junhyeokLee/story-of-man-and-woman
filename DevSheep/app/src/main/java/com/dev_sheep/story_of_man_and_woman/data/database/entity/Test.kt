package com.dev_sheep.story_of_man_and_woman.data.database.entity

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dev_sheep.story_of_man_and_woman.utils.ListStringConverter

@Entity
@TypeConverters(ListStringConverter::class)
data class Test( @PrimaryKey
                 @NonNull
                 var id: String,
                 var abilities: List<String>? = null,
                 var attack: Int? = null,
                 var base_exp: String? = null,
                 var category: String? = null,
                 var cycles: String? = null,
                 var defense: Int? = null,
                 var egg_groups: String? = null,
                 var evolutions: List<String>? = null,
                 var evolvedfrom: String? = null,
                 var female_percentage: String? = null,
                 var genderless: Int? = null,
                 var height: String? = null,
                 var hp: Int? = null,
                 var imageurl: String? = null,
                 var male_percentage: String? = null,
                 var name: String? = null,
                 var reason: String? = null,
                 var special_attack: Int? = null,
                 var special_defense: Int? = null,
                 var speed: Int? = null,
                 var total: Int? = null,
                 var typeofpokemon: List<String>? = null,
                 var weaknesses: List<String>? = null,
                 var weight: String? = null,
                 var xdescription: String? = null,
                 var ydescription: String? = null,
                 var favorited: Boolean = false,
                 var viewType: Int? = null){
    companion object {
        const val HEADER = 0
        const val FEED = 1
    }
}
