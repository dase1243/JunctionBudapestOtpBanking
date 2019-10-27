package com.junction.otpbanking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

public class AddressesListAdapter extends ArrayAdapter<Atm> {

    private int resourceLayout;
    private Context mContext;

    public AddressesListAdapter(Context context, int resource, List<Atm> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.custom_list_layout, null);
        }

        Atm atm = getItem(position);

        TextView tvAddress = v.findViewById(R.id.tvAddress);
        TextView tvSavedTime = v.findViewById(R.id.tvSavedTime);
        TextView tvEstimation = v.findViewById(R.id.tvEstimation);
        TextView tvPeopleInLine = v.findViewById(R.id.tvPeopleInLine);
        TextView tvMoneyPresence = v.findViewById(R.id.tvMoneyPresence);

        assert atm != null;

        tvAddress.setText(atm.getAddress());

        long routeTiming = atm.getRouteTiming();

        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR);
        int m = calendar.get(Calendar.MINUTE);
        int count = h * 2 + m / 30;
        long awaitingTime = atm.getLineCount()[count] / 15 * 90 - routeTiming;
        int finalTime = (int) ((routeTiming + (awaitingTime < 0 ? 0 : awaitingTime)) / 60);
        tvSavedTime.setText("Estimated spend time: " + finalTime);

        tvEstimation.setText("Time to get there by feet: " + atm.getRouteTiming() / 60);

        tvPeopleInLine.setText("People in line: " + atm.getLineCount()[count]);
        tvMoneyPresence.setText("Money inside ATM: " + atm.getMoneyPresence());

        return v;
    }

}
