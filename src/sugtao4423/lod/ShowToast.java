package sugtao4423.lod;

import com.loopj.android.image.SmartImageView;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ShowToast{

	public enum Type{
		NORMAL
	}

	private View toastView;
	private TextView main_message, tweet;
	private SmartImageView icon;

	public ShowToast(Context context, String text, int duration, Type toastType){
		ApplicationClass appClass = (ApplicationClass)context.getApplicationContext();

		toastView = appClass.getToastView();
		main_message = appClass.getToast_Main_Message();
		tweet = appClass.getToast_Tweet();
		icon = appClass.getToast_Icon();

		switch(toastType){
		case NORMAL:
			normalToast(text, context, duration);
			break;
		}
	}

	public ShowToast(Context context, int resId, int duration, Type toastType){
		this(context, context.getString(resId), duration, toastType);
	}

	public ShowToast(Context context, String text, int duration){
		this(context, text, duration, Type.NORMAL);
	}

	public ShowToast(Context context, int resId, int duration){
		this(context, resId, duration, Type.NORMAL);
	}

	public ShowToast(Context context, String text){
		this(context, text, Toast.LENGTH_SHORT, Type.NORMAL);
	}

	public ShowToast(Context context, int resId){
		this(context, resId, Toast.LENGTH_SHORT, Type.NORMAL);
	}

	public void normalToast(String text, Context context, int duration){
		Toast toast = new Toast(context);

		main_message.setText(text);
		main_message.setTextColor(Color.WHITE);
		main_message.setPadding(3, 2, 3, 2);

		tweet.setVisibility(View.GONE);
		icon.setVisibility(View.GONE);

		toast.setDuration(duration);
		toast.setView(toastView);
		toast.show();
	}
}
