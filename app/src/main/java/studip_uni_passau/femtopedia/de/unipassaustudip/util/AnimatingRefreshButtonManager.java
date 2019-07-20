package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class AnimatingRefreshButtonManager {

    private final MenuItem mRefreshItem;
    private final Animation mRotationAnimation;

    private boolean mIsRefreshInProgress = false;

    public AnimatingRefreshButtonManager(Context context, MenuItem refreshItem) {
        mRefreshItem = refreshItem;
        mRotationAnimation = AnimationUtils.loadAnimation(context, R.anim.spin_counterclockwise);

        mRotationAnimation.setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        if (!mIsRefreshInProgress)
                            stopAnimation();
                    }
                }
        );

        mRotationAnimation.setRepeatCount(Animation.INFINITE);
    }

    public void onRefreshBeginning() {
        if (mIsRefreshInProgress)
            return;
        mIsRefreshInProgress = true;

        stopAnimation();
        mRefreshItem.setActionView(R.layout.refresh_action_view);
        View actionView = mRefreshItem.getActionView();
        if (actionView != null)
            actionView.startAnimation(mRotationAnimation);
    }

    public void onRefreshComplete() {
        mIsRefreshInProgress = false;
    }

    private void stopAnimation() {
        View actionView = mRefreshItem.getActionView();
        if (actionView == null)
            return;
        actionView.clearAnimation();
        mRefreshItem.setActionView(null);
    }

}
