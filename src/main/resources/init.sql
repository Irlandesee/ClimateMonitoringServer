--create tables
create table if not exists operatore_autorizzati(
                                                    codice_fiscale varchar(16) not null,
                                                    email varchar(50) not null,
                                                    primary key(codice_fiscale, email)
);
create table if not exists area_interesse(
                                             areaid varchar(100) primary key,
                                             denominazione varchar(100) not null,
                                             stato varchar(100) not null,
                                             latitudine double precision not null,
                                             longitudine double precision not null
);
create table if not exists centro_monitoraggio(
                                                  centroid varchar(100) primary key,
                                                  nomecentro varchar(100) not null,
                                                  comune varchar(100) not null,
                                                  country varchar(100) not null,
                                                  aree_interesse_ids character varying[]
);
create table if not exists city(
                                   geoname_id varchar(100) primary key,
                                   ascii_name varchar(100) not null,
                                   country varchar(100) not null,
                                   country_code varchar(100) not null,
                                   latitude double precision not null,
                                   longitude double precision not null
);
create table if not exists nota_parametro_climatico(
                                                       notaid varchar(100) primary key,
                                                       nota_vento varchar(256),
                                                       nota_umidita varchar(256),
                                                       nota_pressione varchar(256),
                                                       nota_temperatura varchar(256),
                                                       nota_precipitazioni varchar(256),
                                                       nota_alt_ghiacciai varchar(256),
                                                       nota_massa_ghiacciai varchar(256)
);
create table if not exists parametro_climatico(
                                                  parameterid varchar(100) primary key,
                                                  centroid varchar(100) not null references centro_monitoraggio(centroid),
                                                  areaid varchar(100) not null references area_interesse(areaid),
                                                  pubdate date not null,
                                                  notaid varchar(100) references nota_parametro_climatico(notaid),
                                                  valore_vento smallint not null,
                                                  valore_umidita smallint not null,
                                                  valore_pressione smallint not null,
                                                  valore_temperatura smallint not null,
                                                  valore_precipitazioni smallint not null,
                                                  valore_alt_ghiacciai smallint not null,
                                                  valore_massa_ghiacciai smallint not null
);
create table if not exists operatore(
                                        nome varchar(100) not null,
                                        cognome varchar(100) not null,
                                        codice_fiscale varchar(16) not null,
                                        email varchar(50) not null,
                                        userid varchar(50) not null,
                                        password varchar(50) not null,
                                        centroid varchar(100) not null references centro_monitoraggio(centroid),
                                        primary key(userid, codice_fiscale)
);
--roles
drop role if exists mattialun;
drop role if exists server_slave;
create role server_slave with createrole login password 'serverSlave';
grant select on public.city, public.area_interesse, public.centro_monitoraggio, public.nota_parametro_climatico, public.parametro_climatico, public.operatore_autorizzati to server_slave;
grant select, insert on public.operatore to server_slave;

drop role if exists operatori;
create role operatori with createrole;
grant select, update, insert, delete on all tables in schema public to operatori;

insert into operatore_autorizzati(codice_fiscale, email) values('LNRMTM97L29F205L', 'mmlunardi@studenti.uninsubria.it');
insert into operatore_autorizzati(codice_fiscale, email) values('UHUT3VQHGPHFE2I3', 'prova1234@gmail.com');