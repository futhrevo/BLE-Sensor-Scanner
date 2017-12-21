package in.hedera.reku.sensorscanner;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by rakeshkalyankar on 18/12/17.
 * http://www.devexchanges.info/2016/03/modal-bottom-sheet-with-material-design.html
 */

public class SettingsDialogFragment extends BottomSheetDialogFragment {

    SeekBar seekbar;
    TextView tv_scan_val;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.bottom_settings, null);
        tv_scan_val = contentView.findViewById(R.id.settings_seek_value);
        seekbar = contentView.findViewById(R.id.seekBar2);
        int oldprogress = readScanVlaue();
        seekbar.setProgress(oldprogress);
        tv_scan_val.setText(processProgress(oldprogress));
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_scan_val.setText(processProgress(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }


    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    private void storeScanvalue(int progress){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("SCAN_TIME",progress);
        editor.apply();
    }

    private int readScanVlaue(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getInt("SCAN_TIME", 0);
    }

    private String processProgress(int progress){
        storeScanvalue(progress);
        int MIN = 0;
        if(progress == MIN){
            progress = 10;
        }else if(progress == 100){
            return "∞";
        }else{
            progress = progress * 10;
        }
        return Integer.toString(progress);
    }
}
