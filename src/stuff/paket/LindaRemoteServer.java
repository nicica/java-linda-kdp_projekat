package stuff.paket;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;



public interface LindaRemoteServer extends Remote {
	public void out(String[] tuple) throws RemoteException;
	public void in(String[] tuple) throws RemoteException;
	public boolean inp(String[] tuple) throws RemoteException;
	public void rd(String[] tuple) throws RemoteException;
	public boolean rdp(String[] tuple) throws RemoteException;
	public void eval(String name, Runnable thread) throws RemoteException;
	public void eval(String className, Object[] construct, String methodName, Object[] arguments) throws RemoteException;
	
	public void log(String s) throws RemoteException;
	public boolean isActive() throws RemoteException;
	public void incW(int i) throws RemoteException;
	public void incTW(int i) throws RemoteException;
	public void incTE(int i) throws RemoteException;
	
	public void newWorkstation(LindaRemoteWorkstation w) throws RemoteException;
	public void newUser(Client up) throws RemoteException;
	public void removeUser(Client c) throws RemoteException;
	public void removeWrokstation(LindaRemoteWorkstation w) throws RemoteException;
	public void findUserAndNotify(String s, String msg) throws RemoteException;
	public void onClose() throws RemoteException;
	public LindaRemoteWorkstation getFirst(UUID id) throws RemoteException;
	public void abortFirstTask(UUID ID) throws RemoteException;
	public void sendLostResults(Client c) throws RemoteException;
	public void updateJobHistory(String key,String status) throws RemoteException;
	public String getTaskInfo(String key) throws RemoteException;
	
	public int getNumW() throws RemoteException;
	public int getNumTW() throws RemoteException;
	public int getNumTE() throws RemoteException;
	public void startCheck() throws RemoteException;
	public void addTaskData(byte [] lib,byte [] job) throws RemoteException;
	public void jobResults(String key,ArrayList<String> value) throws RemoteException;
	public String getJobResults(String key) throws RemoteException;
	public void alertUser(String s, String msg,String[] tuple) throws RemoteException;
	
	public void updateBusy(LindaRemoteWorkstation wr) throws RemoteException;
}
