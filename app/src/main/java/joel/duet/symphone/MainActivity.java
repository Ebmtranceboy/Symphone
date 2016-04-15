package joel.duet.symphone;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.csounds.CsoundObj;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import joel.duet.symphone.controller.Effect;
import joel.duet.symphone.controller.Fx;
import joel.duet.symphone.controller.Live;
import joel.duet.symphone.controller.Master;
import joel.duet.symphone.controller.Material;
import joel.duet.symphone.controller.Options;
import joel.duet.symphone.controller.Orchestra;
import joel.duet.symphone.controller.PatchBay;
import joel.duet.symphone.controller.ScoreController;
import joel.duet.symphone.databinding.ActivityMainBinding;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.controller.Instrument;
import joel.duet.symphone.model.Matrix;
import joel.duet.symphone.model.PreferenceManager;
import joel.duet.symphone.model.Score;

public class MainActivity extends AppCompatActivity {
    //private static final String TAG = "MainActivity";
    private final User user = new User();
    public static CsoundObj csoundObj = new CsoundObj(false, true);
    public final CsoundUtil csoundUtil = new CsoundUtil(this);
    public static Runnable sensible_code;
    public final List<String> listInstr = new ArrayList<>();
    public ArrayAdapter<String> instr_adapter;
    public final List<String> listEffect = new ArrayList<>();
    public ArrayAdapter<String> effect_adapter;
    private File csd;

    public class User {
        public final ObservableInt currentViewIndex = new ObservableInt();
        public final ObservableBoolean[] views = new ObservableBoolean[Default.nViews];
        public final ObservableField<String> currentInstrument = new ObservableField<>();
        public final ObservableField<String> currentInstrumentCode = new ObservableField<>();
        public final ObservableField<String> currentEffect = new ObservableField<>();
        public final ObservableField<String> currentEffectCode = new ObservableField<>();
        public final ObservableBoolean pianoMode = new ObservableBoolean();
        public final ObservableBoolean liveLoudnessMode = new ObservableBoolean();
        public final ObservableBoolean polyphonicMode = new ObservableBoolean();
        public final ObservableBoolean soloMode = new ObservableBoolean();
        public final ObservableBoolean isMajor = new ObservableBoolean();
        public final ObservableBoolean scoreEditMode = new ObservableBoolean();
        public final ObservableBoolean patternEditMode = new ObservableBoolean();
        public final ObservableBoolean playLoudnessMode = new ObservableBoolean();
        public MainActivity activity;
        public ActivityMainBinding binding;

        User() {
            currentViewIndex.set(Default.INDEX_WELCOME);
            for (int i = 0; i < Default.nViews; i++)
                views[i] = new ObservableBoolean();
        }

        @SuppressWarnings("unused")
        int getCurrentViewIndex() {
            return currentViewIndex.get();
        }

        public void setCurrentViewIndex(int viewIndex) {
            if (currentViewIndex.get() >= 0) views[currentViewIndex.get()].set(false);
            if (viewIndex >= 0) views[viewIndex].set(true);
            currentViewIndex.set(viewIndex);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);
        user.binding = binding;
        user.activity = this;

        binding.setUser(user);

        ListView menu = binding.listItem;
        SimpleImageArrayAdapter menu_adapter =
                new SimpleImageArrayAdapter(this, Default.menu_icons);
        menu.setAdapter(menu_adapter);

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == Default.INDEX_SAVE_PROJECT) saveProjectToFile();
                else if (i == Default.INDEX_OPEN_PROJECT) openFileProject();
                else if (i == Default.INDEX_NEW_PROJECT) resetToNewProject();
                else if (i == Default.INDEX_RENDER_PROJECT) renderProject();
                else if (i == Default.INDEX_REINIT_CSOUND) reinitCsound();
                else {
                    if (i == Default.INDEX_LIVE) Live.reinit(user);

                    user.setCurrentViewIndex(i);
                }
            }
        });

        instr_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listInstr);
        effect_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listEffect);
        reinit(user);
    }

    private void reinit(User user){
        PreferenceManager.getInstance().initialize(this);
        Matrix.getInstance().spy();
        Matrix.getInstance().update();

        listInstr.clear();
        listEffect.clear();
        listInstr.addAll(CSD.instruments.getSet());
        listEffect.addAll(CSD.effects.getSet());
        instr_adapter.notifyDataSetChanged();
        effect_adapter.notifyDataSetChanged();

        user.setCurrentViewIndex(Default.INDEX_WELCOME);
        user.pianoMode.set(true);
        user.liveLoudnessMode.set(false);
        user.polyphonicMode.set(true);
        user.soloMode.set(true);
        user.isMajor.set(false);
        user.scoreEditMode.set(false);

        Orchestra.reinit(user);
        Instrument.reinit(user);
        PatchBay.reinit(user);
        Master.reinit(user);
        Options.reinit(user);
        Live.reinit(user);
        Fx.reinit(user);
        Effect.reinit(user);
        Material.reinit(user);
        ScoreController.reinit(user);
    }

    @Override
    public void onBackPressed() {

        if (user.getCurrentViewIndex() == Default.INDEX_INSTRUMENT) {
            Instrument.updateModel(user);
            user.setCurrentViewIndex(Default.INDEX_ORCHESTRA);

        } else if (user.getCurrentViewIndex() == Default.INDEX_EFFECT) {
            Effect.updateModel(user);
            user.setCurrentViewIndex(Default.INDEX_FX);

        } else if (user.getCurrentViewIndex() == Default.INDEX_PATTERN) {
            user.setCurrentViewIndex(Default.INDEX_SCORE);

        } else {
            if (user.getCurrentViewIndex() != Default.INDEX_WELCOME) {
                if (user.getCurrentViewIndex() == Default.INDEX_MATERIAL) {
                    CSD.globals = user.binding.material.material.getText().toString();
                }
                user.setCurrentViewIndex(Default.INDEX_WELCOME);

            } else super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getInstance().savePreferences();
    }

    private void saveProjectToFile() {
        SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                new ContextThemeWrapper(MainActivity.this, R.style.csoundAlertDialogStyle),
                "FileSave..",
                new SimpleFileDialog.SimpleFileDialogListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        int index = chosenDir.indexOf("//");
                        if (index >= 0) {
                            chosenDir = chosenDir.substring(index + 1);
                        }
                        File newFile = new File(chosenDir);
                        CSD.projectName = CSD.extractName(newFile.getName());
                        csoundUtil.saveStringAsExternalFile(PreferenceManager.project().toString(),
                                newFile.getAbsolutePath());
                    }
                }
        );
        if (csd != null) {
            fileOpenDialog.default_file_name = csd.getParent();
        } else {
            fileOpenDialog.default_file_name =
                    Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
    }

    private void openFileProject() {
        csoundObj.stop();
        sensible_code = new Runnable() {
            @Override
            public void run() {
                SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                        new ContextThemeWrapper(MainActivity.this, R.style.csoundAlertDialogStyle),
                        "FileOpen..",
                        new SimpleFileDialog.SimpleFileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                File file = new File(chosenDir);
                                CSD.projectName = CSD.extractName(file.getName());
                                MainActivity.this.OnFileChosen(file);
                            }
                        }
                );
                if (csd != null) {
                    fileOpenDialog.default_file_name = csd.getAbsolutePath();
                } else {
                    fileOpenDialog.default_file_name =
                            Environment.getExternalStorageDirectory().getAbsolutePath();
                }
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
            }
        };

        final ConfirmationFragment confirmation = new ConfirmationFragment();
        confirmation.show(getSupportFragmentManager(), "Open project Fragment");
    }

    private void OnFileChosen(File file) {
        csd = file;
        PreferenceManager.resetProject();
        try {
            JSONObject project =
                    new JSONObject(csoundUtil.getExternalFileAsString(csd.getAbsolutePath()));
            PreferenceManager.loadProject(project);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        reinit(user);
    }

    private void resetToNewProject() {
        csoundObj.stop();
        sensible_code = new Runnable() {
            @Override
            public void run() {
                PreferenceManager.resetProject();
                user.setCurrentViewIndex(Default.INDEX_WELCOME);
                CSD.projectName = Default.new_project_name;
            }
        };

        final ConfirmationFragment confirmation = new ConfirmationFragment();
        confirmation.show(getSupportFragmentManager(), "New project Fragment");
    }

    private void renderProject() {
        csoundUtil.saveStringAsExternalFile(Score.sendPatterns(Score.allPatterns(), true, 0),
                "/storage/sdcard0/" + CSD.projectName + ".csd");
    }

    private void reinitCsound() {
        csoundObj.stop();
        csoundObj.getCsound().Stop();
        csoundObj.getCsound().Reset();
        csoundObj.getCsound().Start();
        csoundObj = new CsoundObj(false, true);
        csoundObj.setMessageLoggingEnabled(true);
        Master.reinit(user);
        Live.reinit(user);
        ScoreController.reinit(user);
    }
}
