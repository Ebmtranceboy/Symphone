package joel.duet.symphone.controller;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.csounds.CsoundObj;

import java.util.LinkedList;
import java.util.List;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.SimpleImageArrayAdapter;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Pattern;
import joel.duet.symphone.model.Score;
import joel.duet.symphone.model.Track;
import joel.duet.symphone.modelview.ScoreView;

/**
 *
 * Created by joel on 04/04/16 at 10:54 at 11:18.
 */
public class ScoreController {
    private static final LinkedList<Integer> bars = new LinkedList<>();
    private static Spinner edition_spinner;
    //private static final String TAG = "ScoreFragment";

    private static ScoreView scoreview;
    private static ArrayAdapter<Integer> bars_adapter;
    public static MainActivity activity;
    private static CsoundObj csoundObj;

    public static void reinit(final MainActivity.User user) {
        activity = user.activity;
        csoundObj = MainActivity.csoundObj;
        scoreview = user.binding.score.scoreView;

        user.binding.score.modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.scoreEditMode.set(!user.scoreEditMode.get());
                ScoreView.tool = ScoreView.Tool.NONE;
                edition_spinner.setSelection(0);
            }
        });

        edition_spinner = user.binding.score.edition;
        SimpleImageArrayAdapter edition_adapter = new SimpleImageArrayAdapter(user.activity, Default.edition_icons);
        edition_spinner.setAdapter(edition_adapter);

        edition_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ScoreView.tool = ScoreView.tools[i];
                scoreview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        user.binding.score.newTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int p_id = Track.getIdPatternSelected();
                final int t_id = Score.getIdTrackSelected();
                Score.createTrack();
                ScoreView.tracks_displayed =
                        Math.max(Score.getNbOfTracks(), Default.min_tracks_displayed);
                ScoreView.track_height =
                        (1.0f - (Default.top_margin + Default.bottom_margin))
                                / (float) ScoreView.tracks_displayed;
                Score.setTrackSelected(t_id);
                Track.setPatternSelected(p_id);
                scoreview.invalidate();
            }
        });

        user.binding.score.extendScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScoreView.number_patches++;
                bars.clear();
                for (int i = 1; i <= ScoreView.number_patches * 16; i++) bars.add(i);
                bars_adapter.notifyDataSetChanged();
                scoreview.invalidate();
            }
        });

        user.binding.score.trimScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<Track> uselessTracks = new LinkedList<>();
                int t, p;
                for (t = 1; t <= Score.getNbOfTracks(); t++) {
                    Score.setTrackSelected(t);
                    final Track track = Score.getTrackSelected();
                    final List<Pattern> uselessPatterns = new LinkedList<>();
                    for (p = 1; p <= track.getNbOfPatterns(); p++) {
                        Track.setPatternSelected(p);
                        final Pattern pattern = Track.getPatternSelected();
                        if (pattern.isEmpty()) uselessPatterns.add(pattern);
                    }
                    track.deletePatterns(uselessPatterns);
                    if (track.isEmpty()) uselessTracks.add(track);
                }
                Score.deleteTracks(uselessTracks);
                Score.setTrackSelected(Score.getNbOfTracks());
                Track.setPatternSelected(1);
                if (Score.getNbOfTracks() == 0) Track.setPatternSelected(0);

                ScoreView.number_patches = 1 + (int) (Score.getSeconds() / (Score.getResolution() / 2));
                bars.clear();
                for (int i = 1; i <= ScoreView.number_patches * 16; i++) bars.add(i);
                bars_adapter.notifyDataSetChanged();

                ScoreView.Focus.reset();
                ScoreView.tracks_displayed =
                        Math.max(Score.getNbOfTracks(), Default.min_tracks_displayed);
                ScoreView.track_height =
                        (1.0f - (Default.top_margin + Default.bottom_margin))
                                / (float) ScoreView.tracks_displayed;
                scoreview.invalidate();
            }
        });

        final Spinner resolution_spinner = user.binding.score.resolution;
        SimpleImageArrayAdapter adapter =
                new SimpleImageArrayAdapter(user.activity, Default.resolution_icons);
        resolution_spinner.setAdapter(adapter);
        resolution_spinner.setSelection(Score.resolution_index);

        final Spinner bars_spinner = user.binding.score.barsSpinner;

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(Score.resolution_index <i)
                    Score.bar_start *= Score.getResolution() / Default.resolutions[i];
                else Score.bar_start /= Default.resolutions[i] / Score.getResolution();

                while(ScoreView.number_patches * 16 <  Score.bar_start) ScoreView.number_patches++;
                bars.clear();
                for (int p = 1; p <= ScoreView.number_patches * 16; p++) bars.add(p);
                bars_adapter.notifyDataSetChanged();

                bars_spinner.setSelection(Score.bar_start);
                Score.resolution_index = i;
                scoreview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ScoreView.number_patches = 1 + (int) (Score.getSeconds() / (Score.getResolution() / 2));

        for (int i = 1; i <= ScoreView.number_patches * 16; i++) bars.add(i);
        bars_adapter = new ArrayAdapter<>(user.activity,
                android.R.layout.simple_list_item_1,
                bars);
        bars_spinner.setAdapter(bars_adapter);
        if(Score.bar_start<ScoreView.number_patches*16) bars_spinner.setSelection(Score.bar_start);
        else bars_spinner.setSelection(1);

        bars_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Score.bar_start = i;
                scoreview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        user.binding.score.play.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View view) {
                                                           String csd = Score.sendPatterns(Score.allPatterns(), false,
                                                                   bars_spinner.getSelectedItemPosition() * Score.getResolution());
                                                           //Log.i(TAG, csd);
                                                           csoundObj.stop();
                                                           csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
                                                       }
                                                   }
        );

        user.binding.score.stop.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                csoundObj.stop();
                                                            }
                                                        }
        );
    }
}
