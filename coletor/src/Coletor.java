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

class Coletor{
	private Conector conect;
	private Configuracao config;
	private Computador comp;
	private final File ERROR_LOG;
	private boolean started;

	public Coletor(){
		this.started = false;
		this.ERROR_LOG = new File("./config/error.log");

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
				int id_comp = Integer.parseInt(this.config.getConfig("id_comp"));
				LocalDate data = null;

				String query = "select top 1 data_hist from Historico_horas (nolock) "
					      +"where id_comp = "+id_comp+" "
					      +"order by data_hist desc";
		
				ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);

				if(res.next()){
					data = LocalDate.parse(res.getString("data_hist"));	
				}else{

					data = LocalDate.now();

					query = "insert into Historico_horas(id_comp, horas, data_hist) values "
					       +"("+id_comp+", '00:00:00', '"+data.toString()+"')";

					this.conect.getConnection().createStatement().execute(query);	
				}	


				this.comp = new Computador(id_comp, data);
			}
			catch(NumberFormatException e){
				this.writeErrorLog("O id_comp não é um valor numerico válido!");
				System.exit(1);
			}
			catch(SQLException e){
				e.printStackTrace();
				this.writeErrorLog("Erro ao fazer uma query no banco de dados! Error message: "+e.getMessage());
				System.exit(1);
			}
		}
		else{
			this.writeErrorLog("Erro ao tentar carregar as configurações!");
			System.exit(1);
		}

	} 

	public static void main(String[] args){
		//Runtime.getRuntime().addShutdownHook()
		
		Coletor colet = new Coletor();
		
		colet.start();	
		/*while(true){
		}*/
	}
	
	private void checkConnection(){
		int trys = 3;
		while(trys != 0 && this.conect.getFailed()){
			this.conect.remakeConnection();
			trys -= 1;
		}

		if(this.conect.getFailed()){
			this.writeErrorLog("Erro ao tentar refazer a conexão com o banco de dados");
			System.exit(1);
		}

	} 

	public void start(){
		try{
			this.checkConnection();
		
			int id_comp = this.comp.getId_comp();
			
			String query = "Select * from Computadores (nolock) where id_comp = "+id_comp;
			ResultSet res = this.conect.getConnection().createStatement().executeQuery(query);
			if(! res.next()){
				this.writeErrorLog("O id_comp no arquivo config.conf é inválido. Por favor verifique.");
				System.exit(1);
			}
			

			if(this.checkDayPassed()){
				query = "select horas from Quant_horas (nolock) where id_comp = "+this.comp.getId_comp();
				res = this.conect.getConnection().createStatement().executeQuery(query);

				res.next();

				String histInsert = "("+id_comp+",'"+res.getString("horas")+"','"+this.comp.getContador().getUltimaData()+"')";

				long daysDiff =  LocalDate.now().toEpochDay() - this.comp.getContador().getUltimaData().toEpochDay() - 1;

				if(daysDiff >= 1){
					for(long i = 1; i <= daysDiff; i++){
						histInsert += ", ("+id_comp+",'00:00:00','"+this.comp.getContador().getUltimaData().plusDays(i)+"')";
					}
				}
				
				query = "insert into Historico_horas(id_comp, horas, data_hist) values "+histInsert;
				
				System.out.println(query);

				this.conect.getConnection().createStatement().execute(query);

				query = "update Quant_horas set horas = '00:00:00' where id_comp = "+id_comp;
				this.conect.getConnection().createStatement().execute(query);
			}

			query = "update Quant_horas set estado = 'L' where id_comp = "+id_comp;
			this.conect.getConnection().createStatement().execute(query);
		}
		catch(SQLException e){
			e.printStackTrace();
			this.writeErrorLog("Error ao fazer uma operação no banco de dados! Error message: "+e.getMessage());
			System.exit(1);
		}
	}

	public boolean checkDayPassed(){
		return this.comp.getContador().getUltimaData().isBefore(LocalDate.now());
	}

	private void writeErrorLog(String message){
		try{
			BufferedWriter writer = new BufferedWriter( new FileWriter( this.ERROR_LOG, true ) );
			writer.append(LocalDateTime.now()+" - "+message+"\n");
			writer.close();
		}
		catch(IOException e){
			System.out.println("\nErro ao escrever no log the erros. Error massage: " + e.getMessage());
		}	
	} 
		
}


