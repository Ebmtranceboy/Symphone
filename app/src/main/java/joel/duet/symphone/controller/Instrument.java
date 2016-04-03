package joel.duet.symphone.controller;


import android.view.View;

import joel.duet.symphone.ConfirmationFragment;
import joel.duet.symphone.MainActivity;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Matrix;

/**
 *
 * Created by joel on 31/03/16 at 15:28 at 15:31 at 15:33 at 16:53.
 */
public class Instrument {
    public static void reinit(final MainActivity.User user) {

        user.binding.instrument.deleteInstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainActivity.sensible_code = new Runnable() {
                    @Override
                    public void run() {
                        Matrix.getInstance().spy();
                        CSD.instruments.remove(user.currentInstrument.get());
                        Matrix.getInstance().update();
                        Orchestra.reinit(user);
                        PatchBay.reinit(user);
                        Master.reinit(user);

                        user.setCurrentViewIndex(Default.INDEX_ORCHESTRA);
                    }
                };

                final ConfirmationFragment confirmation = new ConfirmationFragment();
                confirmation.show(user.activity.getSupportFragmentManager(), "Delete instrument Fragment");
            }
        });
    }

    public static void updateModel(MainActivity.User user) {
        CSD.Content content = CSD.instruments.get(user.currentInstrument.get());
        if (content != null)
            CSD.instruments.put(user.currentInstrument.get(),
                    new CSD.Content(user.binding.instrument.instrumentCode.getText().toString(),
                            content.gainL,
                            content.gainR));
    }
}
