package sugtao4423.icondialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

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

	public IconDialogAdapter(Context context, IconItem[] items){
		super(context, android.R.layout.select_dialog_item, android.R.id.text1, items);
		this.context = context;
	}

	class ViewHolder{
		ImageView image;
		TextView text;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		IconItem item = getItem(position);
		if(convertView == null){
			int dip32 = (int)(32 * context.getResources().getDisplayMetrics().density);
			int dip8 = (int)(8 * context.getResources().getDisplayMetrics().density);

			ImageView image = new ImageView(context);
			image.setId(114514);
			LayoutParams imageParams = new LayoutParams(dip32, dip32);
			imageParams.setMargins(dip8, dip8, dip8, dip8);
			imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			imageParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			image.setLayoutParams(imageParams);

			TextView text = new TextView(context);
			LayoutParams textParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			textParams.addRule(RelativeLayout.RIGHT_OF, image.getId());
			textParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			text.setLayoutParams(textParams);

			RelativeLayout layout = new RelativeLayout(context);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			layout.setLayoutParams(layoutParams);
			layout.addView(image);
			layout.addView(text);

			holder = new ViewHolder();
			holder.image = image;
			holder.text = text;

			convertView = layout;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.image.setImageResource(item.getResource());
		holder.text.setText(item.getTitle());
		return convertView;
	}
}