import java.time.LocalDate;
import java.time.LocalTime;

class Cronometro{
	private long ultimoTempo, tempoAcumulado;
        private LocalDate ultimaData;
	
	public Cronometro(long tempoInicial, LocalDate ultimaData){
		this.ultimoTempo = (tempoInicial < 0 ? System.currentTimeMillis() : tempoInicial);
		this.ultimaData = ultimaData;
		this.tempoAcumulado = 0;
	}

	public void contar(){
		long novoTempo = System.currentTimeMillis();
		this.tempoAcumulado += novoTempo - this.ultimoTempo; 
	}

	public void contar(long novoTempo){
		if(novoTempo > 0){
			this.tempoAcumulado += novoTempo - this.ultimoTempo;
			this.ultimoTempo = novoTempo;
		}
	} 	

	public long getTempoAcumulado() { return this.tempoAcumulado; }

	public String getTempoAcumuladoFormat(){
		int[] tempo = new int[] {((int)this.tempoAcumulado) ,0 ,0 ,0};
		int[] tempoDiv = new int[] {1000, 60, 60}; 

		for(byte i = 0; i < tempoDiv.length; i++){
			if(tempo[i] / tempoDiv[i] >= 1){
				tempo[i + 1] =(int) Math.floor(tempo[i] / tempoDiv[i]);
				tempo[i] = (int) tempo[i] % tempoDiv[i];
			}
			else{
				break;
			}
		}

		return LocalTime.of(tempo[3], tempo[2], tempo[1], tempo[0] * 1000000).toString();

	}

	public LocalDate getUltimaData(){ return this.ultimaData; }

	public void setUltimaData(LocalDate ultimaData ){ this.ultimaData = ultimaData; }	
}
