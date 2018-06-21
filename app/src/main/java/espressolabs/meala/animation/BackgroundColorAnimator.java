package espressolabs.meala.animation;

import android.view.View;

public class BackgroundColorAnimator extends ColorAnimator {

    public BackgroundColorAnimator(final View target, int... values) {
        super(values);

        addUpdateListener(valueAnimator -> {
            int value = (int) valueAnimator.getAnimatedValue();
            target.setBackgroundColor(value);
        });

    }
}
