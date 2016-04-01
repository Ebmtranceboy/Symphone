package joel.duet.symphone.controller;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.model.CSD;

/**
 *
 * Created by joel on 01/04/16 at 23:12 at 23:15.
 */
public class Effect {
    public static void init(final MainActivity.User user) {
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
