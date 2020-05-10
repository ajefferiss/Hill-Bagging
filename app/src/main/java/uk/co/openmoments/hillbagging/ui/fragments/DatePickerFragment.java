package uk.co.openmoments.hillbagging.ui.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.co.openmoments.hillbagging.interfaces.DialogFragmentListener;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private DialogFragmentListener callback;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(requireActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        callback.receiveResult(format.format(calendar.getTime()));
    }

    public void setCallback(DialogFragmentListener callback) {
        this.callback = callback;
    }
}
