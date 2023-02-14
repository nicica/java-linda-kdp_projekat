package stuff.paket;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import stuff.paket.gui.UserApp;
import stuff.paket.gui.WorkstationApp;

public class UserProgram implements Linda,Client {

	private String host;
	private int port;
	private UserApp ua=null;
	private Client client;
	
    private final UUID ID = UUID.randomUUID();

    private boolean provera=false;

    private Registry registry;
	public static int idZad=0;
    private LindaRemoteServer linda;
    
   // private LindaImpl tupleSpace;
    
    public UserProgram(String host, int port, UserApp ua) throws RemoteException, NotBoundException {
    	this.host = host;
		this.port = port;
		this.ua = ua;
		client=this;
		UnicastRemoteObject.exportObject(client,0);
		registry=LocateRegistry.getRegistry(host, port);
		linda= (LindaRemoteServer) registry.lookup("/Server");
		if(!linda.isActive())
    		throw new RemoteException();
    	linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Registrovan klijent ID:"+ID);
    	linda.newUser(client);
    	provera();
	}
    
    	
    
    private void provera() {
		if(!provera) {
		new Thread(()->{
			provera=true;
			while(true) {
			try {
				Thread.sleep(2000);
				if(linda==null)
					break;
				if(getServer()==null)
				{
					serverTurnsOff();
					break;
				}
			} catch (RemoteException e) {

				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			provera=false;
		}).start();}
	}
    
	@Override
	public synchronized void out(String[] tuple) {
		try {
			linda.out(tuple);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void in(String[] tuple) {
		try {
			linda.in(tuple);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean inp(String[] tuple) {
		// TODO Auto-generated method stub
		try {
			return linda.inp(tuple);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void rd(String[] tuple) {
		try {
			linda.rd(tuple);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean rdp(String[] tuple) {
		// TODO Auto-generated method stub
		try {
			return linda.rdp(tuple);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void eval(String name, Runnable thread) {
		try {
			linda.eval(name, thread);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void eval(String className, Object[] construct, String methodName, Object[] arguments) {
		try {
			linda.eval(className, construct, methodName, arguments);
		} catch (RemoteException e) {
	// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public String getID() {
		return ID.toString();
	}
	public void send(String[] tuple,byte [] lib,byte[] job) throws RemoteException{
		linda.addTaskData(lib, job);
		linda.out(tuple);
		linda.in(tuple);
	}
	@Override
	public void log(String s) throws RemoteException{
		if(ua!=null)
			ua.guiLogU(s);
		else
			System.out.println(s);
	}
	@Override
	public LindaRemoteServer getServer() throws RemoteException {
		try {
			linda.isActive();
			return linda;
			}
			catch(Exception e) {
				return null;
			}
	}
	@Override
	public void dissconect() throws RemoteException {
		linda.removeUser(this);
		if(linda.isActive())
			linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Klijent ID:"+ID+" se odjavio");
		linda=null;
		
	}
	@Override
	public UUID getUUID() throws RemoteException {
		// TODO Auto-generated method stub
		return ID;
	}
	@Override
	public void reconnect() throws RemoteException, NotBoundException {
		client=this;
		//UnicastRemoteObject.exportObject(client,0);
		registry=LocateRegistry.getRegistry(host, port);
		linda= (LindaRemoteServer) registry.lookup("/Server");
		if(!linda.isActive())
    		throw new RemoteException();
    	linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Registrovan klijent ID:"+ID);
    	linda.newUser(client);
    	provera();
	}




	@Override
	public void serverTurnsOff() throws RemoteException {
		ua.reset();
		linda=null;
		ua.guiLogU("Veza prekinuta");
	}
	@Override
	public void popupmsg(String msg,String[] tuple) throws RemoteException {
		ua.popup(msg, tuple);
	}
	
}
