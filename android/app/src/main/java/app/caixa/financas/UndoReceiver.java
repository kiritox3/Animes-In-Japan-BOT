package app.caixa.financas;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/** Botão "Desfazer" do aviso: tira o lançamento da fila antes de o app gravar. */
public class UndoReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        String id = i.getStringExtra("id");
        boolean ok = id != null && NotifStore.remove(c, id);
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm != null) nm.cancel(i.getIntExtra("notif", 0));
        Toast.makeText(c, ok ? "Lançamento desfeito" : "Já foi gravado. Apague pelo app.", Toast.LENGTH_SHORT).show();
    }
}
