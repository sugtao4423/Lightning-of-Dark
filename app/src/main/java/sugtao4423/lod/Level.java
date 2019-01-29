package sugtao4423.lod;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Level{

    private SharedPreferences pref;
    private int experience;

    public Level(Context context){
        pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        experience = pref.getInt(Keys.EXPERIENCE, 0);
    }

    public int getLevel(){
        int level = 0;
        int exp = 0;
        for(; experience >= exp; level++){
            int i = level * 100;
            exp += (int)Math.log(i) * i;
        }
        return level - 1;
    }

    public int getNextExp(){
        int level = getLevel() + 1;
        int exp = 0;
        for(int i = 0; i < level; i++){
            int j = i * 100;
            exp += (int)Math.log(j) * j;
        }
        return exp - experience;
    }

    public int getTotalExp(){
        return experience;
    }

    /**
     * @param exp
     * @return is level up
     */
    public boolean addExp(int exp){
        int oldLevel = getLevel();
        experience += exp;
        pref.edit().putInt(Keys.EXPERIENCE, experience).commit();
        return oldLevel != getLevel();
    }

    /**
     * @return (0 - 150 random + level) exp
     */
    public int getRandomExp(){
        return (int)(Math.random() * 150.0D) + getLevel();
    }

}
