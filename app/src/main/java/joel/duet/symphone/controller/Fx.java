package joel.duet.symphone.controller;

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
 * Created by joel on 01/04/16 at 22:26 at 23:01.
 */
public class Fx {
    private static File effect_file;

    public static void reinit(final MainActivity.User user) {

        user.binding.fx.newEffectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputTextDialogFragment newEffectDialog = new InputTextDialogFragment();
                newEffectDialog.caller = new Call(user);
                newEffectDialog.show(user.activity.getSupportFragmentManager(), "fragment_new_effect");
            }
        });

        user.binding.fx.listEffectView.setAdapter(user.activity.effect_adapter);

        user.binding.fx.listEffectView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String effectName = user.activity.listEffect.get(i);
                user.currentEffect.set(effectName);
                user.currentEffectCode.set(CSD.effects.get(effectName).code);

                user.setCurrentViewIndex(Default.INDEX_EFFECT);
            }
        });

        user.binding.fx.importEffectButton.setOnClickListener(new View.OnClickListener() {
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
                if (effect_file != null) fileOpenDialog.default_file_name = effect_file.getParent();
                else
                    fileOpenDialog.default_file_name = Environment.getExternalStorageDirectory().getAbsolutePath();
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
            }
        });
    }

    private static void OnFileChosen(MainActivity.User user, File file) {
        effect_file = file;
        String effect_text =
                user.activity.csoundUtil.getExternalFileAsString(file.getAbsolutePath());
        ParseEffect effect = new ParseEffect(effect_text);
        String effectName = effect.name;
        if(effectName != null) {
            Matrix.getInstance().spy();
            CSD.effects.put(effectName, new CSD.Content(effect.body, 1.0, 1.0));
            Matrix.getInstance().update();
            user.activity.listEffect.add(effectName);
            user.activity.effect_adapter.notifyDataSetChanged();
            PatchBay.reinit(user);
            Master.reinit(user);
        }
    }

    private static class ParseEffect {
        String name, body;
        private final java.util.regex.Pattern header =
                java.util.regex.Pattern.compile(" *opcode +(\\w+)\\b");
        private final java.util.regex.Pattern footer =
                java.util.regex.Pattern.compile(" *endop\\b");

        ParseEffect(String text) {
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
                if(matcher.find()) break;
                else body += lines[i] + "\n";
                i++;
            }
        }
    }

    static class Call implements InputTextDialogFragment.EditNameDialogListener {
        final MainActivity.User user;

        Call(MainActivity.User aUser) {
            user = aUser;
        }

        @Override
        public void onFinishEditDialog(String effectName) {
            Matrix.getInstance().spy();
            CSD.effects.put(effectName, new CSD.Content("ain1, ain2 xin\n"
                    + "\nxout ain1, ain2\n", 1.0, 1.0));
            Matrix.getInstance().update();
            PatchBay.reinit(user);
            Master.reinit(user);

            user.activity.listEffect.add(effectName);
            user.activity.effect_adapter.notifyDataSetChanged();

            user.currentEffect.set(effectName);
            user.currentEffectCode.set(CSD.effects.get(effectName).code);

            user.setCurrentViewIndex(Default.INDEX_EFFECT);
        }
    }

}
