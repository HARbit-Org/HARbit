# Authentication ProGuard Rules
# Keep all DTO classes for serialization

# Keep all DTOs
-keep class com.example.harbit.data.remote.dto.** { *; }

# Keep Retrofit interfaces
-keep interface com.example.harbit.data.remote.service.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializers for DTOs
-keep,includedescriptorclasses class com.example.harbit.**$$serializer { *; }
-keepclassmembers class com.example.harbit.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.harbit.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp and Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Retrofit does reflection on generic parameters
-keepattributes Signature
-keepattributes Exceptions

# DataStore
-keep class androidx.datastore.*.** {*;}
