use Energy_monitor;
go

set NOCOUNT on;

insert into Setores(nome) values ('RH'),
				 ('Desenvolvimento'),
				 ('Suporte'),
				 ('Servidores');
go

insert into Computadores(id_tipo, nome, max_consu, media_consu) values  (1, 'Computador Marcos', 250, 140),
									(1, 'Computador Mariana', 310, 265),
									(2, 'Computador Josefa', 260, 180),
									(2, 'Computador Andre', 280, 210),
									(3, 'Deathstar', 450, 395);
go

insert into Quant_horas values  (1, '00:00:00', 'D'),
				(2, '00:00:00', 'D'),
				(3, '00:00:00', 'D'),
				(4, '00:00:00', 'D'),
				(5, '00:00:00', 'D');
go

insert into Computador_Setor values  	(1, 1),
					(2, 2),
					(3, 1),
					(4, 3),
					(5, 4);
go


insert into Historico_horas(id_comp, horas, data_hist) values  (1, '00:00:00', '2020-05-10'),
								(2, '00:00:00', '2020-05-10'),
								(3, '00:00:00', '2020-05-10'),
								(4, '00:00:00', '2020-05-10'),
								(5, '00:00:00', '2020-05-10'),
								(1, '08:10:12', '2020-05-11'),
								(2, '04:22:54', '2020-05-11'),
								(3, '07:58:15', '2020-05-11'),
								(5, '16:02:22', '2020-05-11'),
								(1, '06:58:15', '2020-05-12'),
								(2, '08:02:37', '2020-05-12'),
								(3, '00:00:00', '2020-05-12'),
								(5, '18:48:15', '2020-05-12');
go

print '';
print '-- Computador completo com tempo e estado atual--';
select   c.nome [Nome computador]
	,t.nome [Tipo]
	,c.max_consu [Consumo máximo]
	,c.media_consu [Consumo médio]
	,q.horas [Quantidade de horas atual]
	,(case when q.estado = 'L' then 'Ligado' else 'Desligado' end) [Estado atual] 
from Computadores c (nolock)
inner join Tipo_computador t (nolock) on (c.id_tipo = t.id_tipo)
inner join Quant_horas     q (nolock) on (q.id_comp = c.id_comp)
order by 6, t.nome, c.nome;
go

print '';
print '-- Computador com tipo por setores';
select   c.nome [Nome computador]
	,t.nome [Tipo]
	,s.nome [Setor]
from Computador_Setor cs (nolock)
inner join Computadores    c (nolock) on (cs.id_comp = c.id_comp)
inner join Tipo_computador t (nolock) on (c.id_tipo = t.id_tipo)
inner join Setores         s (nolock) on (cs.id_setor = s.id_setor)
order by 3, 1;
go

print '';
print '-- Computador historico --';
select   c.nome      [Nome computador]
	,h.horas     [Horas aculadas]
	,h.data_hist [Data]
from Computadores c (nolock)
inner join Historico_horas h (nolock) on (h.id_comp = c.id_comp)
order by h.data_hist, h.horas;

set NOCOUNT off;
