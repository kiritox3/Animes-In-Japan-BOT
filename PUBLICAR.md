# Como publicar o app na Play Store

Este guia leva o Trocado (pasta `financas/` + `android/`) até a Play Store, com assinatura Premium e login na nuvem. Siga na ordem. Os passos marcados com **(você)** só podem ser feitos na sua conta.

## 1. Nome do app

O app se chama **Trocado**. Para trocar o nome, mude:

- `android/app/src/main/res/values/strings.xml` → `app_name`
- `financas/index.html` → a constante `APP_NAME`, `name:'Trocado'` em `defaults()` e o `<title>`
- `financas/privacidade.html` → o nome no primeiro parágrafo

O identificador do pacote (`app.trocado`, em `android/app/build.gradle`) é permanente depois do primeiro envio à loja. Se quiser trocar, troque antes.

## 2. Firebase: login com Google e nuvem (você)

1. Entre em https://console.firebase.google.com e crie um projeto.
2. **Authentication** → Começar → ative o provedor **Google** (o app usa só login com Google). No provedor Google, abra "Configuração do SDK da Web" e copie o **ID do cliente Web** (termina com `.apps.googleusercontent.com`).
3. **Firestore Database** → Criar banco → modo de produção → região `southamerica-east1` (São Paulo).
4. Em Firestore → **Regras**, cole o conteúdo do arquivo `firestore.rules` deste repositório e publique.
5. **Configurações do projeto** → Seus apps → adicione um app **Android** com o pacote `app.trocado` e a impressão digital **SHA-1**:
   - APK de teste (chave `android/app/caixa.jks`): `FD:3A:AF:FE:76:37:03:43:E1:2A:D7:FB:AB:4F:A4:5A:9E:33:87:6F`
   - Versão da Play Store: copie o SHA-1 de **Play Console → Integridade do app → Assinatura do app** e adicione também.
6. Adicione um app **Web** (ícone `</>`) e copie o objeto `firebaseConfig`.
7. Coloque a configuração no app de um destes jeitos:
   - **Sem recompilar (uso pessoal):** no app, Config → Salvamento na nuvem → Configurar nuvem com Google. Cole o `firebaseConfig` e o ID do cliente Web.
   - **Para todos os usuários:** em `financas/index.html`, preencha `FIREBASE_CONFIG` com o objeto e `GOOGLE_WEB_CLIENT_ID` com o ID do cliente Web.

Os valores do `firebaseConfig` não são senhas: quem protege os dados são as regras do passo 4. O login com Google no Android usa o Credential Manager (a tela do Google não abre dentro do app); por isso o SHA-1 do passo 5 é obrigatório.

9. **Notificações para os usuários:** no app Android do passo 5, baixe o `google-services.json` e coloque em `android/app/google-services.json`. O build liga o Firebase sozinho quando o arquivo existe. Ele não é secreto e pode ir para o repositório.

## 2.1 Mandar notificações para os usuários (você)

Com o `google-services.json` no projeto, cada app instalado entra em grupos (tópicos):

| Tópico | Quem recebe |
|---|---|
| `todos` | Todo mundo que deixou "Novidades e dicas" ligado em Config |
| `gratis` | Quem usa a versão grátis |
| `premium` | Assinantes |

Para mandar: Console do Firebase → **Messaging** → **Nova campanha** → **Notificações**. Escreva título e texto e, em "Segmentação", escolha **Tópico** e digite `todos`, `gratis` ou `premium`. Dá para agendar. Não precisa de servidor.

O usuário pode desligar em Config → Novidades e dicas. Use com moderação: notificação demais faz as pessoas desinstalarem.

## 3. Conta de desenvolvedor Google Play (você)

1. Crie em https://play.google.com/console (taxa única de US$ 25).
2. Contas pessoais novas precisam fazer um **teste fechado com pelo menos 12 testadores por 14 dias** antes de liberar a produção. Comece por aí.
3. Configure o **perfil de pagamentos** (necessário para vender assinaturas).

## 4. Chave de assinatura (você)

O arquivo `android/app/caixa.jks` é uma chave de **teste** e está público no repositório. Não use para a loja.

1. Gere a sua chave no computador (precisa do Java):
   ```
   keytool -genkeypair -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
   Guarde o arquivo e as senhas num lugar seguro. Sem eles você não consegue enviar atualizações.
2. No GitHub do repositório → Settings → Secrets and variables → Actions, crie:
   - `CAIXA_KEYSTORE_BASE64`: o arquivo convertido em texto (`base64 -w0 upload.jks` no Linux/Mac, ou `certutil -encode` no Windows, sem as linhas de cabeçalho)
   - `CAIXA_KEYSTORE_PASSWORD`: a senha do arquivo
   - `CAIXA_KEY_ALIAS`: `upload`
   - `CAIXA_KEY_PASSWORD`: a senha da chave
3. A cada envio, o GitHub Actions gera o `Trocado-playstore.aab` assinado com essa chave, na release `caixa-android-latest` e nos artefatos da execução.
4. No Play Console, aceite o **Play App Signing** (o Google guarda a chave final; a sua vira a "chave de upload").

## 5. Criar o app e as assinaturas no Play Console (você)

1. Crie o app, envie o `Trocado-playstore.aab` para uma faixa de **teste interno**.
2. Em **Monetizar → Produtos → Assinaturas**, crie duas assinaturas com estes IDs exatos (o app procura por eles):
   - `premium_mensal`: plano base de renovação mensal (sugestão: R$ 9,90, com 7 dias grátis como oferta)
   - `premium_anual`: plano base de renovação anual (sugestão: R$ 79,90)
3. Em **Configurações → Teste de licença**, adicione seu email para testar compras sem cobrança.
4. Instale o app pelo link do teste interno (pela Play Store). A tela Premium vai mostrar os preços reais.

## 6. Formulários da Play Store (você)

- **Política de privacidade**: publique `financas/privacidade.html`. Uma forma é ativar o GitHub Pages do repositório (Settings → Pages → branch). O endereço fica parecido com `https://SEU-USUARIO.github.io/Animes-In-Japan-BOT/financas/privacidade.html`. Se for diferente, atualize `PRIVACY_URL` em `financas/index.html`.
- **Segurança dos dados**: declare que o app coleta **informações financeiras** (histórico de compras, outras informações financeiras) e **email** (só com conta), para **funcionalidade do app** e **gerenciamento da conta**; dados **criptografados em trânsito**; o usuário **pode pedir exclusão** (dentro do app); **não compartilhados** com terceiros.
- **Exclusão de conta**: informe que é feita em Config → Salvamento na nuvem → Excluir conta e dados da nuvem. A Play também pede um link web para pedir exclusão; pode ser a própria página de privacidade.
- **Acesso a notificações**: o app mostra uma explicação antes de pedir o acesso (exigência da loja). Na ficha, explique que a leitura serve para anotar gastos automaticamente.
- **Classificação de conteúdo**, **público-alvo** (maiores de 18 recomendado para finanças) e **app de finanças**: preencha os questionários. Apps de finanças pessoais (só controle de gastos) não precisam das licenças de instituição financeira, mas declare que o app não oferece empréstimos.

## 6.1 Ficha da loja (pronta)

A pasta `loja/` tem o material da ficha:

- `loja/ficha.md`: nome, descrição curta, descrição completa e notas para os formulários
- `loja/icone-512.png`: ícone da ficha (512 x 512)
- `loja/destaque-1024x500.png`: imagem de destaque
- `loja/telas/`: 6 capturas de tela de celular (1080 x 1920) com legenda

As telas usam dados de exemplo e cartões sem marca de banco, para evitar problemas de marca registrada na revisão do Google.

## 7. Por que o Play Protect bloqueou o APK

Fora da loja, o Android bloqueia apps que pedem acesso às notificações quando instalados pelo navegador. Isso deixa de acontecer quando o app é instalado pela Play Store. Para testar antes disso, use o teste interno (passo 5) ou instale por cabo com `adb install Trocado.apk`.

## O que é grátis e o que é Premium

| Grátis | Premium |
|---|---|
| Lançamentos, contas e cartões (até 2 contas e 2 cartões) | Contas e cartões ilimitados |
| Leitura das notificações com revisão manual | Anotação automática, mesmo com o app fechado, com "Desfazer" no aviso |
| Resumo do mês, faturas, gráfico por categoria e dos 6 meses | Análises completas (gasto por dia, forma de pagamento, faturas, maiores gastos) |
| 1 meta e 3 limites por categoria | Metas e limites ilimitados |
| Backup manual em arquivo | Backup na nuvem com sincronização entre celulares |
| Cores prontas | Qualquer cor + exportar planilha CSV |
| Importar extrato OFX/CSV, colar notificações | (igual) |

Os limites ficam em `FREE` e as vantagens em `PREM_FEATURES`, no `financas/index.html`.

## Limitações conhecidas

- A confirmação da assinatura é feita no próprio celular, pela biblioteca do Google Play. Para evitar fraudes em escala, o ideal é validar as compras num servidor (Google Play Developer API ou Firebase Functions). Dá para fazer depois.
- Com o app fechado, a anotação automática grava no celular assim que a notificação chega. Ela entra no histórico e na nuvem quando o app é aberto.
