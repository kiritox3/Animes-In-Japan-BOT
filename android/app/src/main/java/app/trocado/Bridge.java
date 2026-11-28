package app.trocado;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.webkit.JavascriptInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Funções que a página chama como window.CaixaAndroid.*. */
final class Bridge {
    private final MainActivity act;

    Bridge(MainActivity a) {
        act = a;
    }

    /** O usuário já liberou o acesso às notificações? */
    @JavascriptInterface
    public boolean hasAccess() {
        String list = Settings.Secure.getString(act.getContentResolver(), "enabled_notification_listeners");
        return list != null && list.contains(act.getPackageName());
    }

    /** Abre a tela do Android onde se libera o acesso às notificações. */
    @JavascriptInterface
    public void openAccess() {
        Intent i = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        act.startActivity(i);
    }

    /** Abre os detalhes do app no Android (bateria, início automático, configurações restritas). */
    @JavascriptInterface
    public void openAppSettings() {
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + act.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        act.startActivity(i);
    }

    /** Entrega as notificações capturadas (JSON) e esvazia a fila. */
    @JavascriptInterface
    public String pull() { return NotifStore.drain(act); }

    /** Notificações do dono do app (Firebase). Devolve false quando o projeto não tem Firebase. */
    @JavascriptInterface
    public boolean pushAvailable() { return PushService.available(act); }

    @JavascriptInterface
    public void setPushTopics(boolean news, boolean premium) { PushService.setTopics(act, news, premium); }

    /** Lista (JSON) de avisos com data: [{id, at, title, text}]. Substitui a anterior. */
    @JavascriptInterface
    public void schedule(String json) { Scheduler.set(act, json); }

    /** Mostra um aviso do app na barra de notificações. */
    @JavascriptInterface
    public boolean notify(String title, String text) { return AppNotify.show(act, title, text); }

    /** Entrar com Google (Credential Manager). A resposta chega em window.caixaGoogle. */
    @JavascriptInterface
    public void googleSignIn(String webClientId) { act.runOnUiThread(() -> GoogleAuth.signIn(act, webClientId, act.sink)); }

    @JavascriptInterface
    public void googleSignOut() { act.runOnUiThread(() -> GoogleAuth.signOut(act)); }

    /** Estado do serviço de leitura (JSON: connected, lastAny, lastBank). */
    @JavascriptInterface
    public String status() { return NotifStore.status(act); }

    /** Publica uma notificação de teste para conferir se a leitura funciona. */
    @JavascriptInterface
    public boolean postTest() { return NotifListener.postTest(act); }

    @JavascriptInterface
    public boolean alertsOn() { return NotifStore.alertsOn(act); }

    @JavascriptInterface
    public void setAlerts(boolean on) { NotifStore.setAlerts(act, on); }

    /** Liga ou desliga o modo automático (só vale com Premium). */
    @JavascriptInterface
    public void setAuto(boolean on) { NotifStore.setAuto(act, on); }

    /** Lista (JSON) de apps que o usuário mandou ignorar. */
    @JavascriptInterface
    public void setBlocked(String json) { Prefs.setBlocked(act, json); }

    @JavascriptInterface
    public boolean isPremium() { return Billing.isPremium(act); }

    @JavascriptInterface
    public void billingLoad() { act.runOnUiThread(act::billingLoad); }

    @JavascriptInterface
    public void buy(String productId) { act.runOnUiThread(() -> act.billingBuy(productId)); }

    @JavascriptInterface
    public void restore() { act.runOnUiThread(act::billingRefresh); }

    /** Abre o gerenciamento de assinaturas do Google Play (cancelar, trocar plano). */
    @JavascriptInterface
    public void manageSubs() {
        Uri u = Uri.parse("https://play.google.com/store/account/subscriptions?package=" + act.getPackageName());
        act.startActivity(new Intent(Intent.ACTION_VIEW, u).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /** Cor das barras do sistema, para combinar com o tema escolhido no app. */
    @JavascriptInterface
    public void setBars(String color, boolean light) { act.runOnUiThread(() -> act.setBars(color, light)); }

    /** Toque curto de vibração (0 = leve, 1 = confirmação). */
    @JavascriptInterface
    public void haptic(int kind) { act.runOnUiThread(() -> act.haptic(kind)); }

    /** Cor de destaque do sistema (Material You, Android 12+). Devolve "" quando não existe. */
    @JavascriptInterface
    public String systemAccent(boolean dark) {
        if (Build.VERSION.SDK_INT < 31) return "";
        try {
            int id = dark ? android.R.color.system_accent1_300 : android.R.color.system_accent1_600;
            int c = act.getResources().getColor(id, act.getTheme());
            return String.format("#%06X", 0xFFFFFF & c);
        } catch (Exception e) {
            return "";
        }
    }

    @JavascriptInterface
    public String version() {
        try {
            return act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "";
        }
    }

    /** Salva um arquivo na pasta Downloads. Devolve onde salvou, ou "" se falhou. */
    @JavascriptInterface
    public String saveFile(String name, String content, String mime) {
        name = name == null ? "arquivo.txt" : name.replaceAll("[^\\w.-]", "_").replaceAll("\\.{2,}", ".");
        if (name.isEmpty() || name.startsWith(".")) name = "arquivo" + name;
        if (mime == null || !mime.matches("[\\w.+-]+/[\\w.+-]+")) mime = "application/octet-stream";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        String type = mime == null || mime.isEmpty() ? "application/octet-stream" : mime;
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                v.put(MediaStore.MediaColumns.MIME_TYPE, type);
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = act.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) return "";
                try (OutputStream os = act.getContentResolver().openOutputStream(uri)) {
                    if (os == null) return "";
                    os.write(bytes);
                }
                return "Downloads/" + name;
            }
            File dir = act.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) return "";
            File f = new File(dir, name);
            try (FileOutputStream os = new FileOutputStream(f)) {
                os.write(bytes);
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    /** Compartilhamento do Android com um texto (backup para o Drive, e-mail...). */
    @JavascriptInterface
    public void shareText(String subject, String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, text);
        Intent chooser = Intent.createChooser(i, subject);
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        act.startActivity(chooser);
    }
}
