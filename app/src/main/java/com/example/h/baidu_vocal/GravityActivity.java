package com.example.h.baidu_vocal;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.h.baidu_vocal.MainActivity.bluetoothUtils;

public class GravityActivity extends AppCompatActivity {
	private SensorManager sensorManager;
	//private ImageView Imagezh;
	private ImageView ImageBj;
	private ImageView Imagecar;
	private float xValue,yValue,zValue;
	private Resources resources;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gravity1);
		initView();
		Toast.makeText(getApplicationContext(), "按返回键可以返回上一个界面", Toast.LENGTH_SHORT).show();

	}
	protected void initView(){
		//ImageView imageqian = (ImageView) findViewById(R.id.qian);
		//ImageView imagehou = (ImageView) findViewById(R.id.hou);
		//ImageView imagezuo = (ImageView) findViewById(R.id.zuo);
		//ImageView imageyou = (ImageView) findViewById(R.id.you);
		//ImageView imagezq = (ImageView) findViewById(R.id.zuoqian);
		//ImageView imageyq = (ImageView) findViewById(R.id.youqian);
		//imagezq =(ImageView)findViewById(R.id.zuohou);
		//ImageView imageyh = findViewById(R.id.youhou);
		ImageBj= findViewById(R.id.beijing);
		Imagecar= findViewById(R.id.car);
		sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(listener, sensor,SensorManager.SENSOR_DELAY_NORMAL);
		resources = getBaseContext().getResources();
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(sensorManager!=null){
			sensorManager.unregisterListener(listener);
		}
	}
	private final SensorEventListener listener=new SensorEventListener(){  //添加了final
		@Override
		public void onSensorChanged(SensorEvent event){

			xValue=event.values[0];
			yValue=event.values[1];
			zValue=event.values[2];
			DateAnsys();
		}
		@Override
		public void onAccuracyChanged(Sensor sensor,int accuracy){}
	};

	private void DateAnsys(){
		if(zValue>8.5){
			//停止
			bluetoothUtils.write("5");
			Drawable imageDrawable = resources.getDrawable(R.drawable.design_point);
			ImageBj.setBackground(imageDrawable);//设置背景
			Imagecar.setImageResource(R.drawable.carqian);
		}//，设置src传入参数为图片资源ID

		else{
			if(xValue>2){//说明向左侧偏了
				if(Math.abs(yValue)<1){//说明是正偏
					//左转
					bluetoothUtils.write("4");
					Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
					ImageBj.setBackground(imageDrawable);//设置背景
					Imagecar.setImageResource(R.drawable.carzuo); //，设置src传入参数为图片资源ID
				}else {//说明是左前偏
					if(yValue<0){
						//，设置src传入参数为图片资源ID
						if(yValue>-3.5){//靠近左侧
							bluetoothUtils.write("4");
							Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
							ImageBj.setBackground(imageDrawable);//设置背景
						}else{//靠近前侧
							bluetoothUtils.write("8");
							Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
							ImageBj.setBackground(imageDrawable);//设置背景
						}
						Imagecar.setImageResource(R.drawable.carzq); //，设置src传入参数为图片资源ID
					}else{//说明是左后偏
						//设置src传入参数为图片资源ID
						if(yValue<3.5){//靠近左侧
							bluetoothUtils.write("4");
							Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
							ImageBj.setBackground(imageDrawable);//设置背景
						}else{//靠近后侧
							bluetoothUtils.write("2");
							Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
							ImageBj.setBackground(imageDrawable);//设置背景
						}
						Imagecar.setImageResource(R.drawable.carzh); //设置src传入参数为图片资源ID
					}
				}
			}else if(xValue<-2){//说明向右侧偏了
				if(Math.abs(yValue)<3){//说明是正偏
					//右转
					bluetoothUtils.write("6");
					Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
					ImageBj.setBackground(imageDrawable);//设置背景
					Imagecar.setImageResource(R.drawable.caryou); //，设置src传入参数为图片资源ID
				}else{//说明是右前偏
					if(yValue<0){
						//设置src传入参数为图片资源ID
						if(yValue>-3){//靠近右侧
							bluetoothUtils.write("6");
							Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
							ImageBj.setBackground(imageDrawable);//设置背景
						}else{//靠近前侧
							bluetoothUtils.write("8");
							Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
							ImageBj.setBackground(imageDrawable);//设置背景
						}
						Imagecar.setImageResource(R.drawable.caryq); //设置src传入参数为图片资源ID
					}else{
						//，设置src传入参数为图片资源ID
						if(yValue<3){//靠近右侧
							bluetoothUtils.write("6");
							Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
							ImageBj.setBackground(imageDrawable);//设置背景
						}else{//靠近后侧
							bluetoothUtils.write("2");
							Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
							ImageBj.setBackground(imageDrawable);//设置背景
						}
						Imagecar.setImageResource(R.drawable.caryh); //，设置src传入参数为图片资源ID
					}
				}
			}else if(Math.abs(xValue)<1){//正前或者正后
				if(yValue<-1){//说明是正前
					//前进
					bluetoothUtils.write("8");
					Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
					ImageBj.setBackground(imageDrawable);//设置背景
					Imagecar.setImageResource(R.drawable.carqian); //设置src传入参数为图片资源ID
				}else if(yValue>2){//说明是正后
					bluetoothUtils.write("2");
					Drawable imageDrawable = resources.getDrawable(R.drawable.design_point1);
					ImageBj.setBackground(imageDrawable);//设置背景
					Imagecar.setImageResource(R.drawable.carhou); //设置src传入参数为图片资源ID
				}

			}
		}




	}
}
