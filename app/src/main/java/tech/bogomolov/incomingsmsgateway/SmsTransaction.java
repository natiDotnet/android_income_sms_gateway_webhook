package tech.bogomolov.incomingsmsgateway;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parsed SMS transaction extracted from the device inbox.
 * Detects the provider (Telebirr, CBE, Dashen, etc.) and pulls out
 * amount, currency, direction and a short summary for display.
 */
public class SmsTransaction {
    public final String address;
    public final String body;
    public final long date;
    public final String provider;
    public final String amount;
    public final String currency;
    public final String direction; // "in" or "out"
    public final String summary;

    private static final Pattern AMOUNT_ETB = Pattern.compile("ETB\\s*([\\d,.]+)");
    private static final Pattern AMOUNT_BIRR = Pattern.compile("([\\d,.]+)\\s*birr", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREDIT = Pattern.compile("(?i)(received|deposited|credited|recharge|top up)");
    private static final Pattern DEBIT = Pattern.compile("(?i)(sent|transferred|withdraw|debited|purchase|paid)");

    public SmsTransaction(String address, String body, long date) {
        this.address = address == null ? "" : address;
        this.body = body == null ? "" : body;
        this.date = date;

        this.provider = detectProvider(this.address, this.body);
        this.amount = extractAmount(this.body);
        this.currency = "ETB";
        this.direction = detectDirection(this.body);
        this.summary = buildSummary();
    }

    private static String detectProvider(String address, String body) {
        String lower = body.toLowerCase(Locale.ROOT);
        if ("127".equals(address) || lower.contains("telebirr")) {
            return "Telebirr";
        }
        if (lower.contains("cbe") || lower.contains("commercial bank")) {
            return "CBE";
        }
        if (lower.contains("dashen")) {
            return "Dashen Bank";
        }
        if (lower.contains("hellocash")) {
            return "HelloCash";
        }
        if (lower.contains("amole")) {
            return "Amole";
        }
        if (lower.contains("e-birr") || lower.contains("ebirr")) {
            return "E-Birr";
        }
        if (address != null && !address.isEmpty()) {
            return address;
        }
        return "Unknown";
    }

    private static String extractAmount(String body) {
        Matcher m = AMOUNT_ETB.matcher(body);
        if (m.find()) {
            return m.group(1);
        }
        m = AMOUNT_BIRR.matcher(body);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private static String detectDirection(String body) {
        if (DEBIT.matcher(body).find()) {
            return "out";
        }
        if (CREDIT.matcher(body).find()) {
            return "in";
        }
        return "unknown";
    }

    private String buildSummary() {
        if (!amount.isEmpty()) {
            return direction.equals("in")
                    ? "Received " + currency + " " + amount
                    : "Sent " + currency + " " + amount;
        }
        return body.length() > 60 ? body.substring(0, 57) + "..." : body;
    }

    public String getFormattedDate() {
        return new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date(date));
    }

    public boolean isTransaction() {
        return !amount.isEmpty() || CREDIT.matcher(body).find() || DEBIT.matcher(body).find();
    }
}
