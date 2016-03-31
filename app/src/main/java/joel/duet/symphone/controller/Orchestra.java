package joel.duet.symphone.controller;

//import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Matrix;
import joel.duet.symphone.model.PreferenceManager;

/**
 *
 * Created by joel on 31/03/16 at 03:04 at 03:06 at 11:11.
 */
public class Orchestra {
    //private static final String TAG = "Orchestra";

    public static void reinit(final MainActivity.User user) {

        user.binding.orchestra.newInstrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setCurrentViewIndex(Default.INDEX_GIVE_INSTRUMENT_NAME);

            }
        });

        user.binding.instrumentNameProvider.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setCurrentViewIndex(Default.INDEX_ORCHESTRA);
            }
        });

        PreferenceManager.getInstance().initialize(user.activity);
        Matrix.getInstance().update();
        //Log.i(TAG, "null cells");

        final List<String> listInstr = new ArrayList<>();
        final ArrayAdapter<String> instr_adapter = new ArrayAdapter<>(user.activity,
                android.R.layout.simple_spinner_item, listInstr);
        user.binding.orchestra.listInstrView.setAdapter(instr_adapter);

        user.binding.instrumentNameProvider.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String instrName = user.binding.instrumentNameProvider.nameEdited.getText().toString();
                Matrix.getInstance().spy();
                CSD.instruments.put(instrName,
                        new CSD.Content("\nga_" + instrName + "_L += 0"
                                + "\nga_" + instrName + "_R += 0\n", 1.0, 1.0));
                Matrix.getInstance().update();
                PatchBay.reinit(user);
                Master.reinit(user);
                Live.reinit(user);
                listInstr.add(instrName);
                instr_adapter.notifyDataSetChanged();

                user.currentInstrument.set(instrName);
                user.currentInstrumentCode.set(CSD.instruments.get(instrName).code);

                user.setCurrentViewIndex(Default.INDEX_INSTRUMENT);
            }
        });

        user.binding.orchestra.listInstrView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String instrName = listInstr.get(i);
                user.currentInstrument.set(instrName);
                user.currentInstrumentCode.set(CSD.instruments.get(instrName).code);

                user.setCurrentViewIndex(Default.INDEX_INSTRUMENT);
            }
        });
    }
}
