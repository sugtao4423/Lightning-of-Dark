package sugtao4423.icondialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import sugtao4423.lod.ApplicationClass;

public class IconDialog{

	private Builder builder;
	private Context context;

	public IconDialog(Context context){
		builder = new Builder(context);
		this.context = context;
	}

	public Builder setTitle(String title){
		return builder.setTitle(title);
	}

	public Builder setItems(IconItem[] items, OnClickListener listener){
		IconDialogAdapter adapter = new IconDialogAdapter(context, items);
		return builder.setAdapter(adapter, listener);
	}

	public AlertDialog show(){
		return builder.show();
	}
}

class IconDialogAdapter extends ArrayAdapter<IconItem>{

	private Context context;
	private Typeface tf;
	private float density;

	public IconDialogAdapter(Context context, IconItem[] items){
		super(context, android.R.layout.select_dialog_item, android.R.id.text1, items);
		this.context = context;
		this.tf = ((ApplicationClass)context.getApplicationContext()).getFontAwesomeTypeface();
		this.density = context.getResources().getDisplayMetrics().density;
	}

	class ViewHolder{
		TextView icon;
		TextView text;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		IconItem item = getItem(position);
		if(convertView == null){
			int dip32 = (int)(32 * density);
			int dip8 = (int)(8 * density);

			TextView icon = new TextView(context);
			icon.setId(114514);
			icon.setTextSize(10 * density);
			icon.setGravity(Gravity.CENTER);
			icon.setTypeface(tf);
			LayoutParams iconParams = new LayoutParams(dip32, dip32);
			iconParams.setMargins(dip8, dip8, dip8, dip8);
			iconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			iconParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			icon.setLayoutParams(iconParams);

			TextView text = new TextView(context);
			LayoutParams textParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			textParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());
			textParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			text.setLayoutParams(textParams);

			RelativeLayout layout = new RelativeLayout(context);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			layout.setLayoutParams(layoutParams);
			layout.addView(icon);
			layout.addView(text);

			holder = new ViewHolder();
			holder.icon = icon;
			holder.text = text;

			convertView = layout;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.icon.setText(String.valueOf(item.getIcon()));
		holder.icon.setTextColor(item.getIconColor());
		holder.text.setText(item.getTitle());
		return convertView;
	}
}