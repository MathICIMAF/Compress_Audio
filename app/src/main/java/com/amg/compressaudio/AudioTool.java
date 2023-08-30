package com.amg.compressaudio;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/* loaded from: classes.dex */
public class AudioTool {
    private File audio;
    private String audioDirectory;
    private Context context;

    private AudioTool(Context context) throws FFmpegNotFoundException {
        this(context, FFmpegReflectionManager.provideClassByName(Constant.FFMPEG_CLASS), FFmpegReflectionManager.provideClassByName(Constant.FFPROBE_CLASS), FFmpegReflectionManager.provideClassByName(Constant.CONFIG_CLASS));
    }

    private AudioTool(Context context, Class<?> ffmpeg, Class<?> ffprobe, Class<?> config) throws FFmpegNotFoundException {
        this.context = context;
        this.audioDirectory = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_MUSIC)[0].getPath();
        FFmpegReflectionManager.ffmpeg = ffmpeg;
        FFmpegReflectionManager.ffprobe = ffprobe;
        FFmpegReflectionManager.config = config;
        FFmpegReflectionManager.initLogLevel();
    }

    public static AudioTool getInstance(Context context) throws FFmpegNotFoundException {
        return new AudioTool(context);
    }

    public static AudioTool getInstance(Context context, Class<?> ffmpeg, Class<?> ffprobe, Class<?> config) throws FFmpegNotFoundException {
        return new AudioTool(context, ffmpeg, ffprobe, config);
    }

    public AudioTool withAudio(String sourceAudio) throws IOException {
        withAudio(new File(sourceAudio));
        return this;
    }

    public AudioTool withAudio(File sourceAudio) throws IOException {
        FileManager.validateInputFile(sourceAudio);
        File file = new File(this.audioDirectory + File.separator + Constant.AUDIO_TOOL_TMP + System.currentTimeMillis() + FileManager.getFileExtension(sourceAudio));
        this.audio = file;
        FileManager.copyFile(sourceAudio, file);
        return this;
    }

    public AudioTool saveCurrentTo(String fullPath) throws IOException {
        FileManager.validateOutputFile(fullPath);
        FileManager.copyFile(this.audio, fullPath);
        return this;
    }

    public void release() {
        File[] listFiles;
        for (File file : new File(this.audioDirectory).listFiles()) {
            if (file.getName().startsWith(Constant.AUDIO_TOOL_TMP)) {
                file.delete();
            }
        }
        clearReferences();
    }

    public AudioTool compressAudio(String format, String bitrate) throws IOException {
        FFmpegExecutor.executeCommandWithBuffer(String.format(Locale.US, "-y -i %s -ar 44100 -ac 2 -ab %s -f %s", this.audio.getPath(), bitrate, format), this.audio);
        return this;
    }

    /* renamed from: com.amg.compressaudio.AudioTool$1  reason: invalid class name */
    /* loaded from: classes.dex */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$amg$compressaudio$Duration;

        static {
            int[] iArr = new int[Duration.values().length];
            $SwitchMap$com$amg$compressaudio$Duration = iArr;
            try {
                iArr[Duration.MILLIS.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$amg$compressaudio$Duration[Duration.SECONDS.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$amg$compressaudio$Duration[Duration.MINUTES.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public long getDuration(Duration duration) {
        int i = AnonymousClass1.$SwitchMap$com$amg$compressaudio$Duration[duration.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    return 0L;
                }
                return (getDurationMillis() % 3600000) / 60000;
            }
            return getDurationMillis() / 1000;
        }
        return getDurationMillis();
    }

    private long getDurationMillis() {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this.audio.getAbsolutePath());
        return Long.parseLong(mediaMetadataRetriever.extractMetadata(9));
    }

    private void clearReferences() {
        this.audio = null;
        this.audioDirectory = null;
        this.context = null;
    }
}
