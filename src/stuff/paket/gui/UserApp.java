package stuff.paket.gui;

import javax.swing.*;

import stuff.paket.UserProgram;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class UserApp   {

	private static final long serialVersionUID = 1L;

	private JFrame frame;
    private JTextField ipField;
    private JTextField portField;
    private JTextField functionField;
    private JTextField classField;
    private JTextField taskField;
    private JTextField libPath;
    private JTextField minField;
    private JTextField maxField;
    
    private JButton connectButton;
    private JButton startJob;
    private JButton cancelJob;
    private JButton dissconect;
    private JButton getResults;
    private JButton getStatus;
    private JButton getR;
    
    private JTextArea logArea;
    
    private String host;
    private int port;
    private UserProgram up=null;

    private byte[] downloadedJob;
    private byte[] downloadedLib;
    
    public void guiLogU(String s) {
        new Thread(()->{
            StringWriter text = new StringWriter();
            PrintWriter out = new PrintWriter(text);
            out.println(logArea.getText());
            out.println(s);
            logArea.setText(text.toString());
        }).start();
    }
    private byte[] jarToByte(String fileName) {
    	 try {
             if(!fileName.endsWith(".jar"))
                 return null;
             File file = new File(fileName);
             byte buffer[] = new byte[(int)file.length()];
             BufferedInputStream input = new
                     BufferedInputStream(new FileInputStream(fileName));
             input.read(buffer,0,buffer.length);
             input.close();
             return (buffer);
         } catch(Exception e){
             System.out.println("FileImpl: "+e.getMessage());
             e.printStackTrace();
             return  null;
         }
    }
    public void reset() {
    	startJob.setEnabled(false);
        cancelJob.setEnabled(false);
        connectButton.setEnabled(true);
        dissconect.setEnabled(false);
        getResults.setEnabled(false);
        getStatus.setEnabled(false);
        getR.setEnabled(false);
    }
    private boolean isInt(String strNum) {
        if (strNum.length()==0) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public void popup(String msg,String[] tuple) {
    	new Thread(()->{
    	int choice = JOptionPane.showOptionDialog(
                null, 
                msg, 
                "Potvrda", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                null, 
                null);
    	try {
        if (choice == JOptionPane.YES_OPTION) {
            // user chose "Yes"
        	up.send(tuple,jarToByte(tuple[2]),jarToByte(tuple[0]+tuple[1]));
        } else {
            // user chose "No"
        	up.getServer().log(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)
    				+ " Zadatak ID:"+tuple[6]+"."+tuple[5]+"--Status: Failed");
    			up.getServer().updateJobHistory(tuple[6]+"."+tuple[5], "Failed");
    			ArrayList<String> a=new ArrayList<>();
    			a.add("Did not finish");
    			up.getServer().jobResults(tuple[6]+"."+tuple[5], a);
        }}
    	catch (Exception e) {
			System.out.println(e.getCause());
		}}).start();
    }
    public UserApp() {
        // Initialize the frame
        frame = new JFrame("User program");
        frame.setSize(650, 700);
        frame.setResizable(false);

        
        String[] options = {"Test","Integral","Sum","Product"};
        JComboBox<String> comboBox = new JComboBox<>(options);
        
        functionField= new JTextField(15);
        
        classField= new JTextField(20);
        
        // Initialize the IP address text field
        ipField = new JTextField(20);
        ipField.setText("IP adresa");

        // Initialize the port text field
        portField = new JTextField(8);
        portField.setText("port");
        
        taskField= new JTextField(4);
        
        libPath= new JTextField(35);
        libPath.setText("Unesite putanju biblioteke");
        minField= new JTextField(3);
        minField.setText("min");
        maxField= new JTextField(3);
        maxField.setText("max");

        // Initialize the connect button
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e-> {

            	try {
            		guiLogU("Konektovanje na server..");
            		if(up!=null) {
             		   if(!ipField.getText().equals(host) && Integer.parseInt(portField.getText())!=port)   
             		   		throw new NotBoundException();
             		   else
             			   up.reconnect();
             	   }
            		else {
            		host=ipField.getText();
            		port = Integer.parseInt(portField.getText());
            		up=new UserProgram(host, port, this);
            		}
            		guiLogU("Uspesna konekcija");
                    startJob.setEnabled(true);
                    cancelJob.setEnabled(true);
                    connectButton.setEnabled(false);
                    dissconect.setEnabled(true);
                    getStatus.setEnabled(true);
                    getR.setEnabled(true);
				} catch (RemoteException | NotBoundException | NumberFormatException e2) {
					guiLogU("Neuspesna Konekcija");
				}
            
        });
        startJob = new JButton("Start a job");
        startJob.setEnabled(false);
        startJob.addActionListener(e-> {
        	try {
        	if(up.getServer().isActive()) {
        		String[] tuple= new String[9];
        		tuple[0]="C:\\Users\\these\\Desktop\\Faks-materijali\\3.Godina\\KDP\\kdpprojjan\\";
        		tuple[1]=comboBox.getSelectedItem().toString()+".jar";
        		tuple[2]=libPath.getText();

        		downloadedLib=jarToByte(tuple[2]);
        		if(downloadedLib==null)
        			{guiLogU("Niste uneli jar fajl");
        			throw new Exception();}
            
        		tuple[3]=functionField.getText();
        		tuple[4]=classField.getText();
        		if(tuple[3].length()==0 || tuple[4].length()==0)
        		{	guiLogU("Nedostaju parametri");
        			throw new Exception();}
        		tuple[5]=up.getID();
        		tuple[7]=minField.getText();
        		tuple[8]=maxField.getText();
        		if(!isInt(tuple[7]) || !isInt(tuple[8]))
        		{guiLogU("Niste uneli broj");
        			throw new Exception();
        		}
        		tuple[6]=Integer.toString(++up.idZad);
            	downloadedJob=jarToByte(tuple[0]+tuple[1]);
            
            	up.send(tuple,downloadedLib,downloadedJob);
            	}
        		else
        			guiLogU("Server je ugasen, probajte ponovo kasnije");
            }catch(Exception e2) {
            	guiLogU("Greska");
            }
        });
        cancelJob = new JButton("Cancel active job");
        cancelJob.setEnabled(false);
        cancelJob.addActionListener(e->{
        	try {
				if(up.getServer().isActive())
				{
					if(up.getServer().getNumTW()>0)
						up.getServer().abortFirstTask(up.getUUID());
					else
						guiLogU("Nema poslatih zadataka");
					
				}
				else
					guiLogU("Server je ugasen, probajte ponovo kasnije");
			} catch (Exception e3) {
				// TODO: handle exception
			}
        });
        dissconect= new JButton("Disconnect");
        dissconect.setEnabled(false);
        dissconect.addActionListener(e->{
        	
        	try {
				up.dissconect();
				guiLogU("Veza prekinuta");
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	startJob.setEnabled(false);
            cancelJob.setEnabled(false);
            connectButton.setEnabled(true);
            dissconect.setEnabled(false);
            getResults.setEnabled(true);
            getStatus.setEnabled(false);
            getR.setEnabled(false);
        	
        });
        
        getResults= new JButton("Get lost results");
        getResults.setEnabled(false);
        getResults.addActionListener(e->{
        	
        	try {
        		if(up.getServer()!=null) {
        			if(up.getServer().isActive())
        			{
        				getResults.setEnabled(false);
                		up.getServer().sendLostResults(up);
                	}
        			else
        				guiLogU("Server trenutno ne radi, molimo pokusajte kasnije");
        		}
        		else
        			guiLogU("Niste povezani na server");
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        });
        
        getStatus= new JButton("Status");
        getStatus.setEnabled(false);
        getStatus.addActionListener(e->{
        	if(taskField.getText()!=null) {
        		try {
					if(up.getServer().getTaskInfo(taskField.getText()+"."+up.getID())==null)
						guiLogU("Nepostojeci zadatak");
					else
						guiLogU("Zadatak ID: "+taskField.getText()+", Status--"+
								up.getServer().getTaskInfo(taskField.getText()+"."+up.getID()));
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}
        });
        getR= new JButton("Result");
        getR.setEnabled(false);
        getR.addActionListener(e->{
        	if(taskField.getText()!=null) {
        		try {
					if(up.getServer().getJobResults(taskField.getText()+"."+up.getID())==null)
						guiLogU("Nepostojeci zadatak");
					else
						guiLogU("Zadatak ID: "+taskField.getText()+", Results:"+
								up.getServer().getJobResults(taskField.getText()+"."+up.getID()));
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        }});
        // Initialize the log area
        logArea = new JTextArea();
        logArea.setEditable(false);

        // Add the components to the frame
        JPanel panel= new JPanel();
        panel.setLayout(new GridLayout(4,1));
        
        JPanel panel1= new JPanel();
        panel1.add(new JLabel("Naziv funkcije:"));
        panel1.add(functionField);
        panel1.add(new JLabel("(Pun) Naziv Klase:"));
        panel1.add(classField);
        
        JPanel panel2 = new JPanel();
        panel2.add(ipField);
        panel2.add(portField);
        
        JPanel panel3 = new JPanel();
        panel3.add(connectButton);
        panel3.add(startJob);
        panel3.add(cancelJob);
        panel3.add(dissconect);
        panel3.add(getResults);
        
        JPanel panel4= new JPanel();
        panel4.add(new JLabel("Zadatak ID:"));
        panel4.add(taskField);
        panel4.add(getStatus);
        panel4.add(getR);
        
        JPanel panel5 = new JPanel();
        panel5.add(comboBox);
        panel5.add(minField);
        panel5.add(maxField);
        panel5.add(libPath);
        
        
        panel.add(panel5);
        panel.add(panel1);
        panel.add(panel2);
        panel.add(panel3);
        frame.add(panel,BorderLayout.NORTH);
        frame.add(new JScrollPane(logArea),BorderLayout.CENTER);
        frame.add(panel4,BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Display the frame
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter()  {
			
			
			
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					if(up!=null && up.getServer()!=null) {
						up.dissconect();
						up=null;
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			
		});
    }


    public static void main(String[] args) {
        new UserApp();
        //gui.setVisible(true);
    }
}
