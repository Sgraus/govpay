delete from anagrafiche where id IN (select id_anagrafica_attestante from rt);
delete from anagrafiche where id IN (select id_anagrafica_versante from rpt);
delete from anagrafiche where id IN (select id_anagrafica_debitore from versamenti);
delete from tracciatixml where id in (select id_tracciato_xml from rpt);
delete from tracciatixml where id in (select id_tracciato_xml from rt);
delete  from tracciatixml where tipo_tracciato<> 'PSP';
DELETE FROM id_messaggio_relativo;
DELETE FROM mail;
DELETE FROM singole_rendicontazioni;
DELETE FROM fr;
DELETE FROM singole_revoche;
DELETE FROM er;
DELETE FROM rr;
DELETE FROM medie_rilevamenti;
DELETE FROM rilevamenti;
DELETE FROM sla;
DELETE FROM eventi;
DELETE FROM iuv;
DELETE FROM rt;
DELETE FROM esiti;
DELETE FROM carrelli;
DELETE FROM rpt;
DELETE FROM singoli_versamenti;
DELETE FROM versamenti;