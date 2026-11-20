package app.trocado;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Entrar com Google pelo Android (a tela de login do Google não abre dentro de um WebView).
 * Devolve o token para a página, que entra no Firebase com ele.
 */
final class GoogleAuth {
    private static final Executor EXEC = Executors.newSingleThreadExecutor();

    private GoogleAuth() {}

    static void signIn(Activity a, String webClientId, Billing.Sink sink) {
        if (webClientId == null || webClientId.trim().isEmpty()) {
            send(sink, "error", "missing_client", "", "", "");
            return;
        }
        GetSignInWithGoogleOption option = new GetSignInWithGoogleOption.Builder(webClientId.trim()).build();
        GetCredentialRequest req = new GetCredentialRequest.Builder().addCredentialOption(option).build();
        CredentialManager.create(a).getCredentialAsync(a, req, new CancellationSignal(), EXEC,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse r) {
                        Credential c = r.getCredential();
                        if (c instanceof CustomCredential && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(c.getType())) {
                            try {
                                GoogleIdTokenCredential g = GoogleIdTokenCredential.createFrom(c.getData());
                                send(sink, "ok", "", g.getIdToken(), g.getId(), g.getDisplayName() == null ? "" : g.getDisplayName());
                            } catch (Exception e) {
                                send(sink, "error", "bad_credential", "", "", "");
                            }
                        } else {
                            send(sink, "error", "bad_credential", "", "", "");
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        String code = e instanceof GetCredentialCancellationException ? "canceled"
                                : e instanceof NoCredentialException ? "no_account" : "failed";
                        send(sink, "error", code, "", "", "");
                    }
                });
    }

    static void signOut(Activity a) {
        CredentialManager.create(a).clearCredentialStateAsync(new ClearCredentialStateRequest(), new CancellationSignal(), EXEC,
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override public void onResult(Void v) { }
                    @Override public void onError(ClearCredentialException e) { }
                });
    }

    private static void send(Billing.Sink sink, String status, String code, String token, String email, String name) {
        try {
            String json = new JSONObject().put("status", status).put("code", code).put("idToken", token)
                    .put("email", email).put("name", name).toString();
            sink.js("window.caixaGoogle&&window.caixaGoogle(" + JSONObject.quote(json) + ")");
        } catch (Exception ignored) { }
    }
}
