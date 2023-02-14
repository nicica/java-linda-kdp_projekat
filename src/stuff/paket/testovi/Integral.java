package stuff.paket.testovi;
import stuff.paket.*;

public class Integral {
	public static void main(String[] args) {
		try {

			// String className = args[0];
			// Linda linda = (Linda) Class.forName(className).newInstance();
			// ToupleSpace.setLinda(linda);

			Linda linda = ToupleSpace.getLinda();
			int num = 10;
			int n = 100;
			double xmin = Integer.parseInt(args[0]);
			double xmax = Integer.parseInt(args[1]);

			String[] bagParameters = { "bagParameters", "" + xmin, "" + xmax,
					"" + n };
			linda.out(bagParameters);
			String[] numNode = { "numNode", "" + num };
			linda.out(numNode);
			Object[] construct = {};
			Object[] arguments = {};
			linda.eval("stuff.paket.testovi.Bag", construct, "run", arguments);

			String[] collectorParameters = { "collectorParameters", "" + n };
			linda.out(collectorParameters);
			linda.eval("stuff.paket.testovi.Collector", construct, "run",
					arguments);

			String[] workerParameters = { "workerParameters", "0" };
			linda.out(workerParameters);
			for (int i = 0; i < num; i++) {
				linda.eval("stuff.paket.testovi.Worker", construct, "run",
						arguments);
			}
			String[] result = { "result", null };
			linda.in(result);
			double integral = Double.parseDouble(result[1]);
			System.out.println("[" + xmin + ", " + xmax + ", " + n + "] = "
					+ integral);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
