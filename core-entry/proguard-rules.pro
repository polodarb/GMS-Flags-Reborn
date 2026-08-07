# ---------------------------------------------------------------------------
# Xposed / LSPosed module entry
# ---------------------------------------------------------------------------
# LSPosed loads the module entry class by its fully-qualified name listed in
# assets/xposed_init. R8 must not rename or remove it, otherwise the framework
# fails with ClassNotFoundException for ua.polodarb.xposed.entry.GmsFlagsXposedEntry.
#
# The whole hook code is invoked reflectively / by-name by the framework: the
# entry class, and — crucially — every XC_MethodHook subclass whose
# beforeHookedMethod / afterHookedMethod the framework calls by name. The Xposed
# API is compileOnly, so R8 does not see these as overrides and would rename them,
# turning every hook into a silent no-op (entry runs, but no callback fires).
# Keep the entire module package to guarantee the callbacks keep their names.
-keep class ua.polodarb.xposed.** { *; }

# The Xposed API is compileOnly — provided by the LSPosed framework at runtime and
# NOT packaged in the APK. Tell R8 not to fail on these "missing" references.
-dontwarn de.robv.android.xposed.**

# Belt-and-suspenders: never rename the framework callback methods on any hook.
-keepclassmembers class * extends de.robv.android.xposed.XC_MethodHook {
    protected void beforeHookedMethod(de.robv.android.xposed.XC_MethodHook$MethodHookParam);
    protected void afterHookedMethod(de.robv.android.xposed.XC_MethodHook$MethodHookParam);
}

# ---------------------------------------------------------------------------
# DexKit — used by the runtime override strategies to locate obfuscated target
# methods. It relies on JNI + reflection, so it must not be shrunk/renamed.
# ---------------------------------------------------------------------------
-keep class org.luckypray.dexkit.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# ---------------------------------------------------------------------------
# protobuf-lite
# ---------------------------------------------------------------------------
# protobuf-lite resolves generated field names ("<name>_") reflectively at
# runtime. If R8 renames them, parsing/serialization throws NoSuchFieldException
# (e.g. "No field packageName_ / name_ ..."). Keep the generated fields.
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
