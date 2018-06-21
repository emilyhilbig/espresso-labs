package espressolabs.meala.animation;

import android.animation.ValueAnimator;
import android.view.View;

public class LayoutParamHeightAnimator extends ValueAnimator {

    public LayoutParamHeightAnimator(final View target, int... values) {
        setIntValues(values);

        addUpdateListener(valueAnimator -> {
            target.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
            target.requestLayout();
        });
    }

    public static LayoutParamHeightAnimator collapse(View target) {
        return new LayoutParamHeightAnimator(target, target.getHeight(), 0);
    }

}
