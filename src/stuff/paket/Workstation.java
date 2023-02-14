package stuff.paket;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
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
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import stuff.paket.gui.WorkstationApp;

public class Workstation implements LindaRemoteWorkstation,Client{
	
	private String host;
	private int port;
	private WorkstationApp wapp=null;
	private Thread lt;
	private LindaRemoteWorkstation client;
	
	private Semaphore sem= new Semaphore(0);
	private boolean busy=false;
	private boolean asleep=false;
	//private LindaImpl tupleSpace;
	
	private boolean provera=false;
	
    private UUID ID = UUID.randomUUID();

    private static Registry registry;
    private LindaRemoteServer linda;
    
    private ReentrantLock lock= new ReentrantLock(true);
    private String[] task= new String[9];
    
    
    
	
	public Workstation(String host, int port, WorkstationApp wa) throws RemoteException, NotBoundException {
		this.host = host;
		this.port = port;
		this.wapp=wa;
		client=this;
		//System.getProperty("java.rmi.server.hostname", host);
		UnicastRemoteObject.exportObject(client,0);
		registry=LocateRegistry.getRegistry(host, port);
		linda=(LindaRemoteServer) registry.lookup("/Server");
		/*try {
			linda= (LindaRemoteServer) Naming.lookup("rmi://"+host+":"+port+"/Server");
		} catch (Exception e) {
		} */
		if(!linda.isActive())
			throw new RemoteException();
		//linda.notify();
		linda.incW(1);
		linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Registrovan radnik ID: "+ID);

		
		linda.newWorkstation(client);
		
		if(linda.getNumTW()>0)
			linda.startCheck();
		
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
	public void out(String[] tuple) throws RemoteException {
		linda.out(tuple);
		
	}

	@Override
	public void in(String[] tuple) throws RemoteException {
		linda.in(tuple);
		
	}

	@Override
	public boolean inp(String[] tuple) throws RemoteException {
		// TODO Auto-generated method stub
		return linda.inp(tuple);
	}

	@Override
	public void rd(String[] tuple) throws RemoteException {
		// TODO Auto-generated method stub
		linda.rd(tuple);
	}

	@Override
	public boolean rdp(String[] tuple) throws RemoteException {
		// TODO Auto-generated method stub
		return linda.rdp(tuple);
	}

	@Override
	public void eval(String name, Runnable thread) throws RemoteException {
		linda.eval(name, thread);
		
	}

	@Override
	public synchronized void eval(String className, Object[] construct, String methodName, Object[] arguments)
			throws RemoteException {
		linda.eval(className, construct, methodName, arguments);
		
	}
	public void reconnect() throws RemoteException, NotBoundException
	{
		client=this;
		//UnicastRemoteObject.exportObject(client,0);
		registry=LocateRegistry.getRegistry(host, port);
		linda= (LindaRemoteServer) registry.lookup("/Server");
		if(!linda.isActive())
			throw new RemoteException();
		//linda.notify();
		linda.incW(1);
		linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Registrovan radnik ID: "+ID);
		linda.newWorkstation(client);
		if(linda.getNumTW()>0)
			linda.startCheck();
		provera();
	}
	@Override
	public void dissconect() throws RemoteException {
		if(task[0]!=null)
		{
			if(!asleep)
				lt.interrupt();
			if(linda.isActive())
				linda.alertUser(task[5], "Job ID: "+task[6]+" nije uspeo do kraja da se izvrsi,"
						+ " da li zelite da probate ponovo?",task);
			
		}
		

		if(linda.isActive()) {
			if(!busy)
				linda.incW(-1);
			else 
				linda.incTE(-1);
		}
			linda.removeWrokstation(this);
			if(linda.isActive())
			linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Radnik ID:"+ID+" se odjavio");
		
		linda=null;
		
	}
	
	
	@Override
	public LindaRemoteServer serverActive() throws RemoteException {
		return linda;
	}
	@Override
	public boolean isBusy() throws RemoteException {
		return busy;
	}
	@Override
	public void setBusy() throws RemoteException {
		busy=true;	
	}
	
	public synchronized void cekaj() {
		try {
			if(!linda.isActive()) {
				asleep=true;
				sem.acquire();
				}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public synchronized void execute(String[] tuple, byte[] lib,byte[] job) throws RemoteException {
		Object[] c= {};
		Object[] d= {tuple[7],tuple[8]};
		lt=new Thread(()->{
			try {
					log("Dobijen zadatak ID: "+tuple[6]+"."+tuple[5]+", "+tuple[1]);
				//wapp.guiLog("Dobio zadatak ID: "+ tuple[6]+", "+tuple[1]);
		inp(tuple);
		task=tuple;
		linda.incTE(1);
		
		linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)
				+ " Zadatak ID:"+tuple[6]+"."+tuple[5]+"--Status: Running");
				linda.updateJobHistory(tuple[6]+"."+tuple[5], "Running");
				
				
					//izvrsavanje posla
					downloadFile(job, "C:\\Users\\these\\Desktop\\Faks-materijali\\3.Godina\\KDP\\kdpprojjan\\"+ID+".jar");
					//downloadLinda();
					downloadFile(lib, "C:\\Users\\these\\Desktop\\Faks-materijali\\3.Godina\\KDP\\kdpprojjan\\LindaImpl.jar");
					ArrayList<String> poruke= doJob(tuple[4], c, tuple[3], d);
					
					Thread.sleep((long) (Math.random()*1000));
					cekaj();
					
					linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)
							+ " Zadatak ID:"+task[6]+"."+tuple[5]+"--Status: Done");
					linda.updateJobHistory(task[6]+"."+tuple[5], "Done");
					linda.jobResults(task[6]+"."+tuple[5], poruke);
					linda.incTE(-1);
					linda.incW(1);
				

					log("Posao ID: "+tuple[6] +"."+tuple[5]+ " zavrsen");

					busy=false;
					linda.updateBusy(this);
		notifyUser(tuple[5],"Job ID: "+task[6]+" finnished with result:");
		for(String msg:poruke)
			notifyUser(tuple[5],msg);
		task=new String[9];

		if(linda.getNumTW()>0)
			linda.startCheck();
			} catch (Exception e) {
				System.out.println(e.getCause()+"Here");
				//e.printStackTrace();
			}

			
		});
		lt.start();
		
	}
	private void notifyUser(String s,String msg) throws RemoteException {
		linda.findUserAndNotify(s, msg);
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
	public void log(String s) throws RemoteException {
		if(wapp!=null)
			wapp.guiLog(s);
		else
			System.out.println(s);
		
	}
	@Override
	public UUID getUUID() throws RemoteException {

			return ID;	
		
	}
	


	@Override
	public void serverTurnsOff() throws RemoteException {
		if(task[0]!=null)
		{
			lt.interrupt();
			if(getServer()!=null)
				notifyUser(task[5],"Zadatak ID: "+task[6]+" nije uspeo do kraja da se izvrsi");
			log("Zadatak ID: "+task[6] +"."+task[5]+ " prekinut");
			task=new String[9];
			busy=false;
			
		}
		linda=null;
		wapp.reset();
		log("Veza prekinuta");
	}


	@Override
	public void interruptProcess() throws RemoteException {
		if(task[0]!=null) {
			lt.interrupt();
			linda.log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)
					+ " Zadatak ID:"+task[6]+"."+task[5]+"--Status: Aborted");
			linda.updateJobHistory(task[6]+"."+task[5], "Aborted");
			ArrayList<String> a=new ArrayList<>();
			a.add("Did not finish");
			linda.jobResults(task[6]+"."+task[5], a);
			notifyUser(task[5],"Zadatak ID: "+task[6]+" je prekinut");
			linda.incTE(-1);
			linda.incW(1);
			busy=false;
			linda.updateBusy(this);
			if(linda.getNumTW()>0)
				linda.startCheck();
		}
		
	}


	@Override
	public void callClose() throws RemoteException {
		serverTurnsOff();
	}
	
	private void downloadFile(byte [] data,String path) {
		 File file = new File(path);
	        try {
	            OutputStream os = new FileOutputStream(file,false);
	            os.write(data);
	            os.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
	private ArrayList<String> doJob(String className, Object[] construct, String methodName, Object[] arguments) {
		
		Process proc = null;
        try {

            proc = Runtime.getRuntime().exec("java -cp "+ID+".jar;LindaImpl.jar; stuff.paket.ToupleSpace "
            + className + " "+methodName+ " "+arguments[0].toString()+ " "+arguments[1].toString()  +"\n",null, 
                            new File("C:\\Users\\these\\Desktop\\Faks-materijali\\3.Godina\\KDP\\kdpprojjan"));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            ArrayList<String> results= new ArrayList<>();
            
            String s;
            do
            {
            	s = stdInput.readLine();
            	if(s!=null)
            		results.add(s);
            	
            }while(s!=null);
            do {
              	s = stdError.readLine();
            	if(s!=null)
            		results.add(s);
            }while(s!=null);
            
            return results;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
	}
	@Override
	public void popupmsg(String msg,String[] tuple) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean asleep() throws RemoteException {
		// TODO Auto-generated method stub
		return asleep;
	}
	@Override
	public void wakeUp() throws RemoteException {
		sem.release();
		asleep=false;
	}
	
	
	public static void main(String[] args) {
		try {
			new Workstation(args[0], Integer.parseInt(args[1]), null);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
