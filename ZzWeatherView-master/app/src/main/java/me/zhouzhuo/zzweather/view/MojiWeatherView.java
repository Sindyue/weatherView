package me.zhouzhuo.zzweather.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.zhouzhuo.zzweather.R;
import me.zhouzhuo.zzweather.other.LineView;

import static android.content.ContentValues.TAG;

/**
 * Created by ccy on 2017-07-28.
 */

public class MojiWeatherView extends View {
    private static final String TAG = "MojiWeatherView";

    private static int DEFAULT_BULE = 0XFF00BFFF;
    private static int DEFAULT_GRAY = Color.GRAY;

    private int backgroundColor;
    private int minViewHeight; //控件的最低高度
    private int minPointHeight;//折线最低点的高度
    private int lineInterval; //折线线段长度
    private float pointRadius; //折线点的半径
    private float textSize; //字体大小
    private float pointGap; //折线单位高度差
    private int defaultPadding; //折线坐标图四周留出来的偏移量
    private float iconWidth;  //天气图标的边长
    private int viewHeight;
    private int viewWidth;
    private int screenWidth;

    private Paint linePaint; //线画笔
    private Paint textPaint; //文字画笔
    private Paint circlePaint; //圆点画笔

    private List<WeatherBean> data = new ArrayList<>(); //元数据
    private List<Pair<Integer, String>> weatherDatas = new ArrayList<>();  //对元数据中天气分组后的集合
    private List<Float> dashDatas = new ArrayList<>(); //不同天气之间虚线的x坐标集合
    private List<PointF> points = new ArrayList<>(); //折线拐点的集合
    private Map<String, Bitmap> icons = new HashMap<>(); //天气图标集合
    private int maxTemperature;//元数据中的最高和最低温度
    private int minTemperature;

    private int scrollPos = 0;  //当前滚动的距离

    public MojiWeatherView(Context context) {
        this(context, null);
    }

    public MojiWeatherView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MojiWeatherView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MiuiWeatherView);
        minPointHeight = (int) ta.getDimension(R.styleable.MiuiWeatherView_min_point_height, dp2pxF(context, 60));
        lineInterval = (int) ta.getDimension(R.styleable.MiuiWeatherView_line_interval, dp2pxF(context, 60));
        backgroundColor = ta.getColor(R.styleable.MiuiWeatherView_background_color, Color.WHITE);
        ta.recycle();

        setBackgroundColor(backgroundColor);

        initSize(context);

        initPaint(context);

        initIcons();

    }

    /**
     * 初始化默认数据
     */
    private void initSize(Context c) {
        screenWidth = getResources().getDisplayMetrics().widthPixels;

        minViewHeight = 3 * minPointHeight;  //默认3倍
        pointRadius = dp2pxF(c, 2.5f);
        textSize = sp2pxF(c, 10);
        defaultPadding = (int) (0.5 * minPointHeight);  //默认0.5倍
        iconWidth = (1.0f / 3.0f) * lineInterval; //默认1/3倍
    }

    /**
     * 计算折线单位高度差
     */
    private void calculatePontGap() {
        int lastMaxTem = -Integer.MAX_VALUE;
        int lastMinTem = Integer.MAX_VALUE;
        for (WeatherBean bean : data) {
            if (bean.temperature > lastMaxTem) {
                maxTemperature = bean.temperature;
                lastMaxTem = bean.temperature;
            }
            if (bean.temperature < lastMinTem) {
                minTemperature = bean.temperature;
                lastMinTem = bean.temperature;
            }
        }
        float gap = (maxTemperature - minTemperature) * 1.0f;
        gap = (gap == 0.0f ? 1.0f : gap);  //保证分母不为0
        pointGap = (viewHeight - minPointHeight - 2 * defaultPadding) / gap;
    }

    private void initPaint(Context c) {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(dp2px(c, 1));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStrokeWidth(dp2pxF(c, 1));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSize(getContext());
        calculatePontGap();
    }

    /**
     * 公开方法，用于设置元数据
     *
     * @param data
     */
    public void setData(List<WeatherBean> data) {
        if (data == null) {
            return;
        }
        this.data = data;
        notifyDataSetChanged();
    }

    public List<WeatherBean> getData() {
        return data;
    }

    public void notifyDataSetChanged() {
        if (data == null) {
            return;
        }
        weatherDatas.clear();
        points.clear();
        dashDatas.clear();

        initWeatherMap(); //初始化相邻的相同天气分组
        requestLayout();
        invalidate();
    }


    /**
     * 初始化天气图标集合
     * （涉及解析、缩放等耗时操作，故不要在ondraw里再去获取图片，提前解析好放在集合里)
     */
    private void initIcons() {
        icons.clear();
        String[] weathers = WeatherBean.getAllWeathers();
        for (int i = 0; i < weathers.length; i++) {
            Bitmap bmp = getWeatherIcon(weathers[i], iconWidth, iconWidth);
            icons.put(weathers[i], bmp);
        }
    }

    /**
     * 根据元数据中连续相同的天气数做分组,
     * pair中的first值为连续相同天气的数量，second值为对应天气
     */
    private void initWeatherMap() {
        weatherDatas.clear();
        String lastWeather = "";
        int count = 0;
        for (int i = 0; i < data.size(); i++) {
            WeatherBean bean = data.get(i);
            if (i == 0) {
                lastWeather = bean.weather;
            }
            if (bean.weather != lastWeather) {
                Pair<Integer, String> pair = new Pair<>(count, lastWeather);
                weatherDatas.add(pair);
                count = 1;
            } else {
                count++;
            }
            lastWeather = bean.weather;

            if (i == data.size() - 1) {
                Pair<Integer, String> pair = new Pair<>(count, lastWeather);
                weatherDatas.add(pair);
            }
        }

        for (int i = 0; i < weatherDatas.size(); i++) {
            int c = weatherDatas.get(i).first;
            String w = weatherDatas.get(i).second;
            Log.d("ccy", "weatherMap i =" + i + ";count = " + c + ";weather = " + w);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            viewHeight = Math.max(heightSize, minViewHeight);
        } else {
            viewHeight = minViewHeight;
        }

        int totalWidth = 0;
        if (data.size() > 1) {
            totalWidth = 2 * defaultPadding + lineInterval * data.size();
        }
        viewWidth = Math.max(screenWidth, totalWidth);  //默认控件最小宽度为屏幕宽度

        setMeasuredDimension(viewWidth, viewHeight);
        calculatePontGap();
        Log.d("ccy", "viewHeight = " + viewHeight + ";viewWidth = " + viewWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data.isEmpty()) {
            return;
        }
        drawAxis(canvas);

//        drawLinesAndPoints(canvas);
        drawLinesAndPointsNew(canvas);

//        drawTemperature(canvas);

        drawWeatherDash(canvas);

        drawWeatherIcon(canvas);

        drawTempWeatherInfo(canvas);

    }

    /**
     * 画时间轴
     *
     * @param canvas
     */
    private void drawAxis(Canvas canvas) {
        canvas.save();
        linePaint.setColor(DEFAULT_GRAY);
        linePaint.setStrokeWidth(dp2px(getContext(), 1));

        canvas.drawLine(defaultPadding,
                viewHeight - defaultPadding,
                viewWidth - defaultPadding,
                viewHeight - defaultPadding,
                linePaint);

        float centerY = viewHeight - defaultPadding + dp2pxF(getContext(), 15);
        float centerX;
        for (int i = 0; i < data.size(); i++) {
            String text = data.get(i).time;
            centerX = defaultPadding + i * lineInterval;
            Paint.FontMetrics m = textPaint.getFontMetrics();
            canvas.drawText(text, 0, text.length(), centerX, centerY - (m.ascent + m.descent) / 2, textPaint);
        }
        canvas.restore();
    }

    /**
     * 画折线和它拐点的园
     *
     * @param canvas
     */
    private void drawLinesAndPoints(Canvas canvas) {
        canvas.save();
        linePaint.setColor(DEFAULT_BULE);
        linePaint.setStrokeWidth(dp2pxF(getContext(), 1));
        linePaint.setStyle(Paint.Style.STROKE);

        Path linePath = new Path(); //用于绘制折线
        points.clear();
        int baseHeight = defaultPadding + minPointHeight;
        float centerX;
        float centerY;
        Point pStart = new Point();
        Point pEnd = new Point();
        Path path = new Path();

        for (int i = 0; i < data.size() - 1; i++) {
            int tem = data.get(i).temperature;
            int temNext = data.get(i + 1).temperature;
            tem = tem - minTemperature;
            temNext = temNext - minTemperature;
            centerY = (int) (viewHeight - (baseHeight + tem * pointGap));
            centerX = defaultPadding + i * lineInterval;

            if (i == 0) {
                linePath.moveTo(centerX, centerY);
            } else {
                linePath.lineTo(centerX, centerY);
            }

            pStart.x = defaultPadding + i * lineInterval;
            pStart.y = (int) (viewHeight - (baseHeight + tem * pointGap));

            pEnd.x = defaultPadding + (i + 1) * lineInterval;
            pEnd.y = (int) (viewHeight - (baseHeight + temNext * pointGap));

            points.add(new PointF(pStart.x, pStart.y));

            //把最后的点加入进来
            if (i == data.size() - 2) {
                points.add(new PointF(pEnd.x, pEnd.y));
            }

            Point point3 = new Point();
//            Point point4 = new Point();
            float wd = (pStart.x + pEnd.x) / 2;
            point3.x = (int) wd;
            point3.y = pStart.y;
//            point4.x = (int) wd;
//            point4.y = pEnd.y;

            if (i == 0) {
                path.moveTo(pStart.x, pStart.y);
            }

            path.quadTo(point3.x, point3.y, pEnd.x, pEnd.y);

//            path.cubicTo(point3.x, point3.y, point4.x, point4.y, pEnd.x, pEnd.y);
        }
//        canvas.drawPath(linePath, linePaint); //画出折线
        canvas.drawPath(path, linePaint);

        //接下来画折线拐点的园
//        float x, y;
//        for (int i = 0; i < points.size(); i++) {
//            x = points.get(i).x;
//            y = points.get(i).y;
//
//            //先画一个颜色为背景颜色的实心园覆盖掉折线拐角
//            circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
//            circlePaint.setColor(backgroundColor);
//            canvas.drawCircle(x, y,
//                    pointRadius + dp2pxF(getContext(), 1),
//                    circlePaint);
//            //再画出正常的空心园
//            circlePaint.setStyle(Paint.Style.STROKE);
//            circlePaint.setColor(DEFAULT_BULE);
//            canvas.drawCircle(x, y,
//                    pointRadius,
//                    circlePaint);
//        }
        canvas.restore();
    }

    /**
     * 画折线和它拐点的园
     *
     * @param canvas
     */
    private void drawLinesAndPointsNew(Canvas canvas) {
        canvas.save();
        linePaint.setColor(DEFAULT_BULE);
        linePaint.setStrokeWidth(dp2pxF(getContext(), 1));
        linePaint.setStyle(Paint.Style.STROKE);

        Path linePath = new Path(); //用于绘制折线
        points.clear();
        int baseHeight = defaultPadding + minPointHeight;
        float centerX;
        float centerY;
        Point pStart = new Point();
        Point pEnd = new Point();
        Path path = new Path();

        for (int i = 0; i < data.size(); i++) {
            int tem = data.get(i).temperature;
            int temNext;
            if (i == data.size() - 1) {
                temNext = 2 * data.get(i).temperature - data.get(i - 1).temperature;
            } else {
                temNext = data.get(i + 1).temperature;
            }
            tem = tem - minTemperature;
            temNext = temNext - minTemperature;


            centerY = (int) (viewHeight - (baseHeight + tem * pointGap));
            centerX = defaultPadding + i * lineInterval;

            if (i == 0) {
                linePath.moveTo(centerX, centerY);
            } else {
                linePath.lineTo(centerX, centerY);
            }

            pStart.x = defaultPadding + i * lineInterval;
            pStart.y = (int) (viewHeight - (baseHeight + tem * pointGap));

            pEnd.x = defaultPadding + (i + 1) * lineInterval;
            pEnd.y = (int) (viewHeight - (baseHeight + temNext * pointGap));

            points.add(new PointF(pStart.x, pStart.y));

            //把最后的点加入进来
            if (i == data.size() - 1) {
                points.add(new PointF(pEnd.x, pEnd.y));
            }

            Point point3 = new Point();
//            Point point4 = new Point();
            float wd = (pStart.x + pEnd.x) / 2;
            point3.x = (int) wd;
            point3.y = pStart.y;
//            point4.x = (int) wd;
//            point4.y = pEnd.y;

            if (i == 0) {
                path.moveTo(pStart.x, pStart.y);
            }

            path.quadTo(point3.x, point3.y, pEnd.x, pEnd.y);

//            path.cubicTo(point3.x, point3.y, point4.x, point4.y, pEnd.x, pEnd.y);
        }
//        canvas.drawPath(linePath, linePaint); //画出折线
        canvas.drawPath(path, linePaint);

        //接下来画折线拐点的园
        canvas.restore();
    }

    /**
     * 画温度描述值
     *
     * @param canvas
     */
    private void drawTemperature(Canvas canvas) {
        canvas.save();

        textPaint.setTextSize(1.2f * textSize); //字体放大一丢丢
        float centerX;
        float centerY;
        String text;
        for (int i = 0; i < points.size(); i++) {
            text = data.get(i).temperatureStr;
            centerX = points.get(i).x;
            centerY = points.get(i).y - dp2pxF(getContext(), 13);
            Paint.FontMetrics metrics = textPaint.getFontMetrics();
            canvas.drawText(text,
                    centerX,
                    centerY - (metrics.ascent + metrics.descent) / 2,
                    textPaint);
        }
        textPaint.setTextSize(textSize);
        canvas.restore();
    }

    /**
     * 画不同天气之间的虚线
     *
     * @param canvas
     */
    private void drawWeatherDash(Canvas canvas) {
        canvas.save();
        linePaint.setColor(DEFAULT_GRAY);
        linePaint.setStrokeWidth(dp2pxF(getContext(), 0.5f));
        linePaint.setAlpha(0xcc);

        //设置画笔画出虚线
        float[] f = {dp2pxF(getContext(), 5), dp2pxF(getContext(), 1)};  //两个值分别为循环的实线长度、空白长度
        PathEffect pathEffect = new DashPathEffect(f, 0);
        linePaint.setPathEffect(pathEffect);

        dashDatas.clear();
        int interval = 0;
        float startX, startY, endX, endY;
        endY = viewHeight - defaultPadding;

        //0坐标点的虚线手动画上
        canvas.drawLine(defaultPadding,
                points.get(0).y + dp2pxF(getContext(), 2),//+ pointRadius
                defaultPadding,
                endY,
                linePaint);
        dashDatas.add((float) defaultPadding);

        for (int i = 0; i < weatherDatas.size(); i++) {
            interval += weatherDatas.get(i).first;
            Log.i(TAG + "11", "drawWeatherDash: i = " + i + ", interval = " + interval +
                    ", weatherDatas.get(i).first = " + weatherDatas.get(i).first);
            if (interval > points.size() - 1) {
                interval = points.size() - 1;
            }

            startX = endX = defaultPadding + interval * lineInterval;
            startY = points.get(interval).y + dp2pxF(getContext(), 2);//+ pointRadius
            Log.i(TAG + "11", "drawWeatherDash: startX = " + startX + ", startY = " + startY);
            dashDatas.add(startX);

            //绘制的是该点的右边线
            canvas.drawLine(startX, startY, endX, endY, linePaint);
        }

        //这里注意一下，当最后一组的连续天气数为1时，是不需要计入虚线集合的，否则会多画一个天气图标
        //若不理解，可尝试去掉下面这块代码并观察运行效果
//        if (weatherDatas.get(weatherDatas.size() - 1).first == 1
//                && dashDatas.size() > 1) {
//            dashDatas.remove(dashDatas.get(dashDatas.size() - 1));
//        }

        linePaint.setPathEffect(null);
        linePaint.setAlpha(0xff);
        canvas.restore();
    }

    /**
     * 画天气图标和它下方文字
     * 若相邻虚线都在屏幕内，图标的x位置即在两虚线的中间
     * 若有一条虚线在屏幕外，图标的x位置即在屏幕边沿到另一条虚线的中间
     * 若两条都在屏幕外，图标x位置紧贴某一条虚线或屏幕中间
     *
     * @param canvas
     */
    private void drawWeatherIcon(Canvas canvas) {
        canvas.save();
        textPaint.setTextSize(0.9f * textSize); //字体缩小一丢丢

        boolean leftUsedScreenLeft = false;

        int scrollX = getScrollX();  //范围控制在0 ~ viewWidth-screenWidth

        //外套 ScrollView 的版本
        if (scrollPos > 0) {
            scrollX = scrollPos;
        }

        float left, right;
        float iconX, iconY;
        float textY;     //文字的x坐标跟图标是一样的，无需额外声明
        iconY = viewHeight - (defaultPadding + minPointHeight / 2.0f);
        textY = iconY + iconWidth / 2.0f + dp2pxF(getContext(), 10);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        for (int i = 0; i < dashDatas.size() - 1; i++) {
            left = dashDatas.get(i);
            right = dashDatas.get(i + 1);

            //以下校正的情况为：两条虚线都在屏幕内或只有一条在屏幕内
            if (left < scrollX &&    //仅左虚线在屏幕外
                    right < scrollX + screenWidth) {
                left = scrollX;
                leftUsedScreenLeft = true;
            }
            if (right > scrollX + screenWidth &&  //仅右虚线在屏幕外
                    left > scrollX) {
                right = scrollX + screenWidth;
            }

            if (right - left > iconWidth) {    //经过上述校正之后左右距离还大于图标宽度
                iconX = left + (right - left) / 2.0f;
            } else {                          //经过上述校正之后左右距离小于图标宽度，则贴着在屏幕内的虚线
                if (leftUsedScreenLeft) {
                    iconX = right - iconWidth / 2.0f;
                } else {
                    iconX = left + iconWidth / 2.0f;
                }
            }


            //以下校正的情况为：两条虚线都在屏幕之外
            if (right < scrollX) {  //两条都在屏幕左侧，图标紧贴右虚线
                iconX = right - iconWidth / 2.0f;
            } else if (left > scrollX + screenWidth) {   //两条都在屏幕右侧，图标紧贴左虚线
                iconX = left + iconWidth / 2.0f;
            } else if (left < scrollX && right > scrollX + screenWidth) {  //一条在屏幕左一条在屏幕右，图标居中
                iconX = scrollX + (screenWidth / 2.0f);
            }


            Bitmap icon = icons.get(weatherDatas.get(i).second);

            //经过上述校正之后可以得到图标和文字的绘制区域
            RectF iconRect = new RectF(iconX - iconWidth / 2.0f,
                    iconY - iconWidth / 2.0f,
                    iconX + iconWidth / 2.0f,
                    iconY + iconWidth / 2.0f);

            canvas.drawBitmap(icon, null, iconRect, null);  //画图标

            canvas.drawText(weatherDatas.get(i).second, //画图标下方文字
                    iconX,
                    textY - (metrics.ascent + metrics.descent) / 2,
                    textPaint);
            Log.i("wyy", "drawWeatherIcon: metrics.ascent = " + metrics.ascent + " , metrics.descent  =" + metrics.descent);
            leftUsedScreenLeft = false; //重置标志位
        }

        textPaint.setTextSize(textSize);
        canvas.restore();
    }

    /**
     * 画滑动距离下的当前天气和时间
     * 根据滑动距离和总可滑动距离的比例进行位置的选择
     *
     * @param canvas
     */
    private void drawTempWeatherInfo(Canvas canvas) {
        canvas.save();
        textPaint.setTextSize(0.9f * textSize); //字体缩小一丢丢

        int scrollX = getScrollX();  //范围控制在0 ~ viewWidth-screenWidth
        //外套 ScrollView 的版本
        if (scrollPos > 0) {
            scrollX = scrollPos;
        }

        float iconX, iconY;
        int presPos;

        //初始的 x 为滑动距离占总滑动距离的百分比
        iconX = defaultPadding + dp2px(getContext(), 18) + (float) scrollX * (viewWidth - 2 * defaultPadding - 33) / (viewWidth - screenWidth);


        //取当前所在的item区间，注意越界问题
        presPos = (int) (iconX - defaultPadding) / lineInterval;
        if (presPos > data.size() - 1) {
            presPos = data.size() - 1;
        }

        int distanceTemp = 0;
        if (presPos < data.size() - 1) {
            distanceTemp = data.get(presPos).temperature - data.get(presPos).getTemperature();
        }

        float distanceY = distanceTemp * ((iconX - defaultPadding) % lineInterval) / lineInterval;

        iconY = points.get(presPos).y - distanceY - dp2pxF(getContext(), 13);

        Log.i(TAG, "drawTempWeatherInfo: pointsX = " + points.get(presPos).x + ", points.get(presPos).y = " + points.get(presPos).y);
        Log.i(TAG, "drawTempWeatherInfo: iconX = " + iconX + ", iconY = " + iconY + ", prePos = " + presPos);

        //先绘制背景
        //新建一只画笔，并设置为绿色属性
        Paint _paint = new Paint();
        _paint.setColor(Color.GREEN);
        //新建矩形r1
        RectF r1 = new RectF();
        r1.left = iconX - dp2px(getContext(), 18);
        r1.right = iconX + dp2px(getContext(), 18);
        r1.top = iconY - dp2px(getContext(), 14);
        r1.bottom = iconY + dp2px(getContext(), 8);
        canvas.drawRoundRect(r1, 10, 10, _paint);

        //绘制温度和天气状况
        canvas.drawText(data.get(presPos).getTemperatureStr() + data.get(presPos).getWeather(), //画图标下方文字
                iconX,
                iconY,
                textPaint);

        textPaint.setTextSize(textSize);
        canvas.restore();
    }

    /**
     * 根据天气获取对应的图标，并且缩放到指定大小
     *
     * @param weather
     * @param requestW
     * @param requestH
     * @return
     */
    private Bitmap getWeatherIcon(String weather, float requestW, float requestH) {
        int resId = getIconResId(weather);
        Bitmap bmp;
        int outWdith, outHeight;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, options);
        outWdith = options.outWidth;
        outHeight = options.outHeight;
        options.inSampleSize = 1;
        if (outWdith > requestW || outHeight > requestH) {
            int ratioW = Math.round(outWdith / requestW);
            int ratioH = Math.round(outHeight / requestH);
            options.inSampleSize = Math.max(ratioW, ratioH);
        }
        options.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeResource(getResources(), resId, options);
        return bmp;
    }


    private int getIconResId(String weather) {
        int resId;
        switch (weather) {
            case WeatherBean.SUN:
                resId = R.drawable.sun;
                break;
            case WeatherBean.CLOUDY:
                resId = R.drawable.cloudy;
                break;
            case WeatherBean.RAIN:
                resId = R.drawable.rain;
                break;
            case WeatherBean.SNOW:
                resId = R.drawable.snow;
                break;
            case WeatherBean.SUN_CLOUD:
                resId = R.drawable.sun_cloud;
                break;
            case WeatherBean.THUNDER:
            default:
                resId = R.drawable.thunder;
                break;
        }
        return resId;
    }

    /**
     * 更新滑动的距离
     *
     * @param scrollPos
     */
    public void setScrollPos(int scrollPos) {
        this.scrollPos = scrollPos;
        invalidate();
    }

    //工具类
    public static int dp2px(Context c, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context c, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, c.getResources().getDisplayMetrics());
    }

    public static float dp2pxF(Context c, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    public static float sp2pxF(Context c, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, c.getResources().getDisplayMetrics());
    }
}
