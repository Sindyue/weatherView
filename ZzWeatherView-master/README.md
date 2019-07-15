# ZzWeatherView


一个显示天气的控件，大致实现小米和墨迹天气的初版


1. 引用组件 MiuiWeatherView 或者 MojiWeatherView ，为了连贯滑动性，在该组件外嵌套一个水平的 ScrollView ，并将相关滑动距离传至子控件中；
2. 参考 MiuiWeatherView 的实现，直接在组件中监听事件滑动，也能实现惯性滑动，不过该组件被嵌套在垂直的 ScrollView 中时，滑动不够连贯，详情自己可通过事件看出


MojiWeatherView 效果图如下：

<img src="https://github.com/Sindyue/weatherView/blob/master/ZzWeatherView-master/art/device-2019-07-15-135248.png"  width="400px"/>

## 用法简介

1）布局

### drawable/scroll_bar_thumb.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient android:startColor="#33999999" android:endColor="#80aaaaaa"
        android:angle="0"/>
    <corners android:radius="6dp" />
</shape>
```



```xml
      <me.zhouzhuo.zzweather.view.MiuiScrollView
            android:id="@+id/miui_weather_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:scrollbarSize="2dp"
            android:scrollbarThumbHorizontal="@drawable/scroll_bar_thumb"
            android:scrollbarTrackHorizontal="@drawable/scroll_bar_thumb">

            <me.zhouzhuo.zzweather.view.MiuiWeatherView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="100dp"
                app:background_color="#ffffff"
                app:line_interval="60dp"
                app:min_point_height="60dp" />
        </me.zhouzhuo.zzweather.view.MiuiScrollView>

```


2）java


```java
        MiuiScrollView miuiWeatherView = (MiuiScrollView) findViewById(R.id.miui_weather_view);

        //填充天气数据
        miuiWeatherView.setData(generateData2(), this);
        
        //数据源
       private List<WeatherBean> generateData2() {
            List<WeatherBean> data = new ArrayList<>();
            //add your WeatherBean to data
            WeatherBean b1;
            int tempture = 20;
            String weather;
            for (int i = 0; i < 25; i++) {
                if (i < 12) {
                    tempture = tempture + 2;
                } else {
                    tempture = tempture - 3;
                }

                weather = (i % 4 == 0) ? WeatherBean.RAIN : WeatherBean.SUN;
                b1 = new WeatherBean(weather, tempture, i + ":00");
                data.add(b1);
            }
            return data;
        }
```

