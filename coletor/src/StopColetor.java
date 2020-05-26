class StopColetor implements Runnable{
	private Coletor colet;

	public StopColetor(Coletor colet){
		this.colet = colet;
	}

	public void run(){
		colet.markTime(true);
	}
}
