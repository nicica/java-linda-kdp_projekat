package stuff.paket.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import stuff.paket.LindaRemoteServer;
import stuff.paket.Workstation;



public class WorkstationApp extends JFrame implements Serializable{
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JTextArea logArea;
    
    private String host;
    private int port;

    private Workstation stanica=null;
    
    public void guiLog(String s) {
            StringWriter text = new StringWriter();
            PrintWriter out = new PrintWriter(text);
            out.println(logArea.getText());
            out.println(s);
            logArea.setText(text.toString());
    }
    public void reset() {
    	connectButton.setEnabled(true);
    }
    public WorkstationApp() {
        // Initialize the frame
        setTitle("Workstation");
        setSize(600, 400);
        setResizable(false);

        // Initialize the IP address text field
        ipField = new JTextField(20);
        ipField.setText("IP adresa");

        // Initialize the port text field
        portField = new JTextField(8);
        portField.setText("port");

        // Initialize the connect button
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e-> {
           try {        	
        	   
        	   guiLog("Konektovanje na server...");
        	   if(stanica!=null) {
        		   if(!ipField.getText().equals(host) && Integer.parseInt(portField.getText())!=port)   
        		   		throw new NotBoundException();
        		   else
        			   stanica.reconnect();
        		   
        	   }
        	   else {
        		   host=ipField.getText();
        	   	   port=Integer.parseInt(portField.getText());
        	   	   stanica=new Workstation(host,port,this);
        	   }
        	 //  stanica.setWapp(this);
        	   connectButton.setEnabled(false);
        	   guiLog("Konekcija uspesna!");
		} catch (RemoteException | NotBoundException | NumberFormatException e2) {
		//	System.out.println(e2.getCause());
			guiLog("Neuspesna konekcija, pokusajte ponovo");
		} 
        });
        
        
        
        // Initialize the log area
        logArea = new JTextArea();
        logArea.setEditable(false);

        // Add the components to the frame
        JPanel panel = new JPanel();
        panel.add(ipField);
        panel.add(portField);
        panel.add(connectButton);
        add(panel,BorderLayout.NORTH);
        add(new JScrollPane(logArea),BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Display the frame
        setVisible(true);
        
        addWindowListener(new WindowAdapter()  {
			
			
			
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					if(stanica!=null && stanica.getServer()!=null) {
						stanica.dissconect();
						stanica=null;
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			
			
		});
        
    }
    
    public static void main(String args[]) {
        new WorkstationApp();
    }
}