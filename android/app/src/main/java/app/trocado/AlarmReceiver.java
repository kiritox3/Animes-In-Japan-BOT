package app.trocado;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Mostra um aviso agendado pelo próprio app (não é acessível por outros apps). */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        String title = i.getStringExtra("title");
        if (title == null || title.isEmpty()) return;
        String text = i.getStringExtra("text");
        AppNotify.show(c, title, text == null ? "" : text);
    }
}
