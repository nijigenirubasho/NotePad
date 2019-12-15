-optimizationpasses 99
# 清除日志代码
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
# 保留行号并且隐藏类名
-renamesourcefileattribute sf
-keepattributes sf,LineNumberTable

-repackageclasses ''
-allowaccessmodification
-verbose
