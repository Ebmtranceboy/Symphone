package joel.duet.symphone.controller;


import joel.duet.symphone.MainActivity;
import joel.duet.symphone.model.CSD;

/**
 *
 * Created by joel on 31/03/16 at 15:28 at 15:31 at 15:33 at 16:53.
 */
public class Instrument {
    public static void init(final MainActivity.User user) {
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
