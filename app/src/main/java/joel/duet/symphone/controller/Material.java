package joel.duet.symphone.controller;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.model.CSD;

/**
 *
 * Created by joel on 03/04/16 at 23:21 at 23:27.
 */

    // TODO : change presentation to listview with editable + checkable items
    // TODO : PMsine (at least for Ensemble)
    /*
    opcode PMsine,a,kk
        kcps, kwet xin
        setksmps 1

        kph phasor kcps
        krat = (1-kwet)/4

        if (kph&lt;0.5-krat) then
          kramp = kph*(0.25-krat)/(0.5-krat)
        elseif (kph&lt;0.5+krat) then
          kramp = (kph-0.5)*(2*krat-0.5)/(2*krat)
        else
          kramp = (kph-1)*(0.25-krat)/(0.5-krat)
        endif
        ksig tablei kph-kramp,-1,1,0,1
        asig = ksig
        xout asig
     endop
*/

public class Material {
    public static void reinit(final MainActivity.User user) {
        user.binding.material.material.setText(CSD.globals);
    }
}
