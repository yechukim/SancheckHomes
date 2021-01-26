package com.example.cs50.arduino;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {

    private static final String TAG = "main";
    //ui
    LinearLayout weather, stroll, window;
    String date_time;
    TextView today_date_time;
    TextView loc, des;
    ImageView weather_icon;


    //firebase
    FirebaseDatabase mDatabase;
    DatabaseReference mReference;

    //화재감지 전화테스트
    TextView fire_status;

    //화재감지 notification
    ImageView iv_fire;
    NotificationManagerCompat notificationManager;
    // Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.siren);
    final long[] VIBRATE_PATTERN = {500, 1000, 300, 1000, 500, 1000, 300, 1000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoPermissions.Companion.loadAllPermissions(this, 101); //모든 퍼미션 다 체크해줌.
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        date_time = simpleDateFormat.format(calendar.getTime());

        connectFirebase();
        //원 3개
        weather = findViewById(R.id.weather);
        stroll = findViewById(R.id.stroll);
        window = findViewById(R.id.window);

        //메인에서 날씨 가져오기 -- API
        get_weather();
        Log.d(TAG, "onCreate: 날씨 가져오는 메소드");
        //날씨 원 text
        today_date_time = findViewById(R.id.today_date_time);
        loc = findViewById(R.id.loc);
        weather_icon = findViewById(R.id.weather_icon);
        des = findViewById(R.id.des);
        //현재 날짜
        today_date_time.setText(date_time);

        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                startActivity(intent);
            }
        });
        stroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StrollActivity.class);
                startActivity(intent);
            }
        });
        window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WindowActivity.class);
                startActivity(intent);
            }
        });

/*        //화재감지전화테스트
        fire_status = (TextView) findViewById(R.id.fire_status);
        // 오류나서 주석처리해놓음
        fire_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),FireNotification.class);
                startActivity(intent);
            }
        });

        //화재감지notification
        iv_fire = (ImageView) findViewById(R.id.iv_fire);
        notificationManager = NotificationManagerCompat.from(this);
        iv_fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOnChannel1(v);
            }
        });*/

    }

    public void get_weather() {
        //open weather API id -> description
        final int weather_id[] = {201, 200, 202, 210, 211, 212, 221, 230, 231, 232,
                300, 301, 302, 310, 311, 312, 313, 314, 321, 500,
                501, 502, 503, 504, 511, 520, 521, 522, 531, 600,
                601, 602, 611, 612, 615, 616, 620, 621, 622, 701,
                711, 721, 731, 741, 751, 761, 762, 771, 781, 800,
                801, 802, 803, 804, 900, 901, 902, 903, 904, 905,
                906, 951, 952, 953, 954, 955, 956, 957, 958, 959,
                960, 961, 962};
        final String weather_des[] = {"가벼운 비 동 천둥구름", "비 동반 천둥구름", "폭우 동반 천둥구름", "약한 천둥구름",
                "천둥구름", "강한 천둥구름", "불규칙적 천둥구름", "약한 연무를 동반한 천둥구름", "연무를 동반한 천둥구름",
                "강한 안개비 동반 천둥구름", "가벼운 안개비", "안개비", "강한 안개비", "가벼운 적은비", "적은비",
                "강한 적은비", "소나기와 안개비", "강한 소나기와 안개비", "소나기", "악한 비", "중간 비", "강한 비",
                "매우 강한 비", "극심한 비", "우박", "약한 소나기 비", "소나기 비", "강한 소나기 비", "불규칙적 소나기 비",
                "가벼운 눈", "눈", "강한 눈", "진눈깨비", "소나기 진눈깨비", "약한 비와 눈", "비와 눈", "약한 소나기 눈",
                "소나기 눈", "강한 소나기 눈", "박무", "연기", "연무", "모래 먼지", "안개", "모래", "먼지", "화산재", "돌풍",
                "토네이도", "구름 한 점 없는 맑은 하늘", "약간의 구름이 낀 하늘", "드문드문 구름이 낀 하늘", "구름이 거의 없는 하늘",
                "구름으로 뒤덮인 흐린 하늘", "토네이도", "태풍", "허리케인", "한랭", "고온", "바람부는", "우박", "바람이 거의 없음",
                "약한 바람", "부드러운 바람", "중간 세기 바람", "신선한 바람", "센 바람", "돌풍에 가까운 센 바람", "돌풍",
                "심각한 돌풍", "폭풍", "강한 폭풍", "허리케인"};

         Log.d(TAG, "get_weather: 메소드 실행");
        //잘 들어옴
        String url = "http://api.openweathermap.org/data/2.5/weather?q=Seoul,kr&appid=b422bc7295c0ae11f8756e015fe316f9";
        Log.d(TAG, "get_weather: url"+url);
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject main_object = response.getJSONObject("main");
                    Log.d(TAG, "main_object : " + main_object);

                    //"weather"부분 가져옴
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);

                    //city name
                    String city = response.getString("name");
                    Log.d(TAG, "onResponse: "+city);
                    //온도 부분 - 일단 안씀
                    //String main = String.valueOf(main_object.getDouble("temp"));

                    //id를 가져옴
                    String id = object.getString("id");
                    Log.d(TAG, "onResponse: "+id);
                    //Log.d(TAG, "id: "+id);
                    String id_description = null;

                    for(int i = 0; i<weather_des.length; i++)
                    {
                        if(Integer.parseInt(id)==weather_id[i])
                        {
                            int index = i;
                            id_description =  weather_des[index];
                        }
                    }

                    //icon
                    String icon = object.getString("icon");
                    String iconUrl = "http://openweathermap.org/img/w/" + icon + ".png";
                    Glide.with(getApplicationContext()).load(iconUrl).into(weather_icon);

                    loc.setText(city);
                    des.setText(id_description);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "err : " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }

    public void connectFirebase(){
        //블루투스 값을 얻어서 data에 저장하기
        String data ="테스트";
        String data2 = "flame";

        //firebase db에 저장하기

        //fireabse 실시간 db관리 객체 얻기
        mDatabase = FirebaseDatabase.getInstance();

        //저장시킬 노드 참조객체 가져오기
        mReference = mDatabase.getReference("test");//안쓰면 최상위 노드
        mReference = mDatabase.getReference("flame");
        mReference.setValue(data);
        mReference.setValue(data2);
    }

/*    public void sendOnChannel1(View v) {
        Notification notification = new NotificationCompat.Builder(this, NotiChannel.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_fire)
                .setTicker("화재감지경보")
                .setContentTitle("화재감지경보")
                .setContentText("화재가 감지되어 자동으로 창문열기 실행됨")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.RED)
                .setSound(Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/"
                        + R.raw.siren))
                .setVibrate(VIBRATE_PATTERN)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }

    public void sendOnChannel2(View v) {

    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int i, String[] strings) {

    }

    @Override
    public void onGranted(int i, String[] strings) {

    }


}