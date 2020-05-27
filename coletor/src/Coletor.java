import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

class Coletor{
	private Conector conect;
	private Configuracao config;
	private Computador comp;
	private long refreshTime;
	private boolean started;
	private final File ERROR_LOG;

	
	// O contrutor do coletor faz as primeiras validações de id_comp, refreshTime, se o id_comp está cadastrado ou
	// em uso. Caso seja a primeira faz ligando o coletor em um computador com id_comp recem cadastrado, será lançado
	// um registro na tabela Historico horas com o dia anterior do atual com 00:00:00 assim fazendo o marco inicial
	// desse id_comp no sistema.
	public Coletor(){
		this.started = false;
		this.ERROR_LOG = new File("./log/error.log");

		try{
			this.ERROR_LOG.createNewFile();
		}
		catch(IOException e){
			System.out.println("Erro ao tentar abrir ou criar o arquivo error.log. Error message: "+e.getMessage());
			System.exit(1);
		}

		int trys = 3;
		while(trys != 0){
			try{
				this.conect = new Conector();
				this.config = new Configuracao();
				break;
			}
			catch(IOException e){
				trys -= 1;		
			}
		}
		
		this.checkConnection();

		if(trys != 0){
			try{
				this.refreshTime = Long.parseLong(this.config.getConfig("refresh_time"));
				int id_comp = Integer.parseInt(this.config.getConfig("id_comp"));
				
				String query = "select * from Quant_horas where id_comp = "+id_comp;
				
				ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);
				
				
				if(!res.next()){ //Faz a checagem se o id_comp na configuração está cadastrado 
					         //no banco de dados 
					this.writeErrorLog("O id_comp indicado não foi cadastrado corretamente!");
					System.exit(1);
				}
				else if(res.getString("estado").equals("L")){ //Faz verificação se o id_comp indicado 
									      //está atualmente em uso no momento
					this.writeErrorLog("O id_comp indicado já está em uso no momento!");
					System.exit(1);
				}


				LocalDate data = null;
				
				//Faz a query para pegar a ultima data que o coletor foi ligado pela tabela
				//Historico_horas
				query = "select top 1 data_hist from Historico_horas (nolock) "
				       +"where id_comp = "+id_comp+" "
				       +"order by data_hist desc";
		
				res = this.conect.getConnection().createStatement().executeQuery(query);
				
				//Caso o id_comp tenha pelo menos um registro de histórico esse o dia seguinte desse
				//do dia no historico será a ultima data que o coletor foi ligado
				if(res.next()){
					data = LocalDate.parse(res.getString("data_hist")).plusDays(1);	
				}
				else{
					//Caso contrario será feito o registro inicial desse id_comp na tabela Historico_horas
					//no dia anterior do atual
					data = LocalDate.now();

					query = "insert into Historico_horas(id_comp, horas, data_hist) values "
					       +"("+id_comp+", '00:00:00', '"+data.minusDays(1).toString()+"')";

					this.conect.getConnection().createStatement().execute(query);	
				}	


				this.comp = new Computador(id_comp, data);
			}
			catch(NumberFormatException e){
				this.writeErrorLog("O id_comp ou refresh_time não é um valor numerico válido!");
				System.exit(1);
			}
			catch(SQLException e){
				e.printStackTrace();
				this.writeErrorLog("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
				System.exit(1);
			}
		}
		else{
			this.writeErrorLog("Erro ao tentar carregar as configurações!");
			System.exit(1);
		}

	} 

	public static void main(String[] args){
		Coletor colet = new Coletor();	
		Runtime.getRuntime().addShutdownHook( new Thread( new StopColetor(colet) ) ); //Adiciona uma rotina quando
											      //o programa é finalizado
		colet.start();	
		while(true){
			colet.markTime(false);
			colet.timeOut();	
		}
	}

	//Checa se a conexão do atributo conect ainda está aberta	
	private void checkConnection(){
		int trys = 3;
		while(trys != 0 && this.conect.getFailed()){
			this.conect.remakeConnection();
			trys -= 1;
		}

		if(this.conect.getFailed()){
			this.writeErrorLog("Erro ao tentar refazer a conexão com o banco de dados! Mensagem de erro: "+this.conect.getReason());
			System.exit(1);
		}

	}	

	public void start(){
		try{
			this.checkConnection();
		
			int id_comp = this.comp.getId_comp();
			String query = "";
			ResultSet res;	

			/*String query = "Select * from Computadores (nolock) where id_comp = "+id_comp;
			ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);
			if(! res.next()){
				this.writeErrorLog("O id_comp no arquivo config.conf é inválido. Por favor verifique.");
				System.exit(1);
			}*/
			
			if(this.checkDayPassed()){ //Verifica se a ultima data no sistema é anterior da atual, 
						   //se passou a quantidade de horas atual tabela Quant_horas
						   //E registrada na tabela Historico de horas com a ultima da do sistema 
				query = "select horas from Quant_horas (nolock) where id_comp = "+this.comp.getId_comp();
				res = this.conect.getConnection().createStatement().executeQuery(query);

				res.next();

				String histInsert = "("+id_comp+",'"+res.getString("horas")+"','"+this.comp.getContador().getUltimaData()+"')";
				//Faz a diferença entre a data atual e a ultima data do sistema para verificar 
				//quantos dias passaram até o dia anterior do atual
				long daysDiff =  LocalDate.now().toEpochDay() - this.comp.getContador().getUltimaData().toEpochDay() - 1;
				
				if(daysDiff >= 1){ //Se passou mais de um dia será inserido na tabela de Historico_horas
						   //os registros com as datas dos dias passados com o acumulado de 00:00:00
					for(long i = 1; i <= daysDiff; i++){
						histInsert += ", ("+id_comp+",'00:00:00','"+this.comp.getContador().getUltimaData().plusDays(i)+"')";
					}
				}
				
				query = "insert into Historico_horas(id_comp, horas, data_hist) values "+histInsert;
				this.conect.getConnection().createStatement().execute(query);

				query = "update Quant_horas set horas = '00:00:00' where id_comp = "+id_comp;
				this.conect.getConnection().createStatement().execute(query);

				this.comp.getContador().setUltimaData(LocalDate.now());
			}else{ //Caso não tenha passa um dia o acumulado na tabela de Quant_horas é pego e
			       //colocado no tempo acumulado do contador do computador
				query = "select horas from Quant_horas (nolock) where id_comp = "+id_comp;
				res = this.conect.getConnection().createStatement().executeQuery(query);

				res.next();
				this.comp.getContador().setTempoAcumulado(Cronometro.parseLocalTimeToLong(LocalTime.parse(res.getString("horas"))));
			}
			
			query = "update Quant_horas set estado = 'L' where id_comp = "+id_comp;
			this.conect.getConnection().createStatement().execute(query);
			
			this.started = true;
		}
		catch(SQLException e){
			e.printStackTrace();
			this.writeErrorLog("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
			System.exit(1);
		}
	}
	
	//Marca o tempo passado desde a ultima marcação assim atualizando o tempo acumulado no banco de dados
	public void markTime(boolean isStopping){
		if(this.started){
			this.checkConnection();
			
			try{
				int id_comp = this.comp.getId_comp();

				if(this.checkDayPassed()) //Caso se um dia passou, atualiza o Historico_horas com a data e
							  //o tempo acumulado na tabela Quant_horas
					this.dayPassMark(isStopping);

				this.comp.getContador().contar();
				
				//Caso se o coletor estiver parando a tabela Quant_horas será atualizada com o acumulado
				//atual e mudará o estado de ligado (L) para desligado (D)
				String query = "update Quant_horas set horas = '"+this.comp.getContador().getTempoAcumuladoFormat()+"', estado = '"+((!isStopping)? "L" : "D")+"' where id_comp = "+id_comp;
				this.conect.getConnection().createStatement().execute(query);
			}
			catch(SQLException e){
				e.printStackTrace();
				this.writeErrorLog("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
				if(!isStopping)//Caso o coletor estiver parando um erro não fará o coletor sair diretamente
					System.exit(1);
			}	
		}
	}

	public boolean checkDayPassed(){
		return this.comp.getContador().getUltimaData().isBefore(LocalDate.now()); 
	}

	public void dayPassMark(boolean isStopping){
		if(this.started){
			int id_comp = this.comp.getId_comp();

			try{
				//Faz o contador do atributo comp contar o tempo que foi passado até às 23:59:59.999 
				this.comp.getContador().contar(LocalDateTime.of(LocalDate.now().minusDays(1),
							       LocalTime.of(23,59,59,(999*1000000)))
						              .toEpochSecond(ZoneOffset.from(OffsetTime.now())) * 1000);	
				//Insere um registro na tabela Historico_horas com o tempo acumulado no contador o seu
				//respectivo dia
				String query = "insert into Historico_horas(id_comp, horas, data_hist) values "
				      	      +"("+id_comp+",'"+this.comp.getContador().getTempoAcumuladoFormat()
				      	      +"','"+LocalDate.now().minusDays(1)+"')";

				this.conect.getConnection().createStatement().execute(query);
		
				this.comp.getContador().zerarAcumulado();
				this.comp.getContador().setUltimaData(LocalDate.now());
			}
			catch(SQLException e){
				e.printStackTrace();
				this.writeErrorLog("Erro ao fazer uma operação no banco de dados! Mensagem de erro: "+e.getMessage());
				if(!isStopping)//Caso se o coletor estiver parando um erro não fará o coletor sair diretamente
					System.exit(1);
			}
		}
	}
	
	//Escreve a mensagem de erro passada no log de error 
	private void writeErrorLog(String message){
		try{
			BufferedWriter writer = new BufferedWriter( new FileWriter( this.ERROR_LOG, true ) );
			writer.append(LocalDateTime.now()+" - "+message+"\n");
			writer.close();
		}
		catch(IOException e){
			System.out.println("\nErro ao escrever no log the erros. Mensagem de erro: " + e.getMessage());
		}	
	} 
	
	//Faz o coletor esperar o tempo configurado na configuração refresh_time
	public void timeOut(){
		try{
			//Caso se o tempo configurado for maior que 10 minutos, o tempo será fracionado de 10 em 10 minutos
			//se não o tempo configurado será utilizado diretamente
			if(this.refreshTime >= 600){
				byte iterations;
				if(Math.floor(this.refreshTime / 600.0) < this.refreshTime / 600.0 )
					iterations = (byte)(Math.floor(this.refreshTime / 600.0) + 1);
				else
					iterations = (byte)(this.refreshTime / 600.0);
			
				long temp;
				for(byte i = 1; i <= iterations; i++){
					temp = (refreshTime - ((i - 1) * 600) <= 600)? 600 
										     : this.refreshTime - ((i-1) * 600);
					Thread.sleep(temp * 1000);				
				}
			}
			else
				Thread.sleep(this.refreshTime * 1000);
		}
		catch(InterruptedException e){
			this.writeErrorLog("O refresh_time indicado nas configurações é muito longo!");
			System.exit(1);
		}
	}
}
