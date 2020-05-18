class Computador{
	private int id_comp, id_tipo;
	private String nome;
	private double consumo_max, consumo_medio;
	
	/* Construtores */
	public Computador(int id_tipo, String nome){
		this.id_comp = 0;
		this.id_tipo = id_tipo;
		this.nome = nome;
		this.consumo_max = -1;
		this.consumo_medio = -1;
	}
	
	public Computador(int id_tipo, String nome, double[] consumos){
		this.id_comp = 0;
		this.id_tipo = id_tipo;
		this.nome = nome;
		this.setConsumos(consumos);
	}

	public Computador(int id_tipo, String nome, int id_comp){
		this.id_comp = id_comp;
		this.id_tipo = id_tipo;
		this.nome = nome;
		this.consumo_max = -1;
		this.consumo_medio = -1;
	}

	public Computador(int id_tipo, String nome, int id_comp, double[] consumos){
		this.id_comp = id_comp;
		this.id_tipo = id_tipo;
		this.nome = nome;
		this.setConsumos(consumos);
	}

	/* Métodos */ 

	//Calcula o consumo max e medio apartir dos valores passados pelo vetor consumos
	public void setConsumos(double[] consumos){
		double somat_consumo = 0;

		for(double consumo : consumos){
			somat_consumo += consumo;
		}

		this.consumo_max = somat_consumo;
		this.consumo_medio = somat_consumo * .5; 
	}
	
	/* Getters e Setters */

	public double getConsumo_max(){ return this.consumo_max; }

	public double getConsumo_medio(){ return this.consumo_medio; }

	public int getId_comp(){ return this.id_comp; }

	public void setId_comp(int id_comp){ this.id_comp = id_comp; }

	public int getId_tipo(){ return this.id_tipo; }

	public void setId_tipo(int id_tipo){ this.id_tipo = id_tipo; }

	public String getNome(){ return this.nome; }

	public void setNome(String nome){ this.nome = nome; }

	/* Métodos sobrescrevidos da classe Object */

	@Override
	public String toString(){
		return "\nNome: "+this.nome+"\n"
		      +"Id_tipo: "+this.id_tipo+"\n"
		      +"Consumo maximo: "+(this.consumo_max == -1 ? "não definido" : String.format("%.2f", this.consumo_max))+"\n"
		      +"Consumo médio: "+(this.consumo_medio == -1 ? "não definido" : String.format("%.2f",  this.consumo_medio))+"\n";
	}	
}
