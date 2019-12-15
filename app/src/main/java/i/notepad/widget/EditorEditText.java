package i.notepad.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import i.notepad.util.MathUtils;

/**
 * 自定义的编辑框{@code EditText}组件
 *
 * @author 511721050589
 */

public class EditorEditText extends EditText {

    private final Rect mRect;
    private final Paint mPaint;

    public EditorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        // 去除默认的下划线
        setBackground(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Rect r = mRect;
        Paint paint = mPaint;

        paint.setColor(Color.GRAY);
        paint.setTextSize(getLineHeight() / 2);
        int count = getLineCount();
        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, r);
            int showLineNum = i + 1;
            // 绘画行线
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            // 绘画居中行号
            canvas.drawText(String.valueOf(showLineNum),
                    MathUtils.average(r.left, r.right) - paint.getTextSize() * MathUtils.getDigitLength(showLineNum) / 4,
                    baseline,
                    paint);
        }

        /* 绘画边框 */
        paint.setColor(Color.RED);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            int sy = getScrollY(), w = getWidth(), h = getHeight() + sy;
            canvas.drawLine(0, sy, w - 1, sy, paint);
            canvas.drawLine(0, sy, 0, h - 1, paint);
            canvas.drawLine(w - 1, h - 1, w - 1, sy, paint);
            canvas.drawLine(w - 1, h - 1, 0, h - 1, paint);
        } else {
            if (getLocalVisibleRect(r)) {
                r.inset(1, 1);
                canvas.drawRect(r, paint);
            }
        }

        super.onDraw(canvas);
    }
}
