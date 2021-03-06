package org.metabrainz.mobile.presentation.features.release_group;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.metabrainz.mobile.R;
import org.metabrainz.mobile.data.sources.api.entities.CoverArt;
import org.metabrainz.mobile.data.sources.api.entities.mbentity.Release;
import org.metabrainz.mobile.presentation.IntentFactory;
import org.metabrainz.mobile.presentation.features.release.ReleaseActivity;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;

class ReleaseGroupReleaseAdapter extends RecyclerView.Adapter {

    private final List<Release> releaseList;
    private final ReleaseGroupViewModel releaseGroupViewModel;
    private final CompositeDisposable compositeDisposable;

    public ReleaseGroupReleaseAdapter(Context context, List<Release> releaseList) {
        this.releaseList = releaseList;
        // Load the ViewModel to fetch cover art for each release item
        releaseGroupViewModel = ViewModelProviders.of((FragmentActivity) context).get(ReleaseGroupViewModel.class);
        compositeDisposable = new CompositeDisposable();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_release_group_item, parent, false);
        return new ReleaseItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReleaseItemViewHolder viewHolder = (ReleaseItemViewHolder) holder;
        if (viewHolder.disposable != null && !viewHolder.disposable.isDisposed())
            compositeDisposable.remove(viewHolder.disposable);

        viewHolder.bind(releaseList.get(position));
    }

    @Override
    public int getItemCount() {
        return releaseList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void setViewVisibility(String text, TextView view) {
        if (text != null && !text.isEmpty() && !text.equalsIgnoreCase("null")) {
            view.setVisibility(View.VISIBLE);
            view.setText(text);
        } else view.setVisibility(GONE);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        compositeDisposable.clear();
    }

    private class ReleaseItemViewHolder extends RecyclerView.ViewHolder {
        final TextView releaseName;
        final TextView releaseLabel;
        final TextView releaseCountry;
        final TextView releaseBarcode;
        final TextView releaseTracks;
        final TextView releaseDate;
        final ImageView coverArtView;
        Disposable disposable;

        ReleaseItemViewHolder(@NonNull View itemView) {
            super(itemView);
            releaseName = itemView.findViewById(R.id.release_name);
            releaseLabel = itemView.findViewById(R.id.release_label);
            releaseBarcode = itemView.findViewById(R.id.release_barcode);
            releaseCountry = itemView.findViewById(R.id.release_country);
            releaseDate = itemView.findViewById(R.id.release_date);
            releaseTracks = itemView.findViewById(R.id.release_tracks);
            coverArtView = itemView.findViewById(R.id.release_cover_art);
        }

        void bind(Release release) {
            releaseName.setText(release.getTitle());
            setViewVisibility(release.labelCatalog(), releaseLabel);
            setViewVisibility(release.getBarcode(), releaseBarcode);
            setViewVisibility(release.getDate(), releaseDate);
            setViewVisibility(release.getCountry(), releaseCountry);
            setViewVisibility(String.valueOf(release.getTrackCount()), releaseTracks);

            coverArtView.setImageDrawable(coverArtView.getContext()
                    .getResources()
                    .getDrawable(R.drawable.link_discog));

            if (release.getCoverArt() != null)
                setCoverArtView(release);
            else
                fetchCoverArtForRelease(release);

            this.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ReleaseActivity.class);
                intent.putExtra(IntentFactory.Extra.RELEASE_MBID, release.getMbid());
                v.getContext().startActivity(intent);
            });
        }

        private void setCoverArtView(Release release) {
            if (release != null && release.getCoverArt() != null && releaseList.contains(release)) {
                // TODO: Search for the first “FRONT” image to use it as cover
                String url = release.getCoverArt()
                        .getImages()
                        .get(0)
                        .getThumbnails()
                        .getSmall();

                if (url != null && !url.isEmpty()) {
                    Picasso.get()
                            .load(Uri.parse(url))
                            .placeholder(R.drawable.link_discog)
                            .into(coverArtView);
                }
            }
        }

        private void addCoverArt(CoverArt coverArt) {
            if (coverArt != null && coverArt.getImages() != null
                    && !coverArt.getImages().isEmpty()) {
                String coverArtRelease = coverArt.getRelease();
                for (Release release : releaseList) {
                    if (coverArtRelease.endsWith(release.getMbid())) {
                        release.setCoverArt(coverArt);
                        setCoverArtView(release);
                        break;
                    }
                }
            }
        }

        private void fetchCoverArtForRelease(Release release) {
            // Ask the viewModel to retrieve the cover art
            // and append it to this release
            disposable = releaseGroupViewModel
                    .fetchCoverArtForRelease(release)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::addCoverArt, Throwable::printStackTrace);
            compositeDisposable.add(disposable);
        }
    }
}
