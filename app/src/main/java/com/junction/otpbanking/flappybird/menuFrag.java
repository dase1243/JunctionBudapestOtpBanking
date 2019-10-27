package com.junction.otpbanking.flappybird;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.junction.otpbanking.FinishTripActivity;
import com.junction.otpbanking.R;

import java.util.Objects;

public class menuFrag extends Fragment {

    public menuFrag() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("menuFrag", "Created");
        final View rootView = inflater.inflate(R.layout.menu_layout, container, false);
        Button playButton = rootView.findViewById(R.id.play);
        Button btnGoBack = rootView.findViewById(R.id.btnGoBack);
        TextView tvYouWon = rootView.findViewById(R.id.tvYouWon);

        int score = ((Activity) Objects.requireNonNull(getContext())).getIntent().getIntExtra("score", -1);
        if (score != -1) {
            if (score > 5) {
                tvYouWon.setText("You won %Company% promo code: PROMO_CODE");
            } else {
                tvYouWon.setText("More lucky next time");
            }
        }
        btnGoBack.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FinishTripActivity.class);
            intent.putExtra("take", getActivity().getIntent().getBooleanExtra("take", false));
            intent.putExtra("put", getActivity().getIntent().getBooleanExtra("put", false));
            intent.putExtra("amount", getActivity().getIntent().getDoubleExtra("amount", -1));
            getActivity().finish();
            startActivity(intent);
        });

        playButton.setOnClickListener(play);
        return rootView;
    }


    //switches to game fragment
    View.OnClickListener play = v -> {
        gameFragment gameFrag = new gameFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame, gameFrag).addToBackStack(null).commit();
    };

}
