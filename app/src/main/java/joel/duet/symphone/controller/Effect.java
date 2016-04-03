package joel.duet.symphone.controller;

import android.view.View;

import joel.duet.symphone.ConfirmationFragment;
import joel.duet.symphone.MainActivity;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Matrix;

/**
 *
 * Created by joel on 01/04/16 at 23:12 at 23:15.
 */
public class Effect {
    public static void reinit(final MainActivity.User user) {
         user.binding.effect.deleteEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainActivity.sensible_code = new Runnable() {
                    @Override
                    public void run() {
                        Matrix.getInstance().spy();
                        CSD.effects.remove(user.currentEffect.get());
                        Matrix.getInstance().update();
                        Fx.reinit(user);
                        PatchBay.reinit(user);
                        Master.reinit(user);

                        user.setCurrentViewIndex(Default.INDEX_FX);
                    }
                };

                final ConfirmationFragment confirmation = new ConfirmationFragment();
                confirmation.show(user.activity.getSupportFragmentManager(), "Delete effect Fragment");
            }
        });
    }

    public static void updateModel(MainActivity.User user) {
        CSD.Content content = CSD.effects.get(user.currentEffect.get());
        if (content != null)
            CSD.effects.put(user.currentEffect.get(),
                    new CSD.Content(user.binding.effect.effectCode.getText().toString(),
                            content.gainL,
                            content.gainR));
    }
}
