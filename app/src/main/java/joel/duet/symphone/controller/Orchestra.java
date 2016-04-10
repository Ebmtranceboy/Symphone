package joel.duet.symphone.controller;

//import android.util.Log;

import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;
import java.util.regex.Matcher;

import joel.duet.symphone.InputTextDialogFragment;
import joel.duet.symphone.MainActivity;
import joel.duet.symphone.R;
import joel.duet.symphone.SimpleFileDialog;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Matrix;

/**
 *
 * Created by joel on 31/03/16 at 03:04 at 03:06 at 11:11 at 20:31 at 15:46.
 */
public class Orchestra {
    //private static final String TAG = "Orchestra";
    private static File instr_file;

    public static void reinit(final MainActivity.User user) {

        user.binding.orchestra.newInstrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputTextDialogFragment newInstrDialog = new InputTextDialogFragment();
                newInstrDialog.caller = new Call(user);
                newInstrDialog.show(user.activity.getSupportFragmentManager(), "fragment_new_instr");
            }
        });

        user.binding.orchestra.listInstrView.setAdapter(user.activity.instr_adapter);

        user.binding.orchestra.listInstrView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String instrName = user.activity.listInstr.get(i);
                user.currentInstrument.set(instrName);
                user.currentInstrumentCode.set(CSD.instruments.get(instrName).code);

                user.setCurrentViewIndex(Default.INDEX_INSTRUMENT);
            }
        });

        user.binding.orchestra.importInstrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                        new ContextThemeWrapper(user.activity, R.style.csoundAlertDialogStyle),
                        "FileOpen..",
                        new SimpleFileDialog.SimpleFileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                OnFileChosen(user, new File(chosenDir));
                            }
                        }
                );
                if (instr_file != null) fileOpenDialog.default_file_name = instr_file.getParent();
                else
                    fileOpenDialog.default_file_name =
                            Environment.getExternalStorageDirectory().getAbsolutePath();
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
            }
        });
    }

    static private void OnFileChosen(MainActivity.User user, File file) {
        instr_file = file;
        String instr_text = user.activity.csoundUtil.getExternalFileAsString(file.getAbsolutePath());
        ParseInstr instr = new ParseInstr(instr_text);
        String instrName = instr.name;
        if (instrName != null) {
            Matrix.getInstance().spy();
            CSD.instruments.put(instrName, new CSD.Content(instr.body, 1.0, 1.0));
            Matrix.getInstance().update();

            user.activity.listInstr.add(instrName);
            user.activity.instr_adapter.notifyDataSetChanged();

            PatchBay.reinit(user);
            Master.reinit(user);
        }
    }

    static private class ParseInstr {
        String name, body;
        private final java.util.regex.Pattern header =
                java.util.regex.Pattern.compile(" *instr +(\\w+)\\b");
        private final java.util.regex.Pattern footer =
                java.util.regex.Pattern.compile(" *endin\\b");

        ParseInstr(String text) {
            String[] lines = text.split("\n");
            int i = 0;
            while (i < lines.length) {
                Matcher matcher = header.matcher(lines[i]);
                if (matcher.find()) {
                    name = matcher.group(1);
                    break;
                }
                i++;
            }
            i++;

            body = "";
            while (i < lines.length) {
                Matcher matcher = footer.matcher(lines[i]);
                if (matcher.find()) break;
                else body += lines[i] + "\n";
                i++;
            }
        }
    }

    static class Call implements InputTextDialogFragment.EditNameDialogListener {
        final MainActivity.User user;
        Call(MainActivity.User aUser){
            user = aUser;
        }

        @Override
        public void onFinishEditDialog(String instrName) {
            Matrix.getInstance().spy();
            CSD.instruments.put(instrName,
                    new CSD.Content("\nga_" + instrName + "_L += 0"
                            + "\nga_" + instrName + "_R += 0\n", 1.0, 1.0));
            Matrix.getInstance().update();
            PatchBay.reinit(user);
            Master.reinit(user);

            user.activity.listInstr.add(instrName);
            user.activity.instr_adapter.notifyDataSetChanged();

            user.currentInstrument.set(instrName);
            user.currentInstrumentCode.set(CSD.instruments.get(instrName).code);

            user.setCurrentViewIndex(Default.INDEX_INSTRUMENT);
        }
    }
}
