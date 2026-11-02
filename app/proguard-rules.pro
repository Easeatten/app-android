
# --- JNA (Java Native Access) ---
# Preserve all JNA classes, methods, and fields required by native libjnidispatch.so
-keep class com.sun.jna.** { *; }
-keepclassmembers class com.sun.jna.** { *; }
-dontwarn com.sun.jna.**

# Preserve native methods across all classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# --- UniFFI Bindings & JNA Structure/Callback mappings ---
# Preserve Structure fields and Callback interfaces used by JNA
-keep class * extends com.sun.jna.Structure { *; }
-keepclassmembers class * extends com.sun.jna.Structure { *; }
-keep class * extends com.sun.jna.Callback { *; }
-keepclassmembers class * extends com.sun.jna.Callback { *; }

# Preserve UniFFI-generated bindings (e.g. sxcapi)
-keep class sxcapi.** { *; }
-keepclassmembers class sxcapi.** { *; }
-dontwarn sxcapi.**

# Keep generic signatures and reflection attributes
-keepattributes Signature,InnerClasses,EnclosingMethod

# Suppress optional desktop AWT references on Android
-dontwarn java.awt.**
