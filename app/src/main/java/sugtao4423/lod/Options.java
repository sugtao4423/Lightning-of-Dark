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
    private boolean isImageOrientationSensor;
    private boolean isVideoOrientationSensor;
    private float userNameFontSize;
    private float contentFontSize;
    private float dateFontSize;

    public Options(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        this.isOpenBrowser = pref.getBoolean(Keys.MENU_OPEN_BROWSER, false);
        this.isRegex = pref.getBoolean(Keys.MENU_REGEX, false);
        this.isMillisecond = pref.getBoolean(Keys.MENU_MILLISECOND, false);
        this.isWebm = pref.getBoolean(Keys.IS_WEBM, false);
        this.nowplayingFormat = pref.getString(Keys.NOWPLAYING_FORMAT, "");
        this.isImageOrientationSensor = pref.getBoolean(Keys.IS_IMAGE_ORIENTATION_SENSOR, false);
        this.isVideoOrientationSensor = pref.getBoolean(Keys.IS_VIDEO_ORIENTATION_SENSOR, false);
        this.userNameFontSize = Float.parseFloat(pref.getString(Keys.USER_NAME_FONT_SIZE, "13"));
        this.contentFontSize = Float.parseFloat(pref.getString(Keys.CONTENT_FONT_SIZE, "13"));
        this.dateFontSize = Float.parseFloat(pref.getString(Keys.DATE_FONT_SIZE, "11"));
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

    public boolean getIsImageOrientationSensor(){
        return this.isImageOrientationSensor;
    }

    public boolean getIsVideoOrientationSensor(){
        return this.isVideoOrientationSensor;
    }

    public float getUserNameFontSize(){
        return userNameFontSize;
    }

    public float getContentFontSize(){
        return contentFontSize;
    }

    public float getDateFontSize(){
        return dateFontSize;
    }

}
