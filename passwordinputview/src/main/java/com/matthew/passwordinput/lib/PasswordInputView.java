package com.matthew.passwordinput.lib;

import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.Component.DrawTask;
import ohos.agp.components.Component.LayoutRefreshedListener;
import ohos.agp.components.Text.TextObserver;
import ohos.agp.components.TextField;
import ohos.agp.render.BlendMode;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.Path;
import ohos.agp.utils.Color;
import ohos.agp.utils.RectFloat;
import ohos.agp.utils.TextAlignment;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import com.hmos.compat.utils.AttrUtils;

/**
 * 密码、验证码输入框.
 *
 * @author matheew
 * @date 2019/6/24
 */
public class PasswordInputView extends TextField implements LayoutRefreshedListener, DrawTask, TextObserver {
    private static final HiLogLabel HI_LOG_LABEL = new HiLogLabel(0, 0, "PasswordInputView");
    private Paint paint;
    private int maxLength;
    private int borderColor;
    private int pwdColor;
    private int inputBorderColor;
    private int radius;
    private int spacing;
    private int pwdStyle;
    private int borderStyle;
    private String asterisk;
    private Path path;
    private RectFloat rectF;
    private BlendMode xfermode;
    private float strokeWidth;
    private float boxWidth;
    private int textLength = 0;
    private float[] linesArray = new float[12];
    private float[] radiusArray = new float[8];
    private Paint.FontMetrics metrics;
    private InputListener inputListener;

    /**
     * Border style.
     */
    public @interface BorderStyle {
        // Box.
        int BOX = 0;
        // Bottom line.
        int LINE = 1;
    }

    /**
     * Password style.
     */
    public @interface PwdStyle {
        // Dots.
        int CIRCLE = 0;
        // Asterisk.
        int ASTERISK = 1;
        // Plaintext.
        int PLAINTEXT = 2;
    }

    /**
     * PasswordInputView constructor.
     *
     * @param context Context
     */
    public PasswordInputView(Context context) {
        super(context);
        init(null);
        addDrawTask(this);
        setLayoutRefreshedListener(this);
        addTextObserver(this);
    }

    /**
     * PasswordInputView Constructor.
     *
     * @param context Context
     * @param attrs AttrSet
     */
    public PasswordInputView(Context context, AttrSet attrs) {
        super(context, attrs);
        init(attrs);
        addDrawTask(this);
        setLayoutRefreshedListener(this);
        addTextObserver(this);
    }

    /**
     * PasswordInputView constructor.
     *
     * @param context Context
     * @param attrs AttrSet
     * @param defStyleAttr String
     */
    public PasswordInputView(Context context, AttrSet attrs, String defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        addDrawTask(this);
        setLayoutRefreshedListener(this);
        addTextObserver(this);
    }

    private void init(AttrSet attrs) {
        initAttribute(attrs);
        textLength = getText() == null ? 0 : getText().length();
        paint = new Paint();
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setTextAlign(TextAlignment.CENTER);
        paint.setTextSize(getTextSize());
        metrics = paint.getFontMetrics();
        path = new Path();
        rectF = new RectFloat();
        xfermode = BlendMode.DST_OUT;
        this.setBackground(null);
        this.setTextCursorVisible(false);
        this.setMaxTextLines(1);
    }

    private void initAttribute(AttrSet attrs) {
        maxLength = AttrUtils.getIntFromAttr(attrs, "pwv_maxLength", 6);
        borderColor = AttrUtils.getColorFromAttr(attrs, "pwv_borderColor", Color.GRAY.getValue());
        pwdColor = AttrUtils.getColorFromAttr(attrs, "pwv_pwdColor", Color.GRAY.getValue());
        inputBorderColor = AttrUtils.getColorFromAttr(attrs, "pwv_haveInputBorderColor", borderColor);
        asterisk = AttrUtils.getStringFromAttr(attrs, "pwv_asterisk");
        if (asterisk == null || asterisk.length() == 0) {
            asterisk = "*";
        } else if (asterisk.length() > 1) {
            this.asterisk = asterisk.substring(0, 1);
        }
        radius = AttrUtils.getDimensionFromAttr(attrs, "pwv_radius", 0);
        strokeWidth = AttrUtils.getDimensionFromAttr(attrs, "pwv_strokeWidth", 2);
        spacing = AttrUtils.getDimensionFromAttr(attrs, "pwv_spacing", 0);
        borderStyle = getHandlepwvborderStyle(AttrUtils.getStringFromAttr(attrs, "pwv_borderStyle", ""));
        pwdStyle = getHandlepwvpwdStyle(AttrUtils.getStringFromAttr(attrs, "pwv_pwdStyle", ""));
    }

    @Override
    public void onRefreshed(Component component) {
        int w = component.getWidth();
        int h = component.getHeight();
        int availableWidth = w - getPaddingLeft() - getPaddingRight();
        int availableHeight = h - getPaddingTop() - getPaddingBottom();
        checkSpacing(availableWidth);
        checkRadius(availableWidth, availableHeight);
    }

    // Calculate boxWidth and check whether the rounded corner size is too large.
    private void checkRadius(int availableWidth, int availableHeight) {
        // The width of each box = (available width-interval width) / number of boxes.
        boxWidth = (availableWidth - (maxLength - 1f) * spacing) / maxLength;
        float availableRadius = Math.min(availableHeight / 2f, boxWidth / 2);
        if (radius > availableRadius) {
            HiLog.debug(HI_LOG_LABEL, "radius is too large, reset it");
            radius = (int) availableRadius;
        } else if (radius < 0) {
            radius = 0;
        }
    }

    // Check if the spacing is too large.
    private void checkSpacing(int availableWidth) {
        if (spacing < 0 || (maxLength - 1) * spacing >= availableWidth) {
            HiLog.debug(HI_LOG_LABEL, "spacing is too large, reset it to zero");
            spacing = 0;
        }
    }

    @Override
    public void onDraw(Component component, Canvas canvas) {
        // Remove the default drawing of EditText.
        int top = getPaddingTop();
        int bottom = getHeight() - getPaddingBottom();
        int start = getPaddingLeft();
        setMaxLength(maxLength);
        float left;
        for (int i = 0; i < maxLength; i++) {
            left = start + (boxWidth + spacing) * i;
            rectF.modify(left, top, left + boxWidth, bottom);
            drawBorder(canvas, i);
            if (i >= textLength) {
                continue;
            }
            drawPassword(canvas, i);
        }
    }

    private void drawBorder(Canvas canvas, int index) {
        Color hmosColor = PasswordInputView.changeParamToColor(index < textLength ? inputBorderColor : borderColor);
        paint.setColor(hmosColor);
        paint.setStyle(Paint.Style.STROKE_STYLE);
        switch (borderStyle) {
            case BorderStyle.BOX:  // Border mode
                if (radius == 0) {
                    // The rounded corner is 0, and the interval is judged.
                    // When the spacing is 0, draw the box first, and draw only the top, right,
                    // and bottom three sides for each of the following sides, avoiding repeated drawing of one side.
                    // If the spacing is not 0, draw the box directly.
                    if (spacing == 0) {
                        if (index == 0) {
                            canvas.drawRect(rectF, paint);
                        } else {
                            fillLinesArray();
                            canvas.drawLines(linesArray, paint);
                        }
                    } else {
                        canvas.drawRect(rectF, paint);
                    }
                } else {
                    // Fillet!=0.
                    // The strategy is the same as above, except that the rounded corners are added.
                    // If there is no spacing and rounded corners, only the first and last rounded corners are drawn.
                    drawBorderSpacing(canvas, index);
                }
                break;
            case BorderStyle.LINE: // Bottom edge
                canvas.drawLine(rectF.left, rectF.bottom, rectF.right, rectF.bottom, paint);
                break;
            default:
                HiLog.debug(HI_LOG_LABEL, "Default");
        }
    }

    private void drawBorderSpacing(Canvas canvas, int index) {
        if (spacing == 0) {
            if (index == 0) {
                fillRadiusArray(true);
                path.reset();
                path.addRoundRect(rectF, radiusArray, Path.Direction.COUNTER_CLOCK_WISE);
                canvas.drawPath(path, paint);
            } else if (index == maxLength - 1) {
                // Here draw the three sides of the last password box with rounded corners.
                // First draw a box with two rounded corners, and then synthesize with xfermode to remove the left side.
                final int layer = canvas.saveLayer(rectF, paint);
                fillRadiusArray(false);
                path.reset();
                path.addRoundRect(rectF, radiusArray, Path.Direction.COUNTER_CLOCK_WISE);
                canvas.drawPath(path, paint);
                paint.setBlendMode(xfermode);
                canvas.drawLine(rectF.left, rectF.top, rectF.left, rectF.bottom, paint);
                paint.setBlendMode(null);
                canvas.restoreToCount(layer);
            } else {
                fillLinesArray();
                canvas.drawLines(linesArray, paint);
            }
        } else {
            path.reset();
            path.addRoundRect(rectF, radius, radius, Path.Direction.COUNTER_CLOCK_WISE);
            canvas.drawPath(path, paint);
        }
    }

    private void drawPassword(Canvas canvas, int index) {
        Color hmosColor = PasswordInputView.changeParamToColor(pwdColor);
        paint.setColor(hmosColor);
        paint.setStyle(Paint.Style.FILL_STYLE);
        switch (pwdStyle) {
            case PwdStyle.CIRCLE: // Draw dots.
                canvas.drawCircle((rectF.left + rectF.right) / 2, (rectF.top + rectF.bottom) / 2, 8, paint);
                break;
            case PwdStyle.ASTERISK: // Draw asterisk.
                canvas.drawText(paint, asterisk, (rectF.left + rectF.right) / 2,
                        (rectF.top + rectF.bottom - metrics.ascent - metrics.descent) / 2);
                break;
            case PwdStyle.PLAINTEXT: // Draw plaintext.
                canvas.drawText(paint, String.valueOf(getText().charAt(index)), (rectF.left + rectF.right) / 2,
                        (rectF.top + rectF.bottom - metrics.ascent - metrics.descent) / 2);
                break;
            default:
                HiLog.debug(HI_LOG_LABEL, "drawPassword: Default case");
        }
        this.setTextColor(Color.TRANSPARENT);
    }

    // When the spacing is 0 and there are rounded corners, only the first and last rounded corners are drawn, and
    // the array of rounded corners is filled here.
    private void fillRadiusArray(boolean firstOrLast) {
        if (firstOrLast) {
            radiusArray[0] = radius;
            radiusArray[1] = radius;
            radiusArray[2] = 0;
            radiusArray[3] = 0;
            radiusArray[4] = 0;
            radiusArray[5] = 0;
            radiusArray[6] = radius;
            radiusArray[7] = radius;
        } else {
            radiusArray[0] = 0;
            radiusArray[1] = 0;
            radiusArray[2] = radius;
            radiusArray[3] = radius;
            radiusArray[4] = radius;
            radiusArray[5] = radius;
            radiusArray[6] = 0;
            radiusArray[7] = 0;
        }
    }

    // When the spacing is 0, the first box is drawn, and each box at the back will only draw the upper and
    // lower right sides. Here is the array to add these three sides.
    private void fillLinesArray() {
        linesArray[0] = rectF.left;
        linesArray[1] = rectF.top;
        linesArray[2] = rectF.right;
        linesArray[3] = rectF.top;
        linesArray[4] = rectF.right;
        linesArray[5] = rectF.top;
        linesArray[6] = rectF.right;
        linesArray[7] = rectF.bottom;
        linesArray[8] = rectF.right;
        linesArray[9] = rectF.bottom;
        linesArray[10] = rectF.left;
        linesArray[11] = rectF.bottom;
    }

    @Override
    public void onTextUpdated(String text, int start, int lengthBefore, int lengthAfter) {
        textLength = text.length();
        invalidate();
        if (textLength > maxLength) {
            setText(text.substring(0, maxLength));
            if (inputListener != null) {
                inputListener.onInputCompleted(text);
            }
        }
    }

    /**
     * Set the radius.
     *
     * @param radius radius
     */
    public void setRadius(int radius) {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        this.radius = radius;
        checkRadius(availableWidth, availableHeight);
        invalidate();
    }

    /**
     * Set the spacing.
     *
     * @param spacing spacing
     */
    public void setSpacing(int spacing) {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        this.spacing = spacing;
        checkSpacing(availableWidth);
        checkRadius(availableWidth, availableHeight);
        invalidate();
    }

    /**
     * Set the maximum length.
     *
     * @param maxLength maximum length
     */
    public void setMaxLength(int maxLength) {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        this.maxLength = maxLength;
        checkSpacing(availableWidth);
        checkRadius(availableWidth, availableHeight);
        invalidate();
    }

    /**
     * Set the border color.
     *
     * @param borderColor border color
     */
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    /**
     * Method to set the pwd color.
     *
     * @param pwdColor pwd color
     */
    public void setPwdColor(int pwdColor) {
        this.pwdColor = pwdColor;
        invalidate();
    }

    /**
     * Set the asterisk character.
     *
     * @param asterisk asterisk
     */
    public void setAsterisk(String asterisk) {
        if (asterisk == null || asterisk.length() == 0) {
            return;
        }
        if (asterisk.length() > 1) {
            this.asterisk = asterisk.substring(0, 1);
        } else {
            this.asterisk = asterisk;
        }
        invalidate();
    }

    /**
     * Set the pwd style.
     *
     * @param pwdStyle pwdStyle
     */
    public void setPwdStyle(@PwdStyle int pwdStyle) {
        this.pwdStyle = pwdStyle;
        invalidate();
    }

    /**
     * Set the border style.
     *
     * @param borderStyle border style
     */
    public void setBorderStyle(@BorderStyle int borderStyle) {
        this.borderStyle = borderStyle;
        invalidate();
    }

    /**
     * Set the stroke width.
     *
     * @param strokeWidth stroke width
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        invalidate();
    }

    /**
     * Set the input listener.
     *
     * @param inputListener inputListener
     */
    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    /**
     * InputListener interface.
     */
    public interface InputListener {
        void onInputCompleted(String text);
    }

    private static Color changeParamToColor(int color) {
        return new Color(color);
    }

    private int getHandlepwvborderStyle(String mode) {
        switch (mode) {
            case "line":
                return 1;
            case "box":
                return 0;
            default:
                HiLog.debug(HI_LOG_LABEL, "getHandlepwvborderStyle default case");
        }
        return BorderStyle.BOX;
    }

    private int getHandlepwvpwdStyle(String mode) {
        switch (mode) {
            case "asterisk":
                return 1;
            case "plaintext":
                return 2;
            case "circle":
                return 0;
            default:
                HiLog.debug(HI_LOG_LABEL, "getHandlepwvpwdStyle default switch case");
        }
        return PwdStyle.CIRCLE;
    }
}