package com.example.infosys.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.infosys.R;
import com.example.infosys.activities.MainActivity;
import com.example.infosys.fragments.communities.CommunityFragment;
import com.example.infosys.model.Community;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {
    private static final String TAG = "CommunityAdapter";
    private final List<Community> communities;
    private final FragmentManager fragmentManager;
    private boolean useIntentNavigation = false; // New flag to control navigation method

    // Existing constructor remains the same
    public CommunityAdapter(List<Community> communities, FragmentManager fragmentManager) {
        this.communities = communities;
        this.fragmentManager = fragmentManager;
    }

    // New method to enable intent-based navigation
    public void enableIntentNavigation(boolean enable) {
        this.useIntentNavigation = enable;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        Community community = communities.get(position);
        holder.communityName.setText(community.getName());
        holder.communityDescription.setText(community.getDescription());
        String memberCount = community.getMemberCount() + " members";
        holder.communityMembers.setText(memberCount);

        Glide.with(holder.itemView.getContext())
                .load(community.getImageUrl())
                .error(R.drawable.logo)
                .circleCrop()
                .into(holder.communityImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
            intent.putExtra("newCommunity", true);
            intent.putExtra("communityId", community.getId());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    public static class CommunityViewHolder extends RecyclerView.ViewHolder {
        TextView communityName, communityDescription, communityMembers;
        ImageView communityImage;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            communityName = itemView.findViewById(R.id.community_name);
            communityDescription = itemView.findViewById(R.id.community_description);
            communityMembers = itemView.findViewById(R.id.community_members);
            communityImage = itemView.findViewById(R.id.community_image);
        }
    }
}