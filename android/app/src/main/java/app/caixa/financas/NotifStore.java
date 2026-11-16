package app.caixa.financas;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

/** Fila das notificações capturadas, guardada no celular até a página do app ler. */
final class NotifStore {
    private static final String PREFS = "caixa_notifs";
    private static final String QUEUE = "queue";
    private static final String SEEN = "seen";
    private static final int MAX_QUEUE = 300;
    private static final long DEDUPE_MS = 15 * 60 * 1000L;

    /** Chamado quando chega notificação nova com o app aberto. */
    static volatile Runnable listener;

    private NotifStore() {}

    static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Guarda a notificação. Devolve o item salvo, ou null se é repetida (o Android reenvia atualizações). */
    static synchronized JSONObject add(Context c, String pkg, String app, String text, long time, JSONObject parsed, boolean auto) {
        SharedPreferences p = prefs(c);
        try {
            String key = pkg + "|" + text.trim();
            JSONArray seen = new JSONArray(p.getString(SEEN, "[]"));
            JSONArray keep = new JSONArray();
            long now = System.currentTimeMillis();
            for (int i = 0; i < seen.length(); i++) {
                JSONObject s = seen.getJSONObject(i);
                if (now - s.optLong("t") > DEDUPE_MS) continue;
                if (key.equals(s.optString("k"))) return null;
                keep.put(s);
            }
            keep.put(new JSONObject().put("k", key).put("t", now));

            JSONObject item = new JSONObject()
                    .put("id", Long.toString(now, 36) + Integer.toString((int) (Math.random() * 1e6), 36))
                    .put("pkg", pkg)
                    .put("app", app)
                    .put("text", text)
                    .put("time", time)
                    .put("auto", auto);
            if (parsed != null) item.put("parsed", parsed);
            JSONArray q = new JSONArray(p.getString(QUEUE, "[]"));
            q.put(item);
            while (q.length() > MAX_QUEUE) q.remove(0);
            p.edit().putString(QUEUE, q.toString()).putString(SEEN, keep.toString()).apply();
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    /** Tira um item da fila (botão "Desfazer" do aviso). */
    static synchronized boolean remove(Context c, String id) {
        SharedPreferences p = prefs(c);
        try {
            JSONArray q = new JSONArray(p.getString(QUEUE, "[]"));
            JSONArray out = new JSONArray();
            boolean found = false;
            for (int i = 0; i < q.length(); i++) {
                JSONObject o = q.getJSONObject(i);
                if (id.equals(o.optString("id"))) { found = true; continue; }
                out.put(o);
            }
            p.edit().putString(QUEUE, out.toString()).apply();
            return found;
        } catch (Exception e) {
            return false;
        }
    }

    /** Entrega tudo o que está na fila e esvazia. */
    static synchronized String drain(Context c) {
        SharedPreferences p = prefs(c);
        String q = p.getString(QUEUE, "[]");
        p.edit().putString(QUEUE, "[]").apply();
        return q;
    }

    static synchronized int count(Context c) {
        try {
            return new JSONArray(prefs(c).getString(QUEUE, "[]")).length();
        } catch (Exception e) {
            return 0;
        }
    }

    static boolean alertsOn(Context c) { return prefs(c).getBoolean("alerts", true); }
    static void setAlerts(Context c, boolean on) { prefs(c).edit().putBoolean("alerts", on).apply(); }

    /** Modo automático: anota sem revisão (recurso Premium). A página informa o estado. */
    static boolean autoOn(Context c) { return prefs(c).getBoolean("auto", false) && Billing.isPremium(c); }
    static void setAuto(Context c, boolean on) { prefs(c).edit().putBoolean("auto", on).apply(); }

    static void ping() {
        Runnable r = listener;
        if (r != null) r.run();
    }
}
