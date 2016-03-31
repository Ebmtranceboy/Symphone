package joel.duet.symphone.controller;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.R;
import joel.duet.symphone.databinding.OptionsBinding;
import joel.duet.symphone.model.CSD;

/**
 *
 * Created by joel on 31/03/16 at 22:53 at 23:42.
 */
public class Options {
    public static void reinit(final MainActivity.User user){

        OptionsBinding binding = user.binding.options;

        EditText options_tempo = binding.optionsTempo;
        options_tempo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try{

                    Float tempo = Float.parseFloat(editable.toString());
                            CSD.tempo_ratio = tempo / 60.0;
                }catch (NumberFormatException ex){
                    ex.printStackTrace();
                }
            }
        });

        binding.tonalMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.isMajor.set(!user.isMajor.get());
            }
        });

        String format = user.activity.getString(R.string.floating_point_format);
        options_tempo.setText(String.format(format,CSD.tempo_ratio * 60));
    }
}
