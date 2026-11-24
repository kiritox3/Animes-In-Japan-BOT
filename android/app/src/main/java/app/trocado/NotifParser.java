package app.trocado;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê o texto de uma notificação de banco e extrai valor, tipo, forma de pagamento, loja e data.
 * É a mesma lógica da página (nParse), em Java, para o app anotar sem precisar estar aberto.
 * A página refaz a leitura ao abrir, com o histórico e as categorias do usuário.
 */
final class NotifParser {
    private NotifParser() {}

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern AMOUNT = Pattern.compile("R\\$\\s?(-?[\\d.]+,\\d{2}|-?\\d+(?:\\.\\d{2})?)", FLAGS);
    private static final Pattern IN = Pattern.compile(
            "(voc[eê] recebeu|recebid[oa]|pix recebido|transfer[eê]ncia recebida|caiu na (sua )?conta|dep[oó]sito|estorno|reembolso|cashback|rendimento|cr[eé]dito em conta|sal[aá]rio)", FLAGS);
    private static final Pattern OUT_WORDS = Pattern.compile("(compra|pagou|enviou|enviad[oa]|pagamento (de|efetuado|realizado|aprovado))", FLAGS);
    private static final Pattern LAST4 = Pattern.compile("final\\s*(\\d{4})", FLAGS);
    private static final Pattern DATE = Pattern.compile("\\b(\\d{2})/(\\d{2})(?:/(\\d{2,4}))?\\b");
    private static final Pattern STOP = Pattern.compile("R\\$|cart[aã]o|final \\d|valor de|conta", FLAGS);
    private static final Pattern[] DESC_OUT = {
            Pattern.compile("\\bem\\s+([^.,;!\\n]+?)(?=\\s+(?:no|na|para|com|via|pelo|pela|às|as|dia|hoje|em \\d)\\b|[.,;!]|$)", FLAGS),
            Pattern.compile("\\bpara\\s+(?!o cart|a conta|seu cart|sua conta)([^.,;!\\n]+?)(?=\\s+(?:no|na|via|com|às|as)\\b|[.,;!]|$)", FLAGS),
            Pattern.compile("\\b(?:no|na)\\s+([A-Z0-9*][^.,;!\\n]+?)(?=\\s+(?:com|no|na|para|via|pelo|pela|às|as)\\b|[.,;!]|$)"),
    };
    private static final Pattern[] DESC_IN = {
            Pattern.compile("\\bde\\s+([^.,;!\\n]+?)(?=\\s+(?:no|na|em|via|pelo|pela|para|com)\\b|[.,;!]|$)", FLAGS),
            Pattern.compile("\\bpor\\s+([^.,;!]+?)(?=[.,;!]|$)", FLAGS),
    };
    private static final String[][] CATS = {
            {"Mercado", "mercado|supermerc|atacad|assa[ií]|carrefour|extra\\b|hortifruti|a[cç]ougue|feira"},
            {"Alimentação", "ifood|restaur|lanch|padaria|burger|pizza|mc ?donald|subway|caf[eé]|sorvet|a[cç]a[ií]|rappi|z[eé] delivery"},
            {"Transporte", "uber|\\b99\\b|99app|cabify|posto|combust|shell|ipiranga|petrobr|estaciona|metr[oô]|[oô]nibus|sem parar|ped[aá]gio"},
            {"Saúde", "farm[aá]|drog|raia|pacheco|cl[ií]nica|hospital|laborat|odonto|academia|smart ?fit"},
            {"Assinaturas", "netflix|spotify|crunchyroll|prime video|amazon prime|disney|hbo|youtube|deezer|apple\\.com|google one|icloud|globoplay"},
            {"Compras", "amazon|shopee|mercado ?livre|magalu|magazine|americanas|aliexpress|shein|renner|riachuelo|zara|kabum|casas bahia"},
            {"Contas", "energia|enel|cemig|copel|sabesp|internet|vivo|claro|\\btim\\b|condom[ií]nio|boleto"},
            {"Lazer", "cinema|ingresso|steam|playstation|xbox|nintendo|teatro"},
    };

    /** Devolve os campos em JSON, ou null se o texto não tem valor em R$. */
    static JSONObject parse(String raw, long time) {
        try {
            String t = raw.replaceAll("\\s+", " ").trim();
            Matcher m = AMOUNT.matcher(t);
            if (!m.find()) return null;
            long cents = toCents(m.group(1));
            if (cents <= 0) return null;

            boolean isIn = IN.matcher(t).find() && !OUT_WORDS.matcher(t.replaceAll("(?i)estorno.*$", "")).find();

            String method = "pix";
            String low = t.toLowerCase(new Locale("pt", "BR"));
            if (low.matches(".*d[eé]bito.*")) method = "debito";
            else if (low.contains("boleto")) method = "boleto";
            else if (low.matches(".*\\bpix\\b.*")) method = "pix";
            else if (low.matches(".*(transfer[eê]ncia|\\bted\\b|\\bdoc\\b).*")) method = "transf";
            else if (low.matches(".*(cart[aã]o|cr[eé]dito|compra (aprovada|de|no)|parcela).*")) method = "credito";
            if (isIn && method.equals("credito")) method = "pix";

            String desc = "";
            String tail = t.substring(m.end());
            for (Pattern p : isIn ? DESC_IN : DESC_OUT) {
                Matcher x = p.matcher(tail);
                if (!x.find()) { x = p.matcher(t); if (!x.find()) continue; }
                String v = x.group(1).replaceAll("\\s*\\d{2}/\\d{2}(/\\d{2,4})?.*$", "").replaceAll("\\s*\\d{1,2}:\\d{2}.*$", "").trim();
                if (!v.isEmpty() && !STOP.matcher(v).find() && v.length() <= 48) { desc = v; break; }
            }
            desc = desc.replaceAll("(?i)^(o|a|os|as)\\s+", "").replaceAll("(?i)\\s+(aprovad[oa]|realizad[oa]|confirmad[oa])$", "");
            if (desc.isEmpty()) {
                Matcher h = Pattern.compile("^([^:]{2,30}):").matcher(t);
                if (h.find() && !h.group(1).toLowerCase().matches(".*(pix|compra|transfer|aviso|alerta).*")) desc = h.group(1).trim();
            }
            if (!desc.isEmpty() && desc.equals(desc.toUpperCase())) desc = title(desc.toLowerCase());

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time > 0 ? time : System.currentTimeMillis());
            String date = String.format(Locale.US, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
            Matcher dm = DATE.matcher(t);
            if (dm.find()) {
                String y = dm.group(3) == null ? String.valueOf(c.get(Calendar.YEAR)) : (dm.group(3).length() == 2 ? "20" + dm.group(3) : dm.group(3));
                String d = y + "-" + dm.group(2) + "-" + dm.group(1);
                if (d.compareTo(date) <= 0) date = d;
            }

            String cat = "";
            if (!isIn) {
                String hay = desc + " " + t;
                for (String[] k : CATS) if (Pattern.compile(k[1], FLAGS).matcher(hay).find()) { cat = k[0]; break; }
            }

            Matcher l4 = LAST4.matcher(t);
            JSONObject o = new JSONObject();
            o.put("type", isIn ? "in" : "out");
            o.put("amount", cents);
            o.put("method", method);
            o.put("desc", desc);
            o.put("date", date);
            o.put("cat", cat);
            o.put("last4", l4.find() ? l4.group(1) : "");
            o.put("confident", !desc.isEmpty());
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    static long toCents(String s) {
        s = s.replace("-", "");
        if (s.contains(",")) s = s.replace(".", "").replace(",", ".");
        try {
            return Math.round(Double.parseDouble(s) * 100);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static String money(long cents) {
        java.text.NumberFormat f = java.text.NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return f.format(cents / 100.0);
    }

    private static String title(String s) {
        StringBuilder b = new StringBuilder(s.length());
        boolean up = true;
        for (char ch : s.toCharArray()) {
            b.append(up ? Character.toUpperCase(ch) : ch);
            up = ch == ' ';
        }
        return b.toString();
    }
}
