-- Limpar dados (apenas para DEV – cuidado em prod!)
IF OBJECT_ID('investment_simulation', 'U') IS NOT NULL
    DELETE FROM investment_simulation;

IF OBJECT_ID('investment_product', 'U') IS NOT NULL
    DELETE FROM investment_product;

-- Popular produtos
INSERT INTO investment_product
    (liquidez_dias, nome, perfil_recomendado, prazo_max_meses, prazo_min_meses, rentabilidade_anual, risco, tipo)
VALUES
    (1,  'CDB Caixa 2026',      'Conservador', 24,  6, 0.12, 'Baixo', 'CDB'),
    (30, 'Fundo CEF Ações',     'Agressivo',   60,  3, 0.18, 'Alto',  'Fundo'),
    (30, 'LCA do Agronegócio',  'Moderado',    60,  6, 0.16, 'Baixo', 'LCA');
