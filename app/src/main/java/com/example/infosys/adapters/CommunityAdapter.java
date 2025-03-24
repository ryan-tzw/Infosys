package com.example.infosys.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.fragments.CommunityFragment;
import com.example.infosys.model.Community;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {
    private static final String TAG = "CommunityAdapter";
    private final List<Community> communities;
    private final FragmentManager fragmentManager;

    public CommunityAdapter(List<Community> communities, FragmentManager fragmentManager) {
        this.communities = communities;
        this.fragmentManager = fragmentManager;
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
//        holder.communityImage.setImageResource(community.getImage());

        holder.itemView.setOnClickListener(v -> {
            CommunityFragment fragment = CommunityFragment.newInstance(community.getId());
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .addToBackStack(null)
                    .commit();
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
