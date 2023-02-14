package stuff.paket;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface Client extends Remote,Serializable {

	public LindaRemoteServer getServer() throws RemoteException;
	public void log(String s) throws RemoteException;
	public void dissconect() throws RemoteException;
	public UUID getUUID() throws RemoteException;
	public void serverTurnsOff() throws RemoteException;
	public void reconnect() throws RemoteException,NotBoundException;
	public void popupmsg(String msg,String[] tuple) throws RemoteException;
}
