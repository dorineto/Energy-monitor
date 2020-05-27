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
	
	//Faz a contagem com o tempo atual
	public void contar(){
		long novoTempo = System.currentTimeMillis();
		this.tempoAcumulado += novoTempo - this.ultimoTempo;
		this.ultimoTempo = novoTempo;
	}
	
	//Faz a contagem com o tempo passado como parâmetro
	public void contar(long novoTempo){
		if(novoTempo > 0){
			this.tempoAcumulado += novoTempo - this.ultimoTempo;
			this.ultimoTempo = novoTempo;
		}
	} 	

	public String getTempoAcumuladoFormat(){
		return Cronometro.timeFormat(this.tempoAcumulado);
	}

	public void setTempoAcumulado(long tempoAcumulado) { this.tempoAcumulado = tempoAcumulado; }
	
	//Transforma o long em uma String com a representação de tempo no formato de hh:mm:ss.mmm
	public static String timeFormat(long tempMilisec){
		if(tempMilisec < 0)
			return "";

		int[] tempo = new int[] {((int)tempMilisec) ,0 ,0 ,0};
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

	//Transforma o LocalTime passado como parametro em milisegundos
	public static long parseLocalTimeToLong(LocalTime time){
		if(time == null)
			return 0;
		
		long[] timeArr = new long[] {time.getHour(), time.getMinute(), time.getSecond(), time.getNano() / 1000000};
		long[] timeDiv = new long[] {60, 60, 1000};
		 
		long timeLong = timeArr[0];
		for(byte i = 0; i < timeDiv.length; i++)
			timeLong = timeLong * timeDiv[i] + timeArr[i+1];
		
		return timeLong;	
	}

	public void zerarAcumulado(){
		this.tempoAcumulado = 0;
	}

	public LocalDate getUltimaData(){ return this.ultimaData; }

	public void setUltimaData(LocalDate ultimaData ){ this.ultimaData = ultimaData; }	
}
