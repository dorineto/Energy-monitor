import java.time.LocalDate;

class Computador{
	private int id_comp;
	private Cronometro contador;

	public Computador(int id_comp, LocalDate ultimaData){
		this.id_comp = id_comp;
		this.contador = new Cronometro(System.currentTimeMillis(), ultimaData); 
	}

	public int getId_comp(){ return this.id_comp; }

	public Cronometro getContador(){ return this.contador; }
}
