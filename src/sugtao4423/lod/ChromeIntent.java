package sugtao4423.lod;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

public class ChromeIntent{

	public ChromeIntent(Context context, Uri uri){
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		builder.setShowTitle(true);
		builder.enableUrlBarHiding();
		builder.addDefaultShareMenuItem();
		builder.setToolbarColor(Color.parseColor(context.getString(R.color.statusBar)));
		builder.build().launchUrl(context, uri);
	}

}
