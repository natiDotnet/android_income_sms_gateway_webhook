package tech.bogomolov.incomingsmsgateway;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Global base server URL setting (issue #125). When set, the webhook URL
 * entered in a forwarding rule can be a partial path (e.g. /webhook/sms)
 * and the base URL is prepended automatically (e.g. https://example.com +
 * /webhook/sms = https://example.com/webhook/sms).
 *
 * Stored in its own SharedPreferences file, separate from the forwarding rules.
 */
public class BaseServerSettings {

    private static final String PREFERENCE = "base_server";
    private static final String KEY_URL = "base_url";

    private final String url;

    public BaseServerSettings(String url) {
        this.url = url == null ? "" : url;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isSet() {
        return !this.url.isEmpty();
    }

    /**
     * Resolves a user-entered URL against the base server. If the URL already
     * has a scheme (http:// or https://) it is returned unchanged. Otherwise
     * the base URL is prepended (with a single separating slash).
     */
    public String resolve(String partialOrFullUrl) {
        String url = partialOrFullUrl == null ? "" : partialOrFullUrl.trim();
        if (url.isEmpty()) {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        if (!isSet()) {
            return url;
        }
        String base = this.url.trim();
        // Normalize: strip trailing slash from base, leading slash from path.
        base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String path = url.startsWith("/") ? url.substring(1) : url;
        return base + "/" + path;
    }

    public static BaseServerSettings load(Context context) {
        SharedPreferences pref = getPreference(context);
        return new BaseServerSettings(pref.getString(KEY_URL, ""));
    }

    public void save(Context context) {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.putString(KEY_URL, this.url);
        editor.commit();
    }

    public void clear(Context context) {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.remove(KEY_URL);
        editor.commit();
    }

    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
    }
}
