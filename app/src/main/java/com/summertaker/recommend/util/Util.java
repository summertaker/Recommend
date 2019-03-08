package com.summertaker.recommend.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.summertaker.recommend.R;
import com.summertaker.recommend.common.BaseApplication;
import com.summertaker.recommend.common.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Util {

    private static String TAG = "== Util";

    public static void setBaseStatusBar(Activity activity) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
    }

    public static void alert(final Context context, String title, String message, final Activity activity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        if (title != null) {
            alert.setTitle(title);
        }
        alert.setMessage(message);
        if (activity == null) {
            alert.setCancelable(true);
        } else {
            alert.setPositiveButton(context.getString(R.string.finish), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    activity.finish();
                }
            });
        }
        alert.setNegativeButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    public static void showKeyboard(Context context, View view) {
        //InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //if (imm != null) {
        //    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        //}
        view.requestFocus();
        ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static String getString(JSONObject json, String key) {
        try {
            String str = json.getString(key);
            if ("null".equals(str)) {
                str = "";
            }
            return str;
        } catch (JSONException e) {
            return "";
        }
    }

    public static int getInt(JSONObject json, String key) {
        try {
            return json.getInt(key);
        } catch (JSONException e) {
            return 0;
        }
    }

    public static boolean getBoolean(JSONObject json, String key) {
        try {
            return json.getBoolean(key);
        } catch (JSONException e) {
            return false;
        }
    }

    public static double getDouble(JSONObject json, String key) {
        try {
            return json.getDouble(key);
        } catch (JSONException e) {
            return 0.0;
        }
    }

    public static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        String s = text;
        s = s.replaceAll("\\(\\d+\\)", "");
        s = s.replaceAll("^[<br>\\n*]+", "");
        s = s.replaceAll("<br>\\n*<br>\\n*[<br>\\n*]+", "<br>");
        //s = s.replaceAll("[\r\n]+", "\n");
        return s;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static String getUrlToFileName(String url) {
        return url.replaceAll("[^A-Za-z0-9]", "");
    }

    public static boolean isFileExists(String fileName) {
        File file = new File(BaseApplication.getDataPath(), fileName);
        return file.exists();
    }

    public static String readFile(String fileName) {
        String data = "";
        File file = new File(BaseApplication.getDataPath(), fileName);
        if (file.exists()) {
            StringBuilder builder = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    //builder.append('\n');
                }
                reader.close();
                data = builder.toString();
                //Log.e(TAG, "READ: " + file.getAbsolutePath());
                //Log.e(TAG, "DATA" + data);
            } catch (IOException e) {
                Log.e(TAG, "FILE: " + file.getAbsolutePath());
                Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "FILE: " + fileName);
            Log.e(TAG, "ERROR: file not exists.");
        }

        return data;
    }

    public static void writeFile(Context context, String fileName, String data) {
        String path = BaseApplication.getDataPath();
        File dir = new File(path);
        boolean success = true;
        if (!dir.exists()) {
            success = dir.mkdirs();
        }
        if (success) {
            File file = new File(path, fileName);
            if (file.isFile()) {
                success = file.delete();
                if (!success) {
                    String msg = "ERROR: file.delete() failed.";
                    Log.e(TAG, msg);
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }
            if (success) {
                try {
                    success = file.createNewFile();
                    if (success) {
                        FileOutputStream fOut = new FileOutputStream(file);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        myOutWriter.append(data);
                        myOutWriter.close();
                        fOut.flush();
                        fOut.close();
                        //Log.e(TAG, "WRITE: " + file.getAbsolutePath());
                        //Log.e(TAG, "DATA: " + data);
                    } else {
                        String msg = "ERROR: file.createNewFile() failed.";
                        Log.e(TAG, msg);
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "FILE: " + file.getAbsolutePath());
                    Log.e(TAG, "ERROR: " + e.getMessage());
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            String msg = "ERROR: dir.mkdirs() failed.";
            Log.e(TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    public static String getToday(String format) {
        // 2016년 3월 22일 (금)
        //Date now = new Date();
        //DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        //String today = df.format(now);

        // 2016년 3월 22일 화요일 오후 10:45
        //String dateTime = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(now);

        Calendar calendar = Calendar.getInstance();
        if (format == null) {
            format = "yyyy-MM-dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static void addNumberFormat(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            boolean isEditing;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isEditing) return;

                String str = editable.toString();
                if (str.isEmpty()) return;

                isEditing = true;

                str = str.replaceAll(",", "");
                int val = Integer.valueOf(str);
                editable.replace(0, editable.length(), Config.NUMBER_FORMAT.format(val));

                isEditing = false;
            }
        });
    }

    /*
    public static boolean isSameDate(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null...");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDate(cal1, cal2);
    }

    public static boolean isSameDate(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
    */

    // 카카오 스탁 앱 실행가기
    public static void startKakaoStock(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.dunamu.stockplus");
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    // 카카오 스탁 앱 내부 액티비티 실행하기
    public static void startKakaoStockDeepLink(Context context, String code) {
        startKakaoStockDeepLink(context, code, 0, 1); // marketIndex: 0=시세, 1=차트
    }

    // 카카오 스탁 앱 내부 액티비티 실행하기
    public static void startKakaoStockDeepLink(Context context, String code, int tabIndex, int marketIndex) {
        // 카카오 스탁 - 내부 링크 (앱 화면 순서대로)
        // tabIndex: 0=시세, 1=뉴스/공시, 2=객장, 3=수익/노트, 4=종목정보
        // marketIndex: [시세] 0=호가, 1=차트, 2=체결, 3=일별, 4=거래원, 5=투자자

        // 카카오 스탁: 시세 > 호가
        String url = "stockplus://viewStock?code=A" + code + "&tabIndex=" + tabIndex + "&marketIndex=" + marketIndex;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
