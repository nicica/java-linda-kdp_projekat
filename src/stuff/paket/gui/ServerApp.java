package stuff.paket.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


import stuff.paket.CentralServer;
import stuff.paket.LindaRemoteServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerApp extends JFrame {

    // Components of the GUI
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private JTextArea statusArea;
    
    
    
    private CentralServer cs=null;

    public void guiLog(String s) {
        new Thread(()->{
            StringWriter text = new StringWriter();
            PrintWriter out = new PrintWriter(text);
            out.println(logArea.getText());
            out.println(s);
            logArea.setText(text.toString());
        }).start();
    }
    public void updateStatus() {
        statusArea.setText("Workstations Avabiable: "+cs.numW+" \tTasks waiting: "+cs.numTW+" \tTasks executing: "+cs.numTE);
    }
    public ServerApp() {
        // Set the title of the window
        setTitle("Central Server");
        
        // Set the layout of the window to be a BorderLayout
        setLayout(new BorderLayout());

        // Create the start button and add an ActionListener to it
        startButton = new JButton("Start Server");
        startButton.addActionListener(e-> {
           try {
        	   if(cs==null) {
        		   System.setProperty("java.rmi.server.hostname", "192.168.0.7");
        		   cs=new CentralServer(this);
        		   Registry registry= LocateRegistry.createRegistry(cs.port);
        		   registry.rebind("/Server", cs);
        		   CentralServer.setLog(System.out);
        	   }
        	   updateStatus();
        	   cs.setActive();
        	   guiLog(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Server je pokrenut");
        	   startButton.setEnabled(false);
        	   stopButton.setEnabled(true);
           
           } catch (Exception e2) {
			e2.printStackTrace();
           }
        });

        // Create the print log button and add an ActionListener to it
       
        
        stopButton= new JButton("Stop Server");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	guiLog(LocalDate.now()+" "+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" Server je zaustavljen");
            		
            	cs.active=false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
        // Create the log area
        logArea = new JTextArea();
        logArea.setEditable(false); // Make the log area uneditable

        // Create a panel to hold the start and print log buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        statusArea= new JTextArea();
        statusArea.setText("Workstations Avabiable: 0 \tTasks waiting: 0 \tTasks executing: 0");
        
        // Add the button panel and log area to the window
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(statusArea,BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set the size of the window and make it visible
        setSize(700, 400);
        setResizable(false);
        setVisible(true);
        addWindowListener(new WindowAdapter()  {
			
			
			
			@Override
			public void windowClosing(WindowEvent e) {
				if(cs!=null && cs.active) {
				cs.active=false;
				try {
					cs.onClose();
				} catch (Exception e1) {
					System.out.println(e1.getCause());
				}
				}
			}
			
			
		});
    
    }

    
    public static void main(String[] args) {
    	new ServerApp();
           
    }
}
