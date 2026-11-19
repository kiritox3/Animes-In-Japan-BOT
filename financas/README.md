# Trocado — finanças pessoais

App de controle financeiro pessoal feito para celular, com tema escuro por padrão. É um único arquivo HTML, sem servidor nem dependências. Os dados ficam salvos no navegador (localStorage).

## Abas
- **Início**: saldo das contas, resumo do mês (gastos, já pago, a pagar, entradas), quanto você pode gastar por dia sem se endividar, botões **Planejamento** e **Posso comprar isso?**, limites mensais, carrossel **Meus cartões** com fatura e limite, últimas despesas e metas de economia. Você escolhe as seções e a ordem delas.
- **Gastos**: total do mês com pago, a pagar e um destaque que você escolhe (Pix enviados, por exemplo). Tem busca, filtros (categoria, conta/cartão, ordenação) e chips de status e forma de pagamento. As faturas dos cartões aparecem separadas das contas avulsas. Também mostra as entradas.
- **Análises**: comparação com o mês anterior, taxa de economia, gastos por categoria, últimos 6 meses, gasto por dia, forma de pagamento, faturas e maiores gastos.
- **Config**: tema (escuro, claro ou sistema), cor de destaque, nome do app, esconder valores, seções do Início, contas, cartões (cores, limite, fechamento e vencimento), categorias (nome, ícone e cor), planejamento e backup.

## Salvamento na nuvem
Aberto pelo link do Claude, o app salva tudo na sua conta do Claude (seu email) e sincroniza entre aparelhos: entre com o mesmo email em outro celular ou computador e abra o mesmo link. Os dados ficam numa área privada de cada pessoa; nem o dono do link vê os dados dos outros. Aberto direto deste arquivo (ou pelo GitHub Pages), o app funciona só com os dados do aparelho.

## Como usar
Abra `index.html` no navegador. Para instalar no celular como app, publique a pasta numa hospedagem estática (GitHub Pages, por exemplo) e use "Adicionar à tela inicial".

## App para Android
A pasta `android/` tem o app instalável. Ele abre esta mesma página e tem um serviço que lê as notificações dos apps de banco (com a permissão "Acesso às notificações" do Android). Cada notificação com valor em R$ e uma palavra de transação (compra, Pix, transferência, pagamento...) vira um lançamento, para revisar ou entrar direto. Apps de conversa e e-mail são ignorados.

O APK é gerado pelo GitHub Actions (`.github/workflows/caixa-android.yml`) a cada mudança e publicado na release `caixa-android-latest`. Para gerar localmente, com o Android SDK instalado: `cd android && ./gradlew assembleRelease`.

O arquivo `android/app/caixa.jks` é uma chave de assinatura só para instalação pessoal, fora da Play Store. A senha está no `build.gradle`. Para publicar na Play Store, troque por uma chave privada guardada em segredo.

## Créditos
As marcas dos bancos (em uma cor) vêm do pacote `@edusites/bancos-brasil`, licença MIT. Os logotipos são marcas registradas dos respectivos bancos e aparecem só para identificar as contas e os cartões do usuário.
