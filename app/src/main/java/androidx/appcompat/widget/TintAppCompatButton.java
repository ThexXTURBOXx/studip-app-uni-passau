package androidx.appcompat.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class TintAppCompatButton extends AppCompatButton {

    private static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;

    public TintAppCompatButton(Context context) {
        super(context);
    }

    public TintAppCompatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, androidx.appcompat.R.attr.buttonStyle);
    }

    public TintAppCompatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        boolean isRequired = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M;
        if (!isRequired) {
            return;
        }

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TintAppCompatButton,
                defStyleAttr, 0);

        try {
            PorterDuff.Mode tintMode = DrawableUtils.parseTintMode(
                    a.getInt(R.styleable.TintAppCompatButton_drawableTintModeCompat, -1),
                    DEFAULT_TINT_MODE);

            boolean hasColor = a.hasValue(R.styleable.TintAppCompatButton_drawableTintCompat);
            if (hasColor) {
                int color = a.getColor(R.styleable.TintAppCompatButton_drawableTintCompat, Color.TRANSPARENT);

                for (Drawable d : getCompoundDrawables())
                    tint(d, color, tintMode);
                for (Drawable d : getCompoundDrawablesRelative())
                    tint(d, color, tintMode);
            }
        } finally {
            a.recycle();
        }
    }

    private void tint(Drawable d, int color, PorterDuff.Mode tintMode) {
        boolean isTintable = d != null && tintMode != null;
        if (!isTintable) {
            return;
        }
        TintInfo ti = new TintInfo();
        ti.mTintMode = tintMode;
        ti.mTintList = ColorStateList.valueOf(color);
        ti.mHasTintList = true;
        ti.mHasTintMode = true;

        AppCompatDrawableManager.tintDrawable(d, ti, new int[]{0});
    }

}
