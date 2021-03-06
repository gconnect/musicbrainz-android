package org.metabrainz.mobile.presentation.features.recording;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.metabrainz.mobile.R;
import org.metabrainz.mobile.data.sources.api.entities.mbentity.Recording;

import java.util.Objects;

public class RecordingInfoFragment extends Fragment {

    private RecordingViewModel recordingViewModel;
    private TextView recordingTitle, recordingArtist, recordingDuration;

    public static RecordingInfoFragment newInstance() {
        return new RecordingInfoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_recording_info, container, false);
        recordingViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RecordingViewModel.class);
        recordingViewModel.initializeRecordingData().observe(getViewLifecycleOwner(), this::setRecordingInfo);
        findViews(layout);
        return layout;
    }

    private void findViews(View layout) {
        recordingArtist = layout.findViewById(R.id.recording_artist);
        recordingDuration = layout.findViewById(R.id.recording_duration);
        recordingTitle = layout.findViewById(R.id.recording_title);
    }

    private void setRecordingInfo(Recording recording) {
        String duration, artist;
        recordingTitle.setText(recording.getTitle());
        duration = recording.getDuration();
        artist = recording.getDisplayArtist();
        if (duration != null) recordingDuration.setText(duration);
        if (artist != null) recordingArtist.setText(artist);
    }
}
