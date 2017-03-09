package com.myweather.app.util;

import android.text.TextUtils;

import com.myweather.app.db.WeatherDB;
import com.myweather.app.model.City;
import com.myweather.app.model.County;
import com.myweather.app.model.Province;

public class Utility {
	// 处理和解析服务器返回的省级数据
	public synchronized static boolean handleProvincesResponse(
			WeatherDB weatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(array[1]);
					province.setProvinceCode(array[0]);
					// 将解析出来的数据存储到Province表
					weatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	// 处理和解析服务器返回的市级数据
	public static boolean handleCitiesResponse(WeatherDB weatherDB,
			String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// 将解析出来的数据存储到city表
					weatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	// 处理和解析服务器返回的县级数据
	public static boolean handleCountiesResponse(WeatherDB WeatherDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					// 将解析出来的数据存储到County表
					WeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

}
