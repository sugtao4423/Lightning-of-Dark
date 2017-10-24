package sugtao4423.lod;

import com.loopj.android.image.SmartImageView;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ShowToast{

	private View toastView;
	private TextView main_message, tweet;
	private SmartImageView icon;

	public ShowToast(String text, Context context, int toastType){
		ApplicationClass appClass = (ApplicationClass)context.getApplicationContext();

		toastView = appClass.getToastView();
		main_message = appClass.getToast_Main_Message();
		tweet = appClass.getToast_Tweet();
		icon = appClass.getToast_Icon();

		// 0: normal, short
		// 1: normal, long
		switch(toastType){
		case 0:
			normalToast(text, context, Toast.LENGTH_SHORT);
			break;
		case 1:
			normalToast(text, context, Toast.LENGTH_LONG);
			break;
		}
	}

	public ShowToast(int resId, Context context, int toastType){
		this(context.getString(resId), context, toastType);
	}

	public void normalToast(String text, Context context, int length){
		Toast toast = new Toast(context);

		main_message.setText(text);
		main_message.setTextColor(Color.WHITE);
		main_message.setPadding(3, 2, 3, 2);

		tweet.setVisibility(View.GONE);
		icon.setVisibility(View.GONE);

		toast.setDuration(length);
		toast.setView(toastView);
		toast.show();
	}
}
