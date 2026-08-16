\c email;

-- API KEY

CREATE TABLE api_key
(
    id                  uuid            default gen_random_uuid()   not null    primary key,
    client_name         varchar(255)                                not null,
    priority            boolean         default false               not null,
    create_date         timestamp       default CURRENT_TIMESTAMP   not null,
    last_update_date    timestamp       default CURRENT_TIMESTAMP   not null,
    active              boolean         default true                not null
);

CREATE INDEX idx_api_key_enabled on api_key (active) where (active = true);

INSERT INTO api_key (id, client_name, priority)
VALUES ('e3b0c442-98fc-4c14-9afb-4c8996fb9242', 'Premium' , true);

INSERT INTO api_key (id, client_name, priority)
VALUES ('709e80c8-8487-4cd1-a4d1-aa9a21815147', 'Padrão', false);


-- BLOCK LIST

CREATE TABLE block_list
(
    uuid                uuid            default gen_random_uuid()   not null    primary key,
    email               varchar(255)                                not null,
    reason              varchar(255)                                not null,
    create_date         timestamp       default CURRENT_TIMESTAMP   not null,
    last_update_date    timestamp       default CURRENT_TIMESTAMP   not null,
    active              boolean         default true                not null
);

CREATE INDEX idx_block_list_enabled on block_list (active) where (active = true);

INSERT INTO block_list (email, reason) VALUES ('spam-user@example.com', 'Envio em massa detectado');
INSERT INTO block_list (email, reason) VALUES ('phishing-scam@hotmail.com', 'Tentativa de phishing');

-- EMAIL TEMPLATE

CREATE TABLE email_template
(
    id                  uuid            default gen_random_uuid()   not null    primary key,
    content             text                                        not null,
    create_date         timestamp       default CURRENT_TIMESTAMP   not null,
    last_update_date    timestamp       default CURRENT_TIMESTAMP   not null,
    active              boolean         default true                not null
);

CREATE INDEX idx_email_template_enabled on email_template (active) where (active = true);

INSERT INTO email_template (id, content)
VALUES (
    'bb73f6da-c829-4c0e-bd18-5526f6aa75bf',
    '<html><body><h1>Bem-vindo {{name}}!</h1><p>Olá, obrigado por se cadastrar em nossa plataforma.</p></body></html>'
);


-- EMAIL DAILY STATS

CREATE TABLE email_daily_stats
(
    id                  uuid            default gen_random_uuid()   not null    primary key,
    stats_date          date                                        not null,
    tenant_id           varchar(100)                                not null,
    subject             varchar(255)                                not null,
    total_processed     integer         default 0                   not null,
    total_delivered     integer         default 0                   not null,
    total_opened        integer         default 0                   not null,
    total_bounce        integer         default 0                   not null,
    total_dropped       integer         default 0                   not null,
    create_date         timestamp       default CURRENT_TIMESTAMP   not null,
    last_update_date    timestamp       default CURRENT_TIMESTAMP   not null,
    constraint unique_date_tenant_subject unique (stats_date, tenant_id, subject)
);

CREATE INDEX idx_email_stats_report on email_daily_stats (tenant_id, stats_date);

