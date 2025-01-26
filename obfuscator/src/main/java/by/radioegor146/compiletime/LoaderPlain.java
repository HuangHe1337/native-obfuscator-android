package by.radioegor146.compiletime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LoaderPlain {

    private static final String LIB_NAME = "%LIB_NAME%";

    public static native void registerNativesForClass(int index, Class<?> clazz);

    static {
        loadNativeLibrary();
    }


    private static void loadNativeLibrary() {
        String libFileName = System.mapLibraryName(LIB_NAME);
        File libFile = getExtractedLibFile(libFileName);

        if (libFile != null) {
            System.load(libFile.getAbsolutePath());
        } else {
            System.err.println("Failed to extract or load native library: " + LIB_NAME);
            // 可根据需要抛出异常或进行其他处理
        }
    }

    private static File getExtractedLibFile(String libFileName) {
        File filesDir = getFilesDir(); // 获取私有 filesDir

        if (filesDir == null) {
            return null; // 文件目录获取失败
        }

        File libFile = new File(filesDir, libFileName);
        if (libFile.exists()) {
            return libFile; // 如果文件已存在，直接返回
        }
        try {
            if (extractLibFromApk(libFileName, libFile)) {
                return libFile;
            }
        }catch(IOException e){
            System.err.println("Failed to extract library from apk" + e.getMessage());
            return null;
        }
        return null;
    }

    // 从 apk 中读取文件到 filesdir
    private static boolean extractLibFromApk(String libFileName, File targetFile) throws IOException{
            try (InputStream is = LoaderPlain.class.getClassLoader().getResourceAsStream("lib/" + getAbi() + "/" + libFileName);
                 FileOutputStream fos = new FileOutputStream(targetFile)) {

                if (is == null) {
                    System.err.println("Resource not found:" + "lib/" + getAbi() + "/" + libFileName);
                    return false;
                }
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                return true;
            }
    }

    private static String getAbi() {
        String osArch = System.getProperty("os.arch");
        if (osArch == null) {
           return  "armeabi-v7a";
        }
        // 适配其他常见架构，这里不列出所有情况
        if (osArch.contains("arm64")) {
            return "arm64-v8a";
        } else if (osArch.contains("arm")) {
            return "armeabi-v7a";
        } else if (osArch.contains("x86_64")) {
            return "x86_64";
        }else if (osArch.contains("x86")){
            return "x86";
        }
        return "armeabi-v7a";
    }

    // 反射获取应用的私有文件目录
    private static File getFilesDir() {
        try {
            // 查找 Application 类
            Class<?> appClass = Class.forName("android.app.Application");
            // 查找 currentApplication 方法
            Method method = appClass.getMethod("currentApplication");
             // 调用 currentApplication 方法获取 Application 实例
            Object currentApplication = method.invoke(null);
            // 查找 getFilesDir 方法
            Method getFilesDirMethod = currentApplication.getClass().getMethod("getFilesDir");
            // 调用 getFilesDir 方法获取 File 实例
            return (File) getFilesDirMethod.invoke(currentApplication);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("Failed to get files directory: " + e.getMessage());
             return null;
        }
    }
}
