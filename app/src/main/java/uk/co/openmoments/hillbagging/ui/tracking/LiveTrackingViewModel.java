package uk.co.openmoments.hillbagging.ui.tracking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LiveTrackingViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LiveTrackingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}