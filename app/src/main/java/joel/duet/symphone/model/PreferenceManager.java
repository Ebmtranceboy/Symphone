package joel.duet.symphone.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
//import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import joel.duet.symphone.modelview.ScoreView;

/**
 *
 * Created by joel on 04/02/16 at 23:19 at 23:21 at 20:23 at 23:57.
 */
public final class PreferenceManager {
    //private static final String TAG = "PreferenceManager";

    private static PreferenceManager self;
    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;
    private boolean isInitialised = false;
    private static final String ORCHESTRA_KEY = "Orchestra";
    private static final String FX_KEY = "FX";
    private static final String MATRIX_KEY = "Matrix";
    private static final String TRACKS_KEY = "Tracks";
    private static final String TEMPO_RATIO_KEY = "Tempo";
    private static final String PROJECT_KEY = "Project";
    private static final String MASTER_GAIN_L_KEY = "GainL";
    private static final String MASTER_GAIN_R_KEY = "GainR";
    private static final String GLOBALS_KEY = "Globals";

    private class TransportTask extends AsyncTask<Runnable, Void, Void> {
        protected Void doInBackground(Runnable... tasks) {

            for (Runnable task : tasks) {
                task.run();
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return null;
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void initialize(Context ctx) {
        final Context context = ctx;
        Runnable task;

        if (!isInitialised) {
            task = new Runnable() {
                @Override
                public void run() {
                    preferences = context.getSharedPreferences(PROJECT_KEY, Context.MODE_PRIVATE);
                    editor = preferences.edit();
                    loadPreferences();
                    isInitialised = true;
                }
            };
            TransportTask.execute(task);
        }
    }

    public static PreferenceManager getInstance() {
        if (self == null) {
            self = new PreferenceManager();
        }
        return self;
    }

    private PreferenceManager() {
    }

    public static JSONObject project() {
        JSONObject json = new JSONObject();

        try {
            json.put(ORCHESTRA_KEY, saveJSONOrchestra());
            json.put(FX_KEY, saveJSONFX());
            json.put(MATRIX_KEY, Matrix.serialize());
            json.put(TRACKS_KEY, saveJSONTracks());
            json.put(TEMPO_RATIO_KEY, CSD.tempo_ratio);
            json.put(MASTER_GAIN_L_KEY, CSD.master_gain_L);
            json.put(MASTER_GAIN_R_KEY, CSD.master_gain_R);
            json.put(GLOBALS_KEY, CSD.globals);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void savePreferences() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                JSONObject json = project();
                editor.putString(PROJECT_KEY, json.toString());
                editor.commit();
            }
        };
        TransportTask.execute(task);
    }

    public static void resetProject() {
        CSD.instruments.clear();
        CSD.effects.clear();
        Score.resetTracks();
        Matrix.getInstance().update();
        CSD.tempo_ratio = 1.0;
        CSD.master_gain_L = 1.0;
        CSD.master_gain_R = 1.0;
        CSD.globals = Default.material;
        CSD.projectName = Default.new_project_name;
    }

    private void loadPreferences() {

        resetProject();
        JSONObject project;

        String feed = preferences.getString(PROJECT_KEY, null);
        if (feed != null) {
            try {
                project = new JSONObject(feed);
                loadProject(project);
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    project = new JSONObject(Default.empty_project);
                    loadProject(project);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

        } else {
            try {
                project = new JSONObject(Default.empty_project);
                loadProject(project);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void loadProject(JSONObject project) throws JSONException {
        //Log.d(TAG, project.toString());
        loadJSONOrchestra(project.getJSONArray(ORCHESTRA_KEY));
        loadJSONFX(project.getJSONArray(FX_KEY));
        loadJSONTracks(project.getJSONObject(TRACKS_KEY));
        Matrix.getInstance().update();
        if (project.getString(MATRIX_KEY).length()
                == (CSD.instruments.size() + CSD.effects.size() + 1) * (CSD.effects.size() + 2))
            Matrix.getInstance().unserialize(project.getString(MATRIX_KEY));
        CSD.tempo_ratio = project.getDouble(TEMPO_RATIO_KEY);
        CSD.master_gain_L = project.getDouble(MASTER_GAIN_L_KEY);
        CSD.master_gain_R = project.getDouble(MASTER_GAIN_R_KEY);
        CSD.globals = project.getString(GLOBALS_KEY);
    }

    private static JSONArray saveJSONOrchestra() throws JSONException {
        final JSONArray instruments = new JSONArray();
        JSONObject instr_obj;

        for (String instr : CSD.instruments.getSet()) {
            instr_obj = new JSONObject();
            instr_obj.put("name", instr);
            instr_obj.put("body", CSD.instruments.get(instr).code);
            instr_obj.put("gainL", CSD.instruments.get(instr).gainL);
            instr_obj.put("gainR", CSD.instruments.get(instr).gainR);
            instruments.put(instr_obj);
        }
        return instruments;
    }

    private static void loadJSONOrchestra(JSONArray instruments) throws JSONException {
        JSONObject instr_obj;
        for (int i = 0; i < instruments.length(); i++) {
            instr_obj = instruments.getJSONObject(i);
            CSD.instruments.put(instr_obj.getString("name"), new CSD.Content(instr_obj.getString("body"), instr_obj.getDouble("gainL"), instr_obj.getDouble("gainR")));
        }
    }

    private static JSONArray saveJSONFX() throws JSONException {
        final JSONArray effects = new JSONArray();
        JSONObject effect_obj;
        CSD.Content content;

        for (String effect : CSD.effects.getSet()) {
            effect_obj = new JSONObject();
            effect_obj.put("name", effect);
            content = CSD.effects.get(effect);
            effect_obj.put("body", content.code);
            effect_obj.put("gainL", content.gainL);
            effect_obj.put("gainR", content.gainR);
            effects.put(effect_obj);
        }
        return effects;
    }

    private static void loadJSONFX(JSONArray effects) throws JSONException {
        JSONObject effect_obj;
        for (int i = 0; i < effects.length(); i++) {
            effect_obj = effects.getJSONObject(i);
            CSD.effects.put(
                    effect_obj.getString("name"),
                    new CSD.Content(effect_obj.getString("body"),
                            effect_obj.getDouble("gainL"), effect_obj.getDouble("gainR")));
        }
    }

    private static JSONObject saveJSONTracks() throws JSONException {
        final int idTrackSelected = Score.getIdTrackSelected();
        final int idPatternSelected = Track.getIdPatternSelected();

        int t, p, n;
        Track track;
        Pattern pattern;
        Pattern.Note note;

        JSONObject jsonObject = new JSONObject();
        final JSONArray tracks = new JSONArray();

        JSONArray patterns, notes;
        JSONObject track_obj, pattern_obj, note_obj, view_obj;

        for (t = 1; t <= Score.getNbOfTracks(); t++) {
            Score.setTrackSelected(t);
            track = Score.getTrackSelected();
            track_obj = new JSONObject();
            patterns = new JSONArray();
            for (p = 1; p <= track.getNbOfPatterns(); p++) {
                Track.setPatternSelected(p);
                pattern = Track.getPatternSelected();
                pattern_obj = new JSONObject();
                pattern_obj.put("start", pattern.start);
                pattern_obj.put("finish", pattern.finish);
                pattern_obj.put("instr", pattern.getInstr());
                notes = new JSONArray();
                for (n = 1; n <= pattern.getNbOfNotes(); n++) {
                    Pattern.setNoteSelected(n);
                    note = Pattern.getNoteSelected();
                    note_obj = new JSONObject();
                    note_obj.put("onset", note.onset);
                    note_obj.put("duration", note.duration);
                    note_obj.put("pitch", note.pitch);
                    note_obj.put("loudness", note.loudness);
                    notes.put(note_obj);
                }
                pattern_obj.put("notes", notes);
                view_obj = new JSONObject();
                view_obj.put("posX", pattern.mPosX);
                view_obj.put("posY", pattern.mPosY);
                view_obj.put("scaleFactorX", pattern.mScaleFactorX);
                view_obj.put("scaleFactorY", pattern.mScaleFactorY);
                view_obj.put("resolution_index", pattern.resolution);
                pattern_obj.put("view", view_obj);
                patterns.put(pattern_obj);
            }
            track_obj.put("patterns", patterns);
            tracks.put(track_obj);
        }
        jsonObject.put("Score_posX", ScoreView.mPosX);
        jsonObject.put("Score_posY", ScoreView.mPosY);
        jsonObject.put("Score_scaleFactorX", ScoreView.mScaleFactorX);
        jsonObject.put("Score_scaleFactorY", ScoreView.mScaleFactorY);
        jsonObject.put("Score_resolution", Score.resolution_index);
        jsonObject.put("Score_bar_start", Score.bar_start);
        jsonObject.put("idTrackSelected", idTrackSelected);
        jsonObject.put("idPatternSelected", idPatternSelected);
        jsonObject.put("tracks", tracks);

        Score.setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);
        return jsonObject;
    }

    private static void loadJSONTracks(JSONObject feed) throws JSONException {
        final int idTrackSelected = feed.getInt("idTrackSelected");
        final int idPatternSelected = feed.getInt("idPatternSelected");
        final JSONArray tracks = feed.getJSONArray("tracks");
        JSONArray patterns, notes;
        int t, p, n;
        JSONObject track_obj, pattern_obj, note_obj, view_obj;
        Track track;
        Pattern pattern;

        for (t = 1; t <= tracks.length(); t++) {
            Score.createTrack();
            Score.setTrackSelected(t);
            track = Score.getTrackSelected();
            track_obj = tracks.getJSONObject(t - 1);
            patterns = track_obj.getJSONArray("patterns");
            for (p = 1; p <= patterns.length(); p++) {
                track.createPattern();
                Track.setPatternSelected(p);
                pattern = Track.getPatternSelected();
                pattern_obj = patterns.getJSONObject(p - 1);
                pattern.start = pattern_obj.getInt("start");
                pattern.finish = pattern_obj.getInt("finish");
                pattern.setInstr(pattern_obj.getString("instr"));
                notes = pattern_obj.getJSONArray("notes");
                for (n = 1; n <= notes.length(); n++) {
                    note_obj = notes.getJSONObject(n - 1);
                    pattern.createNote(
                            note_obj.getInt("onset")
                            , note_obj.getInt("duration")
                            , note_obj.getInt("pitch")
                            , note_obj.getInt("loudness"));
                }
                view_obj = pattern_obj.getJSONObject("view");
                pattern.mPosX = (float) view_obj.getDouble("posX");
                pattern.mPosY = (float) view_obj.getDouble("posY");
                pattern.mScaleFactorX = (float) view_obj.getDouble("scaleFactorX");
                pattern.mScaleFactorY = (float) view_obj.getDouble("scaleFactorY");
                pattern.resolution = view_obj.getInt("resolution_index");
            }
        }

        ScoreView.mPosX = (float) feed.getDouble("Score_posX");
        ScoreView.mPosY = (float) feed.getDouble("Score_posY");
        ScoreView.mScaleFactorX = (float) feed.getDouble("Score_scaleFactorX");
        ScoreView.mScaleFactorY = (float) feed.getDouble("Score_scaleFactorY");
        Score.resolution_index = feed.getInt("Score_resolution");
        Score.bar_start = feed.getInt("Score_bar_start");

        Score.setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);
    }
}
