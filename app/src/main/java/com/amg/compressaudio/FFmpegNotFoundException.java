package com.amg.compressaudio;

/* loaded from: classes.dex */
class FFmpegNotFoundException extends Exception {
    /* JADX INFO: Access modifiers changed from: package-private */
    public FFmpegNotFoundException() {
        super("FFMPEG library not found! Please add implementation 'com.arthenica:mobile-ffmpeg-full:4.3.1.LTS' to your gradle file!");
    }
}
