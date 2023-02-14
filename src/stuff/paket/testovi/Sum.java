package stuff.paket.testovi;

import stuff.paket.Linda;
import stuff.paket.ToupleSpace;

public class Sum {

	public static void main(String[] args) {
		Linda linda= ToupleSpace.getLinda();
		double min=Integer.parseInt(args[0]);
		double max=Integer.parseInt(args[1]);
		
		String[] parametri = { "bagParameters", "" + min, "" + max};
		linda.out(parametri);
		Object[] c= {};
		Object[] a= {};
		linda.eval("stuff.paket.testovi.Sabirac", a, "run", c);
		
		String[] relzutat= {"relzutat",null};
		linda.in(relzutat);
		System.out.println("Sum("+args[0]+","+args[1]+") = "+relzutat[1]);

	}

}
