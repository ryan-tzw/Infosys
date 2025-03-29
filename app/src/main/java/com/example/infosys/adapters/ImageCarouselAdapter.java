package com.example.infosys.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.infosys.R;

import java.util.List;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder> {
    private final List<Uri> imageUris;
    private final OnImageRemoveListener removeListener;

    public ImageCarouselAdapter(List<Uri> imageUris, OnImageRemoveListener removeListener) {
        this.imageUris = imageUris;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = imageUris.get(position);

        Glide.with(holder.imageView.getContext())
                .load(uri)
                .into(holder.imageView);

//        holder.deleteButton.setOnClickListener(v -> {
//            removeListener.onRemove(uri);
//        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public interface OnImageRemoveListener {
        void onRemove(Uri uri);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
//        ImageButton deleteButton;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
//            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}

