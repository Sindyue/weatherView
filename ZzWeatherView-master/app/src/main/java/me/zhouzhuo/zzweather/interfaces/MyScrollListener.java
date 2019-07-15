package me.zhouzhuo.zzweather.interfaces;


import me.zhouzhuo.zzweather.view.ScrollViewWithRecycler;

/**
 * Created by uncle2 on 2016/7/27.
 */
public interface MyScrollListener {
    void onScrollChanged(ScrollViewWithRecycler scrollViewWithRecycler, int x, int y, int oldx, int oldy);

}
