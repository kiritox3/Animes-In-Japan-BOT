package app.caixa.financas;

import android.content.ContentValues;
import android.content.Context;
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

/** Funções que a página do Caixa chama como window.CaixaAndroid.*. */
final class Bridge {
    private final Context ctx;

    Bridge(Context c) {
        ctx = c.getApplicationContext();
    }

    /** O usuário já liberou o acesso às notificações para o Caixa? */
    @JavascriptInterface
    public boolean hasAccess() {
        String list = Settings.Secure.getString(ctx.getContentResolver(), "enabled_notification_listeners");
        return list != null && list.contains(ctx.getPackageName());
    }

    /** Abre a tela do Android onde se libera o acesso às notificações. */
    @JavascriptInterface
    public void openAccess() {
        Intent i = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    /** Entrega as notificações capturadas (JSON) e esvazia a fila. */
    @JavascriptInterface
    public String pull() {
        return NotifStore.drain(ctx);
    }

    @JavascriptInterface
    public boolean alertsOn() {
        return NotifStore.alertsOn(ctx);
    }

    @JavascriptInterface
    public void setAlerts(boolean on) {
        NotifStore.setAlerts(ctx, on);
    }

    @JavascriptInterface
    public String version() {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "";
        }
    }

    /** Salva um arquivo (backup) na pasta Downloads. Devolve onde salvou, ou "" se falhou. */
    @JavascriptInterface
    public String saveFile(String name, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                v.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) return "";
                try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
                    if (os == null) return "";
                    os.write(bytes);
                }
                return "Downloads/" + name;
            }
            File dir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
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

    /** Abre o compartilhamento do Android com um texto (ex.: backup para o Drive ou e-mail). */
    @JavascriptInterface
    public void shareText(String subject, String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, text);
        Intent chooser = Intent.createChooser(i, subject);
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(chooser);
    }
}
