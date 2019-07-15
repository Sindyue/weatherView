package me.zhouzhuo.zzweatherview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ZzWeatherView
 *
 * @author Zz
 * 2016/12/8 9:29
 */
public class ZzWeatherViewSimple extends HorizontalScrollView {

    private List<WeatherModel> list;
    private Paint dayPaint;
    private Paint nightPaint;

    protected Path pathDay;

    public static final int LINE_TYPE_CURVE = 1; //曲线

    private int lineType = LINE_TYPE_CURVE;
    private float lineWidth = 6f;

    private int dayLineColor = 0xff78ad23;
    private int nightLineColor = 0xff23acb3;

    private int columnNumber = 6;

    private OnWeatherItemClickListener weatherItemClickListener;

    public ZzWeatherViewSimple(Context context) {
        this(context, null);
    }

    public ZzWeatherViewSimple(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ZzWeatherViewSimple(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        dayPaint = new Paint();
        dayPaint.setColor(dayLineColor);
        dayPaint.setAntiAlias(true);
        dayPaint.setStrokeWidth(lineWidth);
        dayPaint.setStyle(Paint.Style.STROKE);

        nightPaint = new Paint();
        nightPaint.setColor(nightLineColor);
        nightPaint.setAntiAlias(true);
        nightPaint.setStrokeWidth(lineWidth);
        nightPaint.setStyle(Paint.Style.STROKE);

        pathDay = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getChildCount() > 0) {
            ViewGroup root = (ViewGroup) getChildAt(0);

            if (root.getChildCount() > 0) {
                float intensity = 0.16f;

                WeatherItemView c = (WeatherItemView) root.getChildAt(0);
                int dX = c.getTempX();
                int dY = c.getTempY();

                TemperatureView tv = c.findViewById(R.id.ttv_day);

                tv.setRadius(10);

                int x0 = (dX + tv.getxPointDay());
                int y0 = (dY + tv.getyPointDay());

                pathDay.reset();

                pathDay.moveTo(x0, y0);

                int lineSize = root.getChildCount();

                //曲线
                float prePreviousPointX = Float.NaN;
                float prePreviousPointY = Float.NaN;
                float previousPointX = Float.NaN;
                float previousPointY = Float.NaN;
                float currentPointX = Float.NaN;
                float currentPointY = Float.NaN;
                float nextPointX = Float.NaN;
                float nextPointY = Float.NaN;


                for (int i = 0; i < lineSize; i++) {

                    //Day
                    if (Float.isNaN(currentPointX)) {
                        WeatherItemView curWI = (WeatherItemView) root.getChildAt(i);
                        //todo 每条item 的宽度：curWI的宽度 curWI.getWidth()
                        int dayX = curWI.getTempX() + curWI.getWidth() * i;
                        int dayY = curWI.getTempY();

                        TemperatureView tempV = curWI.findViewById(R.id.ttv_day);
                        tempV.setRadius(10);

                        //day2
                        currentPointX = (dayX + tempV.getxPointDay());
                        currentPointY = (dayY + tempV.getyPointDay());
                    }


                    if (Float.isNaN(previousPointX)) {
                        if (i > 0) {
                            WeatherItemView preWI = (WeatherItemView) root.getChildAt(Math.max(i - 1, 0));

                            int dayX0 = preWI.getTempX() + preWI.getWidth() * (i - 1);
                            int dayY0 = preWI.getTempY();

                            TemperatureView tempV0 = preWI.findViewById(R.id.ttv_day);
                            tempV0.setRadius(10);

                            //day1
                            previousPointX = dayX0 + tempV0.getxPointDay();
                            previousPointY = dayY0 + tempV0.getyPointDay();

                        } else {
                            previousPointX = currentPointX;
                            previousPointY = currentPointY;
                        }
                    }

                    if (Float.isNaN(prePreviousPointX)) {
                        if (i > 1) {

                            WeatherItemView preWI = (WeatherItemView) root.getChildAt(Math.max(i - 2, 0));

                            int dayX0 = preWI.getTempX() + preWI.getWidth() * (i - 2);
                            int dayY0 = preWI.getTempY();
                            TemperatureView tempV0 = (TemperatureView) preWI.findViewById(R.id.ttv_day);
                            tempV0.setRadius(10);

                            //pre pre day
                            prePreviousPointX = (int) (dayX0 + tempV0.getxPointDay());
                            prePreviousPointY = (int) (dayY0 + tempV0.getyPointDay());


                        } else {
                            prePreviousPointX = previousPointX;
                            prePreviousPointY = previousPointY;
                        }
                    }

                    // nextPoint is always new one or it is equal currentPoint.
                    if (i < lineSize - 1) {

                        WeatherItemView nextWI = (WeatherItemView) root.getChildAt(Math.min(root.getChildCount() - 1, i + 1));

                        int dayX1 = nextWI.getTempX() + nextWI.getWidth() * (i + 1);
                        int dayY1 = nextWI.getTempY();

                        TemperatureView tempV1 = (TemperatureView) nextWI.findViewById(R.id.ttv_day);

                        tempV1.setRadius(10);
                        //day3
                        nextPointX = (int) (dayX1 + tempV1.getxPointDay());
                        nextPointY = (int) (dayY1 + tempV1.getyPointDay());

                    } else {
                        nextPointX = currentPointX;
                        nextPointY = currentPointY;
                    }


                    //Day and Night
                    if (i == 0) {
                        // Move to start point.
                        pathDay.moveTo(currentPointX, currentPointY);
                    } else {
                        // Calculate control points.
                        final float firstDiffX = (currentPointX - prePreviousPointX);
                        final float firstDiffY = (currentPointY - prePreviousPointY);
                        final float secondDiffX = (nextPointX - previousPointX);
                        final float secondDiffY = (nextPointY - previousPointY);
                        final float firstControlPointX = previousPointX + (intensity * firstDiffX);
                        final float firstControlPointY = previousPointY + (intensity * firstDiffY);
                        final float secondControlPointX = currentPointX - (intensity * secondDiffX);
                        final float secondControlPointY = currentPointY - (intensity * secondDiffY);
                        pathDay.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                                currentPointX, currentPointY);
                    }

                    // Shift values by one back to prevent recalculation of values that have
                    // been already calculated.
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                    currentPointX = nextPointX;
                    currentPointY = nextPointY;
                }

                canvas.drawPath(pathDay, dayPaint);
            }
        }
    }


    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        Log.i("wyy", "onScrollChanged: x = " + x + ", y = " + y + ", oldx = " + oldx + " , oldy = " + oldy);
        super.onScrollChanged(x, y, oldx, oldy);
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        dayPaint.setStrokeWidth(lineWidth);
        nightPaint.setStrokeWidth(lineWidth);
        invalidate();
    }

    public void setDayLineColor(int color) {
        this.dayLineColor = color;
        dayPaint.setColor(dayLineColor);
        invalidate();
    }

    public void setNightLineColor(int color) {
        this.nightLineColor = color;
        nightPaint.setColor(nightLineColor);
        invalidate();
    }

    public void setDayAndNightLineColor(int dayColor, int nightColor) {
        this.dayLineColor = dayColor;
        this.nightLineColor = nightColor;
        dayPaint.setColor(dayLineColor);
        nightPaint.setColor(nightLineColor);
        invalidate();
    }

    public List<WeatherModel> getList() {
        return list;
    }

    public void setOnWeatherItemClickListener(OnWeatherItemClickListener weatherItemClickListener) {
        this.weatherItemClickListener = weatherItemClickListener;
    }

    public void setList(final List<WeatherModel> list) {
        this.list = list;
        int screenWidth = getScreenWidth();
        int maxDay = getMaxDayTemp(list);
        int maxNight = getMaxNightTemp(list);
        int minDay = getMinDayTemp(list);
        int minNight = getMinNightTemp(list);
        int max = maxDay > maxNight ? maxDay : maxNight;
        int min = minDay < minNight ? minDay : minNight;
        removeAllViews();
        LinearLayout llRoot = new LinearLayout(getContext());
        llRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        llRoot.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < list.size(); i++) {
            WeatherModel model = list.get(i);
            final WeatherItemView itemView = new WeatherItemView(getContext());
            itemView.setMaxTemp(max);
            itemView.setMinTemp(min);
            itemView.setDate(model.getDate());
            itemView.setWeek(model.getWeek());
            itemView.setDayTemp(model.getDayTemp());
            itemView.setDayWeather(model.getDayWeather());
            if (model.getDayPic() == 0) {
                if (model.getDayWeather() != null) {
                    itemView.setDayImg(PicUtil.getDayWeatherPic(model.getDayWeather()));
                }
            } else {
                itemView.setDayImg(model.getDayPic());
            }
            itemView.setNightWeather(model.getNightWeather());
            itemView.setNightTemp(model.getNightTemp());
            if (model.getNightPic() == 0) {
                if (model.getNightWeather() != null) {
                    itemView.setNightImg(PicUtil.getNightWeatherPic(model.getNightWeather()));
                }
            } else {
                itemView.setNightImg(model.getNightPic());
            }
            itemView.setWindOri(model.getWindOrientation());
            itemView.setWindLevel(model.getWindLevel());
            itemView.setAirLevel(model.getAirLevel());
            itemView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / columnNumber, ViewGroup.LayoutParams.WRAP_CONTENT));
            itemView.setClickable(true);
            final int finalI = i;
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (weatherItemClickListener != null) {
                        weatherItemClickListener.onItemClick(itemView, finalI, list.get(finalI));
                    }
                }
            });
            llRoot.addView(itemView);
        }
        addView(llRoot);
        invalidate();
    }

    public void setColumnNumber(int num) throws Exception {
        if (num > 2) {
            this.columnNumber = num;
            setList(this.list);
        } else {
            throw new Exception("ColumnNumber should lager than 2");
        }
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    private int getMinDayTemp(List<WeatherModel> list) {
        if (list != null) {
            return Collections.min(list, new DayTempComparator()).getDayTemp();
        }
        return 0;
    }

    private int getMinNightTemp(List<WeatherModel> list) {
        if (list != null) {
            return Collections.min(list, new NightTempComparator()).getNightTemp();
        }
        return 0;
    }


    private int getMaxNightTemp(List<WeatherModel> list) {
        if (list != null) {
            return Collections.max(list, new NightTempComparator()).getNightTemp();
        }
        return 0;
    }

    private int getMaxDayTemp(List<WeatherModel> list) {
        if (list != null) {
            return Collections.max(list, new DayTempComparator()).getDayTemp();
        }
        return 0;
    }

    public int getLineType() {
        return lineType;
    }

    public void setLineType(int lineType) {
        this.lineType = lineType;
        invalidate();
    }

    private static class DayTempComparator implements Comparator<WeatherModel> {

        @Override
        public int compare(WeatherModel o1, WeatherModel o2) {
            if (o1.getDayTemp() == o2.getDayTemp()) {
                return 0;
            } else if (o1.getDayTemp() > o2.getDayTemp()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private static class NightTempComparator implements Comparator<WeatherModel> {

        @Override
        public int compare(WeatherModel o1, WeatherModel o2) {
            if (o1.getNightTemp() == o2.getNightTemp()) {
                return 0;
            } else if (o1.getNightTemp() > o2.getNightTemp()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public interface OnWeatherItemClickListener {
        void onItemClick(WeatherItemView itemView, int position, WeatherModel weatherModel);
    }

}
