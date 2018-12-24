package com.eafricar.hyke;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class BottomSheetRadioGroup extends BottomSheetDialogFragment {
    String mTag;

    private RadioGroup mRadioGroup;

    public static BottomSheetRadioGroup newInstance(String tag){

        BottomSheetRadioGroup f = new BottomSheetRadioGroup();
        Bundle args = new Bundle();
        args.putString("TAG",tag);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTag = getArguments().getString("TAG");


            }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_radio_group, container, false);
        //TextView

        mRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.HykeShared);

        return view;
    }

}
