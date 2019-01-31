package sugtao4423.lod.swipe_image_viewer;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tenthbit.view.ZoomImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sugtao4423.lod.R;

public class ImageFragment extends Fragment{

    private ZoomImageView image;
    private String url;
    private byte[] non_orig_image;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        url = bundle.getString(ImagePagerAdapter.BUNDLE_KEY_URL);
        image = new ZoomImageView(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        new AsyncTask<String, Void, Bitmap>(){
            private ProgressDialog progDialog;

            @Override
            protected void onPreExecute(){
                progDialog = new ProgressDialog(getActivity());
                progDialog.setMessage(getString(R.string.loading));
                progDialog.setIndeterminate(false);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setCancelable(true);
                progDialog.show();
            }

            @Override
            protected Bitmap doInBackground(String... params){
                try{
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream is = connection.getInputStream();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while((len = is.read(buffer)) > 0){
                        bout.write(buffer, 0, len);
                    }
                    non_orig_image = bout.toByteArray();
                    Bitmap myBitmap = BitmapFactory.decodeByteArray(non_orig_image, 0, non_orig_image.length);
                    is.close();
                    bout.close();
                    connection.disconnect();
                    return myBitmap;
                }catch(IOException e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result){
                if(result != null){
                    progDialog.dismiss();
                    image.setImageBitmap(result);
                }else{
                    Toast.makeText(getActivity(), R.string.error_get_image, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(url);

        return image;
    }

    public byte[] getNonOrigImage(){
        return non_orig_image;
    }

}
