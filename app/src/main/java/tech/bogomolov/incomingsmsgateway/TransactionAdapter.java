package tech.bogomolov.incomingsmsgateway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class TransactionAdapter extends ArrayAdapter<SmsTransaction> {

    public TransactionAdapter(@NonNull Context context, ArrayList<SmsTransaction> data) {
        super(context, R.layout.transaction_item, data);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.transaction_item, parent, false);
        }

        SmsTransaction tx = getItem(position);

        ImageView icon = convertView.findViewById(R.id.icon_provider);
        TextView provider = convertView.findViewById(R.id.text_provider);
        TextView amount = convertView.findViewById(R.id.text_amount);
        TextView summary = convertView.findViewById(R.id.text_summary);
        TextView date = convertView.findViewById(R.id.text_date);
        View directionDot = convertView.findViewById(R.id.direction_dot);

        provider.setText(tx.provider);
        amount.setText(tx.amount.isEmpty() ? "—" : tx.amount);
        summary.setText(tx.summary);
        date.setText(tx.getFormattedDate());

        // Provider icon
        switch (tx.provider) {
            case "Telebirr":
                icon.setImageResource(R.drawable.ic_telebirr);
                break;
            case "CBE":
                icon.setImageResource(R.drawable.ic_cbe);
                break;
            case "Dashen Bank":
                icon.setImageResource(R.drawable.ic_dashen);
                break;
            case "HelloCash":
                icon.setImageResource(R.drawable.ic_hellocash);
                break;
            default:
                icon.setImageResource(R.drawable.ic_sms_generic);
                break;
        }

        // Direction dot + amount color
        if ("in".equals(tx.direction)) {
            directionDot.setBackgroundColor(getContext().getColor(R.color.colorSuccess));
            amount.setTextColor(getContext().getColor(R.color.colorSuccess));
            amount.setText("+" + amount.getText());
        } else if ("out".equals(tx.direction)) {
            directionDot.setBackgroundColor(getContext().getColor(R.color.colorDanger));
            amount.setTextColor(getContext().getColor(R.color.colorDanger));
            amount.setText("−" + amount.getText());
        } else {
            directionDot.setBackgroundColor(getContext().getColor(R.color.colorEmptyIcon));
            amount.setTextColor(getContext().getColor(R.color.colorEmptyIcon));
        }

        return convertView;
    }
}
