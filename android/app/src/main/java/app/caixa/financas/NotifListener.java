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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê as notificações que mencionam dinheiro (R$ + palavra de transação) e guarda para o Caixa.
 * Apps de conversa e redes sociais são ignorados para não capturar mensagens pessoais.
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

    private static final String CHANNEL = "capturas";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String pkg = sbn.getPackageName();
            if (pkg == null || pkg.equals(getPackageName()) || SKIP.contains(pkg)) return;
            Notification n = sbn.getNotification();
            if (n == null || (n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
            Bundle e = n.extras;
            if (e == null) return;
            CharSequence title = e.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = e.getCharSequence(Notification.EXTRA_BIG_TEXT);
            if (text == null) text = e.getCharSequence(Notification.EXTRA_TEXT);
            String all = ((title != null ? title + "\n" : "") + (text != null ? text : "")).trim();
            if (all.isEmpty()) return;
            Matcher m = MONEY.matcher(all);
            if (!m.find() || !WORDS.matcher(all).find()) return;

            String app = pkg;
            try {
                PackageManager pm = getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                app = String.valueOf(pm.getApplicationLabel(ai));
            } catch (Exception ignored) { }

            if (NotifStore.add(this, pkg, app, all, sbn.getPostTime())) {
                NotifStore.ping();
                alert(app, m.group());
            }
        } catch (Exception ignored) {
            // Nunca deixar uma notificação estranha derrubar o serviço.
        }
    }

    /** Aviso do próprio Caixa: "Gasto capturado". Toque abre o app para revisar. */
    private void alert(String app, String value) {
        if (!NotifStore.alertsOn(this) || NotifStore.listener != null) return;
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm == null) return;
        nm.createNotificationChannel(new NotificationChannel(CHANNEL, "Lançamentos capturados", NotificationManager.IMPORTANCE_LOW));
        Intent open = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        int count = NotifStore.count(this);
        Notification note = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_caixa)
                .setContentTitle(count > 1 ? count + " lançamentos para revisar" : "Lançamento capturado: " + value)
                .setContentText("De " + app + ". Toque para revisar no Caixa.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        nm.notify(1, note);
    }
}
