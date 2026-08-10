# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# files in subprojects' build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any custom rules here to keep specific classes/members from being obfuscated or shrunk

# Keep kotlinx.serialization data classes and serializers for AutoUpdate
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclassmembers class **$serializer {
    *** INSTANCE;
}
-keep class com.tkno.blueiris.util.UpdateUtil** { *; }
-keep class com.tkno.blueiris.ui.page.settings.about.** { *; }

