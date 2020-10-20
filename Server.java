/*
 * Student Name : Pavithra Rathinasabapathy
 * Student ID: 1001698736 
 * 
 * References:
 * Below links are used to develop this project
 * https://stackoverflow.com/questions/15247752/gui-client-server-in-java 
 * https://stackoverflow.com/questions/22728794/how-to-put-timer-into-a-gui
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * https://www.geeksforgeeks.org/socket-programming-in-java/
 * https://stackoverflow.com/questions/41506997/java-multithreading-synchronizedthis-on-a-thread-class
 * https://stackoverflow.com/questions/13671502/java-implement-own-message-queue-threadsafe
 */

package project2DS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class Server {
	private static ServerSocket ss = null;
	private static Socket s  = null;
	private static final int maxClientsCount = 3; // variable to store maximum clients - in this case 3
	private static final ClientHandler[] threads = new ClientHandler[maxClientsCount]; // thread array to store all the threads
	public static final List<String> listA = new ArrayList<String>();
	public static final List<String> listB = new ArrayList<String>();
	public static final List<String> listC = new ArrayList<String>();
	public static boolean quit = false;
	
	public static void main(String[] args) throws IOException {
		
		// open a server socket
		ss = new ServerSocket(5056);
		
		// Create a Server GUI
		final ServerGUI gui = new ServerGUI();
		
		// Read the contents of the Repository upon starting the server and store it to three list
		// corresponding to the Queues
		final ConversionRules cr = new ConversionRules(listA,listB,listC, quit);
		Thread readText = new Thread(cr);
		readText.start();
		
		// Button to close and terminate server
		gui.close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				quit = true;
				gui.frame.dispose();	
				System.exit(0);
			}
		} );
 
		// Instantiate the Three Message Queues A, B, C and initialize the capacity to 100
		MessageQueue queueA = new MessageQueue(100, listA);
		MessageQueue queueB = new MessageQueue(100, listB);
		MessageQueue queueC = new MessageQueue(100, listC);
		
		//Display that Server is waiting for Connections
		gui.display.append("Welcome! Waiting for the Clients to connect.. \n");
		
		while(true) {
			s = null;			
			try {
				// Accept the input connections
				s=ss.accept();
				// open input and output streams
				final DataInputStream dis = new DataInputStream(s.getInputStream());
				final DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				
				// Get Username to differentiate the client
				String uName = dis.readUTF();
				
				/* 
				 * This block of code gets the user name from the Client
				 * Adds the current client's user name to clientList and creates a new thread for the client
				 * and adds the thread to thread array.
				 * If there are three clients already connected, do not allow clients to connect  
				 */
				int i=0;				
				for(i=0;i<maxClientsCount; i++) {
					if(threads[i] == null) {
						threads[i] = new ClientHandler(s, gui, dis,dos, uName,threads);// create a new thread and start it
						Thread t = new Thread(threads[i]);
						t.start(); 
						threads[i].setQueues(queueA, queueB, queueC); // Send three message queues to the clients.
						break;
					}
				}
				// Allowing only 3 clients to connect. when there are more than 3 clients, reject the connection
				if(i == maxClientsCount) {
					dos.writeUTF("Max Client Count Reached.. Server Busy.. Try again Later");
					dis.close();
					dos.close();
					s.close();					
				}
				
				
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
}

/* ClientHandler Thread - Creates a separate thread for each client
 * This block keeps reading the messages form the Client and Displays them in the ServerGUI.
 * Display all the connected user to the Server GUI
 */ 
class ClientHandler implements Runnable {
	
	public ClientHandler[] threads; // Array of Threads to 
    public Socket s;                // Socket of the current Thread
    public ServerGUI gui;           // Server GUI object to display the message from client
    public DataInputStream dis;     // Input Stream of Current Thread
    public DataOutputStream dos;	// Output Stream of Current Thread 
    public String name;				// To Store the User name of current thread
    public int maxClients;			// Max Client to loop through threads
    public MessageQueue queueA, queueB, queueC; // Three output Queues A, B, C
    String rec = "", res = ""; // To store the messages to display it to the user 
    
    public ClientHandler(Socket s, ServerGUI gui, DataInputStream dis, DataOutputStream dos, String uName, ClientHandler[] threads) throws IOException
    { 
        this.s = s; 
        this.gui = gui;
        this.dis = dis;
        this.dos = dos;
        this.name = uName; 
        this.threads = threads;
        this.maxClients = threads.length;
       
    } 
    public void setQueues(MessageQueue A, MessageQueue B, MessageQueue C) {
    	this.queueA = A;
    	this.queueB = B;
    	this.queueC = C;
    }
	public void run() {		
		String uName = this.name;
				
		/* This block of Code displays that current Thread/User have joined the server
		 * and displays all the currently connected users in its GUI
		 * Loop through the Threads and display their names
		 */
		synchronized(this) {
			gui.display.append("User " + uName +  " Joined \n");
			gui.display.append("Users Currently Connected: \n");
			for(int i =0;i<this.maxClients;i++) {
				if(threads[i] != null) {
					gui.display.append(threads[i].name + "\n");
				}
				
			}
		}
		
		/* 
		 * This block of code receives the input length and input queue *
		 * and either upload the input length to the corresponding queue or check for the messages from queue selected by user
		 */
		while(true) {
			try {
				
				String msg = dis.readUTF(); // red input message from user
				if(msg.equals("upload")) {
					 // if upload, then read input length and queue
					String length = dis.readUTF();
					String queueSelected = dis.readUTF();
					gui.display.append("Client " + uName + " is uploading length "+ length + "to" + queueSelected + "\n");
					
					// Upload it to the corresponding queue
					if(queueSelected.equals("Queue A")) {
						res = queueA.upload(length);
					}
					else if (queueSelected.equals("Queue B")) {
						res = queueB.upload(length);
					}
					else {
						res = queueC.upload(length);
					}
					
					
					// Display that the user has uploaded the message to the queue in the server GUI
					gui.display.append(uName + " uploaded the following message to " + queueSelected + "\n");
					String data[] = res.split(",");
					for(int i=0;i<5;i++) {
						gui.display.append(data[i] + "\n");
					}
					dos.writeUTF(res); // Send that message has been uploaded to the client
				}
				else if(msg.equals("QUIT")) {
					// Once the user gives "QUIT" command, remove the current thread from the array of thread
					// Close all the connections and end waiting for messages from client
					// Display that the user has left to Server GUI and close connections					
					synchronized(this) {
						for(int i =0;i<this.maxClients;i++) {
							if(threads[i] != null) {
								if(threads[i].name.equals(uName)) { // check for current thread using name
									threads[i] = null;
								}
							}							
						}
					}
					// Display that the user left
					gui.display.append("User: "+ uName + " left \n"); 
					// Close the connections 
					s.close();
					dos.close();
					dis.close();
					break;
				}
				else {
					// Check the Correcponding queue for messages
					if(msg.equals("Queue A")) {
						 rec  = queueA.receive();
					}
					else if (msg.equals("Queue B")) {
						 rec = queueB.receive();
					}
					else {
						 rec = queueC.receive();
					}
					if(rec.equals("0")) {
						gui.display.append("Client "+uName + " tried reading the Queue. But queue is empty \n");
					}
					else {
						String[] data = rec.split(",");
						// Display the message to GUI and send it to the client
						gui.display.append(uName + " read the following message from " + msg + "\n");
						for(int i=0;i<5;i++) {
							gui.display.append(data[i] + "\n");
						}
					}					
					dos.writeUTF(rec);
				}
				
			}
			catch (Exception e) {
				try {
					// Once the user gives "QUIT" command, remove the current thread from the array of thread
					synchronized(this) {
						for(int i =0;i<this.maxClients;i++) {
							if(threads[i] != null) {
								if(threads[i].name.equals(uName)) { // check for current thread using name
									threads[i] = null;
								}
							}							
						}
					}				
					// Display that the user has left to Server GUI and close connections
					gui.display.append("User: "+ uName + " left \n"); 
					s.close();
					dos.close();
					dis.close();
					break;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}		
		
	}		
}


/*  
 * Class to read the contents of the repository(Text file) and save it in the list.
 * This block of code reads the content from repository check of the text file has been changed or modified since the last read.
 * If the file is modified the contents are being read from the file again. 
 */
class ConversionRules implements Runnable{
	
	public boolean lastModified = false;  // to indicate if the file is modified
	public List<String> qA = new ArrayList<String>(); // List A to store conversion rules for Queue A
	public List<String> qB = new ArrayList<String>(); // List B to store conversion rules for Queue B
	public List<String> qC = new ArrayList<String>(); // List C to store conversion rules for Queue C
	public long lastModifiedtime;  // To store the last modified time of the repository
	public boolean quit, modified = false; // variable to Check the file until server is terminated
	
	public ConversionRules(List<String> qA, List<String> qB, List<String> qC, boolean quit) {
		this.qA = qA;
		this.qB = qB;
		this.qC = qC;
		this.quit = quit;
	}
	
	
	// Function to compare the last modified time every two seconds
	public boolean checkModifiedTime(File file) {
		long currentTime = file.lastModified(); // get the current modified time
		if(lastModifiedtime == currentTime) {  // check if the file has been changed by comparing the last modified time
			return false;
		}
		else {
			//if yes, set the last modified time to recent time and return yes
			lastModifiedtime = currentTime;
			return true;
		}
	}
	
	public void run() {
		
		// Read the file until the server is terminated
		while(!quit) {
			// Read the contents of the file one by one according to the queue and add it to the corresponding list
			File formula = new File("C:/Users/User/Desktop/metrics.txt");
			// get the last modified time
			lastModifiedtime = formula.lastModified();
			Scanner sc;
			try {
				// Read the 
				sc = new Scanner(formula);
				int i=0, j=0;
				
				while(i!=3) {
					String queue = sc.nextLine();
					if(queue.equals("A")) {
						while(j<5) {
							qA.add(sc.nextLine());
							j++;
						}
						j=0;
					}
					else if(queue.equals("B")) {
						while(j<5) {
							qB.add(sc.nextLine());
							j++;
						}
						j=0;
					}
					else if(queue.equals("C")) {
						while(j<5) {
							qC.add(sc.nextLine());
							j++;
						}
						j=0;
					}
					i++;
				}
				sc.close();
				
				// Check the last modified time of the repository every 2 seconds
				while(true) {
					boolean fileChanged = checkModifiedTime(formula);
					if(fileChanged) {
						// if yes clear the list and read the contents again
						qA.clear();
						qB.clear();
						qC.clear();
						break;
					}
					Thread.sleep(2000);
				}
		
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}


/*  
 * Class for Message Queue
 */
class MessageQueue {
	
	private int capacity; // Capacity of the queue
	private Vector<String> queue = new Vector<String>(capacity); // Queue
	public List<String> formList = new ArrayList<String>(); // List corresponding to this queue which stores the contents(Formula) of the repository
	String[] converted_length = new String[5]; // To store the converted values

	
	public MessageQueue(int capacity, List<String> list) {
        this.capacity = capacity;
        this.formList = list;
        
    }
	
	
	// This function converts the input length to each of the metrics given in the repository
	public void convertLength(String message) {
		
		for(int i=0;i<5;i++) {
			String[] data = formList.get(i).split(",");
			// Get the input length and value from repository and multiply
			int inputLength = Integer.valueOf(message);
			double multiply_factor =  Double.parseDouble(data[1]);
			converted_length[i] = data[0] + ": " + (multiply_factor*inputLength); // multiply and store the length
		}
		
	}
	
	
	// Get the input message, convert it to metrics in repository and upload it
	public synchronized String upload(String message) {
		convertLength(message);
		String upload_message = converted_length[0];
		for(int i=1;i<5;i++) {
			upload_message = upload_message + "," + converted_length[i];
		}
		queue.add(upload_message);
		return upload_message; // return the converted metrics
    }
	
	// Check for the messages in the queue and return it 
	public synchronized String receive() {
		if(!queue.isEmpty()) {
			String msg = queue.get(0); // get the message from queue
			queue.remove(0); // remove it from the queue
			return msg;
		}
		
        return "0"; // return if queue is empty
    }
	
}

/* Server Side GUI */
class ServerGUI extends JFrame {
	
	private static final long serialVersionUID = 1L;

	JFrame frame; 
	JTextArea display; // Text area to display message in the Server GUI
	JScrollPane scroll; // Scroll pane
	JButton close; // Close button
	
	public ServerGUI() {
		
		// Create a frame and Jpanel to the ServerGUI
		frame = new JFrame("Message Broker");
		JPanel jp = new JPanel();
		
		// Text Area to display the messages
		display = new JTextArea(20,50);
		display.setEditable(false);
		
		//Scroll pane to scroll the text area
		scroll = new JScrollPane(display);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jp.add(scroll);
		
		// Quit button to terminate
		close = new JButton("QUIT");
		jp.add(close);
		
		frame.add(jp);
		frame.setSize(700, 600); //set Dimension of the Frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on clicking X button
		frame.setVisible(true); 
		
	}
}
