package stuff.paket.testovi;

import stuff.paket.Linda;
import stuff.paket.ToupleSpace;

public class Multiplicator implements Runnable {

	Linda linda;
	double min;
	double max;
	
	public Multiplicator() { 
		linda=ToupleSpace.getLinda();
		String[] parametri = { "bagParameters", null,null };
		linda.in(parametri);
		min = Double.parseDouble(parametri[1]);
		max = Double.parseDouble(parametri[2]);
	}
	
	
	@Override
	public void run() {
		double sum=1.0;
		while(min<=max)
		{
			try {
				Thread.sleep((long) (Math.random()*100));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			sum*=(Math.cos(min)/(Math.pow(Math.E, min)));
			min++;
				
		}
		String[] relzutat = { "relzutat", "" + sum };
		linda.out(relzutat);
	}

}
