package com.amg.compressaudio;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/* loaded from: classes.dex */
class FileManager {
    FileManager() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void validateInputFile(File inputFile) throws IOException {
        if (!inputFile.exists() || inputFile.isDirectory()) {
            throw new FileNotFoundException();
        }
    }

    static void validateInputFile(String path) throws IOException {
        validateInputFile(new File(path));
    }

    static void validateOutputFile(File inputFile) throws IOException {
        if (!inputFile.getParentFile().exists() || inputFile.isDirectory()) {
            throw new FileNotFoundException("Invalid output path or you use directory path instead of file path!");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void validateOutputFile(String path) throws IOException {
        validateOutputFile(new File(path));
    }

    static void writeFile(String outputPath, String data) {
        try {
            FileWriter fileWriter = new FileWriter(outputPath);
            fileWriter.write(data);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] bArr = new byte[1024];
        while (true) {
            int read = in.read(bArr);
            if (read == -1) {
                return;
            }
            out.write(bArr, 0, read);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static File copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel channel = new FileInputStream(sourceFile).getChannel();
        FileChannel channel2 = new FileOutputStream(destFile).getChannel();
        channel2.transferFrom(channel, 0L, channel.size());
        channel.close();
        channel2.close();
        return destFile;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static File copyFile(File sourceFile, String destFile) throws IOException {
        return copyFile(sourceFile, new File(destFile));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static File createBuffer(File sourceFile) throws IOException {
        String parent = sourceFile.getParent();
        return copyFile(sourceFile, addFileTitle(parent, Constant.AUDIO_TOOL_TMP + Constant.AUDIO_TOOL_BUFFER + getFileExtension(sourceFile)));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void overwriteFromBuffer(File sourceFile, File bufferFile) throws IOException {
        copyFile(bufferFile, sourceFile);
        bufferFile.delete();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(Constant.DOT);
        if (lastIndexOf == -1) {
            return Constant.EMPTY_STRING;
        }
        return name.substring(lastIndexOf);
    }

    static String[] getPathFromFiles(File[] files) {
        String[] strArr = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            strArr[i] = files[i].getPath();
        }
        return strArr;
    }

    static String addFileTitle(File file, String title) {
        if (file.getPath().charAt(file.getPath().length() - 1) == File.pathSeparatorChar) {
            return file.getPath() + title;
        }
        return file.getPath() + File.separator + title;
    }

    static String addFileTitle(String path, String title) {
        return addFileTitle(new File(path), title);
    }

    static File bufferAssetAudio(Context context, String audioDirectory, String assetAudio) {
        try {
            InputStream open = context.getAssets().open(assetAudio);
            File file = new File(audioDirectory, Constant.AUDIO_TOOL_TMP + assetAudio);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.flush();
            copyFile(open, fileOutputStream);
            open.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return file;
        } catch (IOException unused) {
            return null;
        }
    }
}
