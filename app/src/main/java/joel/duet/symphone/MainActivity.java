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
import android.widget.ListView;

import com.csounds.CsoundObj;

import joel.duet.symphone.controller.Effect;
import joel.duet.symphone.controller.Fx;
import joel.duet.symphone.controller.Live;
import joel.duet.symphone.controller.Master;
import joel.duet.symphone.controller.Material;
import joel.duet.symphone.controller.Options;
import joel.duet.symphone.controller.Orchestra;
import joel.duet.symphone.controller.PatchBay;
import joel.duet.symphone.databinding.ActivityMainBinding;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.controller.Instrument;
import joel.duet.symphone.model.Matrix;
import joel.duet.symphone.model.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    //private static final String TAG = "MainActivity";
    final User user = new User();
    public static CsoundObj csoundObj = new CsoundObj(false, true);
    public final CsoundUtil csoundUtil = new CsoundUtil(this);
    public static Runnable sensible_code;

    public class User {
        public ObservableInt currentViewIndex = new ObservableInt();
        public ObservableBoolean views[] = new ObservableBoolean[Default.nViews];
        public ObservableField<String> currentInstrument = new ObservableField<>();
        public ObservableField<String> currentInstrumentCode = new ObservableField<>();
        public ObservableField<String> currentEffect = new ObservableField<>();
        public ObservableField<String> currentEffectCode = new ObservableField<>();
        public ObservableBoolean pianoMode = new ObservableBoolean();
        public ObservableBoolean loudnessMode = new ObservableBoolean();
        public ObservableBoolean polyphonicMode = new ObservableBoolean();
        public ObservableBoolean soloMode = new ObservableBoolean();
        public ObservableBoolean isMajor = new ObservableBoolean();
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

        user.setCurrentViewIndex(Default.INDEX_WELCOME);
        user.pianoMode.set(true);
        user.loudnessMode.set(false);
        user.polyphonicMode.set(true);
        user.soloMode.set(true);
        user.isMajor.set(false);

        Orchestra.reinit(user);
        Instrument.reinit(user);
        PatchBay.reinit(user);
        Master.reinit(user);
        Options.reinit(user);
        Live.reinit(user);
        Fx.reinit(user);
        Effect.reinit(user);
        Material.reinit(user);
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
