package sugtao4423.lod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.loopj.android.image.WebImageCache;

import java.text.DecimalFormat;
import java.util.HashMap;

import sugtao4423.lod.utils.DBUtil;
import sugtao4423.lod.utils.Utils;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;

public class Settings extends AppCompatActivity{

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        app = (App)getApplicationContext();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
        getSupportActionBar().setTitle("設定");
    }

    public static class MyPreferencesFragment extends PreferenceFragment{

        private App app;
        private Preference autoLoadTLInterval;

        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            app = (App)getActivity().getApplicationContext();

            CheckBoxPreference listAsTL = (CheckBoxPreference)findPreference("listAsTL");
            autoLoadTLInterval = findPreference("autoLoadTLInterval");
            Preference listSetting = findPreference("listSetting");
            Preference clearCache = findPreference("clearCache");
            setCacheSize(clearCache);

            listAsTL.setChecked(app.getCurrentAccount().getListAsTL() > 0);
            listAsTL.setSummary(app.getCurrentAccount().getListAsTL() > 0 ? String.valueOf(app.getCurrentAccount().getListAsTL()) : null);
            listAsTL.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue){
                    boolean isCheck = Boolean.parseBoolean(newValue.toString());
                    return selectListAsTL(preference, isCheck);
                }
            });

            setAutoLoadTLIntervalSummary(app.getCurrentAccount().getAutoLoadTLInterval());
            autoLoadTLInterval.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference preference){
                    clickAutoLoadTLInterval(getActivity());
                    return false;
                }
            });

            listSetting.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference preference){
                    Intent intent = new Intent(getActivity(), Settings_List.class);
                    startActivity(intent);
                    return false;
                }
            });

            clearCache.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference preference){
                    new WebImageCache(getActivity().getApplicationContext()).clear();
                    setCacheSize(preference);
                    Toast.makeText(getActivity().getApplicationContext(), "キャッシュが削除されました", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

        public boolean selectListAsTL(final Preference preference, boolean isCheck){
            final DBUtil dbutil = app.getAccountDBUtil();
            if(!isCheck){
                new AlertDialog.Builder(getActivity())
                        .setTitle("解除しますか？")
                        .setPositiveButton("OK", new OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                dbutil.updateListAsTL(-1, app.getCurrentAccount().getScreenName());
                                dbutil.updateAutoLoadTLInterval(0, app.getCurrentAccount().getScreenName());
                                preference.setSummary(null);
                                setAutoLoadTLIntervalSummary(0);
                                app.reloadAccountFromDB();
                            }
                        }).setNegativeButton("Cancel", new OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        ((CheckBoxPreference)preference).setChecked(true);
                    }
                }).show();
                return true;
            }

            final HashMap<String, Long> listMap = new HashMap<String, Long>();
            new AsyncTask<Void, Void, ResponseList<UserList>>(){
                @Override
                protected ResponseList<UserList> doInBackground(Void... params){
                    try{
                        return app.getTwitter().getUserLists(app.getTwitter().getScreenName());
                    }catch(IllegalStateException | TwitterException e){
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(ResponseList<UserList> result){
                    if(result == null){
                        new ShowToast(getActivity().getApplicationContext(), R.string.error_getList);
                        return;
                    }
                    for(UserList l : result){
                        listMap.put(l.getName(), l.getId());
                    }
                    final String[] listNames = (String[])listMap.keySet().toArray(new String[0]);
                    new AlertDialog.Builder(getActivity())
                            .setTitle("TLとして読み込むリストを選択してください")
                            .setCancelable(false)
                            .setItems(listNames, new OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    long selectedListId = listMap.get(listNames[which]);
                                    dbutil.updateListAsTL(selectedListId, app.getCurrentAccount().getScreenName());
                                    preference.setSummary(String.valueOf(selectedListId));
                                    app.reloadAccountFromDB();
                                }
                            }).show();
                }
            }.execute();
            return true;
        }

        public void clickAutoLoadTLInterval(final Context context){
            final EditText intervalEdit = new EditText(context);
            intervalEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
            FrameLayout editContainer = new FrameLayout(context);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = Utils.convertDpToPx(context, 24);
            params.leftMargin = margin;
            params.rightMargin = margin;
            intervalEdit.setLayoutParams(params);
            editContainer.addView(intervalEdit);

            new AlertDialog.Builder(context)
                    .setMessage("自動で取得する間隔を秒で入力してください")
                    .setView(editContainer)
                    .setPositiveButton("OK", new OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            if(intervalEdit.getText().toString().isEmpty()){
                                return;
                            }
                            boolean isListAsTL = app.getCurrentAccount().getListAsTL() > 0;
                            int interval = Integer.parseInt(intervalEdit.getText().toString());
                            if(!isListAsTL && interval > 0 && interval < 60){
                                new ShowToast(context, R.string.error_autoLoadTLInterval, Toast.LENGTH_LONG);
                                return;
                            }
                            app.getAccountDBUtil().updateAutoLoadTLInterval(interval, app.getCurrentAccount().getScreenName());
                            setAutoLoadTLIntervalSummary(interval);
                            app.reloadAccountFromDB();
                        }
                    }).show();
        }

        public void setAutoLoadTLIntervalSummary(int interval){
            autoLoadTLInterval.setSummary("設定値: " + interval + "  (0: 無効)");
        }

        public void setCacheSize(final Preference clearCache){
            new AsyncTask<Void, Void, String>(){
                @Override
                protected String doInBackground(Void... params){
                    DecimalFormat df = new DecimalFormat("#.#");
                    df.setMinimumFractionDigits(2);
                    df.setMaximumFractionDigits(2);
                    return df.format((double)new WebImageCache(getActivity().getApplicationContext()).getCacheSize() / 1024 / 1024) + "MB";
                }

                @Override
                protected void onPostExecute(String result){
                    clearCache.setSummary("キャッシュ: " + result);
                }
            }.execute();
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        app.loadOption();
    }

}