ALTER TABLE PLAYER_MATCHDATA ADD COLUMN ENEMY BOOLEAN NOT NULL DEFAULT 'false';
update settings set propkey=regexp_replace(propkey,'QP_3','QP_4') where propkey like 'layoutQP_3%'