# ProGuard & R8 optimization rules for Al-Ezzi Grocery POS

# Keep Room Database and Entities
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class com.example.data.local.entities.** { *; }
-keep class com.example.data.local.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Compose models and states
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# General size optimizations
-repackageclasses
-allowaccessmodification
