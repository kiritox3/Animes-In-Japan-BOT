# Caixa — finanças pessoais

App minimalista de controle financeiro, feito para celular. Um único arquivo HTML, sem servidor nem dependências; os dados ficam salvos no navegador (localStorage).

## Funções
- **Início**: saldo do mês, receitas × despesas, saldo acumulado, pendências (contas a pagar, a receber e faturas a vencer), gastos por categoria com orçamento e gráfico dos últimos 6 meses.
- **Extrato**: lançamentos do mês agrupados por dia, busca e filtros (despesas, receitas, cartão, pendentes).
- **Lançamentos**: despesa ou receita, categoria, data, pago/recebido, repetição mensal (contas fixas, salário).
- **Cartões**: limite usado/disponível, dia de fechamento e vencimento, faturas por mês (aberta, fechada, paga, vencida), compras parceladas distribuídas nas faturas, marcar fatura como paga.
- **Ajustes**: orçamento mensal por categoria, categorias personalizadas, backup/restauração em JSON.

## Como usar
Abra `index.html` no navegador. Para instalar no celular como app, publique a pasta em qualquer hospedagem estática (ex.: GitHub Pages) e use "Adicionar à tela inicial".
