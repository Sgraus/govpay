-- GP-677 (3.1.x_blocco_portale)
ALTER TABLE rpt ADD COLUMN bloccante BOOLEAN DEFAULT true;
update rpt set bloccante = true;
ALTER TABLE rpt MODIFY COLUMN bloccante BOOLEAN NOT NULL;

-- Autorizzazione tutti i domini e tutte le entrate per utenza
ALTER TABLE utenze ADD COLUMN autorizzazione_domini_star BOOLEAN DEFAULT false;
update utenze set autorizzazione_domini_star = false;
ALTER TABLE utenze MODIFY COLUMN autorizzazione_domini_star BOOLEAN NOT NULL;

ALTER TABLE utenze ADD COLUMN autorizzazione_tributi_star BOOLEAN DEFAULT false;
update utenze set autorizzazione_tributi_star = false;
ALTER TABLE utenze MODIFY COLUMN autorizzazione_tributi_star BOOLEAN NOT NULL;


-- 11/03/2019 (Correzione bug autorizzazione utenze_tributi)
ALTER TABLE utenze_tributi ADD COLUMN id_tipo_tributo BIGINT;
UPDATE utenze_tributi ut, tributi t SET ut.id_tipo_tributo = t.id_tipo_tributo WHERE ut.id_tributo = t.id;
ALTER TABLE utenze_tributi MODIFY COLUMN id_tipo_tributo BIGINT NOT NULL;
ALTER TABLE utenze_tributi DROP FOREIGN KEY fk_nzt_id_tributo;
ALTER TABLE utenze_tributi DROP COLUMN id_tributo;
ALTER TABLE utenze_tributi ADD CONSTRAINT fk_nzt_id_tipo_tributo FOREIGN KEY (id_tipo_tributo) REFERENCES tipi_tributo(id);

-- 13/03/2019 (Eliminazione colonna principal dalla tabella Acl e sostituzione con la foreign key verso l'utenza)
ALTER TABLE acl ADD COLUMN id_utenza BIGINT;
UPDATE acl a, utenze u SET a.id_utenza = u.id WHERE a.principal is not null and a.principal = u.principal_originale;
ALTER TABLE acl DROP COLUMN principal;
ALTER TABLE acl ADD CONSTRAINT fk_acl_id_utenza FOREIGN KEY (id_utenza) REFERENCES utenze(id);

-- 15/03/2019 campo GLN della tabella Domini non piu' obbligatorio
ALTER TABLE domini MODIFY COLUMN gln VARCHAR(35) NULL;

