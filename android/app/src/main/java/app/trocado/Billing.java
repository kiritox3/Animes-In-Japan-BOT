package app.trocado;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assinatura Premium pelo Google Play.
 * Os produtos precisam existir no Play Console com estes IDs (veja PUBLICAR.md).
 */
final class Billing implements PurchasesUpdatedListener {
    static final String[] SUBS = {"premium_mensal", "premium_anual"};

    interface Sink { void js(String code); }

    private final Activity act;
    private final Sink sink;
    private BillingClient client;
    private final Map<String, ProductDetails> details = new HashMap<>();

    Billing(Activity a, Sink s) {
        act = a;
        sink = s;
    }

    static boolean isPremium(Context c) {
        if (BuildConfig.UNLOCK_PREMIUM) return true;
        return NotifStore.prefs(c).getBoolean("premium", false);
    }

    private void setPremium(boolean on) {
        NotifStore.prefs(act).edit().putBoolean("premium", on).apply();
    }

    void start() {
        client = BillingClient.newBuilder(act)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .enableAutoServiceReconnection()
                .build();
        client.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult r) {
                if (r.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    refresh();
                    loadProducts();
                } else {
                    status("unavailable", r.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() { }
        });
    }

    void end() {
        if (client != null) client.endConnection();
    }

    /** Busca preços e planos no Google Play e manda para a página. */
    void loadProducts() {
        if (client == null || !client.isReady()) { status("unavailable", "Google Play indisponível"); return; }
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();
        for (String id : SUBS) {
            list.add(QueryProductDetailsParams.Product.newBuilder().setProductId(id).setProductType(BillingClient.ProductType.SUBS).build());
        }
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(list).build(), (r, result) -> {
            JSONArray out = new JSONArray();
            try {
                for (ProductDetails pd : result.getProductDetailsList()) {
                    details.put(pd.getProductId(), pd);
                    List<ProductDetails.SubscriptionOfferDetails> offers = pd.getSubscriptionOfferDetails();
                    if (offers == null || offers.isEmpty()) continue;
                    ProductDetails.SubscriptionOfferDetails o = offers.get(0);
                    List<ProductDetails.PricingPhase> phases = o.getPricingPhases().getPricingPhaseList();
                    ProductDetails.PricingPhase base = phases.get(phases.size() - 1);
                    String trial = "";
                    for (ProductDetails.PricingPhase ph : phases) if (ph.getPriceAmountMicros() == 0) trial = ph.getBillingPeriod();
                    out.put(new JSONObject()
                            .put("id", pd.getProductId())
                            .put("price", base.getFormattedPrice())
                            .put("micros", base.getPriceAmountMicros())
                            .put("period", base.getBillingPeriod())
                            .put("trial", trial));
                }
            } catch (Exception ignored) { }
            sink.js("window.caixaBilling&&window.caixaBilling(" + JSONObject.quote(new JSONObject(map("products", out)).toString()) + ")");
        });
    }

    /** Abre a tela de compra do Google Play. Precisa rodar na thread da interface. */
    void buy(String productId) {
        ProductDetails pd = details.get(productId);
        if (client == null || !client.isReady() || pd == null || pd.getSubscriptionOfferDetails() == null || pd.getSubscriptionOfferDetails().isEmpty()) {
            status("unavailable", "Plano indisponível no momento");
            return;
        }
        String token = pd.getSubscriptionOfferDetails().get(0).getOfferToken();
        List<BillingFlowParams.ProductDetailsParams> params = new ArrayList<>();
        params.add(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(pd).setOfferToken(token).build());
        client.launchBillingFlow(act, BillingFlowParams.newBuilder().setProductDetailsParamsList(params).build());
    }

    /** Confere no Google Play se a assinatura está ativa (restaurar compra / abrir o app). */
    void refresh() {
        if (client == null || !client.isReady()) return;
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (r, purchases) -> {
            if (r.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
            boolean any = false;
            for (Purchase p : purchases) {
                if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    any = true;
                    ack(p);
                }
            }
            setPremium(any);
            status(any ? "premium" : "free", "");
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult r, List<Purchase> purchases) {
        int code = r.getResponseCode();
        if (code == BillingClient.BillingResponseCode.OK && purchases != null) {
            boolean any = false;
            for (Purchase p : purchases) {
                if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) { any = true; ack(p); }
                else if (p.getPurchaseState() == Purchase.PurchaseState.PENDING) status("pending", "");
            }
            if (any) { setPremium(true); status("premium", "bought"); }
        } else if (code == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            refresh();
        } else if (code == BillingClient.BillingResponseCode.USER_CANCELED) {
            status("canceled", "");
        } else {
            status("error", r.getDebugMessage());
        }
    }

    private void ack(Purchase p) {
        if (p.isAcknowledged()) return;
        client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.getPurchaseToken()).build(), r -> { });
    }

    private void status(String s, String msg) {
        try {
            String json = new JSONObject().put("status", s).put("message", msg == null ? "" : msg).put("premium", isPremium(act)).toString();
            sink.js("window.caixaBilling&&window.caixaBilling(" + JSONObject.quote(json) + ")");
        } catch (Exception ignored) { }
    }

    private static Map<String, Object> map(String k, Object v) {
        Map<String, Object> m = new HashMap<>();
        m.put(k, v);
        return m;
    }
}
