# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontoptimize
# 忽略警告
-ignorewarnings
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-allowaccessmodification
-repackageclasses ''
# 保护注解
-keepattributes *Annotations*

# 保持类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.**{*;}
-dontwarn android.support.**

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.View.MenuIten);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class com.ksc.client.ads.bean.* {
    public <fields>;
    public <methods>;
}
-keep interface com.ksc.client.ads.callback.* {
    public <methods>;
}
-keep class com.ksc.client.ads.config.*{
    public <fields>;
    public <methods>;
}
-keep class com.ksc.client.ads.proto.**{*;}
-keep class com.ksc.client.ads.view.KSCCloseVideoPromptView {
    public protected <fields>;
    public protected <methods>;
}
-keep class com.ksc.client.ads.view.KSCCountDownView {
    public protected <fields>;
    public protected <methods>;
}
-keep class com.ksc.client.ads.view.KSCCountDownView$* {*;}
-keep class com.ksc.client.ads.view.KSCLandingPageView {
    public protected <fields>;
    public protected <methods>;
}
-keep class com.ksc.client.ads.view.KSCLandingPageView$* {*;}
-keep class com.ksc.client.ads.view.KSCMobileAdActivity {
    public protected <fields>;
    public protected <methods>;
}
-keep class com.ksc.client.ads.view.KSCMobileAdActivity$* {*;}
-keep class com.ksc.client.ads.view.KSCNetPromptView {
    public protected <fields>;
    public protected <methods>;
}
-keep class com.ksc.client.ads.view.KSCNetPromptView$* {*;}
-keep class com.ksc.client.ads.view.KSCVideoView {
    public protected <fields>;
    public protected <methods>;
}
-keep class com.ksc.client.ads.view.KSCVideoView$* {*;}
-keep class com.ksc.client.ads.DownloadService{
    public protected <fields>;
    public protected <methods>;
}
-keep class com.ksc.client.ads.DownloadService$*{*;}
-keep class com.ksc.client.ads.KSCADAgent{
    public protected <fields>;
    public protected <methods>;
}
-keep class com.ksc.client.ads.KSCADAgent$*{*;}
-keep class com.ksc.client.ads.KSCBlackBoard{*;}
-keep class com.ksc.client.ads.KSCMediaState{*;}
-dontwarn com.ksc.client.ads.**

-keep class com.ksc.client.util.**{*;}
-keep class com.ksc.client.toolbox.**{*;}