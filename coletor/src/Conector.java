import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

import java.io.IOException;

class Conector{
	private Configuracao config;
	private Connection conn;
	private String reason;
	private boolean failed;

	public Conector() throws IOException{
		this.failed = false;
		this.reason = "";

		this.config = new Configuracao();	
		
		this.conn = null;

		this.makeConnection();
	}
	
	//Tenta fazer a conexão com o banco de dados com as informações passadas no arquivo de configuração 
	private void makeConnection(){
		int trys = 3;
		while(trys != 0){
			try{
				if(this.conn != null)
					this.conn.close();
				
				this.conn = DriverManager.getConnection("jdbc:sqlserver://"+this.config.getConfig("banco_dados")+";"
							 		   +"databaseName=Energy_monitor;"
									   +"user="+this.config.getConfig("user")+";"
									   +"password="+this.config.getConfig("pwd"));
				break;
			}
			catch(SQLException e){
				trys -= 1;

				if(trys == 0){
					this.reason = e.getMessage();
					this.failed = true;
				}	
			}
		}
	}
	
	//Verifica se a conexão ainda é valida e está utilizavel
	//se não estiver tenta refazer a conexão
	public void remakeConnection(){
		try{
			if( this.conn == null || this.conn.isClosed() || (! this.conn.isValid( this.conn.getNetworkTimeout() ))){
				this.failed = false;
				this.makeConnection();
			}
		}
		catch(SQLException e){
			this.reason = e.getMessage();
			this.failed = true;
		}
	}

	public String getReason(){ return this.reason; }

	public Connection getConnection() { return this.conn; }

	public boolean getFailed() { return this.failed; }
}

