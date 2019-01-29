package sugtao4423.lod.swipe_image_viewer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.PagerTabStrip;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.tenthbit.view.ZoomViewPager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;

import sugtao4423.lod.App;
import sugtao4423.lod.ChromeIntent;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.utils.Regex;

public class ImageFragmentActivity extends AppCompatActivity{

    public static final String INTENT_EXTRA_KEY_TYPE = "type";
    public static final String INTENT_EXTRA_KEY_URLS = "urls";
    public static final String INTENT_EXTRA_KEY_POSITION = "position";

    public static final int TYPE_ICON = 0;
    public static final int TYPE_BANNER = 1;

    private App app;
    private ImagePagerAdapter adapter;
    private ZoomViewPager pager;
    private String[] urls;
    private int type;
    private String currentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.show_image_pager);
        app = (App)getApplicationContext();
        if(app.getOptions().getIsImageOrientaionSensor()){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        Intent intent = getIntent();
        urls = intent.getStringArrayExtra(INTENT_EXTRA_KEY_URLS);
        type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1);
        int pos = intent.getIntExtra(INTENT_EXTRA_KEY_POSITION, 0);
        adapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);

        pager = (ZoomViewPager)findViewById(R.id.show_image_pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(urls.length - 1);
        pager.setCurrentItem(pos);

        PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.show_image_pager_tab_strip);
        strip.setTabIndicatorColor(Color.parseColor(getString(R.color.pagerTabText)));
        strip.setDrawFullUnderline(true);
    }

    public void image_option_click(View v){
        currentUrl = urls[pager.getCurrentItem()];
        new AlertDialog.Builder(this)
                .setItems(new String[]{"ブラウザで開く", "保存する"}, new OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        if(which == 0){
                            new ChromeIntent(ImageFragmentActivity.this, Uri.parse(currentUrl));
                        }else if(which == 1){
                            saveImage();
                        }
                    }
                }).show();
    }

    public void saveImage(){
        if(!hasWriteExternalStoragePermission()){
            requestWriteExternalStoragePermission();
            return;
        }
        if(type == TYPE_BANNER){
            Matcher banner = Regex.userBannerUrl.matcher(currentUrl);
            if(!banner.find()){
                new ShowToast(getApplicationContext(), R.string.urlNotMatchPatternDontSave);
                return;
            }
            byte[] non_orig_image = ((ImageFragment)adapter.getItem(pager.getCurrentItem())).getNonOrigImage();
            save(banner.group(2), ".jpg", non_orig_image, false);
            return;
        }

        String orig = "";
        if(type != TYPE_ICON){
            orig = ":orig";
        }

        final Matcher pattern = Regex.userIconUrl.matcher(currentUrl);
        if(!pattern.find()){
            new ShowToast(getApplicationContext(), R.string.urlNotMatchPatternDontSave);
            return;
        }

        new AsyncTask<String, Void, byte[]>(){
            private ProgressDialog progDialog;

            @Override
            protected void onPreExecute(){
                progDialog = new ProgressDialog(ImageFragmentActivity.this);
                progDialog.setMessage("Loading...");
                progDialog.setIndeterminate(false);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setCancelable(true);
                progDialog.show();
            }

            @Override
            protected byte[] doInBackground(String... params){
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
                    byte[] result = bout.toByteArray();
                    is.close();
                    bout.close();
                    connection.disconnect();
                    return result;
                }catch(IOException e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final byte[] result){
                if(result != null){
                    progDialog.dismiss();
                    if(type == TYPE_ICON){
                        save(pattern.group(2), pattern.group(3), result, false);
                    }else{
                        save(pattern.group(2), pattern.group(3), result, true);
                    }
                }else{
                    new ShowToast(getApplicationContext(), R.string.error_getOriginalImage);
                }
            }
        }.execute(currentUrl + orig);
    }

    public void save(final String fileName, final String type, final byte[] byteImage, final boolean isOriginal){
        final String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;
        final String imgPath = saveDir + "/" + fileName + type;

        if(new File(imgPath).exists()){
            new AlertDialog.Builder(this)
                    .setTitle("エラー:ファイルが既に存在しています")
                    .setItems(new String[]{"上書き", "ファイル名を指定して保存", "キャンセル"}, new OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            if(which == 0){
                                output(imgPath, byteImage, isOriginal);
                            }else if(which == 1){
                                final EditText edit = new EditText(ImageFragmentActivity.this);
                                edit.setText(fileName);
                                new AlertDialog.Builder(ImageFragmentActivity.this)
                                        .setTitle("ファイル名を指定してください")
                                        .setView(edit)
                                        .setNegativeButton("キャンセル", null)
                                        .setPositiveButton("OK", new OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which){
                                                String newPath = saveDir + "/" + edit.getText().toString() + type;
                                                if(new File(newPath).exists()){
                                                    save(fileName, type, byteImage, isOriginal);
                                                }else{
                                                    output(newPath, byteImage, isOriginal);
                                                }
                                            }
                                        }).show();
                            }
                        }
                    }).show();
        }else{
            output(imgPath, byteImage, isOriginal);
        }
    }

    public void output(String imgPath, byte[] byteImage, boolean isOriginal){
        FileOutputStream fos;
        try{
            fos = new FileOutputStream(imgPath, true);
            fos.write(byteImage);
            fos.close();
        }catch(IOException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        if(isOriginal){
            Toast.makeText(this, "オリジナルを保存しました\n" + imgPath, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "保存しました\n" + imgPath, Toast.LENGTH_LONG).show();
        }
    }

    public boolean hasWriteExternalStoragePermission(){
        int writeExternalStorage = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return writeExternalStorage == PackageManager.PERMISSION_GRANTED;
    }

    public void requestWriteExternalStoragePermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 364);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != 364){
            return;
        }
        if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            saveImage();
        }else{
            new ShowToast(getApplicationContext(), R.string.permission_rejected);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        app.getUseTime().start();
    }

    @Override
    public void onPause(){
        super.onPause();
        app.getUseTime().stop();
    }

}
