package app.caixa.financas;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Lê as notificações que mencionam dinheiro (R$ + palavra de transação) e anota no app,
 * mesmo com o app fechado. Apps de conversa e redes sociais são ignorados.
 */
public class NotifListener extends NotificationListenerService {

    private static final Set<String> SKIP = new HashSet<>(Arrays.asList(
            "com.whatsapp", "com.whatsapp.w4b", "org.telegram.messenger", "org.thoughtcrime.securesms",
            "com.instagram.android", "com.facebook.orca", "com.facebook.katana", "com.twitter.android",
            "com.discord", "com.google.android.gm", "com.microsoft.office.outlook", "com.google.android.apps.messaging",
            "com.samsung.android.messaging", "com.android.mms", "com.zhiliaoapp.musically", "com.ss.android.ugc.trill",
            "com.google.android.youtube", "com.snapchat.android", "com.linkedin.android", "com.reddit.frontpage"));

    private static final Pattern MONEY = Pattern.compile("R\\$\\s?-?[\\d.]+(,\\d{2})?");
    private static final Pattern WORDS = Pattern.compile(
            "(?i)(compra|pix|transfer|pagamento|pagou|pago|recebeu|recebid|d[eé]bito|cr[eé]dito|cart[aã]o|enviou|enviad|dep[oó]sito|saque|boleto|estorno|fatura|assinatura|cobran)");
    private static final Pattern NOT_TX = Pattern.compile(
            "(?i)(fatura (fechou|fechada|dispon[ií]vel|vence)|limite dispon[ií]vel|oferta|promo[cç][aã]o|empr[eé]stimo pr[eé]-aprovado|cashback dispon[ií]vel|c[oó]digo de (verifica|seguran)|token)");

    static final String CHANNEL = "capturas";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String pkg = sbn.getPackageName();
            if (pkg == null || pkg.equals(getPackageName()) || SKIP.contains(pkg)) return;
            if (Prefs.blocked(this, pkg)) return;
            Notification n = sbn.getNotification();
            if (n == null || (n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
            Bundle e = n.extras;
            if (e == null) return;
            CharSequence title = e.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = e.getCharSequence(Notification.EXTRA_BIG_TEXT);
            if (text == null) text = e.getCharSequence(Notification.EXTRA_TEXT);
            String all = ((title != null ? title + "\n" : "") + (text != null ? text : "")).trim();
            if (all.isEmpty() || !MONEY.matcher(all).find() || !WORDS.matcher(all).find() || NOT_TX.matcher(all).find()) return;

            String app = pkg;
            try {
                PackageManager pm = getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                app = String.valueOf(pm.getApplicationLabel(ai));
            } catch (Exception ignored) { }

            JSONObject parsed = NotifParser.parse(all, sbn.getPostTime());
            boolean auto = parsed != null && parsed.optBoolean("confident") && NotifStore.autoOn(this);
            JSONObject item = NotifStore.add(this, pkg, app, all, sbn.getPostTime(), parsed, auto);
            if (item == null) return;
            NotifStore.ping();
            alert(item, parsed, auto, app);
        } catch (Exception ignored) {
            // Nunca deixar uma notificação estranha derrubar o serviço.
        }
    }

    /** Aviso do próprio app: "Anotado" (automático) ou "Para revisar". */
    private void alert(JSONObject item, JSONObject parsed, boolean auto, String app) {
        if (!NotifStore.alertsOn(this) || NotifStore.listener != null) return;
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm == null) return;
        nm.createNotificationChannel(new NotificationChannel(CHANNEL, getString(R.string.channel_capturas), NotificationManager.IMPORTANCE_LOW));

        String id = item.optString("id");
        int notifId = id.hashCode();
        Intent open = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String value = parsed != null ? NotifParser.money(parsed.optLong("amount")) : "";
        String desc = parsed != null ? parsed.optString("desc") : "";
        boolean in = parsed != null && "in".equals(parsed.optString("type"));
        String title = auto
                ? (in ? "Entrada anotada: " : "Gasto anotado: ") + value
                : (in ? "Entrada para revisar: " : "Gasto para revisar: ") + value;
        String body = (desc.isEmpty() ? "" : desc + " · ") + "de " + app + (auto ? "" : ". Toque para revisar.");

        Notification.Builder b = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_caixa)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pi)
                .setAutoCancel(true);
        if (auto) {
            Intent undo = new Intent(this, UndoReceiver.class).putExtra("id", id).putExtra("notif", notifId);
            PendingIntent upi = PendingIntent.getBroadcast(this, notifId, undo, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            b.addAction(new Notification.Action.Builder(null, "Desfazer", upi).build());
        }
        nm.notify(notifId, b.build());
    }
}
