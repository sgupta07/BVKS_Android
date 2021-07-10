package com.iskcon.bvks.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iskcon.bvks.R;
import com.iskcon.bvks.databinding.LayoutDropDownSortBinding;
import com.iskcon.bvks.model.Lecture;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Amandeep Singh
 * @date 18 /11/2020
 */

public class Utils {
    private static Utils ourInstance = new Utils();
    private BottomSheetDialog mBsDropDown;

    private Utils() {
    }

    public static Utils getInstance() {
        return ourInstance;
    }

    public static void showDatePicker(Context context, @Nullable Long selectedDate,DatePickerListener listener) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        if (selectedDate != null && selectedDate > 0) {
            c.setTimeInMillis(selectedDate);
        }
        int mYear, mMonth, mDay, mHour, mMinute;
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.MyDatePickerStyle,
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    cal.set(Calendar.MONTH, monthOfYear);
                    cal.set(Calendar.YEAR, year);
                    listener.onDateSelected(cal.getTimeInMillis());
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public static int[] splitToComponentTimes(long millis) {
        long longVal = millis;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours, mins, secs};
        return ints;
    }

    public static int[] graphTimeManage(int seconds) {
        long longVal = seconds;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours, mins, secs};
        return ints;
    }

    public static String monthName(int month) {
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "Aug";
            case 9:
                return "Sept";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "";
        }
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static Date getDateFromMillis(long milliSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return calendar.getTime();
    }

    public final void showToast(Context context, String message) {
        if (message != null && context != null) {
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void hideKeyboard(Context context, View view) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void easyPrinter(String message, String className, Object obj) {
        System.out.println("Easy_Printer---------------------Start-------------------------------");
        if (obj != null) {
            System.out.println("Easy_Printer-" + className + "----->" + message + "<---->" + obj);
        } else {
            System.out.println("Easy_Printer-" + className + "---->" + message);
        }
        System.out.println("Easy_Printer---------------------End---------------------------------");
    }

    public void showSortDropDown(Activity activity, String tittle, String[] arrDropDown, int selectedPosition, DropDownClickListener dropDownSelectInterface) {
        mBsDropDown = new BottomSheetDialog(activity);
        @SuppressLint("InflateParams")
        LayoutDropDownSortBinding layoutDropDownSortBinding = DataBindingUtil.inflate(activity.getLayoutInflater(),
                R.layout.layout_drop_down_sort, null, false);
        mBsDropDown.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = mBsDropDown.getWindow().getAttributes();
        layoutParams.x = 100; // right margin
        layoutParams.y = 170; // top margin
        mBsDropDown.getWindow().setAttributes(layoutParams);
        mBsDropDown.setContentView(layoutDropDownSortBinding.getRoot());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        layoutDropDownSortBinding.tvHeading.setText(tittle);
        layoutDropDownSortBinding.rvDropDown.setLayoutManager(linearLayoutManager);
        layoutDropDownSortBinding.rvDropDown.setAdapter(new DropDownSortAdapter(arrDropDown, selectedPosition, new DropDownSortAdapter.DropDownClickListener() {
            @Override
            public void onClick(int position, String option) {
                dropDownSelectInterface.onSelect(position, option);
                if (mBsDropDown.isShowing()) {
                    mBsDropDown.dismiss();
                }
            }
        }));
        mBsDropDown.show();
    }

    public String createLectureDescriptionForShare(Lecture lecture) {
        SimpleDateFormat mDateFormat;
        mDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String descriptions = "";
        String duration = "• Duration: " + LectureUtil.getFormattedTime(lecture.mediaLength);
        String location = "\n• Location: " + lecture.place;
        String record = "\n• Date of Recording:" + mDateFormat.format(lecture.date);
        if (!TextUtils.isEmpty(lecture.verse)) {
            descriptions = duration + "\n•" + lecture.verse + record + location;
            Log.i("TAG_LINK_111111", descriptions);
        } else {
            descriptions = duration + record + location;
            Log.i("TAG_LINK_22222222", descriptions);
        }
        return descriptions;
    }

    public String getLectureThumbnailUrlForShare(Lecture lecture) {
        if (lecture != null && !TextUtils.isEmpty(lecture.thumbnailUrl)) {
            return lecture.thumbnailUrl;
        } else {
            return "https://bvks-d1ac.kxcdn.com/wp-content/uploads/2018/05/20151441/GM.jpg";
        }

    }

    public String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    public interface DropDownClickListener {
        void onSelect(int position, String option);
    }

    public interface DatePickerListener {
        void onDateSelected(long millis);
    }
}

