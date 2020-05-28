import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Map;

class Configuracao{
	private Hashtable<String, String> config; 
	private final File CONFIG_FILE; 

	public Configuracao() throws IOException{
		this.config = new Hashtable<String, String>();
		this.CONFIG_FILE = new File("./config/config.conf");
		
		if(!this.CONFIG_FILE.isFile()){
			this.CONFIG_FILE.createNewFile();
		}

		this.readConfigFile();
	}
	
	//Pega as informações do arquivo de configuração e coloca em um dicionario
	private void readConfigFile() throws IOException{
		BufferedReader read = new BufferedReader( new FileReader(this.CONFIG_FILE) );
		String line = read.readLine();

		while(true){
			if(line == null)
				break;
			
			String[] entry = line.split(":");
			this.config.put(entry[0].trim(), entry[1].trim());
			
			line = read.readLine();
		}
		
		read.close();
	}

	public String getConfig(String key){
		return this.config.getOrDefault(key, ""); 
	}
}
