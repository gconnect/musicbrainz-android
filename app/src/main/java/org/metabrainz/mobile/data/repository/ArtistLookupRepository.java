package org.metabrainz.mobile.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.metabrainz.mobile.data.sources.Constants;
import org.metabrainz.mobile.data.sources.api.LookupService;
import org.metabrainz.mobile.data.sources.api.MusicBrainzServiceGenerator;
import org.metabrainz.mobile.data.sources.api.entities.ArtistWikiSummary;
import org.metabrainz.mobile.data.sources.api.entities.CoverArt;
import org.metabrainz.mobile.data.sources.api.entities.WikiDataResponse;
import org.metabrainz.mobile.data.sources.api.entities.mbentity.Artist;
import org.metabrainz.mobile.data.sources.api.entities.mbentity.Release;
import org.metabrainz.mobile.util.SingleLiveEvent;

import java.util.Objects;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistLookupRepository {
    public static final int METHOD_WIKIPEDIA_URL = 0;
    public static final int METHOD_WIKIDATA_ID = 1;
    private final static LookupService service = MusicBrainzServiceGenerator
            .createService(LookupService.class, true);
    private static ArtistLookupRepository repository;
    private static MutableLiveData<Artist> artistData;
    private static SingleLiveEvent<ArtistWikiSummary> artistWikiSummary;

    private ArtistLookupRepository() {

        artistData = new MutableLiveData<>();
        artistWikiSummary = new SingleLiveEvent<>();
    }

    public static ArtistLookupRepository getRepository() {
        if (repository == null) repository = new ArtistLookupRepository();
        return repository;
    }

    public static void destroyRepository() {
        repository = null;
    }

    public MutableLiveData<Artist> initializeArtistData() {
        return artistData;
    }

    public SingleLiveEvent<ArtistWikiSummary> initializeWikiData() {
        return artistWikiSummary;
    }

    public void getArtist(String MBID, boolean isLoggedIn) {
        if (isLoggedIn) fetchArtistWithUserData(MBID);
        else fetchArtist(MBID);
    }

    public void getArtistWikiSummary(String string, int method) {
        if (method == METHOD_WIKIPEDIA_URL)
            fetchArtistWiki(string);
        else
            fetchArtistWikiData(string);
    }

    private void fetchArtistWithUserData(String MBID) {
        service.lookupArtist(MBID, Constants.LOOKUP_ARTIST_PARAMS + Constants.USER_DATA_PARAMS)
                .enqueue(new Callback<Artist>() {
                    @Override
                    public void onResponse(Call<Artist> call, Response<Artist> response) {
                        Artist artist = response.body();
                        artistData.setValue(artist);
                    }

                    @Override
                    public void onFailure(Call<Artist> call, Throwable t) {

                    }
                });
    }

    private void fetchArtist(String MBID) {
        service.lookupArtist(MBID, Constants.LOOKUP_ARTIST_PARAMS).enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(@NonNull Call<Artist> call, @NonNull Response<Artist> response) {
                Artist artist = response.body();
                artistData.setValue(artist);
            }

            @Override
            public void onFailure(@NonNull Call<Artist> call, @NonNull Throwable t) {

            }
        });
    }

    private void fetchArtistWiki(String title) {
        service.getWikipediaSummary(title).enqueue(new Callback<ArtistWikiSummary>() {
            @Override
            public void onResponse(@NonNull Call<ArtistWikiSummary> call, @NonNull Response<ArtistWikiSummary> response) {
                ArtistWikiSummary wiki = response.body();
                artistWikiSummary.setValue(wiki);
            }

            @Override
            public void onFailure(@NonNull Call<ArtistWikiSummary> call, @NonNull Throwable t) {

            }
        });
    }

    private void fetchArtistWikiData(String id) {
        service.getWikipediaLink(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                String title = "";
                try {
                    String jsonResponse = Objects.requireNonNull(response.body()).string();
                    JsonElement element = new JsonParser().parse(jsonResponse);
                    JsonObject result = element.getAsJsonObject()
                            .getAsJsonObject("entities").getAsJsonObject(id);
                    WikiDataResponse wikiDataResponse = new Gson().fromJson(result, WikiDataResponse.class);
                    title = Objects.requireNonNull(wikiDataResponse.getSitelinks().get("enwiki")).getTitle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fetchArtistWiki(title);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
            }
        });
    }

    /**
     * For a given release ID, fetches the cover arts and updates the release w¡th that info
     *
     * @param release Release for which the cover art is to be retrieved
     */
    public Single<CoverArt> fetchCoverArtForRelease(Release release) {
        return service.getCoverArt(release.getMbid());
    }
}
