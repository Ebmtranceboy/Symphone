package joel.duet.symphone.controller;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.csounds.CsoundObj;

import joel.duet.symphone.InputTextDialogFragment;
import joel.duet.symphone.MainActivity;
import joel.duet.symphone.SimpleImageArrayAdapter;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Score;
import joel.duet.symphone.model.Track;
import joel.duet.symphone.modelview.PatternView;

/**
 *
 * Created by joel on 04/04/16 at 14:39 at 15:09 at 10:36.
 */
public class PatternController {
    private static PatternView patternview;
    private static final String TAG = "PatternFragment";

    private static MainActivity activity;
    private static CsoundObj csoundObj;

    public static void reinit(final MainActivity.User user) {
        activity = user.activity;
        csoundObj = MainActivity.csoundObj;

        final ImageButton arpeggio_button = user.binding.pattern.arpeggio;
        final ToggleButton mode_button = user.binding.pattern.modeButtonPattern;

        mode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.patternEditMode.set(!user.patternEditMode.get());
            }
        });

        final ToggleButton loudnessButton = user.binding.pattern.loudnessButton;
        loudnessButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                user.playLoudnessMode.set(!user.playLoudnessMode.get());
            }
        });

        final Spinner instrument_spinner = user.binding.pattern.patternInstrument;
        instrument_spinner.setAdapter(user.activity.instr_adapter);
        instrument_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        Track.getPatternSelected().setInstr(CSD.instruments.getArray()[i]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

        arpeggio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputTextDialogFragment commandLabDialog = new InputTextDialogFragment();
                commandLabDialog.caller = patternview;
                commandLabDialog.show(user.activity.getSupportFragmentManager(), "fragment_command_lab");
            }
        });

        user.binding.pattern.recenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Track.getPatternSelected().mPosX = 0;
                Track.getPatternSelected().mPosY = Default.initial_pattern_height;
                patternview.invalidate();
            }
        });

        final Spinner resolution_spinner = user.binding.pattern.resolutionPattern;
        SimpleImageArrayAdapter adapter =
                new SimpleImageArrayAdapter(user.activity, Default.resolution_icons);
        resolution_spinner.setAdapter(adapter);

        resolution_spinner.setSelection(Track.getPatternSelected().resolution);

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Track.getPatternSelected().resolution = i;
                patternview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        user.binding.pattern.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String csd = Score.sendPatterns(Track.getPatternSelected().singleton(), false,
                        Track.getPatternSelected().start,
                        Track.getPatternSelected().finish);
                Log.i(TAG, csd);
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });

        user.binding.pattern.inContext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String csd = Score.sendPatterns(Score.allPatterns(), false,
                        Track.getPatternSelected().start,
                        Track.getPatternSelected().finish);
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });


        user.binding.pattern.stopPattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                csoundObj.stop();
            }
        });

        patternview = user.binding.pattern.patternView;
        patternview.note_loudness = user.binding.pattern.noteLoudness;

        user.patternEditMode.set(mode_button.isChecked());
        user.playLoudnessMode.set(loudnessButton.isChecked());

        int instr_selected = 0;
        String names[] = CSD.instruments.getArray();
        String name = Track.getPatternSelected().getInstr();
        while(instr_selected<CSD.instruments.size() && !names[instr_selected].equals(name))
            instr_selected ++;
        instrument_spinner.setSelection(instr_selected);


    }

    /*
    @Override
    public void onFinishEditDialog(String inputText) {
        String[] command = inputText.split(" +");
        int n = patternview.pattern.getNbOfNotes();
        int period = patternview.pattern.getNote(n-1).onset+patternview.pattern.getNote(n-1).duration;

        if(command[0].equals("repeat")){
            boolean roomLeft = true;
            int mark = period;

            while(roomLeft){
                for(int i=0; i<n; i++) {
                    Pattern.Note note = patternview.pattern.getNote(i);
                    if (mark + note.onset + note.duration <
                            patternview.pattern.finish - patternview.pattern.start)
                        patternview.pattern.createNote(
                                mark + note.onset,
                                note.duration,
                                note.pitch,
                                note.loudness);
                    else roomLeft = false;
                }
                if(roomLeft) mark += period;
            }
        } else if(command[0].equals("transpose")){
            try {
                int delta = Integer.parseInt(command[1]);
                for (int i = 0; i < n; i++) {
                    Pattern.Note note = patternview.pattern.getNote(i);
                    note.pitch += delta;
                }
            } catch (NumberFormatException exp){exp.printStackTrace();}
        }

        patternview.invalidate();
    }
    */

}
