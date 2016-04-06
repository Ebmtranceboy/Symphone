package joel.duet.symphone.controller;

//import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.csounds.CsoundObj;
import com.csounds.bindings.ui.CsoundSliderBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.R;
import joel.duet.symphone.databinding.LiveBinding;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Score;
import joel.duet.symphone.modelview.ChordsView;
import joel.duet.symphone.modelview.KeyboardView;

/**
 *
 * Created by joel on 31/03/16 at 22:48 at 23:46.
 */
public class Live {
    //private static final String TAG = "Live";

    static private MainActivity activity;
    static private CsoundObj csoundObj;
    static private KeyboardView keyboard;
    static private ChordsView chordsView;
    private final static int[] touchIds = new int[10];
    private final static float[] touchX = new float[10];
    private final static float[] touchY = new float[10];
    private static SeekBar ktrlx, ktrly;
    private static String lastPch = "3.00";

    private static int getTouchIdAssignment() {
        for (int i = 0; i < touchIds.length; i++) {
            if (touchIds[i] == -1) {
                return i;
            }
        }
        return -1;
    }

    private static int getTouchId(int touchId) {
        for (int i = 0; i < touchIds.length; i++) {
            if (touchIds[i] == touchId) {
                return i;
            }
        }
        return -1;
    }

    private static void ktrlBindings() {
        csoundObj.addBinding(new CsoundSliderBinding(ktrlx, "ktrlx", 0, 1));
        csoundObj.addBinding(new CsoundSliderBinding(ktrly, "ktrly", 0, 1));
//        csoundObj.addBinding(new CsoundButtonBinding(button1, "button1", 1));
    }
    public static void reinit(final MainActivity.User user) {
        if(CSD.instruments.size()>0) doReinit(user);
    }

    public static void doReinit(final MainActivity.User user) {
        activity = user.activity;
        csoundObj = MainActivity.csoundObj;
        csoundObj.stop();
        csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.part()));

        LiveBinding binding = user.binding.live;

        for (int i = 0; i < touchIds.length; i++) {
            touchIds[i] = -1;
            touchX[i] = -1;
            touchY[i] = -1;
        }

        // populate oct spinner
        final Spinner select_oct = binding.selectOct;
        ArrayAdapter<CharSequence> oct_adapter =
                ArrayAdapter.createFromResource(activity,
                        R.array.oct_array, android.R.layout.simple_spinner_item);
        oct_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_oct.setAdapter(oct_adapter);
        select_oct.setSelection(7);

        // populate instr spinner
        final Spinner select_instr = binding.selectInstr;
        final ArrayAdapter<String> instr_adapter =
                new ArrayAdapter<>(activity.getBaseContext(),
                        android.R.layout.simple_spinner_item, CSD.instruments.getArray());
        select_instr.setAdapter(instr_adapter);

        final ToggleButton keyboadButton = binding.pianoMode;
        keyboadButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.pianoMode.set(isChecked);
            }
        });

        final ToggleButton loudnessButton = binding.loudness;
        loudnessButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.liveLoudnessMode.set(isChecked);
            }
        });

        final ToggleButton phonicModeButton = binding.phonicMode;
        phonicModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.polyphonicMode.set(isChecked);
            }
        });

        final ToggleButton soloModeButton = binding.solo;
        soloModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.soloMode.set(isChecked);
                if (isChecked)
                    soloModeButton.setBackgroundResource(R.drawable.ic_read_partition);
                else soloModeButton.setBackgroundResource(R.drawable.ic_play_in_context);
            }
        });

        ktrlx = binding.ktrlx;
        ktrly = binding.ktrly;
        ktrlBindings();

        final ImageButton recordButton = binding.liveRecord;
        ImageButton playButton = binding.livePlay;
        ImageButton patternizeButton = binding.patternize;

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setImageResource(R.drawable.ic_recording);

                csoundObj.stop();
                ktrlBindings();
                String csd =
                        user.soloMode.get() ? CSD.recordPart((String) select_instr.getSelectedItem()) :
                                Score.sendPatternsForRecord((String) select_instr.getSelectedItem(),
                                        Score.allPatterns());
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setImageResource(R.drawable.ic_menu_live);

                csoundObj.stop();
                ktrlBindings();
                String csd =
                        user.soloMode.get() ? CSD.part() :
                                Score.sendPatterns(Score.allPatterns(), false, 0);
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
                csoundObj.sendScore(
                        activity.csoundUtil.getExternalFileAsString(
                                Default.score_events_absoluteFilePath).
                                replaceAll("i +\\w+ +",
                                        "i\"" + select_instr.getSelectedItem() + "\" "));
            }
        });

        patternizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setImageResource(R.drawable.ic_menu_live);

                activity.csoundUtil.patternize((String) select_instr.getSelectedItem(),
                        user.soloMode.get());
                user.binding.score.scoreView.invalidate();
            }
        });

        binding.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setImageResource(R.drawable.ic_menu_live);

                final ExecutorService e = Executors.newScheduledThreadPool(1);

                // schedule it to execute starting from now
                ((ScheduledExecutorService) e).schedule(new Runnable() {
                    @Override
                    public void run() {
                        csoundObj.stop();
                        ktrlBindings();
                        csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.part()));
                    }
                }, 500, TimeUnit.MILLISECONDS);

                // in case record_mode is on, this is needed for foutir to catch the last note
                csoundObj.sendScore("i\"Silencer\" 0 0 \"" + select_instr.getSelectedItem() + "\" " + 1);
            }
        });
        // define keyboard

        keyboard = binding.Keyboard;
        keyboard.draw(-1, -1, false);
        keyboard.show();

        keyboard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int key;
                final int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        for (int i = 0; i < event.getPointerCount(); i++) {
                            int pointerId = event.getPointerId(i);
                            int id = getTouchId(pointerId);

                            if (id == -1) {

                                id = getTouchIdAssignment();

                                if (id != -1) {
                                    touchIds[id] = pointerId;
                                    touchX[id] = event.getX(i);
                                    touchY[id] = event.getY(i);
                                    key = keyboard.draw(touchX[id], touchY[id], true);
                                    //Log.i(TAG, "key=" + key);

                                    if (!user.polyphonicMode.get())
                                        csoundObj.sendScore("i\"Silencer\" 0 0 \""
                                                + select_instr.getSelectedItem() + "\" " + (id + 1));
                                    String pch =
                                            select_oct.getSelectedItem() + "."
                                                    + (key < 10 ? "0" : "") + key;
                                    csoundObj.sendScore("i\"Voicer\" 0 0 \""
                                            + select_instr.getSelectedItem() + "\" " + (id + 1) + " "
                                            + pch + " "
                                            + (user.liveLoudnessMode.get() ? CSD.pressure2dB(event.getPressure()) :
                                            CSD.defaultLoudness2dB()) + " "
                                            + lastPch);
                                    lastPch = pch;
                                }
                            }
                        }
                        keyboard.show();
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:

                        int activePointerIndex = event.getActionIndex();
                        int pointerId = event.getPointerId(activePointerIndex);

                        int id = getTouchId(pointerId);
                        if (id != -1) {
                            touchIds[id] = -1;

                            if (user.polyphonicMode.get())
                                csoundObj.sendScore("i\"Silencer\" 0 0 \""
                                        + select_instr.getSelectedItem() + "\" " + (id + 1));

                            keyboard.draw(touchX[id], touchY[id], false);
                            keyboard.show();
                        }

                        break;
                }
                return true;
            }
        });

        chordsView = binding.ChordsView;
        chordsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final int action = event.getActionMasked();
                final int indexChord = chordsView.whatis(event.getX(), event.getY());
                final int minIndex = user.isMajor.get() ? 0 : Default.Flavor.nbMajor;
                final int supIndex = user.isMajor.get() ? Default.Flavor.nbMajor : Default.flavors.length - Default.Flavor.nbMajor;
                int index, normalizedKey, oct;
                String pch;


                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (indexChord >= 0 && indexChord < supIndex) {


                            for (Integer key : Default.flavors[indexChord + minIndex].intervals) {
                                if(key>=0) {
                                    pch = select_oct.getSelectedItem() + "."
                                                    + (key < 10 ? "0" : "") + key;
                                    normalizedKey = key;
                                } else {
                                    normalizedKey = key + 12;
                                    oct = Integer.parseInt((String)select_oct.getSelectedItem());
                                    pch = "" + (oct-1)+ "."
                                                    + (normalizedKey < 10 ? "0" : "") + normalizedKey;

                                }
                                index = indexChord + normalizedKey;
                                csoundObj.sendScore("i\"Voicer\" 0 0 \""
                                        + select_instr.getSelectedItem() + "\" " + index + " "
                                        + pch + " "
                                        + (user.liveLoudnessMode.get() ? CSD.pressure2dB(event.getPressure()) :
                                            CSD.defaultLoudness2dB()) + " "
                                        + lastPch);

                                lastPch = pch;

                            }

                            chordsView.show(indexChord);

                            /*
                            // TODO Basic arpeggiator:
                            List<Pattern.Note> notes = new ArrayList<>();
                            int offset = 0;
                            for(Integer key : Default.flavors[indexChord].intervals) {
                                notes.add(new Pattern.Note(offset, 16, 69 + key, Default.default_loudness));
                                offset += 32;
                            }
                            Pattern pattern = new Pattern(notes,(String)select_instr.getSelectedItem());
                            pattern.start = 0;
                            pattern.finish = offset;

                            String csd = Score.sendPatterns(pattern.singleton(), false,pattern.start,pattern.finish);
                            csoundObj.stop();
                            csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
                            */

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (indexChord >= 0 && indexChord < supIndex) {

                            for (Integer key : Default.flavors[indexChord + minIndex].intervals) {
                                normalizedKey = key;
                                if(key<0) normalizedKey = key+12;
                                index = indexChord + normalizedKey;
                                csoundObj.sendScore("i\"Silencer\" 0 0 \""
                                        + select_instr.getSelectedItem() + "\" " + index);
                            }
                        }
                        chordsView.show(-1);

                        break;
                }
                chordsView.invalidate();
                return true;
            }
        });
    }



}
