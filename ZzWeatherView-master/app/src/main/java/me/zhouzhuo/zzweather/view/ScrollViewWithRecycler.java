package me.zhouzhuo.zzweather.view;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import me.zhouzhuo.zzweather.interfaces.MyScrollListener;


/**
 * Created by uncle2 on 2016/7/25.
 * 用于嵌套RecyclerView的Scrollview类(保持惯性滑动)
 */
public class ScrollViewWithRecycler extends NestedScrollView implements MyScrollListener {

    private int downY;
    private int mTouchSlop;

    private MyScrollListener myScrollListener = null;


    public ScrollViewWithRecycler(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public ScrollViewWithRecycler(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public ScrollViewWithRecycler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //上划到顶时允许父控件截断滑动事件
                if (getScrollY() == 0) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) e.getRawY();
                if (Math.abs(moveY - downY) > mTouchSlop) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(e);
    }

    public void setScrollViewListener(MyScrollListener myScrollListener) {
        this.myScrollListener = myScrollListener;
    }


    @Override
    public void onScrollChanged(ScrollViewWithRecycler scrollViewWithRecycler, int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (myScrollListener != null) {
            myScrollListener.onScrollChanged(this, x, y, oldx, oldy);
        }

    }
}
