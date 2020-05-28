import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.io.IOException;

import java.util.ArrayList; 
import java.util.Scanner;

class Painel{
	private Conector conect;
	private Relatorio relat;
	private Controle  contr;

	public Painel(){
		int trys = 3;
		while(trys != 0){
			try{
				this.conect = new Conector();
				break;
			}catch(IOException e){
				trys -= 1;
			}
		}

		if(trys == 0)
			throw new ConnectionException("Erro ao carregar o arquivo de configuração! Tente novamente mais tarde.");

		this.relat = new Relatorio();
		this.contr = new Controle();
	}

	public static void main(String[] args){
		boolean continuarPainel = true;
		Scanner sc = new Scanner(System.in);
		Painel p = null;	

		try{
			p = new Painel();
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			continuarPainel = false;
		}

		while(continuarPainel){
			System.out.println("\n----- Painel -----");
			int escSubMenu = Painel.getMenuOption(new String[] {"Relatórios", "Controle", "Sair"});
			
			switch(escSubMenu){
				case 1:
					break;
				case 2:
					boolean continuarSubMenu = true;
					while(continuarSubMenu){
						System.out.println("\n----- Controle -----");
						int escFunc =  Painel.getMenuOption(new String[] {"Cadastrar computador"
										    		 ,"Alterar informações de um computador"
										    		 ,"Remover computador"
												 ,"Pegar ID de um computador"
										    		 ,"Cadastrar setor"
										    		 ,"Adicionar computador(es) à um setor"
										    		 ,"Remover computador(es) de um setor"
										    		 ,"Alterar nome do setor"
										    		 ,"Remover setor"
										    		 ,"Voltar"
										    		 ,"Sair"});

						switch(escFunc){
							case 1:  //Cadastrar computador
								//Fazer implementação
								break;
							case 2:  //Alterar informações do computador
								//Fazer implementação
								break;
							case 3:  //Remover computador
								//Fazer implementação
								break;
							case 4: //Pegar ID de um computador
								p.pegaId_comp();
								break;
							case 5:  //Cadastrar Setor
								//Fazer implemnetação
								break;
							case 6:  //Adicionar computador(es) a um setor
								//Fazer implementação
								break;
							case 7:  //Remover computador(es) de um setor
								//Fazer implementação
								break;
							case 8:  //Alterar nome do setor
								//Fazer implementação
								break;
							case 9:  //Remover setor
								//Fazer implementação
								break;
							case 10:  //Voltar para o painel
								continuarSubMenu = false;
								break;
							case 11: //Sair
								continuarSubMenu = false;
								continuarPainel = false;
								break;
							default:
								System.out.println("Opção inválida! Tente novamente.");
						}
					}
					break;
				case 3:
					continuarPainel = false;
					break;
				default:
					System.out.println("Opção inválida! Tente novamente.");
			}
		} 	
	}

	//Faz um menu apartir do vetor de Strings passados e retorna o número da escolha do usuário, 
	//caso o usuário tenha escolhido uma opção inválida será retornado -1 
	public static int getMenuOption(String[] options){ 
		if(options.length == 0)
			throw new IllegalArgumentException("O array tem que ter no minimo 1 elemento");
		
		Scanner sc = new Scanner(System.in);
		
		for(int i = 0; i < options.length; i++){
			System.out.println((i + 1) + " - "+ options[i]);
		}
		
		System.out.print("\nEscolha uma das opções acima: ");
		try{
			int esc = Integer.parseInt(sc.nextLine());
			return (esc < 1 || esc > options.length) ? -1 : esc;	
		}catch(NumberFormatException e){ return -1; }
	}
	
	//Checa a se a conexão do conector ainda está ativa, caso a conexão não estiver mais ativa,
	//execultará uma nova conexão, se o processo falhar será tentado mais duas vez. Caso acabar a quantidade
	//de tentativas será lançado uma ConnectionException 
	public static void checkConnection(Conector conect){
		int trys = 3;
		while(trys != 0 && conect.getFailed()){
			conect.remakeConnection();
			trys -= 1;
		}

		if(conect.getFailed())
			throw new ConnectionException("Erro ao tentar conectar com o banco de dados! tente novamente mais tarde."); 
	}	

	//Faz um menu de escolha para o usuário apartir das informações da tabela Tipo_computador, e retorna o
	//id_tipo da escolha feita pelo o usuário. Caso o usuário escolha uma opção inválida será retornado -1.
	//Caso não tenha tipos cadastrados no banco de dados retornará -2.
	//Caso seja passado um Connection nulo ou erro ao execultar uma consulta será retornado -3. 
	public static int getId_tipo(Connection conect, boolean hadExit){
		if(conect == null)
			return -3;
		try{
			ArrayList<Integer> ids = new ArrayList<Integer>();
			ArrayList<String> options = new ArrayList<String>();

			String query = "select * from Tipo_computador (nolock)";
			ResultSet res = conect.createStatement().executeQuery(query);

			System.out.print("\n");
			while(res.next()){
				ids.add(res.getInt("id_tipo"));
				options.add(res.getString("nome"));
			}
			
			String[] optionsArr = new String[options.size() + (hadExit? 1 : 0)];
			optionsArr = options.toArray(optionsArr);

			if(hadExit)
				optionsArr[options.size()] = "Sair";

			int esc = Painel.getMenuOption( optionsArr );

			if(esc == -1)
				return -1;

			return esc == optionsArr.length? 0 : ids.get(esc - 1); 
		}catch(IllegalArgumentException e){
			System.out.println("Sem tipo de computador cadastrado!");
			return -2;
		}catch(SQLException e){
			System.out.println("Erro ao tentar fazer um processo no banco de dados! Teste novamente.");
			return -3;
		}
	}

	//Faz um menu de escolha para o usuário apartir das informações da tabela Conputadores, e retorna o
	//id_comp da escolha feita pelo o usuário. Caso o usuário escolha uma opção inválida será retornado -1. 
	//Caso não tenha computadores cadastrados no banco de dados retornará -2.
	//Caso seja passado um Connection nulo ou erro ao execultar uma consulta será retornado -3. 
	public static int getId_comp(Connection conect, boolean hadExit){
		if(conect == null)
			return -3;
		try{
			ArrayList<Integer> ids = new ArrayList<Integer>();
			ArrayList<String> options = new ArrayList<String>();

			String query = "select id_comp, nome from Computadores (nolock)";
			ResultSet res = conect.createStatement().executeQuery(query);

			System.out.print("\n");
			while(res.next()){
				ids.add(res.getInt("id_comp"));
				options.add(res.getString("nome"));
			}
			
			String[] optionsArr = new String[options.size() + (hadExit? 1 : 0)];
			optionsArr = options.toArray(optionsArr);

			if(hadExit)
				optionsArr[options.size()] = "Sair";

			int esc = Painel.getMenuOption( optionsArr );
			
			if(esc == -1)
				return -1;

			return esc == optionsArr.length? 0 : ids.get(esc - 1); 
		}
		catch(IllegalArgumentException e){
			System.out.println("Sem computadores cadastrados!");
			return -2;
		}catch(SQLException e){
			System.out.println("Erro ao tentar fazer um processo no banco de dados! Teste novamente.");
			return -3;
		}
	}

	//Faz um menu de escolha para o usuário apartir das informações da tabela Setores, e retorna o
	//id_setor da escolha feita pelo o usuário. Caso o usuário escolha uma opção inválida será retornado -1. 
	//Caso não tenha setores cadastrados no banco de dados retornará -2.
	//Caso seja passado um Connection nulo ou erro ao execultar uma consulta será retornado -3. 
	public static int getId_setor(Connection conect, boolean hadExit){
		if(conect == null)
			return -3;
		try{	
			ArrayList<Integer> ids = new ArrayList<Integer>();
			ArrayList<String> options = new ArrayList<String>();

			String query = "select * from Setores (nolock)";
			ResultSet res = conect.createStatement().executeQuery(query);

			System.out.print("\n");
			while(res.next()){
				ids.add(res.getInt("id_setor"));
				options.add(res.getString("nome"));
			}

			String[] optionsArr = new String[options.size() + (hadExit? 1 : 0)];
			optionsArr = options.toArray(optionsArr);

			if(hadExit)
				optionsArr[options.size()] = "Sair";
			
			int esc = Painel.getMenuOption( optionsArr );
			
			if(esc == -1)
				return -1;
				
			return esc == optionsArr.length? 0 : ids.get(esc - 1);
		}catch(IllegalArgumentException e){
			System.out.println("Sem setores cadastrados!");
			return -2;
		}catch(SQLException e){		
			System.out.println("Erro ao tentar fazer um processo no banco de dados! Teste novamente.");
			return -3;
		}

	}

	public void pegaId_comp(){
		int id_comp = 0;
		do{
			id_comp = Painel.getId_comp(this.conect.getConnection(), true);
			if(id_comp == -1)
				System.out.println("Opção inválida");
		}while(id_comp == -1);

		if(id_comp == -3)
			System.exit(1);
		else if(id_comp != 0)
			System.out.println("O ID do computador: "+id_comp);
	}
}
