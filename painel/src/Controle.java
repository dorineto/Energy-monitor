import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;

import java.util.ArrayList;

class Controle{
	private Conector conect;

	public Controle(){
		try{
			this.conect = new Conector();
			Painel.checkConnection(this.conect);
		}
		catch(IOException e){
			System.out.println("Erro ao tentar ler o arquivo de configurações! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	//Cadastra um novo computador de acordo com o Computador passado no parâmetro.
	//Caso exista no banco de dados um computador com o mesmo id_tipo, nome e valores de consumo
	//o computador não é inserido 
	public void cadastraComputador(Computador comp){
		try{
			Painel.checkConnection(this.conect);
			
			String query =  "select * from Computadores (nolock) where "
				       +"id_tipo="+comp.getId_tipo()+" and "
				       +"nome='"+comp.getNome()+"' and "
				       +"max_consu=cast("+comp.getConsumo_max()+" as decimal(7,2)) and "
				       +"media_consu=cast("+comp.getConsumo_medio()+" as decimal(7,2))";

			ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);

			if(res.next())
				throw new IllegalArgumentException("O computador "+comp.getNome()+" já está cadastrado!");

			query = "insert into Computadores(id_tipo, nome, max_consu, media_consu) values "
				      +"("+comp.getId_tipo()+", '"+comp.getNome()+"', "
				      +"cast("+comp.getConsumo_max()+" as decimal(7,2)), "
				      +"cast("+comp.getConsumo_medio()+" as decimal(7,2)))";

			this.conect.getConnection().createStatement().execute(query);

			query =  "insert into Quant_horas values "
				+"((select top 1 c.id_comp from Computadores c where c.id_tipo="+comp.getId_tipo()+" and "
				+"c.nome='"+comp.getNome()+"' and "
				+"c.max_consu=cast("+comp.getConsumo_max()+" as decimal(7,2)) and "
				+"c.media_consu=cast("+comp.getConsumo_medio()+" as decimal(7,2))), "
				+"'00:00:00.000', 'D')";

			this.conect.getConnection().createStatement().execute(query);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	//Altera as informações de um computador com as informações passadas pelo objeto Computador
	public void alteraComputador(Computador comp){
		try{
			Painel.checkConnection(this.conect);

			String query = "update Computadores set id_tipo="+comp.getId_tipo()+", "
				      +"nome='"+comp.getNome()+"', "
				      +"max_consu=cast("+comp.getConsumo_max()+" as decimal(7,2)), "
				      +"media_consu=cast("+comp.getConsumo_medio()+"as decimal(7,2)) "
				      +"where id_comp="+comp.getId_comp();

			this.conect.getConnection().createStatement().execute(query);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	//Remove o computador e suas depedências 
	public void removeComputador(int id_comp){
		try{
			Painel.checkConnection(this.conect);	
		
			String[] tabelas = new String[] {"Quant_horas", "Computador_Setor", "Historico_horas", "Computadores"};
			for(String tabela : tabelas){
				String query = "delete from "+tabela+" where id_comp="+id_comp;
				this.conect.getConnection().createStatement().execute(query);
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	//Cadastra no banco de dados o nome do setor passado por parâmtro
	public void cadastraSetor(String nomeSetor){
		try{
			Painel.checkConnection(this.conect);

			String query = "insert into Setores(nome) values ('"+nomeSetor+"')";
			this.conect.getConnection().createStatement().execute(query);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	//Adiciona os computadores passados no ArrayList de Integer no setor passado no id_setor
	public void adicionaComputadorSetor(int id_setor, ArrayList<Integer> id_comps){
		try{
			Painel.checkConnection(this.conect);

			String inserts = "";
			for(int id_comp : id_comps)
				inserts += "("+id_comp+", "+id_setor+"),";

			inserts = inserts.substring(0, inserts.length() - 1);
			
			String query = "insert into Computador_Setor values "+inserts;
			this.conect.getConnection().createStatement().execute(query);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	//Remove os computadores passados pelo ArrayList de Integer no setor passado pelo id_setor
	public void removeComputadorSetor(int id_setor, ArrayList<Integer> id_comps){
		try{
			Painel.checkConnection(this.conect);

			String removes = "(";
			for(int id_comp : id_comps)
				removes += id_comp+",";

			removes = removes.substring(0, removes.length() - 1) + ")";

			String query = "delete from Computador_Setor where id_setor="+id_setor+" "
				      +"and id_comp in "+removes;

			this.conect.getConnection().createStatement().execute(query);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	//Altera o nome do setor com o novoNome passado
	public void alteraNomeSetor(int id_setor, String novoNome){
		try{
			Painel.checkConnection(this.conect);

			String query = "update Setores set nome='"+novoNome+"' where id_setor="+id_setor;
			this.conect.getConnection().createStatement().execute(query);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	//Remove o setor e suas dependências 
	public void removeSetor(int id_setor){
		try{
			Painel.checkConnection(this.conect);

			String query = "delete from Computador_Setor where id_setor="+id_setor;
			this.conect.getConnection().createStatement().execute(query);

			query = "delete from Setores where id_setor="+id_setor;
			this.conect.getConnection().createStatement().execute(query);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}
}
