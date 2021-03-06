package org.metabrainz.mobile.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import org.metabrainz.mobile.data.sources.Constants;
import org.metabrainz.mobile.data.sources.api.LookupService;
import org.metabrainz.mobile.data.sources.api.MusicBrainzServiceGenerator;
import org.metabrainz.mobile.data.sources.api.entities.CoverArt;
import org.metabrainz.mobile.data.sources.api.entities.mbentity.Release;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReleaseLookupRepository {
    private final static LookupService service = MusicBrainzServiceGenerator
            .createService(LookupService.class, true);
    private static ReleaseLookupRepository repository;
    private final MutableLiveData<Release> releaseLiveData;
    private final MutableLiveData<CoverArt> coverArtData;

    private ReleaseLookupRepository() {
        releaseLiveData = new MutableLiveData<>();
        coverArtData = new MutableLiveData<>();
    }

    public static ReleaseLookupRepository getRepository() {
        if (repository == null) repository = new ReleaseLookupRepository();
        return repository;
    }

    public static void destroyRepository() {
        repository = null;
    }

    public MutableLiveData<Release> initializeReleaseLiveData() {
        return releaseLiveData;
    }

    public void getRelease(String MBID) {
        service.lookupRelease(MBID, Constants.LOOKUP_RELEASE_PARAMS)
                .enqueue(new Callback<Release>() {
                    @Override
                    public void onResponse(@NonNull Call<Release> call, @NonNull Response<Release> response) {
                        Release release = response.body();
                        releaseLiveData.setValue(release);
                    }

                    @Override
                    public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {

                    }
                });
    }

    public MutableLiveData<CoverArt> initializeCoverArtData() {
        return coverArtData;
    }

    public void getCoverArt(String MBID) {
        service.getCoverArtAll(MBID).enqueue(new Callback<CoverArt>() {
            @Override
            public void onResponse(@NonNull Call<CoverArt> call, @NonNull Response<CoverArt> response) {
                CoverArt coverArt = response.body();
                coverArtData.setValue(coverArt);
            }

            @Override
            public void onFailure(@NonNull Call<CoverArt> call, @NonNull Throwable t) {

            }
        });
    }
}
