package sugtao4423.lod;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Options{

	private boolean isOpenBrowser;
	private boolean isRegex;
	private boolean isMillisecond;
	private boolean isWebm;
	private String nowplayingFormat;
	private boolean isImageOrientaionSensor;
	private boolean isVideoOrientationSensor;

	public Options(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		this.isOpenBrowser = pref.getBoolean(Keys.MENU_OPEN_BROWSER, false);
		this.isRegex = pref.getBoolean(Keys.MENU_REGEX, false);
		this.isMillisecond = pref.getBoolean(Keys.MENU_MILLISECOND, false);
		this.isWebm = pref.getBoolean(Keys.IS_WEBM, false);
		this.nowplayingFormat = pref.getString(Keys.NOWPLAYING_FORMAT, "");
		this.isImageOrientaionSensor = pref.getBoolean(Keys.IS_IMAGE_ORIENTATION_SENSOR, false);
		this.isVideoOrientationSensor = pref.getBoolean(Keys.IS_VIDEO_ORIENTATION_SENSOR, false);
	}

	public boolean getIsOpenBrowser(){
		return this.isOpenBrowser;
	}

	public boolean getIsRegex(){
		return this.isRegex;
	}

	public boolean getIsMillisecond(){
		return this.isMillisecond;
	}

	public boolean getIsWebm(){
		return this.isWebm;
	}

	public String getNowplayingFormat(){
		return this.nowplayingFormat;
	}

	public boolean getIsImageOrientaionSensor(){
		return this.isImageOrientaionSensor;
	}

	public boolean getIsVideoOrientationSensor(){
		return this.isVideoOrientationSensor;
	}

}
