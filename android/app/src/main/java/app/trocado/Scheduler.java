package app.trocado;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Avisos com data marcada (fatura fechou, vence amanhã, vence hoje...).
 * A página manda a lista sempre que os dados mudam; os alarmes funcionam com o app fechado
 * e são refeitos quando o celular reinicia.
 */
final class Scheduler {
    private static final String KEY = "schedule";

    private Scheduler() {}

    static synchronized void set(Context c, String json) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am == null) return;
        cancelAll(c, am);
        NotifStore.prefs(c).edit().putString(KEY, json == null ? "[]" : json).apply();
        arm(c, am, json);
    }

    /** Depois de reiniciar o celular. */
    static synchronized void restore(Context c) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am == null) return;
        arm(c, am, NotifStore.prefs(c).getString(KEY, "[]"));
    }

    private static void arm(Context c, AlarmManager am, String json) {
        try {
            JSONArray a = new JSONArray(json == null ? "[]" : json);
            long now = System.currentTimeMillis();
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                long at = o.optLong("at");
                if (at <= now) continue;
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent(c, o.optInt("id"), o.optString("title"), o.optString("text")));
            }
        } catch (Exception ignored) { }
    }

    private static void cancelAll(Context c, AlarmManager am) {
        try {
            JSONArray a = new JSONArray(NotifStore.prefs(c).getString(KEY, "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                am.cancel(intent(c, o.optInt("id"), "", ""));
            }
        } catch (Exception ignored) { }
    }

    private static PendingIntent intent(Context c, int id, String title, String text) {
        Intent i = new Intent(c, AlarmReceiver.class).putExtra("title", title).putExtra("text", text);
        return PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
