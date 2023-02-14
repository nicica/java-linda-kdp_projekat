package stuff.paket;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;


import stuff.paket.gui.*;

public class CentralServer extends UnicastRemoteObject implements LindaRemoteServer{
	
	public final int port=4001;
	private ServerApp sa;
	public boolean active=false;
	public static int numW=0;
    public static int numTW=0;
    public static int numTE=0;
    
    private final int x=1500;
	//private final String host="localhost";
    

    private LindaImpl tupleSpace;
    private ArrayList<LindaRemoteWorkstation> workstations;
    private ArrayList<Client> users;
    private ArrayList<UUID> clientsID;

    private HashMap<UUID,Boolean> busyWorkers; 
    private ArrayList<UUID> stationsID;
    private ArrayList<String[]> tasks=new ArrayList<>();
    private ReentrantLock lock= new ReentrantLock(true);
    
    private HashMap<String, ArrayList<String>> lostJobs;
    private HashMap<String, ArrayList<String>> jobHistoryResults;
    private HashMap<String, String> jobHistory;
    private HashMap<UUID,String[]> lastTask;
    
    private ArrayList<byte []> taskLib;
    private ArrayList<byte []> taskJob;
    
    //private Registry registry;
    
    
    private Thread lt= new Thread();
    private Semaphore sem= new Semaphore(0);
    
	public CentralServer(ServerApp sa) throws RemoteException, MalformedURLException, NotBoundException {
		tupleSpace= new LindaImpl();
		this.sa=sa;	
		/*registry=LocateRegistry.getRegistry("localhost", 4028);
		tupleSpace= (LindaImpl) registry.lookup("/tuple");*/

		workstations=new ArrayList<>();
		clientsID= new ArrayList<>();
		stationsID= new ArrayList<>();
		busyWorkers= new HashMap<>();
		lastTask= new HashMap<>();
		
		
		new Thread(()->{
			while(true) {
				try {
					Thread.sleep(x);
					if(!active) {
						sem.acquire();
						int tsks=0;
						for(String[] t:tasks)
							tsks++;
						numTW=tsks;
						int wrkrs=0;
						int bizi=0;
						for(int i=0;i<workstations.size();i++)
						{
							try {
							if(!workstations.get(i).isBusy())
								wrkrs++;
							else
								bizi++;
							}
							catch (Exception e) {
								try {
									if(busyWorkers.get(stationsID.get(i)))
									{
										incTE(-1);
										alertUser(lastTask.get(stationsID.get(i))[5], "Job ID: "+lastTask.get(stationsID.get(i))[6]
										+" nije uspeo do kraja da se izvrsi,"
												+ " da li zelite da probate ponovo?",lastTask.get(stationsID.get(i)));
									}
									log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Radnik ID:"+
											stationsID.get(i)+" se odjavio");
									workstations.remove(i);
									busyWorkers.remove(stationsID.get(i));
									lastTask.remove(stationsID.get(i));
									stationsID.remove(i);
									numW=workstations.size();
									sa.updateStatus();
								} catch (RemoteException e1) {
									//e1.printStackTrace();
								}
							}
						}
						numW=wrkrs;
						numTE=bizi;
						sa.updateStatus();
					}
					if(workstations.size()>0) {
						
					for(int i=0;i<workstations.size();i++)
					{
						try {
						if(!workstations.get(i).isBusy())
							workstations.get(i).log("Server Ping");
						}
						catch (Exception e) {
							try {
								if(busyWorkers.get(stationsID.get(i))) {
									incTE(-1);
									alertUser(lastTask.get(stationsID.get(i))[5], "Job ID: "+lastTask.get(stationsID.get(i))[6]
									+" nije uspeo do kraja da se izvrsi,"
											+ " da li zelite da probate ponovo?",lastTask.get(stationsID.get(i)));
								}
								log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Radnik ID:"+
										stationsID.get(i)+" se odjavio");
								workstations.remove(i);
								busyWorkers.remove(stationsID.get(i));
								lastTask.remove(stationsID.get(i));
								stationsID.remove(i);
								numW=workstations.size();
								sa.updateStatus();
							} catch (RemoteException e1) {
								//e1.printStackTrace();
							}
						}
						}
					
					}
				
				} catch (InterruptedException e) {
				}
				
			}
		}).start();
		
	}

	@Override
	public synchronized void out(String[] tuple) throws RemoteException {
		tupleSpace.out(tuple);
	}

	@Override
	public void in(String[] tuple) throws RemoteException {
		tupleSpace.in(tuple);
		incTW(1);
		sa.guiLog(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+
				" Primljen zadatak ID:"+tuple[6]+"."+tuple[5]+"--Status: Ready");
		updateJobHistory(tuple[6]+"."+tuple[5], "Ready");
		tasks.add(tuple);
		if(numW>0)
			startCheck();
		else
			findUserAndNotify(tuple[5], "Trenutno nema slobodnih radnika, molimo sacekajte");
	}
	
	@Override
	public void findUserAndNotify(String s,String msg) throws RemoteException{
		
		for(int i=0;i< users.size();i++) {
			try {
			if(s.equals(users.get(i).getUUID().toString()))
			{
				users.get(i).log(msg);
				return;
			}
		}
			catch(RemoteException e) {
				users.remove(i);
				clientsID.remove(i);
			}
		}
		
		if(lostJobs==null)
			lostJobs=new HashMap<>();
		if(lostJobs.get(s) == null)
			lostJobs.put(s, new ArrayList<>());
		lostJobs.get(s).add(msg);
	}
	

	
	@Override
	public boolean inp(String[] tuple) throws RemoteException {
		return tupleSpace.inp(tuple);
	}

	@Override
	public void rd(String[] tuple) throws RemoteException {
	   tupleSpace.rd(tuple);
		
	}

	@Override
	public boolean rdp(String[] tuple) throws RemoteException {
		// TODO Auto-generated method stub
		return tupleSpace.rdp(tuple);
	}

	@Override
	public void eval(String name, Runnable thread) throws RemoteException {
		tupleSpace.eval(name, thread);
		
	}

	@Override
	public void eval(String className, Object[] construct, String methodName, Object[] arguments)
			throws RemoteException {
		tupleSpace.eval(className, construct, methodName, arguments);
		
	}
	@Override
	public void addTaskData(byte[] lib, byte[] job) throws RemoteException {
		if(taskLib==null)
			taskLib=new ArrayList<>();
		if(taskJob== null)
			taskJob= new ArrayList<>();
		taskLib.add(lib);
		taskJob.add(job);
		
	}

	@Override
	public void log(String s) throws RemoteException {
		sa.guiLog(s);
		
	}
	@Override
	public boolean isActive() throws RemoteException {
		// TODO Auto-generated method stub
		return active;
	}
	@Override
	public void incW(int i) throws RemoteException {
		numW+=i;
		if(active)
			sa.updateStatus();
	}
	@Override
	public void incTW(int i) throws RemoteException {
		numTW+=i;
		sa.updateStatus();
		
	}
	@Override
	public void newWorkstation(LindaRemoteWorkstation w) throws RemoteException {
	//	UnicastRemoteObject.exportObject(w,0);
		if(workstations==null)
		workstations=new ArrayList<>();
		
		
		workstations.add(w);
		if(stationsID==null)
			stationsID=new ArrayList<>();
		stationsID.add(w.getUUID());
		if(busyWorkers==null)
			busyWorkers= new HashMap<>();
		busyWorkers.putIfAbsent(w.getUUID(), false);
	}
	@Override
	public int getNumW() throws RemoteException {
		return numW;
	}

	@Override
	public int getNumTW() throws RemoteException{
		return numTW;
	}
	
	@Override
	public int getNumTE( ) throws RemoteException {
		return numTE;
	}
	@Override
	public synchronized void startCheck() throws RemoteException {
		
		lt= new Thread(()->{
			String[] task;
			byte[] lib,job;
			lock.lock(); {
		    	for(LindaRemoteWorkstation wr:workstations) {
		    		try {
		    			if(!wr.isBusy()) {
		    				log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+
		    						" Zadatak ID:"+tasks.get(0)[6]+"."+tasks.get(0)[5]+"--Status: Scheduled");
		    				updateJobHistory(tasks.get(0)[6]+"."+tasks.get(0)[5], "Scheduled");
		    				wr.setBusy();
		    				tupleSpace.out(tasks.get(0));
		    				task=tasks.get(0);
		    				lib=taskLib.get(0);
		    				job=taskJob.get(0);
		    				tasks.remove(tasks.get(0));
		    				taskLib.remove(taskLib.get(0));
		    				taskJob.remove(taskJob.get(0));
		    				
		    				incTW(-1);
		    				incW(-1);
		    				busyWorkers.put(wr.getUUID(), true);
		    				lastTask.put(wr.getUUID(), task);
		    				wr.execute(task,lib,job);
		    				break;
		    				}
		    			} catch (RemoteException e) {
		    				// TODO Auto-generated catch block
		    				System.out.println(e.getCause());
		    			}
		    		}
		    			
		    	}
			lock.unlock();
	    	});
		lt.start();
	}
	@Override
	public void incTE(int i) throws RemoteException {
		numTE+=i;
		sa.updateStatus();
	}
	@Override
	public void newUser(Client up) throws RemoteException {
		if(users==null)
			users= new ArrayList<>();
		users.add(up);
		if(clientsID==null)
			clientsID= new ArrayList<>();
		clientsID.add(up.getUUID());
		
	}

	@Override
	public void removeUser(Client c) throws RemoteException {
		users.remove(c);
		clientsID.remove(c.getUUID());
		
	}

	@Override
	public void removeWrokstation(LindaRemoteWorkstation w) throws RemoteException {
		workstations.remove(w);
		stationsID.remove(w.getUUID());
		busyWorkers.remove(w.getUUID());
		lastTask.remove(w.getUUID());
	}

	@Override
	public void onClose() throws RemoteException {
		if(workstations!=null)
		{
			
			for(LindaRemoteWorkstation wr:workstations) {
			wr.callClose();
		}
		}
		if(users!=null) {
			if(users.size()>0) {
			for(String[] task:tasks) {
				findUserAndNotify(task[5],"Zadatak ID: "+task[6]+" Nije izvrsen");
			}
			for(Client c:users) {
				c.serverTurnsOff();
			}}
		}
		
	}

	@Override
	public LindaRemoteWorkstation getFirst(UUID id) throws RemoteException {
		if(workstations!=null)
		{
			for(LindaRemoteWorkstation wr:workstations) {
				if(wr.isBusy() && lastTask.get(wr.getUUID())[5].equals(id.toString()))
					return wr;
			}
		}
		
		return null;
	}
	@Override
	public void abortFirstTask(UUID ID) throws RemoteException {
		boolean vrati=true;
		int i;
		for(i=0;i<tasks.size();i++) {
			if(tasks.get(i)[5].equals(ID.toString())) {
				vrati=false;
				break;}
		}
		if(vrati)
			return;
		log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+
				" Zadatak ID:"+tasks.get(i)[6]+"."+tasks.get(i)[5]+"--Status: Aborted");
		updateJobHistory(tasks.get(i)[6]+"."+tasks.get(i)[5], "Aborted");
		ArrayList<String> a=new ArrayList<>();
		a.add("Did not finish");
		jobResults(tasks.get(i)[6]+"."+tasks.get(i)[5], a);
		findUserAndNotify(tasks.get(i)[5],"Zadatak ID: "+tasks.get(i)[6]+" Nije izvrsen");
		tasks.remove(i);
		incTW(-1);
		
	}
	@Override
	public void sendLostResults(Client c) throws RemoteException {
		if(lostJobs==null)
		{
			c.log("Nema zaostalih rezultata");
			return;
		}
		ArrayList<String> entry=lostJobs.get(c.getUUID().toString());
		if(entry==null)
			c.log("Nema zaostalih rezultata");
		else
		{
			for(String s:entry)
				c.log(s);
			lostJobs.remove(c.getUUID().toString());
		}
		
	}
	@Override
	public void updateJobHistory(String key, String status) throws RemoteException {
		if(jobHistory==null)
			jobHistory= new HashMap<>();
		jobHistory.put(key, status);
		
	}
	@Override
	public String getTaskInfo(String key) throws RemoteException {
		if(jobHistory==null)
		{
			return null;
		}
		return jobHistory.get(key);
		
	}
	@Override
	public void jobResults(String key,ArrayList<String> value) throws RemoteException {
		if(jobHistoryResults==null)
			jobHistoryResults=new HashMap<>();
		jobHistoryResults.put(key, value);
		
	}
	@Override
	public String getJobResults(String key) throws RemoteException {
		if(jobHistoryResults==null)
			return null;
		if(jobHistoryResults.get(key)==null)
			return null;
		StringBuilder sb= new StringBuilder();
		for(String line:jobHistoryResults.get(key))
		{
			sb.append("\n");
			sb.append(line);
		}
		return sb.toString();
	}
	@Override
	public void alertUser(String s, String msg,String[] tuple) throws RemoteException {
		for(Client c: users) {
			if(s.equals(c.getUUID().toString()))
			{
				c.popupmsg(msg,tuple);
				return;
			}
		}
		log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)
				+ " Zadatak ID:"+tuple[6]+"."+tuple[5]+"--Status: Failed");
		updateJobHistory(tuple[6]+"."+tuple[5], "Failed");
		ArrayList<String> a=new ArrayList<>();
		a.add("Job ID: "+tuple[6]+" finnished with result:");
		a.add("Did not finish");
		if(lostJobs==null)
			lostJobs=new HashMap<>();
		lostJobs.put(s, a);
		a.remove(0);
		jobResults(tuple[6]+"."+tuple[5], a);
		
	}
	public void setActive() throws RemoteException {
		active=true;
		sem.release();
		for(LindaRemoteWorkstation wr:workstations)
		{
			if(wr.asleep())
				wr.wakeUp();
		}
	}
	@Override
	public void updateBusy(LindaRemoteWorkstation wr) throws RemoteException {
		busyWorkers.put(wr.getUUID(), false);
	}
	
}
