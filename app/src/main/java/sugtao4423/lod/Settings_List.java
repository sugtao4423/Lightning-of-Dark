package sugtao4423.lod;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import sugtao4423.lod.utils.DBUtil;
import sugtao4423.lod.utils.Utils;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;

public class Settings_List extends AppCompatActivity{

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        app = (App)getApplicationContext();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
    }

    public static class MyPreferencesFragment extends PreferenceFragment{

        private Preference select_List, startApp_loadList;
        private DBUtil dbUtil;
        private String myScreenName;
        private App app;

        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_list);

            select_List = findPreference("select_List");
            startApp_loadList = findPreference("startApp_loadList");

            app = (App)getActivity().getApplicationContext();
            dbUtil = app.getAccountDBUtil();
            myScreenName = app.getCurrentAccount().getScreenName();

            setSummary();

            startApp_loadList.setOnPreferenceClickListener(new OnPreferenceClickListener(){

                @Override
                public boolean onPreferenceClick(Preference preference){
                    final String[] selectedListNames = dbUtil.getSelectListNames(myScreenName);
                    boolean[] selectedLoadList = new boolean[selectedListNames.length];
                    final ArrayList<String> selectLoadList = new ArrayList<String>();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.choose_app_start_load_list)
                            .setMultiChoiceItems(selectedListNames, selectedLoadList, new OnMultiChoiceClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked){
                                    if(isChecked){
                                        selectLoadList.add(selectedListNames[which]);
                                    }else{
                                        selectLoadList.remove(selectedListNames[which]);
                                    }
                                }
                            }).setPositiveButton(R.string.ok, new OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    dbUtil.updateStartAppLoadLists(Utils.implode(selectLoadList), myScreenName);
                                    app.resetAccount();
                                    setSummary();
                                }
                            }).setNegativeButton(R.string.cancel, null);
                    if(!selectedListNames[0].equals("")){
                        builder.show();
                    }else{
                        new ShowToast(getActivity().getApplicationContext(), R.string.list_not_selected);
                    }
                    return false;
                }
            });

            select_List.setOnPreferenceClickListener(new OnPreferenceClickListener(){

                @Override
                public boolean onPreferenceClick(Preference preference){
                    new AsyncTask<Void, Void, ResponseList<UserList>>(){
                        @Override
                        protected ResponseList<UserList> doInBackground(Void... params){
                            try{
                                return app.getTwitter().getUserLists(myScreenName);
                            }catch(TwitterException e){
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(ResponseList<UserList> result){
                            if(result == null){
                                new ShowToast(getActivity().getApplicationContext(), R.string.error_get_list);
                                return;
                            }
                            final HashMap<String, Long> listMap = new HashMap<String, Long>();
                            for(UserList l : result){
                                listMap.put(l.getName(), l.getId());
                            }

                            final String[] listItem = (String[])listMap.keySet().toArray(new String[0]);
                            final LinkedHashMap<String, Long> checkedList = new LinkedHashMap<String, Long>();

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.choose_list)
                                    .setMultiChoiceItems(listItem, new boolean[listItem.length], new OnMultiChoiceClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which, boolean isChecked){
                                            if(isChecked){
                                                checkedList.put(listItem[which], listMap.get(listItem[which]));
                                            }else{
                                                checkedList.remove(listItem[which]);
                                            }
                                        }
                                    }).setPositiveButton(R.string.ok, new OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    String[] checkedListNames = checkedList.keySet().toArray(new String[0]);
                                    Long[] checkedListIds = checkedList.values().toArray(new Long[0]);

                                    dbUtil.updateSelectListNames(Utils.implode(checkedListNames), myScreenName);
                                    dbUtil.updateSelectListIds(Utils.implode(checkedListIds), myScreenName);
                                    app.resetAccount();
                                    setSummary();
                                }
                            }).show();
                        }
                    }.execute();
                    return false;
                }
            });
        }

        public void setSummary(){
            String[] selectList = dbUtil.getSelectListNames(myScreenName);
            String[] startAppLoadList = dbUtil.getNowStartAppLoadList(myScreenName);
            String summary1 = getString(R.string.param_setting_value_str, Utils.implode(selectList));
            String summary2 = getString(R.string.param_setting_value_str, Utils.implode(startAppLoadList));
            select_List.setSummary(summary1);
            startApp_loadList.setSummary(summary2);
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