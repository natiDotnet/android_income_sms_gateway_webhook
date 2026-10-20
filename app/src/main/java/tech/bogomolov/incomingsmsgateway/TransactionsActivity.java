package tech.bogomolov.incomingsmsgateway;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class TransactionsActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE_READ_SMS = 1;

    private TransactionAdapter adapter;
    private ArrayList<SmsTransaction> transactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactions);

        ListView listView = findViewById(R.id.list_transactions);
        listView.setAdapter(adapter);

        TextView empty = findViewById(R.id.empty_transactions);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, PERMISSION_CODE_READ_SMS);
            empty.setText(getString(R.string.permission_read_sms_needed));
            empty.setVisibility(View.VISIBLE);
        } else {
            loadTransactions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE_READ_SMS) {
            TextView empty = findViewById(R.id.empty_transactions);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadTransactions();
            } else {
                empty.setText(getString(R.string.permission_read_sms_needed));
                empty.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadTransactions() {
        Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
        String[] projection = {
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
        };

        Cursor cursor = getContentResolver().query(
                uri,
                projection,
                null,
                null,
                Telephony.Sms.DATE + " DESC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String address = cursor.getString(
                            cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(
                            cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    long date = cursor.getLong(
                            cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));

                    SmsTransaction tx = new SmsTransaction(address, body, date);
                    // Only keep messages that look like transactions
                    if (tx.isTransaction()) {
                        transactions.add(tx);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        adapter.notifyDataSetChanged();

        TextView empty = findViewById(R.id.empty_transactions);
        if (transactions.isEmpty()) {
            empty.setText(getString(R.string.empty_transactions));
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
