package me.zhouzhuo.zzweather.other;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import me.zhouzhuo.zzweather.R;


public class OtherMainActivity extends AppCompatActivity {

    private LineView lineView,lineView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_other);
        lineView= (LineView) findViewById(R.id.lineView);
        lineView2= (LineView) findViewById(R.id.lineView2);
        List<String> yList=new ArrayList<>();
        yList.add("100%");
        yList.add("80%");
        yList.add("60%");
        yList.add("40%");
        yList.add("20%");
        yList.add("0.0%");
        List<String> xList=getCurrentWeekDay();
        lineView.setViewData(yList,xList);
        lineView2.setViewData(yList,xList);
        lineView2.setmLineType(LineView.LineType.ARC);
    }

    /**
     * 获取最近一周的时间 MM-dd
     */
    private List<String> getCurrentWeekDay() {
        List<String> data = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            data.add(sdf.format(calendar.getTime()));
        }
        return data;
    }

}
