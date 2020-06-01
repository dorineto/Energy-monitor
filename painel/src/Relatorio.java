import java.util.Scanner;

import java.io.IOException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.time.DateTimeException;

class Relatorio{
	private Conector conect;

	public Relatorio(){
		try{
			this.conect = new Conector();
			Painel.checkConnection(this.conect);
		}
		catch(IOException e){
			System.out.println("Erro ao tentar carregar o arquivo de configuração! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
		catch(ConnectionException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	public void relGastoMensalAtual(String filter){
		try{
			Painel.checkConnection(this.conect);
			Scanner sc = new Scanner(System.in);

			LocalDate dataEsc = LocalDate.now();

			System.out.print("Deseja selecionar um mês e ano diferente do atual?(s/N): ");
			if(sc.nextLine().equalsIgnoreCase("s")){
				boolean continuar = true;
				while(continuar){
					try{
						System.out.print("\nInsira o mês e ano, no seguinte formato aaaa-mm: ");
						dataEsc = LocalDate.parse(sc.nextLine()+"-01");
						continuar = false;
					}
					catch(DateTimeException e){
						System.out.println("A data inserida é inválida! Tente novamente.");
					}
				}
			}
			
			String data_final = dataEsc.getYear()+"-"+dataEsc.getMonthValue();

			if(dataEsc.getMonthValue() == 2){
				data_final += "-"+((dataEsc.isLeapYear())? "29" : "28");
			}
			else{
				data_final += "-31";
				
				try{
					LocalDate.parse(data_final);
				}
				catch(DateTimeException e){
					data_final = dataEsc.getYear()+"-"+dataEsc.getMonthValue()+"-30";
				}
			}


			String query = "select c.nome [NomeComp], h.data_hist [Data], c.media_consu [MediaConsumo], h.horas [TempoAcumulado], "
				      +"cast(cast(((((datepart(hh, h.horas) * 60 + datepart(mi, h.horas)) * 60 + datepart(ss,h.horas)) * 60 + datepart(ms, h.horas)) / 3600000.0 * c.media_consu / 1000.0) as decimal(5,3)) as varchar(8)) + 'kWh' [GastoCalc] "
				      +"from Computadores c (nolock) "
				      +"inner join Historico_horas h (nolock) on (h.id_comp = c.id_comp) "
				      +filter
				      +"and h.data_hist between '"+dataEsc.getYear()+"-"+dataEsc.getMonthValue()+"-01' and '"+data_final+"' "
				      +"order by h.data_hist desc, c.nome, h.horas desc";

			ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);

			double acumulado = 0;
			String output = "Nome Computador |  Data  |  Media de consumo  | TempoAcumulado | Gasto do dia (em kWh)\n";
			while(res.next()){
				String[] colunas = new String[] {"NomeComp", "Data", "MediaConsumo", "TempoAcumulado"
								,"GastoCalc"};
				
				String linha = "";
				for(String coluna : colunas){
					linha += res.getString(coluna)+" ";
					if(coluna.equals("GastoCalc")){
						String gasto = res.getString(coluna);

						acumulado += Double.parseDouble(gasto.substring(0,gasto.length()-3));
					}
				}

				output += linha.trim() + "\n";
			}

			System.out.println(output + String.format("Total gasto: %.3f kWh", acumulado));
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

	public void relExpGastoMensal(String filter, int quantHoras){
		try{
			Painel.checkConnection(this.conect);

			String query = "select c.nome, c.media_consu from Computadores c (nolock) "
				      +filter+" "
				      +"order by 1, 2 desc";	

			ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);
			
			double acumuladoGeral = 0;
			String output = "Nome computador | Expectativa de consumo do mensal\n----------\n";
			while(res.next()){
				double media = res.getDouble(2);
				
				double acumuladoLocal = 0;
				for(byte i = 0; i <= 30; i++)
					acumuladoLocal += quantHoras * media / 1000.0;
				
				output += res.getString(1)+" "+String.format("%.3f kWh", acumuladoLocal)+"\n";

				acumuladoGeral += acumuladoLocal;
			}

			System.out.println(output
					  +"----------"
					  +"\n\nTotal gasto esperado: "+String.format("%.3f kWh", acumuladoGeral));
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
