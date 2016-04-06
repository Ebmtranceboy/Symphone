package joel.duet.symphone.modelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Matrix;
import android.widget.ImageView;

import joel.duet.symphone.R;
import joel.duet.symphone.model.Default;
import joel.duet.symphone.model.Pattern;
import joel.duet.symphone.model.Track;

/**
 *
 * Created by joel on 22/01/16 at 23:26 at 23:28.
 */
public final class PatternView extends View {
    private static final String TAG = "PatternView";
    private static final Paint paint = new Paint();
    private static final float scale_height = 10.0f;
    private static final float line_height = scale_height / (float) Default.max_midi_note;
    private static final float[] coords = new float[2];
    private static final Matrix mMatrix = new Matrix();
    private static final Matrix mInverse = new Matrix();

    private static int bar_begin = -1;
    private static int bar_end;
    private static int insertion_line;
    private static float mLastTouchX, mLastTouchY;
    private static int mActivePointerId;
    private static int width;
    private static float width_per_tick;
    private static int height;
    private static int number_ticks;
    private ScaleGestureDetector mScaleDetector;
    boolean patternEditMode;
    boolean playLoudnessMode;

    public ImageView note_loudness;

    public PatternView(Context context) {
		super(context);
		init(context);
	}

	public PatternView(Context context, AttributeSet attrs) {
		super(context, attrs);
                TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.joel_duet_symphone_modelview_PatternView,
                0, 0);

        try {
            patternEditMode = a.getBoolean(R.styleable.joel_duet_symphone_modelview_PatternView_patternEditMode, false);
            playLoudnessMode = a.getBoolean(R.styleable.joel_duet_symphone_modelview_PatternView_playLoudnessMode, false);
        } finally {
            a.recycle();
        }

		init(context);
	}

    @SuppressWarnings("unused")
    public void setPatternEditMode(boolean b) {
        patternEditMode = b;
        invalidate();
        requestLayout();
    }

    @SuppressWarnings("unused")
    public void setPlayLoudnessMode(boolean b) {
        playLoudnessMode = b;
        invalidate();
        requestLayout();
    }

	public PatternView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

    private void init(Context ctx) {
        //paint.setAntiAlias(true);
        mScaleDetector = new ScaleGestureDetector(ctx, new ScaleListener());
    }

    private static void drawBackground(Canvas canvas) {
        PatternView.paint.setStyle(Paint.Style.FILL);
        PatternView.paint.setColor(Color.parseColor("#7F7F7F"));
        canvas.drawPaint(PatternView.paint);
    }

    private static void setStrokeWhite() {
        PatternView.paint.setStyle(Paint.Style.STROKE);
        PatternView.paint.setColor(Color.parseColor("#FFFFFF"));
    }

    private static void setStrokeBlack() {
        PatternView.paint.setStyle(Paint.Style.STROKE);
        PatternView.paint.setColor(Color.parseColor("#000000"));
    }

    private static void setFillAlpha() {
        PatternView.paint.setStyle(Paint.Style.FILL);
        PatternView.paint.setARGB(64, 255, 255, 255);
    }

    private static void setFillGrey(int loudness) {
        PatternView.paint.setStyle(Paint.Style.FILL);
        int grey = 178 + (loudness-1)*11;
        PatternView.paint.setARGB(255, grey, grey, grey);
    }

    private static void drawReset(Canvas canvas) {
        drawBackground(canvas);
        setStrokeWhite();
    }

    private int getResolution() {
        return Default.resolutions[Track.getPatternSelected().resolution];
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Pattern pattern = Track.getPatternSelected();
            if (Math.abs(detector.getCurrentSpanX()) < Math.abs(detector.getCurrentSpanY()))
                pattern.mScaleFactorY *= detector.getScaleFactor();
            else if (Math.abs(detector.getCurrentSpanY()) < Math.abs(detector.getCurrentSpanX()))
                pattern.mScaleFactorX *= detector.getScaleFactor();
            else {
                pattern.mScaleFactorY *= detector.getScaleFactor();
                pattern.mScaleFactorX *= detector.getScaleFactor();
            }

            // Don't let the object get too small or too large.
            pattern.mScaleFactorX = Math.max(0.1f, Math.min(pattern.mScaleFactorX, 5.0f));
            pattern.mScaleFactorY = Math.max(0.1f, Math.min(pattern.mScaleFactorY, 5.0f));

            invalidate();
            return true;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Pattern pattern = Track.getPatternSelected();
        number_ticks = pattern.finish - pattern.start;

        width = getWidth();
        width_per_tick = width / (float)number_ticks;
        height = getHeight();
        drawReset(canvas);

        canvas.save();

        mMatrix.setTranslate(pattern.mPosX, pattern.mPosY);
        mMatrix.postScale(pattern.mScaleFactorX, pattern.mScaleFactorY
                , super.getWidth() * 0.5f, super.getHeight() * 0.5f);
        mMatrix.invert(mInverse);
        coords[0] = 0;
        coords[1] = 13;
        mInverse.mapPoints(coords);

        canvas.concat(mMatrix);

        for (int m = 1; m <= Default.max_midi_note; m++) {
            setStrokeWhite();
            canvas.drawText("" + (Default.max_midi_note - m),
                    coords[0],
                    (m - 0.4f) * line_height * height,
                    paint);
            for (int i = 1; i <= Math.round((double)number_ticks / getResolution()); i++) {
                drawBar(i, canvas, m);
            }
        }

        for (int n = 1; n <= pattern.getNbOfNotes(); n++) {
            Pattern.setNoteSelected(n);
            final Pattern.Note note = Pattern.getNoteSelected();
            setFillGrey(note.loudness);
            canvas.drawRect(note.onset * width_per_tick
                    , (Default.max_midi_note - note.pitch - 1) * height * line_height
                    , (note.onset + note.duration) * width_per_tick
                    , (Default.max_midi_note - note.pitch) * height * line_height, paint);
            setStrokeBlack();
            canvas.drawRect(note.onset * width_per_tick
                    , (Default.max_midi_note - note.pitch - 1) * height * line_height
                    , (note.onset + note.duration) * width_per_tick
                    , (Default.max_midi_note - note.pitch) * height * line_height, paint);
        }

        if (bar_begin >= 0) {
            setFillAlpha();
            canvas.drawRect(bar_begin * getResolution() * width_per_tick
                    , (insertion_line - 1) * height * line_height
                    , (bar_end + 1) * getResolution() *width_per_tick
                    , insertion_line * height * line_height, paint);
        }

        canvas.restore();
    }

    private int closestX(float x0) {
        float d, dist = Float.MAX_VALUE;
        float x;
        int p, c = 0;
        for (p = 1; p <= number_ticks / getResolution(); p++) {
            x = (p - 0.5f) * getResolution() * width_per_tick;
            d = Math.abs(x - x0);
            if (d < dist) {
                dist = d;
                c = p - 1;
            }
        }
        return c;
    }

    private static int closestY(float y0) {
        float d, dist = Float.MAX_VALUE;
        float y;
        int m, c = 0;
        for (m = 1; m <= Default.max_midi_note; m++) {
            y = (m - 0.5f) * line_height * height;
            d = Math.abs(y - y0);
            if (d < dist) {
                dist = d;
                c = m;
            }
        }
        return c;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        note_loudness.setVisibility(INVISIBLE);
        Pattern pattern = Track.getPatternSelected();

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                Pattern.Note note = null;

                coords[0] = x;
                coords[1] = y;
                mInverse.mapPoints(coords);

                if (patternEditMode) {
                    insertion_line = closestY(coords[1]);
                    boolean existing_note = false;
                    int n = 1;
                    while (n <= pattern.getNbOfNotes() && !existing_note) {
                        Pattern.setNoteSelected(n);
                        note = Pattern.getNoteSelected();
                        existing_note =
                                note.pitch == Default.max_midi_note - insertion_line
                                        && note.onset * width / number_ticks <= coords[0]
                                        && coords[0] <= (note.onset + note.duration) * width_per_tick;
                        n++;
                    }

                    if (!existing_note ) {
                        if(!playLoudnessMode) {
                            bar_begin = closestX(coords[0]);
                            bar_end = bar_begin;
                        }
                    } else {
                        if(playLoudnessMode){
                            note.loudness = note.loudness % 8 + 1;
                            note_loudness.setImageResource(Default.loudness_icons[8-note.loudness]);
                            note_loudness.setVisibility(VISIBLE);
                        } else pattern.deleteNote(note);
                    }
                    invalidate();
                } else if(playLoudnessMode){
                    insertion_line = closestY(coords[1]);
                    boolean existing_note = false;
                    int n = 1;
                    while (n <= pattern.getNbOfNotes() && !existing_note) {
                        Pattern.setNoteSelected(n);
                        note = Pattern.getNoteSelected();
                        existing_note =
                                note.pitch == Default.max_midi_note - insertion_line
                                        && note.onset * width / number_ticks <= coords[0]
                                        && coords[0] <= (note.onset + note.duration) * width_per_tick;
                        n++;
                    }
                    if(existing_note) {
                        note_loudness.setImageResource(Default.loudness_icons[8-note.loudness]);
                        note_loudness.setVisibility(VISIBLE);
                        Log.i(TAG,"Should appear");
                    } else Log.i(TAG,"Missed");
                } else Log.i(TAG,"Dead");

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    if (!patternEditMode) {
                        //final float tx = pattern.mPosX;
                        //final float ty = pattern.mPosY;
                        pattern.mPosX += dx;
                        pattern.mPosY += dy;
                        /*if(!isBounded()){
                            pattern.mPosX = tx;
                            pattern.mPosY = ty;
                        }*/
                    } else {
                        coords[0] = x;
                        coords[1] = y;
                        mInverse.mapPoints(coords);
                        bar_end = closestX(coords[0]);
                    }
                    invalidate();
                }
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                if (0 <= bar_begin && bar_begin < bar_end + 1) {
                    final int start = bar_begin * getResolution();
                    final int finish = (bar_end + 1) * getResolution();
                    final int pitch = Default.max_midi_note - insertion_line;
                    int n = 1;
                    boolean non_overlap = true;
                    while (n <= pattern.getNbOfNotes() && non_overlap) {
                        Pattern.setNoteSelected(n);
                        Pattern.Note note = Pattern.getNoteSelected();
                        non_overlap = note.pitch != pitch
                                || (note.onset <= start && note.onset + note.duration <= start)
                                || (note.onset >= finish && note.onset + note.duration >= finish);
                        n++;
                    }
                    if (non_overlap) {
                        pattern.createNote(start, finish - start,
                                Default.max_midi_note - insertion_line, Default.default_loudness);
                    }
                }
                bar_begin = -1;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                invalidate();
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex =
                        (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private void drawBar(int p, Canvas canvas, int m) {
        switch (m % 12) {
            case 1:
            case 3:
            case 6:
            case 8:
            case 10:
                paint.setARGB(255, 9, 9, 9);
                break;
            default:
                paint.setARGB(255, 255, 255, 255);
        }
        final float margin = 1;
        canvas.drawRect((p - 1) * getResolution() * width_per_tick + margin
                , (m - 1) * height * line_height + margin
                , p * getResolution() * width_per_tick - margin
                , m * height * line_height - margin, paint);
        if (m == 1) {
            setStrokeWhite();
            canvas.drawText("" + p
                    , (p - 0.7f) * getResolution() * width_per_tick, coords[1]
                    , paint);
        }
    }
}
