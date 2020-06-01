import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.io.IOException;

import java.util.ArrayList; 
import java.util.Scanner;
import java.util.HashSet;
import java.util.Iterator;

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
					boolean continuarSubMenu = true;
				        while(continuarSubMenu){
						System.out.println("\n----- Relatórios -----");
						int escRel = Painel.getMenuOption(new String[] {"Relatório de gasto mensal atual"
											       ,"Relatório de espectativa mensal"
											       ,"Voltar"
											       ,"Sair"});

						switch(escRel){
							case 1: //Relatório de gasto atual
								String filtro = p.getFilter();
								if(!filtro.isEmpty())
									p.getRelatorio().relGastoMensalAtual(filtro);
								break;
							case 2: //Relatório de espectativa
								filtro = p.getFilter();
								if(!filtro.isEmpty()){
									int quant_horas = 0;
									while(quant_horas <= 0){
										try{
											System.out.print("\nInsirá a quantidade horas média para o calculo: ");
											quant_horas = Integer.parseInt(sc.nextLine());
										}
										catch(NumberFormatException e){
											quant_horas = 0;
										}
										if(quant_horas <= 0)
											System.out.println("Quantidade de horas inválida! Tente novamente.");
									}

									
									p.getRelatorio().relExpGastoMensal(filtro, quant_horas);
								}
								break;
							case 3: //Voltar
								continuarSubMenu = false;
								break;
							case 4: //Sair
								continuarSubMenu = false;
								continuarPainel = false;
								break;
							default:
								System.out.println("Opção inválida!");
						}

					}	
					break;
				case 2:
					continuarSubMenu = true;
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
								p.cadastraComputadorUI();
								break;
							case 2:  //Alterar informações do computador
								p.alteraComputadorUI();
								break;
							case 3:  //Remover computador
								p.removeComputadorUI();
								break;
							case 4: //Pegar ID de um computador
								p.pegaId_comp();
								break;
							case 5:  //Cadastrar Setor
								p.cadastraSetorUI();
								break;
							case 6:  //Adicionar computador(es) a um setor
								p.adicionaComputadorSetorUI();	
								break;
							case 7:  //Remover computador(es) de um setor
								p.removeComputadorSetorUI();
								break;
							case 8:  //Alterar nome do setor
								p.alteraNomeSetorUI();
								break;
							case 9:  //Remover setor
								p.removeSetorUI();
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
	
	//Pega as informações de id_tipo, nome do computador e consumos maximos e médios, 
	//e passa a intânciancia de computador com essas informações para o controle para finalizar o cadastro
	public void cadastraComputadorUI(){
		try{
			Painel.checkConnection(this.conect);

			int id_tipo = this.getEscTipo_computador();
			if(id_tipo != 0 && id_tipo != -2){
				Scanner sc = new Scanner(System.in);

				Computador novoComp = new Computador(id_tipo
								    ,this.getNomeComputador()
								    ,this.getConsumos());

				String query = "select nome from Tipo_computador where id_tipo="+id_tipo;
				ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);
				res.next();

				System.out.println("\nTipo do computador: "+res.getString("nome")+"\n"
						   +novoComp);

				System.out.print("Deseja cadastrar o computador acima?(s/N): ");
				if(sc.nextLine().equalsIgnoreCase("s")){
					this.contr.cadastraComputador(novoComp);
					System.out.println("O computador "+novoComp.getNome()+" foi cadastrado com sucesso!");
				}
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao tentar fazer uma operação no banco de dados. Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
		catch(IllegalArgumentException e){
			System.out.println(e.getMessage());
		}
	}

	//Pede a informação do nome de um compututador e devolve o nome um uma String
	private String getNomeComputador(){
		Scanner sc = new Scanner(System.in);

		System.out.print("\nInsira o novo nome do computador: ");
		return sc.nextLine();
	}

	//Pega a informação do id_tipo escolhido pelo usuário e devolve o id_tipo escolhido pelo usuário
	private int getEscTipo_computador(){
		try{
			Painel.checkConnection(this.conect);

			int id_tipo = -1;
			while(id_tipo == -1 && id_tipo != -2){
				id_tipo = Painel.getId_tipo(this.conect.getConnection(),true);
				if(id_tipo == -1)
					System.out.println("Opção inválida!");
				else if(id_tipo == -3)
					System.exit(1);
			}

			return id_tipo;
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return -1;
	}

	//Pede as informações de consumo de processador, memória RAm, memória de armazenamento (HDD ou SDD), placa mãe,
	//placa de videos se tiver e monitor e devolve os consumos em array de double
	private double[] getConsumos(){
		Scanner sc = new Scanner(System.in);

		ArrayList<Double> listaDeConsumos = new ArrayList<Double>();

		String[] perguntas = new String[] {"processador", "memória(s) RAM(s)", "memória(s) de armazenamento(s)", 
						   "placa mãe", "placa(s) de vídeo", "monitor(es)"};
		String[] limites = new String[] {"1", "1n", "1n", "1", "0n", "0n"};
		for(byte i = 0; i < perguntas.length; i++){
			byte quant = 0;
			if(limites[i].equals("1")){
				quant = 1;
			}
			else{
				byte min = Byte.parseByte(limites[i].substring(0,1));
				do{
					System.out.print("Quantos/quantas "+perguntas[i]+" têm nesse computador (min:"+min+"): ");
					try{
						quant = Byte.parseByte(sc.nextLine());
					}
					catch(NumberFormatException e){
						quant = -1;
					}

					if(quant < min)
						System.out.println("Quantidade inválida. tente novamente!");
				}while(quant < min);

			}
			
			double consumoSum = 0;
			for(byte j = 1; j <= quant; j++){
				double consumo = 0;
				while(consumo <= 0){
					System.out.print("Quanto consome (em watts) a/o "
							+perguntas[i].replaceAll("\\(s\\)|\\(es\\)", "")
							+(quant == 1? ": " : " "+i+": "));
					
					try{
						consumo = Double.parseDouble(sc.nextLine()); 
					}
					catch(NumberFormatException e){
						consumo = 0;
					}
					
					if(consumo == 0)
						System.out.println("Consumo inválido. tente novamente!");
				}
				consumoSum += consumo;
			}
			listaDeConsumos.add(consumoSum);
		}

		double[] consumoArr = new double[listaDeConsumos.size()];
		for(byte i = 0; i < consumoArr.length; i++)
			consumoArr[i] = listaDeConsumos.get(i);
	
		return consumoArr; 
	}

	//Pega o id_comp escolhido e pode fazer as seguintes alterações: modificar o nome, modificar o tipo e 
	//alterar o consumo essas alterações podem ser feitas varias vezes até o usuário confirmar a alteração
	public void alteraComputadorUI(){
		try{
			Painel.checkConnection(this.conect);
			
			int id_comp = -1;
			while(id_comp == -1 && id_comp != -2){
				id_comp = Painel.getId_comp(this.conect.getConnection(), true);
				if(id_comp == -1)
					System.out.println("Opção inválida");
				else if(id_comp == -3)
					System.exit(1);
			}

			if(id_comp != 0 && id_comp != -2){
				String query = "select c.id_tipo [IdTipoComp], c.nome [NomeComp], c.max_consu [MaxConsu], c.media_consu [MediaConsu], t.nome [TipoNome] from Computadores c (nolock) "
					      +"inner join Tipo_computador t (nolock) on (c.id_tipo = t.id_tipo) "
					      +"where id_comp="+id_comp;
				ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);
				res.next();

				Computador comp = new Computador(res.getInt("IdTipoComp"), res.getString("NomeComp"), id_comp);

				comp.setConsumo_max(res.getDouble("MaxConsu"));
				comp.setConsumo_medio(res.getDouble("MediaConsu"));
				
				String nomeTipo = res.getString("TipoNome");

				boolean continuar = true;	
				while(continuar){
					String compStr = "---Anteriormente---\n"
							+"Tipo computador: "+nomeTipo+"\n"
							+comp
							+"---Atualmente---\n";
					
					System.out.print("\n");
					int esc = Painel.getMenuOption(new String[] {"Nome", "Tipo computador", "Consumos", "Sair"});
					switch(esc){
						case 1:
							comp.setNome(this.getNomeComputador());
							break;
						case 2:
							int id_tipo = -1;
							while(id_tipo == -1 && id_tipo != -2){
								id_tipo = Painel.getId_tipo(this.conect.getConnection(), true);
								if(id_tipo == -1)
									System.out.println("Opção inválida!");
								else if(id_tipo == -3)
									System.exit(1);
							}
							
							if(id_tipo != 0 && id_tipo != -2){
								comp.setId_tipo(id_tipo);
							}
							break;
						case 3:
							comp.setConsumos(this.getConsumos());
							break;
						case 4:
							continuar = false;
							break;
						default:
							System.out.println("Opção inválida!");
					}
					
					if(esc != -1 && esc != 4){
						Scanner sc = new Scanner(System.in);
						
						query = "select nome from Tipo_computador (nolock) "
							+"where id_tipo = "+comp.getId_tipo();

						res = this.conect.getConnection().createStatement().executeQuery(query);

						res.next();

						System.out.println("\n"+compStr
							  	  +"Tipo computador: "+res.getString("nome")+"\n"
							  	  +comp);
						
						System.out.print("Deseja aplicar as alterações acima?(s/N): ");
						if(sc.nextLine().equalsIgnoreCase("s")){
							contr.alteraComputador(comp);
							System.out.println("Alterações aplicadas com sucesso!");
							continuar = false;
						}
						else{
							System.out.print("\nDeseja continuar fazendo mudanças?(n/S): ");
							continuar = !sc.nextLine().equalsIgnoreCase("n");
						}
					}
				}
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao tentar fazer uma operação no banco de dados! Menagem de erro: "+e.getMessage());
			System.exit(1);
		}	
	}

	//Remove o computador escolhido pelo usuário e suas dependências
	public void removeComputadorUI(){
		try{
			Painel.checkConnection(this.conect);

			int id_comp = -1;
			while(id_comp == -1 && id_comp != -2){
				id_comp = Painel.getId_comp(this.conect.getConnection(), true);
				if(id_comp == -1)
					System.out.println("Opção inválida!");
				else if(id_comp == -3)
					System.exit(1);
			}

			if(id_comp != 0 && id_comp != -2){
				Scanner sc = new Scanner(System.in);
				String query = "select nome from Computadores where id_comp="+id_comp;

				ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);
				res.next();

				String nomeComp = res.getString("nome");

				System.out.print("\nDeseja remover o computador "+nomeComp+"?(s/N): ");
				if(sc.nextLine().equalsIgnoreCase("s")){
					contr.removeComputador(id_comp);
					System.out.println("O computador "+nomeComp+" foi removido com sucesso!");
				}

			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao tentar fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}
	
	//Mostra para o usuário qual é o id_comp do computador escolhido para depois utilizar no coletor
	public void pegaId_comp(){
		int id_comp = 0;
		do{
			id_comp = Painel.getId_comp(this.conect.getConnection(), true);
			if(id_comp == -1)
				System.out.println("Opção inválida");
			if(id_comp == -3)
				System.exit(1);
		}while(id_comp == -1 && id_comp != -2);

		if(id_comp != 0 && id_comp != -2)
			System.out.println("O ID do computador: "+id_comp);
	}

	//Cadastra um setor pedindo o nome do setor para o usuário
	public void cadastraSetorUI(){
		Scanner sc = new Scanner(System.in);

		System.out.print("\nInsira o nome do setor: ");
		String nomeSetor = sc.nextLine();
		
		System.out.print("\nDeseja mesmo inserir o setor "+nomeSetor+"?(s/N): ");
		if(sc.nextLine().equalsIgnoreCase("s")){
			this.contr.cadastraSetor(nomeSetor);
			System.out.println("\nSetor cadastrado com sucesso!");
		}
	}
	
	//Cadastra os computadores selecionados pelo usuário
	public void adicionaComputadorSetorUI(){
		try{
			Painel.checkConnection(this.conect);
			Scanner sc = new Scanner(System.in);
			
			int id_setor = -1;
			while(id_setor == -1 && id_setor != -2){
				id_setor = Painel.getId_setor(this.conect.getConnection(),true);
				if(id_setor == -1)
					System.out.println("Opção inválida!");
				else if(id_setor == -3)
					System.exit(1);
			}
			
			if(id_setor != 0 && id_setor != -2){
				ArrayList<Integer> id_comps = new ArrayList<Integer>();
				ArrayList<String> nome_comps = new ArrayList<String>();

				String query = "Select c.id_comp, c.nome from Computadores c (nolock) "
					      +"where c.id_comp not in "
					      +"(select cs.id_comp from Computador_Setor cs (nolock) where cs.id_setor="+id_setor+")";
				ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);

				while(res.next()){
					id_comps.add(res.getInt(1));
					nome_comps.add(res.getString(2));	
				}
				
				nome_comps.add("Sair");


				if(!id_comps.isEmpty()){
					System.out.print("\n");
					
					for(int i = 0; i < nome_comps.size(); i++)
						System.out.println((i + 1)+" - "+nome_comps.get(i));

					System.out.print("\nEscolha um ou mais computadores acima(separando por espaço cada opção): ");
					
					String[] escs = sc.nextLine().split(" ");
					HashSet<Integer> opcoesValidas = new HashSet<Integer>();
					
					boolean sair = false;
					boolean mensagemDeErro = false;
					for(String esc : escs){
						try{
							int opcao = Integer.parseInt(esc);
							if(opcao == nome_comps.size()){
								sair = true;
								break;
							}
							else if(opcao - 1 < 0 ||  opcao > nome_comps.size())
								mensagemDeErro = true;
							else	
								opcoesValidas.add(opcao);
						} 
						catch(NumberFormatException e){
							mensagemDeErro = true;
						}
					}

					if(!sair && !opcoesValidas.isEmpty()){
						ArrayList<Integer> id_compsSel = new ArrayList<Integer>();
						String strNomeComps = "";
						
						for(int opcoes : opcoesValidas){ 
							strNomeComps += nome_comps.get(opcoes - 1) + ", ";
							id_compsSel.add(id_comps.get(opcoes - 1)); 		
						}

						strNomeComps = strNomeComps.substring(0, strNomeComps.length() - 2);

						if(mensagemDeErro)
							System.out.println("Houve um(as) opção(ões) invalida(s)!");

						System.out.print("Deseja cadastrar o(s) computadore(s) "+strNomeComps+"?(s/N): ");
						if(sc.nextLine().equalsIgnoreCase("s")){
							this.contr.adicionaComputadorSetor(id_setor, id_compsSel);
							System.out.println("O(s) cadastro(s) realizado(s) com sucesso!");
						}
					}
					else if(mensagemDeErro && !sair)
						System.out.println("Opção(ões) inválida(s)!");
				}
				else
					System.out.println("Não há mais computadores a serem cadastrados nesse setor!");
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao tentar fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}
	
	//Remove os computadores selecionados pelo usuário de um setor que também foi selecionado pelo usuário
	public void removeComputadorSetorUI(){
		try{
			Painel.checkConnection(this.conect);

			int id_setor = -1;
			while(id_setor == -1 && id_setor != -2){
				id_setor = Painel.getId_setor(this.conect.getConnection(),true);
				if(id_setor == -1)
					System.out.println("Opção inválida!");
				else if(id_setor == -3)
					System.exit(1);
			}

			if(id_setor != 0 && id_setor != -2){
				ArrayList<Integer> id_comps = new ArrayList<Integer>();
				ArrayList<String> nome_comps = new ArrayList<String>();
				
				Scanner sc = new Scanner(System.in);

				String query = "select c.id_comp, c.nome from Computadores c (nolock) "
					      +"inner join Computador_Setor cs (nolock) on (cs.id_comp = c. id_comp) "
					      +"where cs.id_setor = "+id_setor;

				ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);

				while(res.next()){
					id_comps.add(res.getInt(1));
					nome_comps.add(res.getString(2));
				}
				
				nome_comps.add("Sair");
				if(!id_comps.isEmpty()){
					System.out.print("\n");
					for(int i = 0; i < nome_comps.size(); i++)
						System.out.println((i+1)+" - "+nome_comps.get(i));

					
					System.out.print("\nEscolha um ou mais computadores acima(separando por espaço cada opção): ");
					String[] escs = sc.nextLine().split(" ");

					HashSet<Integer> opcoesValidas = new HashSet<Integer>();

					boolean sair = false;
					boolean mensagemDeErro = false;
					for(String esc : escs){
						try{
							int opcao = Integer.parseInt(esc);
							if(opcao == nome_comps.size()){
								sair = true;
								break;
							}	
							else if(opcao - 1 < 0 || opcao > nome_comps.size())
								mensagemDeErro = true;
							else
								opcoesValidas.add(opcao);

						}
						catch(NumberFormatException e){
							mensagemDeErro = true;
						}
					}

					if(!sair && !opcoesValidas.isEmpty()){
						ArrayList<Integer> id_compsSel = new ArrayList<Integer>();
						
						String strNomeComps = "";
						for(int opcao : opcoesValidas){
							strNomeComps += nome_comps.get(opcao - 1)+", ";
							id_compsSel.add(id_comps.get(opcao - 1));
						}
						
						strNomeComps = strNomeComps.substring(0, strNomeComps.length() - 2);

						if(mensagemDeErro)
							System.out.println("Houve um(as) opção(ões) invalida(s)!");

						
						System.out.print("Deseja remover o(s) computadore(s) "+strNomeComps+" do setor?(s/N): ");
						if(sc.nextLine().equalsIgnoreCase("s")){
							this.contr.removeComputadorSetor(id_setor, id_compsSel);
							System.out.println("O(s) computador(es) removido(s) com sucesso!");
						}

					}
					else if(mensagemDeErro && !sair)
						System.out.println("Opção(ões) inválida(s)!");
				}
				else
					System.out.println("Não há mais computadores a serem removidos desse setor!");
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao tentar fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	//Altera o nome de um setor escolhido pelo usuário
	public void alteraNomeSetorUI(){
		try{
			Painel.checkConnection(this.conect);
			
			int id_setor = -1; 
			while(id_setor == -1 && id_setor != -2){
				id_setor = Painel.getId_setor(this.conect.getConnection(), true);
				if(id_setor == -1)
					System.out.println("Opção inválida!");
				else if(id_setor == -3)
					System.exit(1);
			}

			if(id_setor != 0 && id_setor != -2){
				Scanner sc = new Scanner(System.in);
				
				System.out.print("\nInsira o novo nome do setor: ");
				String novoNome = sc.nextLine();
				
				System.out.print("Deseja mesmo alterar o nome do setor para "+novoNome+"?(s/N): ");
				if(sc.nextLine().equalsIgnoreCase("s")){
					this.contr.alteraNomeSetor(id_setor, novoNome);
					System.out.println("O nome do setor foi alterado com sucesso!");
				}
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	//Remove o setor escolhido pelo usuário
	public void removeSetorUI(){
		try{
			Painel.checkConnection(this.conect);

			int id_setor = -1;
			while(id_setor == -1 && id_setor != -2){
				id_setor = Painel.getId_setor(this.conect.getConnection(), true);
				if(id_setor == -1)
					System.out.println("Opção inválida!");
				else if(id_setor == -3)
					System.exit(1);
			}
			
			if(id_setor != 0 && id_setor != -2){
				Scanner sc = new Scanner(System.in);
				
				String query = "select nome from Setores where id_setor="+id_setor;
				ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);
				res.next();
				
				String nomeSetor = res.getString("nome");

				System.out.print("Deseja remover o setor "+nomeSetor+"?(s/N): ");
				if(sc.nextLine().equalsIgnoreCase("s")){
					this.contr.removeSetor(id_setor);
					System.out.println("O setor "+nomeSetor+" foi removido com sucesso!");
				}	
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao tentar fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}

	public String getFilter(){
		try{
			Painel.checkConnection(this.conect);
			
			boolean continuar = true;
			while(continuar){
				String filter = "";

				System.out.println("\nQual por filtro deseja tirar o relatório:\n ");
				int esc = Painel.getMenuOption(new String[] {"Por computador", "Por setor", "Sair"});
				
				switch(esc){
					case 1:
						int id_comp = -1;
						while(id_comp == -1 && id_comp != -2){
							id_comp = Painel.getId_comp(this.conect.getConnection(), true);
							if(id_comp == -1)
								System.out.println("Opção inválida!");
							else if(id_comp == -3)
								System.exit(1);
						}

						if(id_comp != 0 && id_comp != -2){
							filter += "where c.id_comp="+id_comp+" ";
							return filter;
						}
						else{
							continuar = false;
						}
						break;
					case 2:
						int id_setor = -1;
						while(id_setor == -1 && id_setor != -2){
							id_setor = Painel.getId_setor(this.conect.getConnection(), true);
							if(id_setor == -1)
								System.out.println("Opção inválida");
							else if(id_setor == -3)
								System.exit(1);
						}
						
						if(id_setor != 0 && id_setor != -2){
							filter += "inner join Computador_Setor cs (nolock) on (cs.id_comp = c.id_comp) "
								 +"where cs.id_setor = "+id_setor+" ";

							return filter;
						}
						else{
							continuar = false;
						}
						break;
					case 3:
						continuar = false;
					default:
						System.out.println("Opção inválida!");		
				}
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return "";
	}

	public Relatorio getRelatorio(){ return this.relat; }
}
