package stuff.paket;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import stuff.paket.UserProgram;

public class ToupleSpace  {
	static Linda linda;
	private CentralServer  server;
	private Workstation stanica;
	private UserProgram up;

	public static Linda getLinda() {
		if(linda==null)
			linda=new LindaImpl();
		return linda;
	}

	public static void setLinda(Linda l) {
		linda = l;
	}
	static {
		linda=new LindaImpl();
	}
	
	
	
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, RemoteException{
		String[] construct = {};
        if(args.length > 2) {
            construct = new String[args.length - 2];
            for (int i = 2; i < args.length; i++) {
                construct[i - 2] = args[i];
            }
        }
        String className = args[0];
        String methodName = args[1];
        Object [] initargs = {};
        Object [] arguments = {};
 //       System.out.println(methodName);
        if(methodName.equals("main")){
            arguments =new Object[]{construct};
        }
        Class threadClass;
		try {
			threadClass = Class.forName(className);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
}
