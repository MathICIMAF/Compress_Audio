package com.amg.compressaudio;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/* loaded from: classes.dex */
public class AudiosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<AudioFile> files;

    public AudiosAdapter(List<AudioFile> files, Context context) {
        this.files = files;
        this.context = context;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudioViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item, parent, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AudioViewHolder audioViewHolder = (AudioViewHolder) holder;
        final AudioFile audioFile = this.files.get(position);
        audioViewHolder.name.setText(audioFile.getName());
        audioViewHolder.lastModified.setText(audioFile.getLastModification());
        audioViewHolder.size.setText(audioFile.getSize());
        audioViewHolder.share.setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.AudiosAdapter.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Uri fromFile;
                Intent intent = new Intent("android.intent.action.SEND");
                if (Build.VERSION.SDK_INT >= 24) {
                    Context applicationContext = AudiosAdapter.this.context.getApplicationContext();
                    fromFile = FileProvider.getUriForFile(applicationContext, AudiosAdapter.this.context.getPackageName() + ".provider", audioFile.getFile());
                } else {
                    fromFile = Uri.fromFile(audioFile.getFile());
                }
                intent.setType("audio/");
                intent.putExtra("android.intent.extra.STREAM", fromFile);
                AudiosAdapter.this.context.startActivity(Intent.createChooser(intent, "Share audio"));
            }
        });
        audioViewHolder.layout.setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.AudiosAdapter.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
                    Uri uriForFile = FileProvider.getUriForFile(AudiosAdapter.this.context, "com.amg.compressaudio.provider", audioFile.getFile());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uriForFile, mimeTypeFromExtension);
                    AudiosAdapter.this.context.startActivity(intent);
                } catch (Exception e) {
                    Log.e(e.toString(), e.getMessage());
                }
            }
        });
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.files.size();
    }

    /* loaded from: classes.dex */
    public static class AudioViewHolder extends RecyclerView.ViewHolder {
        public TextView lastModified;
        public LinearLayout layout;
        public TextView name;
        public ImageButton share;
        public TextView size;

        public AudioViewHolder(View itemView) {
            super(itemView);
            this.layout = (LinearLayout) itemView.findViewById(R.id.audio_layout);
            this.name = (TextView) itemView.findViewById(R.id.title);
            this.size = (TextView) itemView.findViewById(R.id.size);
            this.share = (ImageButton) itemView.findViewById(R.id.share);
            this.lastModified = (TextView) itemView.findViewById(R.id.modification);
        }
    }
}
