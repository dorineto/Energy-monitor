import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

class Dashboard{
	private Conector conect;
	private String query;

	public Dashboard(){
		this.query = "";

		try{
			this.conect = new Conector();
			Painel.checkConnection(this.conect);
		}
		catch(IOException e){
			System.out.println("Erro ao tentar carregar o arquivo de configurações! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args){
		Dashboard d = new Dashboard();
		d.setup();

		System.out.println("\nPara sair aperte <Control+c>\n");
		try{
			while(true){
				d.display();
				Thread.sleep(7500);
			}
		}
		catch(InterruptedException e){
			System.out.println("Tempo esperado foi muito longo!");
		}
	}
	
	//Modifica a query utilizada no caso se o usuário quiser ver por setor ou mudar a ordem que os registros
	//estão organizados
	public void setup(){
		try{
			Painel.checkConnection(this.conect);
			
			//Verifica se há computadores cadastrados para serem monitorados
			String checkQuery = "select * from Computadores";
			ResultSet res = this.conect.getConnection().createStatement().executeQuery(checkQuery);
				
			if(!res.next()){
				System.out.println("Não há computadores cadastrados!");
				System.exit(1);
			}
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados!");
			System.exit(1);
		}
		
		Scanner sc = new Scanner(System.in);
		String filter = "";

		System.out.print("\nDeseja visualizar um setor?(s/N): ");
		if(sc.nextLine().equalsIgnoreCase("s")){
			int esc = -1;
			do{
				esc = Painel.getId_setor(this.conect.getConnection(), true);
				if(esc == -1)
					System.out.println("Opção Invalida");
				else if(esc == -3) //Caso de algum erro em uma consulta ao banco de dados
					System.exit(1);
			}while(esc == -1 && esc != -2); //-2 será retornado pelo getId_setor se não há setores cadastrados
			
			if(esc != 0 && esc != -2){
				filter = "inner join Computador_Setor cs (nolock) on (cs.id_comp = c.id_comp) "
					+"inner join Setores s (nolock) on (cs.id_setor = s.id_setor) "
					+"where s.id_setor = "+esc+" ";
			}
		}

		filter += "order by q.estado desc"; //Organiza os registros para que os ligados 
						    //estejam no topo no monitoramento
		System.out.print("\nDeseja organizar a ordem dos computadores?(s/N): ");
		if(sc.nextLine().equalsIgnoreCase("s")){
			boolean continuar = true;
			while(continuar){	
				System.out.print("\n");

				String ordem = ", ";
				int esc = Painel.getMenuOption(new String[] {"Nome", "Tempo", "Ultima data", "Consumo", "voltar"});
				switch(esc){
					case 1: //Nome
						ordem += "c.nome ";
						break;
					case 2: //Tempo
						ordem += "q.horas ";
						break;
					case 3: //Ultima data
						ordem += "4 ";
						break;
					case 4: //Consumo
						ordem += "5 ";
						break;
					case 5: //Voltar
						continuar = false;
						break;
					default:
						System.out.println("Opção inválida!");
				}

				if(esc != 5){
					System.out.print("\n");
					boolean continuarSub = true;
					while(continuarSub){
						int escSub = Painel.getMenuOption(new String[] {"Ascendente", "Descendente", "Voltar", "Sair"});
						
						switch(escSub){
							case 1: //Ascendente
								filter += ordem + "asc";
								continuar = false;
								continuarSub = false;
								break;
							case 2: //Descendente
								filter += ordem + "desc";
								continuar = false;
								continuarSub = false;
								break;
							case 4: //Sair
								continuar = false;
							case 3: //Voltar
								continuarSub = false;
							        break;
							default:
								System.out.println("Opção inválida!");	
						}
					}
				}

			}	
		}

		this.query =  "select c.nome [Nome do Computador] "
			    +",q.horas [Tempo acumulado atual] "
			    +",case when q.estado = 'L' then 'Ligado' else 'Desligado' end [Estado atual] "
                            +",isnull(cast((select top 1 dateadd(dd,1,data_hist) " 
		            +"from Historico_horas h (nolock) "
			    +"where h.id_comp = c.id_comp order by h.data_hist desc) as varchar(12)), N'Sem registro') [Ultima data ligado] "
			    +",cast(cast(((((datepart(hh, q.horas) * 60 + datepart(mi, q.horas)) *  60  + datepart(ss, q.horas)) * 1000 + datepart(ms, q.horas)) / 3600000.0 * c.media_consu / 1000.0) as decimal(4,3)) as varchar(7)) + 'kWh' [Consumo acumulado] "
			    +"from Computadores c (nolock) "
			    +"inner join Quant_horas q (nolock) on (q.id_comp = c.id_comp) "
			    +filter;
	}

	//Retorna uma String formatada com o resultado da query criada no método setup 
	public void display(){
		try{
			Painel.checkConnection(this.conect);

			ResultSet res = this.conect.getConnection().createStatement().executeQuery(this.query);
			String display = this.formatDisplay(res);
			System.out.println(display);
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

	//Formata a os resultados retornados pela query criada
	private String formatDisplay(ResultSet res){
		try{
			ArrayList<String[]> rowSet = new ArrayList<String[]>();
		
			byte[] padding = new byte[] { 0, 13, 10, 13}; //Tamanhos maximos dos textos mais um 
			while(res.next()){
				String[] row = new String[5];
			
				row[0] = res.getString(1);
				if(padding[0] < row[0].length()) //Verifica qual é o maior nome de computador
					padding[0] = (byte) row[0].length();

				for(byte i = 1; i < row.length; i++)
					row[i] = res.getString(i + 1); 
				
				rowSet.add(row);
			}

			padding[0] += 1; 
			String display = "Nome do computador | Tempo acumulado | Estado | Ultima data | Consumo atual\n";
			for(String[] row : rowSet){
				for(byte i = 0; i < row.length; i++){
					if(i == row.length - 1){
						display += row[i];
						continue;
					}

					String space = " ";
					for(byte j = 0; j <  padding[i] - row[i].length() - 1; j++)
					       space += " ";

					display += row[i] + space;	
				}
				display += "\n";
			}

			return display;
		}
		catch(SQLException e){
			System.out.println("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}

		return "";
	}
}
