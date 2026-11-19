package app.trocado;

import android.content.Context;

import org.json.JSONArray;

/** Pequenas preferências que o serviço de notificações precisa ler sem a página aberta. */
final class Prefs {
    private Prefs() {}

    static boolean blocked(Context c, String pkg) {
        try {
            JSONArray a = new JSONArray(NotifStore.prefs(c).getString("blocked", "[]"));
            for (int i = 0; i < a.length(); i++) if (pkg.equals(a.optString(i))) return true;
        } catch (Exception ignored) { }
        return false;
    }

    static void setBlocked(Context c, String json) {
        NotifStore.prefs(c).edit().putString("blocked", json == null ? "[]" : json).apply();
    }
}
