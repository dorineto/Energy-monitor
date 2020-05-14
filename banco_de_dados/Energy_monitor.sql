if exists(select * from sys.databases where database_id = DB_id(N'Energy_monitor')) 
begin
	drop database Energy_monitor;
end

create database Energy_monitor
go

use Energy_monitor
go

if exists( select * from sys.objects where object_id = Object_id(N'Tipo_computador') and type in (N'U') )
begin
	drop table Tipo_computador;
end

create table Tipo_computador(
	 id_tipo int identity(1,1)
	,nome varchar(30)
        ,primary key(id_tipo)
);

if exists( select * from sys.objects where object_id = Object_id(N'Computadores') and type in (N'U') )
begin
	drop table Computadores;
end

create table Computadores(
	 id_comp int identity(1,1)
	,id_tipo int not null
	,nome varchar(50)
	,max_consu decimal(7,2) not null
	,media_consu decimal(7,2) not null
	,primary key(id_comp)
	,foreign key(id_tipo) references Tipo_computador(id_tipo)
);

if exists(select * from sys.objects where object_id = Object_id(N'Quant_horas') and type in (N'U'))
begin
	drop table Quant_horas;
end

create table Quant_horas(
	 id_comp int
	,horas time(3) not null
	,estado char(1) not null check( estado in (N'L', N'D'))
	,primary key(id_comp)
	,foreign key(id_comp) references Computadores(id_comp)
);

if exists(select * from sys.objects where object_id = Object_id(N'Historico_horas') and type in (N'U'))
begin
	drop table Historico_horas;
end

create table Historico_horas(
	 id_hist int identity(1,1)
	,id_comp int not null
	,horas time(3) not null
	,data_hist date not null
	,primary key(id_hist)
	,foreign key(id_comp) references Computadores(id_comp)
);

if exists(select * from sys.objects where object_id = Object_id(N'Setores') and type in (N'U'))
begin
	drop table Setores;
end

create table Setores(
	 id_setor int identity(1,1)
	,nome varchar(40)
	,primary key(id_setor)
)

if exists(select * from sys.objects where object_id = Object_id(N'Computador_Setor') and type in (N'U'))
begin
	drop table Computador_Setor;
end

create table Computador_Setor(
	 id_comp int
	,id_setor int
	,primary key(id_comp, id_setor)
	,foreign key(id_comp) references Computadores(id_comp)
	,foreign key(id_setor) references Setores(id_setor)
);
