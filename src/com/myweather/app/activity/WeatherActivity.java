package com.myweather.app.activity;

import net.youmi.android.normal.banner.BannerManager;
import net.youmi.android.normal.banner.BannerViewListener;
import net.youmi.android.normal.common.ErrorCode;
import net.youmi.android.normal.spot.SpotListener;
import net.youmi.android.normal.spot.SpotManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myweather.app.R;
import com.myweather.app.service.AutoUpdateService;
import com.myweather.app.util.HttpCallbackListener;
import com.myweather.app.util.HttpUtil;
import com.myweather.app.util.Utility;

public class WeatherActivity extends BaseActivity implements OnClickListener {
	private LinearLayout weatherInfoLayout;
	/**
	 * 用于显示城市名第一行代码——Android 514
	 */
	private TextView cityNameText;
	/**
	 * 用于显示发布时间
	 */
	private TextView publishText;
	/**
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	/**
	 * 用于显示气温1
	 */
	private TextView temp1Text;
	/**
	 * 用于显示气温2
	 */
	private TextView temp2Text;
	/**
	 * 用于显示当前日期
	 */
	//private TextView currentDateText;
	/**
	 * 切换城市按钮
	 */
	private Button switchCity;
	/**
	 * 更新天气按钮
	 */
	private Button refreshWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		// 初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		//currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号时就去查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 没有县级代号时就直接显示本地天气
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		//设置广告条
		setupBannerAd();
		// 设置插屏广告
		setupSpotAd();
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 查询县级代号所对应的天气代号。
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * 查询天气代号所对应的天气。
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}

	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
	 */
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(final String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							Log.d("weatherActivity",weatherCode);
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
		});
	}

	/**
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		//publishText.setText(prefs.getString("publish_time", "") + "发布");
		publishText.setText(prefs.getString("current_date", ""));
		//currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	
	/**
	 * 设置广告条广告
	 */
	private void setupBannerAd() {
				/**
				 * 普通布局
				 */
				// 获取广告条
				View bannerView = BannerManager.getInstance(mContext)
						.getBannerView(mContext, new BannerViewListener() {
							@Override
							public void onRequestSuccess() {
								logInfo("请求广告条成功");
							}
		
							@Override
							public void onSwitchBanner() {
								logDebug("广告条切换");
							}
		
							@Override
							public void onRequestFailed() {
								logError("请求广告条失败");
							}
						});
				// 实例化广告条容器
				LinearLayout bannerLayout = (LinearLayout) findViewById(R.id.ll_banner);
				// 添加广告条到容器中
				bannerLayout.addView(bannerView);
//
//		/**
//		 * 悬浮布局
//		 */
//		// 实例化LayoutParams
//		FrameLayout.LayoutParams layoutParams =
//				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//		// 设置广告条的悬浮位置，这里示例为右下角
//		layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
//		// 获取广告条
//		final View bannerView = BannerManager.getInstance(mContext)
//				.getBannerView(mContext, new BannerViewListener() {
//
//					@Override
//					public void onRequestSuccess() {
//						logInfo("请求广告条成功");
//
//					}
//
//					@Override
//					public void onSwitchBanner() {
//						logDebug("广告条切换");
//					}
//
//					@Override
//					public void onRequestFailed() {
//						logError("请求广告条失败");
//					}
//				});
//		// 添加广告条到窗口中
//		((Activity) mContext).addContentView(bannerView, layoutParams);
	}
	
	/**
	 * 设置插屏广告
	 */
	private void setupSpotAd() {
		// 设置插屏图片类型，默认竖图
		//		// 横图
		//		SpotManager.getInstance(mContext).setImageType(SpotManager
		// .IMAGE_TYPE_HORIZONTAL);
		// 竖图
		SpotManager.getInstance(mContext).setImageType(SpotManager.IMAGE_TYPE_VERTICAL);

		// 设置动画类型，默认高级动画
		//		// 无动画
		//		SpotManager.getInstance(mContext).setAnimationType(SpotManager
		// .ANIMATION_TYPE_NONE);
		//		// 简单动画
		//		SpotManager.getInstance(mContext).setAnimationType(SpotManager
		// .ANIMATION_TYPE_SIMPLE);
		// 高级动画
		SpotManager.getInstance(mContext)
				.setAnimationType(SpotManager.ANIMATION_TYPE_ADVANCED);

				// 展示插屏广告
				SpotManager.getInstance(mContext).showSpot(mContext, new SpotListener() {

					@Override
					public void onShowSuccess() {
						logInfo("插屏展示成功");
					}

					@Override
					public void onShowFailed(int errorCode) {
						logError("插屏展示失败");
						switch (errorCode) {
						case ErrorCode.NON_NETWORK:
							showShortToast("网络异常");
							break;
						case ErrorCode.NON_AD:
							showShortToast("暂无插屏广告");
							break;
						case ErrorCode.RESOURCE_NOT_READY:
							showShortToast("插屏资源还没准备好");
							break;
						case ErrorCode.SHOW_INTERVAL_LIMITED:
							showShortToast("请勿频繁展示");
							break;
						case ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE:
							showShortToast("请设置插屏为可见状态");
							break;
						default:
							showShortToast("请稍后再试");
							break;
						}
					}

					@Override
					public void onSpotClosed() {
						logDebug("插屏被关闭");
					}

					@Override
					public void onSpotClicked(boolean isWebPage) {
						logDebug("插屏被点击");
						logInfo("是否是网页广告？%s", isWebPage ? "是" : "不是");
					}
				});
			}

	
	@Override
	public void onBackPressed() {
		// 点击后退关闭插屏广告
		if (SpotManager.getInstance(mContext).isSpotShowing()) {
			SpotManager.getInstance(mContext).hideSpot();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 插屏广告
		SpotManager.getInstance(mContext).onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 插屏广告
		SpotManager.getInstance(mContext).onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 展示广告条窗口的 onDestroy() 回调方法中调用
		BannerManager.getInstance(mContext).onDestroy();
		// 插屏广告
		SpotManager.getInstance(mContext).onDestroy();
	}

}