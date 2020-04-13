package uk.co.openmoments.hillbagging.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.co.openmoments.hillbagging.interfaces.DialogFragmentListener;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private DialogFragmentListener callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        callback.receiveResult(format.format(calendar.getTime()));
    }

    public void setCallback(DialogFragmentListener callback) {
        this.callback = callback;
    }
}
