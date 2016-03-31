/* 
 
 BaseCsoundActivity.java:
 
 Copyright (C) 2011 Victor Lazzarini, Steven Yi
 
 This file is part of Csound Android Examples.
 
 The Csound Android Examples is free software; you can redistribute it
 and/or modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.   
 
 Csound is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with Csound; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 02111-1307 USA
 
 */

package joel.duet.symphone;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;

import android.util.Log;

import android.content.Context;
import android.os.Environment;

import joel.duet.symphone.model.CSD;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Pattern;
import joel.duet.symphone.model.Score;
import joel.duet.symphone.model.Track;

public final class CsoundUtil {
    /* Log.d("CsoundObj", "FRAMES:" + ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).
                getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));*/
    private final Context context;
    private static final String TAG = "CsoundUtil";

    public CsoundUtil(Context ctx) {
        context = ctx;
    }

    protected String getResourceFileAsString(int resId) {
        StringBuilder str = new StringBuilder();

        InputStream is = context.getResources().openRawResource(resId);
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;

        try {
            while ((line = r.readLine()) != null) {
                str.append(line).append("\n");
            }
        } catch (IOException ios) {
            ios.printStackTrace();
        }

        return str.toString();
    }

    public File createTempFile(String csd) {
        File f = null;

        try {
            f = File.createTempFile("temp", ".part", context.getCacheDir());
            //Log.i(TAG,context.getCacheDir().getName());
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(csd.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

    public void saveStringAsExternalFile(String str, String absoluteFilePath) {
        final File root = Environment.getExternalStorageDirectory();
        try {
            if (root.canWrite()) {
                final FileWriter filewriter =
                        new FileWriter(
                                new File(absoluteFilePath));
                final BufferedWriter out = new BufferedWriter(filewriter);
                out.write(str);
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not write file " + e.getMessage());
        }
    }

    public String getExternalFileAsString(String absoluteFilePath) {
        final File root = Environment.getExternalStorageDirectory();
        try {
            if (root.canRead()) {
                FileReader filereader =
                        new FileReader(
                                new File(absoluteFilePath));
                final BufferedReader in = new BufferedReader(filereader);
                final StringBuilder buffer = new StringBuilder("");
                String line;
                while ((line = in.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                in.close();
                return buffer.toString();
            }
        } catch (IOException e) {
            return "";
        }
        return "";
    }

    public void patternize(String instr_name, boolean solo_mode) {
        final int idTrackSelected = Score.getIdTrackSelected();
        final int idPatternSelected = Track.getIdPatternSelected();
        Score.createTrack();
        Score.setTrackSelected(Score.getNbOfTracks());
        Track track = Score.getTrackSelected();
        track.createPattern();
        Track.setPatternSelected(1);
        Pattern pattern = Track.getPatternSelected();
        pattern.setInstr(instr_name);

        String lines[] = getExternalFileAsString(Default.score_events_absoluteFilePath).split("\\n");
        final java.util.regex.Pattern istatement =
                java.util.regex.Pattern.compile("\\s*i\\s*\\d+\\s+(\\d+.?\\d*)\\s+(\\d+.?\\d*)\\s+(\\d+.?\\d*)\\s+(-?\\d+.?\\d*)");

        pattern.start = -1;
        pattern.finish = 0;
        for (String line : lines) {
            Matcher matcher = istatement.matcher(line);
            while (matcher.find()) {
                int onset = (int) Math.round(Double.parseDouble(matcher.group(1)) * Default.ticks_per_second * CSD.tempo_ratio);
                int duration = (int) Math.round(Double.parseDouble(matcher.group(2)) * Default.ticks_per_second);
                if (duration == 0) duration = 1;

                if (pattern.start < 0) pattern.start = onset;
                if (onset + duration > pattern.finish) pattern.finish = onset + duration;

                int pitch = (int) Math.round(Double.parseDouble(matcher.group(3)) * 100);
                int key = pitch % 100;
                int oct = (pitch - key) / 100 - 3;

                int loudness = CSD.dB2Loudness(Float.parseFloat(matcher.group(4)));
                pattern.createNote(onset - pattern.start, duration, oct * 12 + key, loudness);
            }
        }
        if(solo_mode) pattern.start = 0;
        pattern.finish += 128; // a full note
        pattern.mPosY = Default.initial_pattern_height;

        Score.setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);
    }

}
