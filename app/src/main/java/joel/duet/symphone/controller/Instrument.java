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
 * Created by joel on 31/03/16 at 15:28 at 15:31 at 15:33 at 16:53.
 */
public class Instrument {
    private static File instr_file;

    public static void reinit(final MainActivity.User user) {

        user.binding.instrument.deleteInstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String instrName = user.currentInstrument.get();
                MainActivity.sensible_code = new Runnable() {
                    @Override
                    public void run() {
                        Matrix.getInstance().spy();
                        CSD.instruments.remove(instrName);
                        Matrix.getInstance().update();

                        user.activity.listInstr.remove(instrName);
                        user.activity.instr_adapter.notifyDataSetChanged();

                        PatchBay.reinit(user);
                        Master.reinit(user);

                        user.setCurrentViewIndex(Default.INDEX_ORCHESTRA);
                    }
                };

                final ConfirmationFragment confirmation = new ConfirmationFragment();
                confirmation.show(user.activity.getSupportFragmentManager(), "Delete instrument Fragment");
            }
        });

        user.binding.instrument.addToInstrLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String instrName = user.currentInstrument.get();
                updateModel(user);
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
                                instr_file = new File(chosenDir);
                                user.activity.csoundUtil.saveStringAsExternalFile(
                                        "instr " + instrName + "\n"
                                                + CSD.instruments.get(instrName).code
                                                + "endin",
                                        instr_file.getAbsolutePath());
                            }
                        }
                );
                if (instr_file != null) {
                    fileOpenDialog.default_file_name = instr_file.getParent();
                } else {
                    fileOpenDialog.default_file_name =
                            Environment.getExternalStorageDirectory().getAbsolutePath();
                }
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
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
