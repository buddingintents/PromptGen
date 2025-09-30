# Add any ProGuard configurations for the new dependencies and UI components

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Material Design components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep CardView
-keep class androidx.cardview.widget.** { *; }

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep JSON classes
-keep class org.json.** { *; }

# Keep model classes (add your data classes here)
-keep class com.buddingintents.promptgen.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Lottie animations
-keep class com.airbnb.lottie.** { *; }

# Keep AdMob classes
-keep class com.google.android.gms.ads.** { *; }

# General rules for reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}