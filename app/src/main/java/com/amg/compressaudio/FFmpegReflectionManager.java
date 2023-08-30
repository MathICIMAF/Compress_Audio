package com.amg.compressaudio;

import java.lang.reflect.InvocationTargetException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class FFmpegReflectionManager {
    static Class<?> config;
    static Class<?> ffmpeg;
    static Class<?> ffprobe;
    private static Class<?> level;

    FFmpegReflectionManager() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int executeFFmpeg(final String command) {
        return ((Integer) runMethod(ffmpeg, "execute", new Class[]{String.class}, new String[]{command})).intValue();
    }

    static int executeFFprobe(final String command) {
        return ((Integer) runMethod(ffprobe, "execute", new Class[]{String.class}, new String[]{command})).intValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Class<?> provideClassByName(final String clazz) throws FFmpegNotFoundException {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException unused) {
            throw new FFmpegNotFoundException();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void initLogLevel() throws FFmpegNotFoundException {
        level = provideClassByName(String.format("%s.Level", config.getName().substring(0, config.getName().lastIndexOf(46))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static synchronized Object runMethod(Class clazz, String method, Class<?>[] types, Object[] args) {
        synchronized (FFmpegReflectionManager.class) {
            try {
                if (types != null) {
                    return clazz.getMethod(method, types).invoke(null, args);
                }
                return clazz.getMethod(method, new Class[0]).invoke(null, new Object[0]);
            } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /* loaded from: classes.dex */
    static class Config {
        Config() {
        }

        static void enableRedirection() {
            FFmpegReflectionManager.runMethod(FFmpegReflectionManager.config, "enableRedirection", null, null);
        }

        static void setLogLevel(int code) {
            FFmpegReflectionManager.runMethod(FFmpegReflectionManager.config, "setLogLevel", new Class[]{FFmpegReflectionManager.level}, new Object[]{provideLevelFrom(code)});
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static String getLastCommandOutput() {
            return (String) FFmpegReflectionManager.runMethod(FFmpegReflectionManager.config, "getLastCommandOutput", null, null);
        }

        static Object provideLevelFrom(int code) {
            return FFmpegReflectionManager.runMethod(FFmpegReflectionManager.level, "from", new Class[]{Integer.TYPE}, new Integer[]{Integer.valueOf(code)});
        }
    }
}
