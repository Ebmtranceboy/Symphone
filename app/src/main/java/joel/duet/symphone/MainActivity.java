package joel.duet.symphone;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.csounds.CsoundObj;

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
                if (i == Default.INDEX_LIVE) Live.reinit(user);
                user.setCurrentViewIndex(i);
            }
        });

        PreferenceManager.getInstance().initialize(this);
        Matrix.getInstance().spy();
        Matrix.getInstance().update();

        listInstr.addAll(CSD.instruments.getSet());
        instr_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listInstr);
        listEffect.addAll(CSD.effects.getSet());
        effect_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listEffect);

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
        //PreferenceManager.getInstance().savePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.reset_csound) {
            csoundObj.stop();
            //csoundObj.forgetMessages();
            csoundObj.getCsound().Stop();
            csoundObj.getCsound().Reset();
            csoundObj.getCsound().Start();
            csoundObj = new CsoundObj(false, true);
            csoundObj.setMessageLoggingEnabled(true);
            Master.reinit(user);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
