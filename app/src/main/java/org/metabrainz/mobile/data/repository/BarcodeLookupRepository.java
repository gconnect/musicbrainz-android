package org.metabrainz.mobile.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import org.metabrainz.mobile.data.sources.api.LookupService;
import org.metabrainz.mobile.data.sources.api.MusicBrainzServiceGenerator;
import org.metabrainz.mobile.data.sources.api.entities.mbentity.Release;
import org.metabrainz.mobile.data.sources.api.entities.response.BarcodeReleaseResponse;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BarcodeLookupRepository {

    private final static LookupService service = MusicBrainzServiceGenerator
            .createService(LookupService.class, false);
    private static BarcodeLookupRepository repository;
    private static MutableLiveData<List<Release>> releaseListData;

    private BarcodeLookupRepository() {
        releaseListData = new MutableLiveData<>();
    }

    public static BarcodeLookupRepository getRepository() {
        if (repository == null) repository = new BarcodeLookupRepository();
        return repository;
    }

    public static void destroyRepository() {
        repository = null;
    }

    public MutableLiveData<List<Release>> getReleaseListData() {
        return releaseListData;
    }

    public void lookupReleasesWithBarcode(String barcode) {
        service.lookupReleaseWithBarcode(barcode).enqueue(new Callback<BarcodeReleaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BarcodeReleaseResponse> call, @NonNull Response<BarcodeReleaseResponse> response) {
                BarcodeReleaseResponse releaseResponse = response.body();
                releaseListData.setValue(Objects.requireNonNull(releaseResponse).getReleases());
            }

            @Override
            public void onFailure(@NonNull Call<BarcodeReleaseResponse> call, @NonNull Throwable t) {
            }
        });
    }
}
