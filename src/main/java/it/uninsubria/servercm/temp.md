drop role if exists server_slave;
create role server_slave with createrole login password 'serverSlave';
grant select on public.city, public.area_interesse, public.centro_monitoraggio, public.nota_parametro_climatico, public.parametro_climatico to server_slave;
grant select, insert, update, delete on public.operatore_autorizzati to server_slave;

drop role if exists operatori;
create role operatori with createrole;
grant select, update, insert, delete on all tables in schema public to operatori;

insert into operatore_autorizzati(codice_fiscale, email) values('LNRMTM97L29F205L', 'mmlunardi@studenti.uninsubria.it');
insert into operatore_autorizzati(codice_fiscale, email) values('UHUT3VQHGPHFE2I3', 'prova1234@gmail.com');
