package stuff.paket;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LindaImpl implements Linda {
	
	/**
	 * 
	 */
//	private Registry registry;
	private static final long serialVersionUID = 1L;
	private static ArrayList<String[]> tupleSpace;
	public LindaImpl() {
			tupleSpace= new ArrayList<>();
			/*try {
				registry= LocateRegistry.createRegistry(4028);
				registry.rebind("/tuple", this);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getCause());
			}*/
		
	}
	
	
	
	public void eval(String name, Runnable thread) {
		Thread t = new Thread(thread, name);
		t.start();
	}

	public void eval(final String className, final Object[] initargs,
					 final String methodName, final Object[] arguments) {
		Thread t = new Thread(() -> {

			try {
				Class threadClass = Class.forName(className);
				Class[] parameterTypes = new Class[initargs.length];
				for (int i = 0; i < initargs.length; i++) {
					parameterTypes[i] = initargs[i].getClass();
				}
				Constructor[] constructors = threadClass.getConstructors();
				Constructor constructor = threadClass
						.getConstructor(parameterTypes);
				Object runningThread = constructor.newInstance(initargs);
				parameterTypes = new Class[arguments.length];
				for (int i = 0; i < arguments.length; i++) {
					parameterTypes[i] = arguments[i].getClass();
				}
				Method method = threadClass.getMethod(methodName,
						parameterTypes);
				method.invoke(runningThread, arguments);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		t.start();

	}

	public synchronized void in(String[] tuple) {
		
		boolean found = false;
		while (!found) {
			for (String[] data : tupleSpace) {
				if (equals(tuple, data)) {
					fill(tuple, data);
					tupleSpace.remove(data);
					return;
				}
			}
				
			try {	
				
				//System.out.println("Ne radi");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized boolean inp(String[] tuple) {
		for (String[] data : tupleSpace) {
			if (equals(tuple, data)) {
				fill(tuple, data);
				tupleSpace.remove(data);
				return true;
			}
		}
		return false;
	}

	public synchronized void out(String[] tuple) {			
		try {
			if(tupleSpace==null)
			tupleSpace=new ArrayList<>();
			if(tuple!=null)
			{tupleSpace.add(tuple);
			notifyAll();
			}
			//registry.rebind("tuple", this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public synchronized void rd(String[] tuple) {
		boolean found = false;
		while (!found) {
			for (String[] data : tupleSpace) {
				if (equals(tuple, data)) {
					fill(tuple, data);
					return;
				}
			}
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public synchronized boolean rdp(String[] tuple) {
		for (String[] data : tupleSpace) {
			if (equals(tuple, data)) {
				fill(tuple, data);
				return true;
			}
		}
		return false;
	}

	private boolean equals(String[] a, String[] b) {
	    if (a == null || b == null || a.length != b.length) {
	        return false;
	    }
	    for (int i = 0; i < a.length; i++) {
	    	if(a[i]==null)
	    		continue;
	        if (!a[i].equals(b[i])) {
	            return false;
	        }
	    }
	    return true;
	}

	private void fill(String a[], String b[]) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == null) {
				a[i] = new String(b[i]);
			}
		}
	}

}
	






