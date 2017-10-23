package com.weiba.web.sharelibrary.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.weiba.web.sharelibrary.R;


/**
 * Created by Leu_Z on 2015/11/25.
 */
public class SuccessDialogFragment extends DialogFragment {

    private String mArgument;
    public static final String ARGUMENT = "argument";



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // mArgument = getActivity().getIntent().getStringExtra(ARGUMENT);
        Bundle bundle = getArguments();
        if (bundle != null)
            mArgument = bundle.getString(ARGUMENT);

    }
    /**
     * 传入需要的参数，设置给arguments
     * @param argument
     * @return
     */
    public static SuccessDialogFragment newInstance(String argument)
    {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, argument);
        SuccessDialogFragment contentFragment = new SuccessDialogFragment();
        //给碎片传了一个bundle进去，在创建碎片时可以取出来。
        contentFragment.setArguments(bundle);
        return contentFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.download_success_dialog, container);

        TextView textView= (TextView) view.findViewById(R.id.text);
        textView.setText(mArgument);
        return view;
    }


}
