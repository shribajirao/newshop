package wrteam.ecart.shop.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AlertDialog;

import wrteam.ecart.shop.R;
import wrteam.ecart.shop.activity.MainActivity;


public class Session {
    public static final String PREFER_NAME = "eKart";
    final int PRIVATE_MODE = 0;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    public Session(Context context) {
        try {
            this._context = context;
            pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
            editor = pref.edit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCount(String id, int value, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(id, value);
        editor.apply();
    }

    public static int getCount(String id, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(id, 0);
    }

    public String getData(String id) {
        return pref.getString(id, "");
    }

    public String getCoordinates(String id) {
        return pref.getString(id, "0");
    }

    public void setData(String id, String val) {
        editor.putString(id, val);
        editor.commit();
    }

    public void setBoolean(String id, boolean val) {
        editor.putBoolean(id, val);
        editor.commit();
    }

    public boolean getBoolean(String id) {
        return pref.getBoolean(id, false);
    }

    public void createUserLoginSession(String profile, String fcmId, String id, String name, String email, String mobile, String password, String referCode) {
        editor.putBoolean(Constant.IS_USER_LOGIN, true);
        editor.putString(Constant.FCM_ID, fcmId);
        editor.putString(Constant.ID, id);
        editor.putString(Constant.NAME, name);
        editor.putString(Constant.EMAIL, email);
        editor.putString(Constant.MOBILE, mobile);
        editor.putString(Constant.PASSWORD, password);
        editor.putString(Constant.REFERRAL_CODE, referCode);
        editor.putString(Constant.PROFILE, profile);
        editor.commit();
    }

    public void logoutUser(Activity activity) {
        editor.clear();
        editor.commit();

        new Session(_context).setBoolean("is_first_time", true);

        Intent i = new Intent(activity, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Constant.FROM, "");
        activity.startActivity(i);
        activity.finish();
    }

    public void logoutUserConfirmation(final Activity activity) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(_context);
        alertDialog.setTitle(R.string.logout);
        alertDialog.setMessage(R.string.logout_msg);
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes, (dialog, which) -> {
            editor.clear();
            editor.commit();

            new Session(_context).setBoolean("is_first_time", true);

            Intent i = new Intent(activity, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(Constant.FROM, "");
            activity.startActivity(i);
            activity.finish();
        });

        alertDialog.setNegativeButton(R.string.no, (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();

    }

}