package com.amg.compressaudio;

import com.amg.compressaudio.FFmpegReflectionManager;
import java.io.File;
import java.io.IOException;

/* loaded from: classes.dex */
class FFmpegExecutor {
    FFmpegExecutor() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void executeCommandWithBuffer(String command, File input) throws IOException {
        File createBuffer = FileManager.createBuffer(input);
        if (FFmpegReflectionManager.executeFFmpeg(String.format("%s %s", command, createBuffer.getPath())) == 0) {
            FileManager.overwriteFromBuffer(input, createBuffer);
            return;
        }
        throw new IOException(FFmpegReflectionManager.Config.getLastCommandOutput());
    }

    static void executeCommand(String command) throws IOException {
        if (FFmpegReflectionManager.executeFFmpeg(command) != 0) {
            throw new IOException(FFmpegReflectionManager.Config.getLastCommandOutput());
        }
    }
}
