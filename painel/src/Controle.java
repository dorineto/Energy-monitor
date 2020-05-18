import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;

class Controle{
	private Conector conect;

	public Controle(){
		int trys = 3;
		while(trys != 0){
			try{
				this.conect = new Conector();
				break;
			}
			catch(IOException e){
				trys -= 1;
			}
		}

		if(trys == 0)
			throw new ConnectionException("Erro ao carregar o arquivo de configuração! Tente novamente mais tarde.");
	}

	

}
