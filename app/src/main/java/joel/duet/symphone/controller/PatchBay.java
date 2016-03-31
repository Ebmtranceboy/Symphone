package joel.duet.symphone.controller;

//import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import joel.duet.symphone.MainActivity;
import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Matrix;

/**
 *
 * Created by joel on 31/03/16 at 16:59 at 16:59 at 17:04.
 */
public class PatchBay {
    //private static final String TAG = "Patchbay";

    public static void reinit(final MainActivity.User user) {
        final GridView grid = user.binding.patchbay.gridView;
        grid.setNumColumns(CSD.effects.size() + 2);

        //if(Matrix.cells==null) Log.i(TAG, "null cells");
        grid.setAdapter(new ArrayAdapter<String>(user.activity,
                android.R.layout.simple_list_item_1, Matrix.cells) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);

                int n = CSD.effects.size() + 2;
                int j = position % n;
                int i = (position - j) / n;

                int color = 0x00FFFFFF; // Transparent
                if (j == 0) {
                    if (i < CSD.instruments.size()) color = Default.instrument_color;
                    else if (i < CSD.instruments.size() + CSD.effects.size())
                        color = Default.effect_color;
                } else if (i == CSD.instruments.size() + CSD.effects.size() && j > 0) {
                    if (j <= CSD.effects.size()) color = Default.effect_color;
                    else color = Default.master_color;
                }

                view.setBackgroundColor(color);
                ((TextView) view).setTextSize(grid.getColumnWidth() / 4.5f);
                ((TextView) view).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                return view;
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int n = CSD.effects.size() + 2;
                int j = position % n;
                int i = (position - j) / n;
                if (Matrix.get(i, j)) Matrix.getInstance().unset(i, j);
                else Matrix.getInstance().set(i, j);
                //Log.i(TAG,Matrix.serialize());
                grid.invalidateViews();
            }
        });

    }
}
