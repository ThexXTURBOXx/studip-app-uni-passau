package studip_uni_passau.femtopedia.de.unipassaustudip.preference.time;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class TimePreference extends DialogPreference {

    private int mTime;
    private int defaultTime;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getTime() {
        return mTime;
    }

    public void setTime(int time) {
        mTime = time;
        persistInt(time);
    }

    @Override
    public void setDefaultValue(Object defaultValue) {
        super.setDefaultValue(defaultValue);
        defaultTime = (int) defaultValue;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        defaultTime = a.getInt(index, defaultTime);
        return defaultTime;
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_time;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setTime(getPersistedInt(mTime));
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        setTime(restore ? getPersistedInt(mTime) : (int) defaultValue);
    }

}
