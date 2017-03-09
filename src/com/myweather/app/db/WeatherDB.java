package com.myweather.app.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.myweather.app.model.City;
import com.myweather.app.model.County;
import com.myweather.app.model.Province;

public class WeatherDB {
	
	//数据库名
	public static final String DB_NAME = "weather";
	//数据库版本
	public static final int VERSION = 1;
	//声明一个私有的静态变量
	private static WeatherDB weatherDB;
	private SQLiteDatabase db;
	//构造器私有化，避免外部直接创建对象
	private WeatherDB(Context context){
		WeatherOpenHelper dbHelper = 
				new WeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}
	
	//创建一个对外的公共的静态方法访问该变量，如果变量没有对象，创建该对象
	public synchronized static WeatherDB getInstance(Context context){
		//如果weathreDB为空，则创建对象，否则返回weatherDB
		if(weatherDB == null){
			weatherDB = new WeatherDB(context);
		}
		return weatherDB;
	}
	
	//将province实例存储到数据库
	public void saveProvince(Province province){
		//因为需要用到province的内部方法，所以需要先判断province对象是否为空，
		//如果province为空，会报空指针异常，
		if(province != null){
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}else{
			Log.d("weatherDB","province is null");
			}
	}
	
	//将city实例存储到数据库
	public void saveCity(City city){
		if(city != null){
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}else{
		Log.d("weatherDB","city is null");
		}
	}
	
	//将county实例存储到数据库
		public void saveCounty(County county){
			if(county != null){
				ContentValues values = new ContentValues();
				values.put("county_name", county.getCountyName());
				values.put("county_code", county.getCountyCode());
				values.put("city_id", county.getCityId());
				db.insert("County", null, values);
			}else{
				Log.d("weatherDB","county is null");
			}
		}
		
		//从数据库读取全国所有省份的信息
		public List<Province> loadProvinces(){
			List<Province> provincesList = new ArrayList<Province>();
			Cursor cursor = db.query("Province", null, null, null, null, null, null);
			//判断查询的表是否有数据，有数据cursor.moveToFisrt会返回true
			if(cursor.moveToFirst()){
				do{
					Province province = new Province();
					province.setId(cursor.getInt(cursor.getColumnIndex("id")));
					province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
					province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
					provincesList.add(province);
				}while(cursor.moveToNext());
			}
			return provincesList;
		}
		
		//从数据库读取某省下所有城市的信息
		public List<City> loadCities(int provinceId){
			List<City> citiesList = new ArrayList<City>();
			Cursor cursor = db.query("City", null, null, null, null, null, null);
			if(cursor.moveToFirst()){
				do{
					City city = new City();
					city.setId(cursor.getInt(cursor.getColumnIndex("id")));
					city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
					city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
					city.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
					citiesList.add(city);
				}while(cursor.moveToNext());
			}
			return citiesList;
		}
		//从数据库读取某城市下所有县的信息
		public List<County> loadCounties(int cityId){
			List<County> countiesList = new ArrayList<County>();
			Cursor cursor = db.query("County", null, null, null, null, null, null);
			if(cursor.moveToFirst()){
				do{
					County county = new County();
					county.setId(cursor.getInt(cursor.getColumnIndex("id")));
					county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
					county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
					county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
					countiesList.add(county);
				}while(cursor.moveToNext());
			}
			return countiesList;
		}

}
