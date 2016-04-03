package joel.duet.symphone.controller;

//import android.util.Log;

import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.R;
import joel.duet.symphone.SimpleFileDialog;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Matrix;

/**
 *
 * Created by joel on 31/03/16 at 03:04 at 03:06 at 11:11 at 20:31.
 */
public class Orchestra {
    //private static final String TAG = "Orchestra";
    private static File instr_file;

    public static void reinit(final MainActivity.User user) {

        user.binding.orchestra.newInstrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setCurrentViewIndex(Default.INDEX_GIVE_INSTRUMENT_NAME);
            }
        });

        user.binding.instrumentNameProvider.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setCurrentViewIndex(Default.INDEX_ORCHESTRA);
            }
        });

        final List<String> listInstr = new ArrayList<>();
        listInstr.addAll(CSD.instruments.getSet());
        final ArrayAdapter<String> instr_adapter = new ArrayAdapter<>(user.activity,
                android.R.layout.simple_spinner_item, listInstr);
        user.binding.orchestra.listInstrView.setAdapter(instr_adapter);

        user.binding.instrumentNameProvider.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String instrName = user.binding.instrumentNameProvider.nameEdited.getText().toString();
                Matrix.getInstance().spy();
                CSD.instruments.put(instrName,
                        new CSD.Content("\nga_" + instrName + "_L += 0"
                                + "\nga_" + instrName + "_R += 0\n", 1.0, 1.0));
                Matrix.getInstance().update();
                PatchBay.reinit(user);
                Master.reinit(user);

                listInstr.add(instrName);
                instr_adapter.notifyDataSetChanged();

                user.currentInstrument.set(instrName);
                user.currentInstrumentCode.set(CSD.instruments.get(instrName).code);

                user.setCurrentViewIndex(Default.INDEX_INSTRUMENT);
            }
        });

        user.binding.orchestra.listInstrView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String instrName = listInstr.get(i);
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
            Orchestra.reinit(user);
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
}
