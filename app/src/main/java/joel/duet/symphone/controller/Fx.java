package joel.duet.symphone.controller;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Matrix;

/**
 *
 * Created by joel on 01/04/16 at 22:26 at 23:01.
 */
public class Fx {
    public static void reinit(final MainActivity.User user) {

        user.binding.fx.newEffectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setCurrentViewIndex(Default.INDEX_GIVE_EFFECT_NAME);
            }
        });

        user.binding.effectNameProvider.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setCurrentViewIndex(Default.INDEX_FX);
            }
        });

        final List<String> listEffect = new ArrayList<>();
        final ArrayAdapter<String> effect_adapter =
                new ArrayAdapter<>(user.activity,
                        android.R.layout.simple_spinner_item, listEffect);
        user.binding.fx.listEffectView.setAdapter(effect_adapter);

        user.binding.effectNameProvider.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String effectName = user.binding.effectNameProvider.nameEdited.getText().toString();
                Matrix.getInstance().spy();
                CSD.effects.put(effectName, new CSD.Content("ain1, ain2 xin\n"
                        + "\nxout ain1, ain2\n", 1.0, 1.0));
                Matrix.getInstance().update();
                PatchBay.reinit(user);
                Master.reinit(user);

                listEffect.add(effectName);
                effect_adapter.notifyDataSetChanged();

                user.currentEffect.set(effectName);
                user.currentEffectCode.set(CSD.effects.get(effectName).code);

                user.setCurrentViewIndex(Default.INDEX_EFFECT);
            }
        });

        user.binding.fx.listEffectView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String effectName = listEffect.get(i);
                user.currentEffect.set(effectName);
                user.currentEffectCode.set(CSD.effects.get(effectName).code);

                user.setCurrentViewIndex(Default.INDEX_FX);
            }
        });

    }
}
