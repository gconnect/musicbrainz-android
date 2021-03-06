package org.metabrainz.mobile.presentation.features.collection;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.metabrainz.mobile.R;
import org.metabrainz.mobile.data.sources.api.entities.mbentity.Release;
import org.metabrainz.mobile.presentation.IntentFactory;
import org.metabrainz.mobile.presentation.features.release.ReleaseActivity;

import java.util.List;

public class CollectionAdapterRelease extends CollectionAdapter {
    private final List<Release> data;

    public CollectionAdapterRelease(List<Release> data) {
        this.data = data;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public CollectionAdapterRelease.ReleaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_release, parent, false);
        return new CollectionAdapterRelease.ReleaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CollectionAdapterRelease.ReleaseViewHolder viewHolder = (CollectionAdapterRelease.ReleaseViewHolder) holder;
        Release release = data.get(position);
        viewHolder.releaseName.setText(release.getTitle());

        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReleaseActivity.class);
            intent.putExtra(IntentFactory.Extra.RELEASE_MBID, release.getMbid());
            v.getContext().startActivity(intent);
        });

        setViewVisibility(release.labelCatalog(), viewHolder.releaseLabel);
        setViewVisibility(release.getDisplayArtist(), viewHolder.releaseArtist);
        setViewVisibility(release.getDisambiguation(), viewHolder.releaseDisambiguation);
        setAnimation(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static class ReleaseViewHolder extends EntityViewHolder {
        final TextView releaseName;
        final TextView releaseArtist;
        final TextView releaseDisambiguation;
        final TextView releaseLabel;

        ReleaseViewHolder(@NonNull View itemView) {
            super(itemView);
            releaseName = itemView.findViewById(R.id.release_name);
            releaseLabel = itemView.findViewById(R.id.release_label);
            releaseDisambiguation = itemView.findViewById(R.id.release_disambiguation);
            releaseArtist = itemView.findViewById(R.id.release_artist);
        }

    }

}
