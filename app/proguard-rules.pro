# Preserve type information for Gson to correctly map data types
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson classes to avoid errors
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep type information for classes that use reflection, especially for generic classes
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Retrofit classes to avoid errors
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
# Keep generic signature of Call, Response (R8 full mode signature strips from non-kept items).
 -keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep OkHttp classes to ensure Retrofit can work properly
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Retain your project's layers, including domain, data, and model
-keep class vn.xdeuhug.camerax.domain.model.** { *; }
-keep class vn.xdeuhug.camerax.data.remote.** { *; }
-keep class vn.xdeuhug.camerax.data.local.** { *; }

# Keep classes using Hilt annotations (if using Dagger Hilt)
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Glide configures to not be deleted when mixing code
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# If you use standard Java reflection
-keepnames class * implements java.lang.reflect.Type

# Retain all annotations to ensure compatibility
-keepattributes *Annotation*


# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

# Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# AOP
-adaptclassstrings
-keepattributes InnerClasses, EnclosingMethod, Signature, *Annotation*

-keepnames @org.aspectj.lang.annotation.Aspect class * {
    public <methods>;
}