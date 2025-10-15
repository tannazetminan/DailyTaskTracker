package com.tannazetm.dailytasktracker.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.tannazetm.dailytasktracker.R;

public class TimeDistributionChart extends View {

    private Paint lowPriorityPaint;
    private Paint mediumPriorityPaint;
    private Paint highPriorityPaint;
    private Paint textPaint;
    private Paint labelPaint;

    private int lowPriorityTime = 0;
    private int mediumPriorityTime = 0;
    private int highPriorityTime = 0;

    private RectF lowRect = new RectF();
    private RectF mediumRect = new RectF();
    private RectF highRect = new RectF();

    public TimeDistributionChart(Context context) {
        super(context);
        init(context);
    }

    public TimeDistributionChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeDistributionChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Initialize paints for each priority level
        lowPriorityPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lowPriorityPaint.setColor(ContextCompat.getColor(context, R.color.completed));
        lowPriorityPaint.setStyle(Paint.Style.FILL);

        mediumPriorityPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mediumPriorityPaint.setColor(ContextCompat.getColor(context, R.color.accent));
        mediumPriorityPaint.setStyle(Paint.Style.FILL);

        highPriorityPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highPriorityPaint.setColor(ContextCompat.getColor(context, R.color.overdue));
        highPriorityPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int lowTime, int mediumTime, int highTime) {
        this.lowPriorityTime = lowTime;
        this.mediumPriorityTime = mediumTime;
        this.highPriorityTime = highTime;
        invalidate(); // Trigger redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int totalTime = lowPriorityTime + mediumPriorityTime + highPriorityTime;

        if (totalTime == 0) {
            // Draw empty state
            String emptyText = "No time tracked yet";
            float textX = width / 2f;
            float textY = height / 2f;
            canvas.drawText(emptyText, textX, textY, labelPaint);
            return;
        }

        // Calculate bar widths
        int padding = 40;
        int barSpacing = 60;
        int barWidth = (width - (2 * padding) - (2 * barSpacing)) / 3;
        int maxBarHeight = height - 100; // Leave space for labels

        // Calculate maximum time for scaling
        int maxTime = Math.max(lowPriorityTime, Math.max(mediumPriorityTime, highPriorityTime));

        // Calculate bar heights proportionally
        float lowHeight = maxTime > 0 ? ((float) lowPriorityTime / maxTime) * maxBarHeight : 0;
        float mediumHeight = maxTime > 0 ? ((float) mediumPriorityTime / maxTime) * maxBarHeight : 0;
        float highHeight = maxTime > 0 ? ((float) highPriorityTime / maxTime) * maxBarHeight : 0;

        // Ensure minimum height for visibility
        if (lowPriorityTime > 0 && lowHeight < 30) lowHeight = 30;
        if (mediumPriorityTime > 0 && mediumHeight < 30) mediumHeight = 30;
        if (highPriorityTime > 0 && highHeight < 30) highHeight = 30;

        int baseY = height - 50; // Base line for bars

        // Draw Low Priority bar
        int lowX = padding;
        lowRect.set(lowX, baseY - lowHeight, lowX + barWidth, baseY);
        canvas.drawRoundRect(lowRect, 12f, 12f, lowPriorityPaint);

        // Draw time label on bar
        if (lowPriorityTime > 0) {
            String timeText = formatTime(lowPriorityTime);
            canvas.drawText(timeText, lowX + barWidth / 2f, baseY - lowHeight - 10, textPaint);
        }

        // Draw "Low" label
        canvas.drawText("Low", lowX + barWidth / 2f, baseY + 35, labelPaint);

        // Draw Medium Priority bar
        int mediumX = lowX + barWidth + barSpacing;
        mediumRect.set(mediumX, baseY - mediumHeight, mediumX + barWidth, baseY);
        canvas.drawRoundRect(mediumRect, 12f, 12f, mediumPriorityPaint);

        if (mediumPriorityTime > 0) {
            String timeText = formatTime(mediumPriorityTime);
            canvas.drawText(timeText, mediumX + barWidth / 2f, baseY - mediumHeight - 10, textPaint);
        }

        canvas.drawText("Medium", mediumX + barWidth / 2f, baseY + 35, labelPaint);

        // Draw High Priority bar
        int highX = mediumX + barWidth + barSpacing;
        highRect.set(highX, baseY - highHeight, highX + barWidth, baseY);
        canvas.drawRoundRect(highRect, 12f, 12f, highPriorityPaint);

        if (highPriorityTime > 0) {
            String timeText = formatTime(highPriorityTime);
            canvas.drawText(timeText, highX + barWidth / 2f, baseY - highHeight - 10, textPaint);
        }

        canvas.drawText("High", highX + barWidth / 2f, baseY + 35, labelPaint);
    }

    private String formatTime(int minutes) {
        if (minutes < 60) {
            return minutes + "m";
        } else {
            int hours = minutes / 60;
            int mins = minutes % 60;
            if (mins == 0) {
                return hours + "h";
            } else {
                return hours + "h\n" + mins + "m";
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Default size
        int desiredWidth = 300;
        int desiredHeight = 200;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        // Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }
}