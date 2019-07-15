package me.zhouzhuo.zzweather.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import java.util.List;

/**
 * author : wyy
 * time   : 2019/7/11
 * desc   :
 */
public class MiuiScrollView extends HorizontalScrollView {

    private MojiWeatherView view;

    public MiuiScrollView(Context context) {
        super(context);
    }

    public MiuiScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        if (view != null) {
            view.setScrollPos(l);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setData(List<WeatherBean> data, Context context) {
        removeAllViews();
        view = new MojiWeatherView(context);
        view.setData(data);
        addView(view);
        invalidate();
    }
}
