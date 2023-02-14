package stuff.paket;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;


public interface LindaRemoteWorkstation extends Remote,Serializable {
	public void out(String[] tuple) throws RemoteException;
	public void in(String[] tuple) throws RemoteException;
	public boolean inp(String[] tuple) throws RemoteException;
	public void rd(String[] tuple) throws RemoteException;
	public boolean rdp(String[] tuple) throws RemoteException;
	public void eval(String name, Runnable thread) throws RemoteException;
	public void eval(String className, Object[] construct, String methodName, Object[] arguments) throws RemoteException;
	public void interruptProcess() throws RemoteException;
	public void callClose() throws RemoteException;
	
	
	public LindaRemoteServer serverActive() throws RemoteException;
	public UUID getUUID() throws RemoteException;
	public boolean isBusy() throws RemoteException;
	public void setBusy() throws RemoteException;
	public void execute(String[] tuple,byte [] lib,byte[] job) throws RemoteException;
	public void log(String s) throws RemoteException;
	public boolean asleep() throws RemoteException;
	public void wakeUp() throws RemoteException;
	public void cekaj() throws RemoteException;
	/*povezati se na server-> registrovati workstation node na njemu
	*primiti jar fajl od servera
	*izlazne tok i relzutat izvrsavanja salje na centralni server
	**/
}
