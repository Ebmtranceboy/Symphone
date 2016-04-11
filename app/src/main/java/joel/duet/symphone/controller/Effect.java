package joel.duet.symphone.controller;

import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.View;

import java.io.File;

import joel.duet.symphone.ConfirmationFragment;
import joel.duet.symphone.MainActivity;
import joel.duet.symphone.R;
import joel.duet.symphone.SimpleFileDialog;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Matrix;

/**
 *
 * Created by joel on 01/04/16 at 23:12 at 23:15.
 */
public class Effect {
    private static File effect_file;

    public static void reinit(final MainActivity.User user) {
         user.binding.effect.deleteEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String effectName = user.currentEffect.get();
                MainActivity.sensible_code = new Runnable() {
                    @Override
                    public void run() {
                        Matrix.getInstance().spy();
                        CSD.effects.remove(effectName);
                        Matrix.getInstance().update();

                        user.activity.listEffect.remove(effectName);
                        user.activity.effect_adapter.notifyDataSetChanged();

                        PatchBay.reinit(user);
                        Master.reinit(user);

                        user.setCurrentViewIndex(Default.INDEX_FX);
                    }
                };

                final ConfirmationFragment confirmation = new ConfirmationFragment();
                confirmation.show(user.activity.getSupportFragmentManager(), "Delete effect Fragment");
            }
        });

        user.binding.effect.addToFxLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateModel(user);
                final String effectName = user.currentEffect.get();
                SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                        new ContextThemeWrapper(user.activity,
                                R.style.csoundAlertDialogStyle),
                        "FileSave..",
                        new SimpleFileDialog.SimpleFileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                int index = chosenDir.indexOf("//");
                                if (index >= 0) {
                                    chosenDir = chosenDir.substring(index + 1);
                                }
                                effect_file = new File(chosenDir);
                                user.activity.csoundUtil.saveStringAsExternalFile(
                                        "opcode " + effectName + ", aa, aa\n"
                                                + CSD.effects.get(effectName).code
                                                + "endop", effect_file.getAbsolutePath());
                            }
                        }
                );
                if (effect_file != null) {
                    fileOpenDialog.default_file_name = effect_file.getParent();
                } else {
                    fileOpenDialog.default_file_name =
                            Environment.getExternalStorageDirectory().getAbsolutePath();
                }
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
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
