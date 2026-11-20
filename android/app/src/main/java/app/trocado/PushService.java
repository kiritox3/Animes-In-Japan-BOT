package app.trocado;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Notificações enviadas pelo dono do app (Firebase Cloud Messaging).
 * Com o app fechado, o Android mostra a notificação sozinho; com ele aberto, mostramos aqui.
 * Os usuários entram nos tópicos "todos" e "gratis" ou "premium" para receber campanhas.
 */
public class PushService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        String title = null, body = null;
        if (msg.getNotification() != null) {
            title = msg.getNotification().getTitle();
            body = msg.getNotification().getBody();
        }
        if (title == null) title = msg.getData().get("title");
        if (body == null) body = msg.getData().get("body");
        if (title == null && body == null) return;
        AppNotify.show(this, title == null ? getString(R.string.app_name) : title, body == null ? "" : body);
    }

    @Override
    public void onNewToken(String token) {
        NotifStore.prefs(this).edit().putString("pushToken", token).apply();
    }

    /** O Firebase só existe quando o projeto tem google-services.json (versão da loja). */
    static boolean available(Context c) {
        return !FirebaseApp.getApps(c).isEmpty();
    }

    /** Liga ou desliga as novidades e escolhe o grupo (grátis ou premium). */
    static void setTopics(Context c, boolean news, boolean premium) {
        if (!available(c)) return;
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        if (news) {
            fm.subscribeToTopic("todos");
            fm.subscribeToTopic(premium ? "premium" : "gratis");
            fm.unsubscribeFromTopic(premium ? "gratis" : "premium");
        } else {
            fm.unsubscribeFromTopic("todos");
            fm.unsubscribeFromTopic("gratis");
            fm.unsubscribeFromTopic("premium");
        }
    }
}
