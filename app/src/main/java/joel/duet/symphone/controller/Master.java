package joel.duet.symphone.controller;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.csounds.CsoundObj;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.R;
import joel.duet.symphone.SlidingCsoundBindingUI;
import joel.duet.symphone.databinding.MasterBinding;
import joel.duet.symphone.databinding.MasterLineBinding;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;

/**
 *
 * Created by joel on 31/03/16 at 19:47 at 20:14.
 */
public class Master {
    private static CsoundObj csoundObj = MainActivity.csoundObj;
    private static MainActivity activity;

    private static void registerLine(LayoutInflater inflater,
                              ViewGroup container,
                              final String componentName,
                              LinearLayout verticalLayout,
                              final int formatId,
                              final boolean isInstr) {
        SeekBar seekBar;

        MasterLineBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.master_line, container, false);
        TextView lineName = binding.componentName;
        final String format = activity.getString(formatId);
        lineName.setText(String.format(format, componentName));

        final TextView gaindb = binding.gainDb;
        seekBar = binding.gain;
        double val;

        int color;
        if (isInstr) {
            color = Default.instrument_color;
            if (formatId == R.string.master_line_L_format)
                val = CSD.instruments.get(componentName).gainL;
            else val = CSD.instruments.get(componentName).gainR;
        } else {
            if (componentName.equals("Master")) {
                color = Default.master_color;
                if (formatId == R.string.master_line_L_format)
                    val = CSD.master_gain_L;
                else val = CSD.master_gain_R;
            } else {
                color = Default.effect_color;
                if (formatId == R.string.master_line_L_format)
                    val = CSD.effects.get(componentName).gainL;
                else val = CSD.effects.get(componentName).gainR;
            }
        }
        seekBar.setProgress((int) Math.round(val* seekBar.getMax()));
        lineName.setBackgroundColor(color);
        gaindb.setText(String.format("%1s", val));

        csoundObj.addBinding(new SlidingCsoundBindingUI(seekBar,
                "ktrl_" + String.format(format, componentName), 0, 1,
                gaindb, isInstr, formatId, componentName));

        verticalLayout.addView(binding.getRoot());
    }

    public static void reinit(final MainActivity.User user){
        activity = user.activity;

        MasterBinding binding = user.binding.master;

        LinearLayout verticalLayout = binding.master;
        LayoutInflater inflater = activity.getLayoutInflater();
        ViewGroup container = (ViewGroup) binding.getRoot();

        verticalLayout.removeAllViews();

        String[] instrNames = CSD.instruments.getArray();
        for (int i = 0; i < CSD.instruments.size(); i++) {
            registerLine(inflater, container, instrNames[i], verticalLayout, R.string.master_line_L_format, true);
            registerLine(inflater, container, instrNames[i], verticalLayout, R.string.master_line_R_format, true);
        }

        String[] effectNames = CSD.effects.getArray();
        for (int i = 0; i < CSD.effects.size(); i++) {
            registerLine(inflater, container, effectNames[i], verticalLayout, R.string.master_line_L_format, false);
            registerLine(inflater, container, effectNames[i], verticalLayout, R.string.master_line_R_format, false);
        }

        registerLine(inflater, container, "Master", verticalLayout, R.string.master_line_L_format, false);
        registerLine(inflater, container, "Master", verticalLayout, R.string.master_line_R_format, false);


    }
}
