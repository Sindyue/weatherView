package me.zhouzhuo.zzweather;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.zhouzhuo.zzweather.view.MiuiScrollView;
import me.zhouzhuo.zzweather.view.MiuiWeatherView;
import me.zhouzhuo.zzweather.view.WeatherBean;
import me.zhouzhuo.zzweatherview.AirLevel;
import me.zhouzhuo.zzweatherview.WeatherItemView;
import me.zhouzhuo.zzweatherview.WeatherModel;
import me.zhouzhuo.zzweatherview.ZzWeatherView;
import me.zhouzhuo.zzweatherview.ZzWeatherViewSimple;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtil.scaleContentView((ViewGroup) findViewById(R.id.activity_main));

//        RadioGroup rgType = (RadioGroup) findViewById(R.id.rg_type);
//        rgType.check(R.id.rb_disc);

        final ZzWeatherView weatherView = (ZzWeatherView) findViewById(R.id.weather_view);
        ZzWeatherViewSimple weatherViewSimple = (ZzWeatherViewSimple) findViewById(R.id.weather_simple_view);
        MiuiScrollView miuiWeatherView = (MiuiScrollView) findViewById(R.id.miui_weather_view);


        miuiWeatherView.setData(generateData2(), this);

        //填充天气数据
        weatherView.setList(generateData());
        weatherViewSimple.setList(generateData());

        //画折线
        weatherView.setLineType(ZzWeatherView.LINE_TYPE_DISCOUNT);
        //画曲线(已修复不圆滑问题)
//        weatherView.setLineType(ZzWeatherView.LINE_TYPE_CURVE);

        //设置线宽
        weatherView.setLineWidth(6f);
        weatherViewSimple.setLineWidth(4f);

        //设置一屏幕显示几列(最少3列)
        try {
            weatherView.setColumnNumber(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //设置白天和晚上线条的颜色
        weatherView.setDayAndNightLineColor(Color.BLUE, Color.RED);

        //点击某一列
        weatherView.setOnWeatherItemClickListener(new ZzWeatherView.OnWeatherItemClickListener() {
            @Override
            public void onItemClick(WeatherItemView itemView, int position, WeatherModel weatherModel) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
            }
        });


//        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.rb_disc:
//                        weatherView.setLineType(ZzWeatherView.LINE_TYPE_DISCOUNT);
//                        break;
//                    case R.id.rb_curve:
//                        weatherView.setLineType(ZzWeatherView.LINE_TYPE_CURVE);
//                        break;
//                }
//            }
//        });

    }

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

    private List<WeatherModel> generateData() {
        List<WeatherModel> list = new ArrayList<WeatherModel>();
        WeatherModel model = new WeatherModel();
        model.setDate("12/07");
        model.setWeek("昨天");
        model.setDayWeather("大雪");
        model.setDayTemp(11);
        model.setNightTemp(5);
        model.setNightWeather("晴");
        model.setWindOrientation("西南风");
        model.setWindLevel("3级");
        model.setAirLevel(AirLevel.EXCELLENT);
        list.add(model);

        WeatherModel model1 = new WeatherModel();
        model1.setDate("12/08");
        model1.setWeek("今天");
        model1.setDayWeather("晴");
        model1.setDayTemp(8);
        model1.setNightTemp(5);
        model1.setNightWeather("晴");
        model1.setWindOrientation("西南风");
        model1.setWindLevel("3级");
        model1.setAirLevel(AirLevel.HIGH);
        list.add(model1);

        WeatherModel model2 = new WeatherModel();
        model2.setDate("12/09");
        model2.setWeek("明天");
        model2.setDayWeather("晴");
        model2.setDayTemp(9);
        model2.setNightTemp(8);
        model2.setNightWeather("晴");
        model2.setWindOrientation("东南风");
        model2.setWindLevel("3级");
        model2.setAirLevel(AirLevel.POISONOUS);
        list.add(model2);

        WeatherModel model3 = new WeatherModel();
        model3.setDate("12/10");
        model3.setWeek("周六");
        model3.setDayWeather("晴");
        model3.setDayTemp(12);
        model3.setNightTemp(9);
        model3.setDayPic(R.drawable.w0);
        model3.setNightPic(R.drawable.w1);
        model3.setNightWeather("晴");
        model3.setWindOrientation("东北风");
        model3.setWindLevel("3级");
        model3.setAirLevel(AirLevel.GOOD);
        list.add(model3);

        WeatherModel model4 = new WeatherModel();
        model4.setDate("12/11");
        model4.setWeek("周日");
        model4.setDayWeather("多云");
        model4.setDayTemp(13);
        model4.setNightTemp(7);
        model4.setDayPic(R.drawable.w2);
        model4.setNightPic(R.drawable.w4);
        model4.setNightWeather("多云");
        model4.setWindOrientation("东北风");
        model4.setWindLevel("3级");
        model4.setAirLevel(AirLevel.LIGHT);
        list.add(model4);

        WeatherModel model5 = new WeatherModel();
        model5.setDate("12/12");
        model5.setWeek("周一");
        model5.setDayWeather("多云");
        model5.setDayTemp(17);
        model5.setNightTemp(8);
        model5.setDayPic(R.drawable.w3);
        model5.setNightPic(R.drawable.w4);
        model5.setNightWeather("多云");
        model5.setWindOrientation("西南风");
        model5.setWindLevel("3级");
        model5.setAirLevel(AirLevel.LIGHT);
        list.add(model5);

        WeatherModel model6 = new WeatherModel();
        model6.setDate("12/13");
        model6.setWeek("周二");
        model6.setDayWeather("晴");
        model6.setDayTemp(13);
        model6.setNightTemp(6);
        model6.setDayPic(R.drawable.w5);
        model6.setNightPic(R.drawable.w6);
        model6.setNightWeather("晴");
        model6.setWindOrientation("西南风");
        model6.setWindLevel("3级");
        model6.setAirLevel(AirLevel.POISONOUS);
        list.add(model6);

        WeatherModel model7 = new WeatherModel();
        model7.setDate("12/14");
        model7.setWeek("周三");
        model7.setDayWeather("晴");
        model7.setDayTemp(19);
        model7.setNightTemp(10);
        model7.setDayPic(R.drawable.w5);
        model7.setNightPic(R.drawable.w7);
        model7.setNightWeather("晴");
        model7.setWindOrientation("西南风");
        model7.setWindLevel("3级");
        model7.setAirLevel(AirLevel.POISONOUS);
        list.add(model7);

        WeatherModel model8 = new WeatherModel();
        model8.setDate("12/15");
        model8.setWeek("周四");
        model8.setDayWeather("雨");
        model8.setDayTemp(22);
        model8.setNightTemp(4);
        model8.setDayPic(R.drawable.w5);
        model8.setNightPic(R.drawable.w8);
        model8.setNightWeather("晴");
        model8.setWindOrientation("西南风");
        model8.setWindLevel("3级");
        model8.setAirLevel(AirLevel.POISONOUS);
        list.add(model8);

        WeatherModel model9 = new WeatherModel();
        model9.setDate("12/15");
        model9.setWeek("周四");
        model9.setDayWeather("雨");
        model9.setDayTemp(22);
        model9.setNightTemp(4);
        model9.setDayPic(R.drawable.w5);
        model9.setNightPic(R.drawable.w8);
        model9.setNightWeather("晴");
        model9.setWindOrientation("西南风");
        model9.setWindLevel("3级");
        model9.setAirLevel(AirLevel.POISONOUS);
        list.add(model9);

        WeatherModel mode20 = new WeatherModel();
        mode20.setDate("12/15");
        mode20.setWeek("周四");
        mode20.setDayWeather("雨");
        mode20.setDayTemp(28);
        mode20.setNightTemp(20);
        mode20.setDayPic(R.drawable.w5);
        mode20.setNightPic(R.drawable.w8);
        mode20.setNightWeather("晴");
        mode20.setWindOrientation("西南风");
        mode20.setWindLevel("3级");
        mode20.setAirLevel(AirLevel.POISONOUS);
        list.add(mode20);

        return list;
    }
}
