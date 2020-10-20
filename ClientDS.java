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
 * https://www.geeksforgeeks.org/java-swing-jcombobox-examples/
 */


package project2DS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

public class ClientDS extends JFrame implements ItemListener {
	
	private static final long serialVersionUID = 1L;
	
	// GUI Components
	public JFrame frame;
	public JLabel userName;
	public JTextField uNameField;
	public JLabel choose, label1, label2, label3;
	public JRadioButton r1,r2;
	public JRadioButton jr1, jr2;
	public ButtonGroup buttonGroup;
	public JTextField inputNumber;
	public String queueValue;
	public JButton ok, sendUName, quit;
	public JTextArea display;
	public JScrollPane scroll;
	public String[] list = {"Queue A", "Queue B", "Queue C"};
	public JComboBox queueSelectList;
	public String clientName = "";
	public boolean closeConn = false, upload = false;
	
	public ClientDS() {
		createClientGUI();
	}

	// Creating a Client GUI
	public void createClientGUI() {
		// create a frame and Panel for Client GUI
		frame = new JFrame("Client GUI");
		JPanel jp = new JPanel();

		// User Name Label
		userName = new JLabel("UserName:");
		jp.add(userName);

		// Text Field to Type User Name
		uNameField = new JTextField(15);
		jp.add(uNameField);
		
		// Send Button to connect to server with typed user name
		sendUName = new JButton("Submit");
		jp.add(sendUName);

		// Label to display the details of the queue.
		label1 = new JLabel("<html>Welcome!<br/>"+ "<br/> Queue A: Meter, Millimeter, Centimeter, Kilometer, Astronomical Unit<br/>"
				+"<br/> Queue B: Parsec, Light Year, Inch, Foot, Yard <br/>"
				+"<br/> Queue C: Mile, Nautical Mile, Americal football Field, Hand, Horse <br/></html>", JLabel.LEFT);
		label1.setEnabled(false);
		jp.add(label1);
		
		//Choose an option label
		choose = new JLabel("Choose one option: ",  SwingConstants.CENTER);
		choose.setEnabled(false);
		jp.add(choose);
		
		//Radio button option to select either upload or check for messages
		jr1 = new JRadioButton("Upload Message");
		jr2 = new JRadioButton("Check for Messages");
		jr1.setEnabled(false);
		jr2.setEnabled(false);
		jr1.addItemListener(this);
		jr2.addItemListener(this);
		jp.add(jr1);
		jp.add(jr2);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(jr1);
		buttonGroup.add(jr2);
		
		// Label to enter a number as input
		label2 = new JLabel(" Enter a number (length in meters):   ");
		label2.setEnabled(false);
		jp.add(label2);
		
		// TExt Field to input a number
		inputNumber = new JTextField(15);
		inputNumber.setEnabled(false);
		jp.add(inputNumber);
		
		
		// Select a queue label 
		label3 = new JLabel("Select a Queue (Queue details provided above):");
		label3.setEnabled(false);
		jp.add(label3);
		
		// Drop down lis to select a queue
		queueSelectList = new JComboBox(list);
		queueSelectList.setEnabled(false);
		jp.add(queueSelectList);
		
		// Text Area to display messages
		display = new JTextArea(10, 40);
		display.setEditable(false);
		display.setLineWrap(true);
		scroll = new JScrollPane(display);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jp.add(scroll);
		
		
		// Buttons to send request and terminate
		ok = new JButton("SUBMIT");
		jp.add(ok);
		quit = new JButton("QUIT");
		jp.add(quit);
		
		frame.add(jp);
		frame.setSize(500, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}
	
	@Override
    public void itemStateChanged(ItemEvent e) {

        int sel = e.getStateChange();
        /* This block of enables and disables the components of the GUI
         * if user selects upload radio button both text field for inputting number 
         * and drop down for queue selection will be enabled.
         * if user clicks check for messages, only the queue selection drop down will be enabled
         */
        if (sel == ItemEvent.SELECTED) {

            JRadioButton button = (JRadioButton) e.getSource();
            String text = button.getText();
            if(text.equals("Upload Message")) {
            	upload = true;
            	label2.setEnabled(true);
            	inputNumber.setEnabled(true);
            	label3.setEnabled(true);
            	queueSelectList.setEnabled(true);
            }
            else {
            	upload = false;
            	label2.setEnabled(false);
            	inputNumber.setEnabled(false);
            	label3.setEnabled(true);
            	queueSelectList.setEnabled(true);
            }
        }
    }
	

	public static void main(String[] args) {
		/*Reference 
		  * 1) https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
		  */
		 try 
	        { 	
			 	// getting localhost ip 
	            InetAddress ip = InetAddress.getByName("localhost");	            
	            // establish the connection with server port 5056 
	            Socket s = new Socket(ip, 5056);
	            // Create a client GUI
			    final ClientDS clientGUI = new ClientDS();
	            // Get Input and output streams for the client socket
	            final DataInputStream dis = new DataInputStream(s.getInputStream()); 
	            final DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			 	
			    // send Username to server when user clicks Send button in client GUI
	            clientGUI.sendUName.addActionListener(new ActionListener() {
	            	public void actionPerformed(ActionEvent ae) {
	            		clientGUI.clientName = clientGUI.uNameField.getText(); // get the name entered by the user.
	    	            try {
	    					dos.writeUTF(clientGUI.clientName); // write it to the server
	    				} catch (IOException e) {
	    					e.printStackTrace();
	    				}
	            		
	            		clientGUI.frame.setTitle(clientGUI.uNameField.getText()); // Set the frame title to the name given by the user      
	            		/* Enable the radio buttons and labels to select options
	            		 * - Upload Messages
	            		 * - Check for messages
	            		 */
	            		clientGUI.label1.setEnabled(true); 
	            		clientGUI.choose.setEnabled(true);
	            		clientGUI.jr1.setEnabled(true);
	            		clientGUI.jr2.setEnabled(true);
	            	}
	            });
			 	
	            
	            /*
	             * This block of code send the input length in meters and the input queue to the server
	             * Clear the user selection once the inout is sent and wait for the further inputs from user
	             */
	            clientGUI.ok.addActionListener(new ActionListener() {
	            	public void actionPerformed(ActionEvent ae) {
	            		// Get the user entered input length and queue from the GUI 
	            		String val = clientGUI.inputNumber.getText();
	            		String queueSelected  = clientGUI.queueSelectList.getSelectedItem().toString();
	            		try {
	            			// if the user choose to upload, send both input length and queue
	            			// or send only the input queue to check the message
	            			if(clientGUI.upload) {
	            				dos.writeUTF("upload"); // Send a message indicating that user is going to upload
	            				dos.writeUTF(val); // Send the input length
	            				clientGUI.display.append("Client is trying to upload the message to " + queueSelected + "\n"); // Display the message to the user GUI
	            			}
	            			else {
	            				clientGUI.display.append("Client is trying to read from the " + queueSelected + "\n"); // Display the message to the user GUI
	            			}
		            		dos.writeUTF(queueSelected);  // Send the input queue  selected	
		            		
		            		// Clear all user selection
		            		clientGUI.buttonGroup.clearSelection(); 
		            		clientGUI.label2.setEnabled(false);		            		
		            		clientGUI.inputNumber.setEnabled(false);
		            		clientGUI.inputNumber.setText("");
		            		clientGUI.label3.setEnabled(false);
		            		clientGUI.queueSelectList.setEnabled(false);
						} catch (IOException e) {
							e.printStackTrace();
						}		            		
	            		
	            	}
	            });
	            
	            // If the user clicks quit button close the connection and terminate
	            clientGUI.quit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							dos.writeUTF("QUIT"); // Write The message "QUIT" to indicate server
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						clientGUI.closeConn = true; // set close connection to true
						clientGUI.frame.dispose(); // Close the GUI frame
					}
				});
	            
	           while(!clientGUI.closeConn) {
	        	   
	        	   // This block of code reads the message from the server and display it to the user
	        	   try {
	        		   String displaytext = dis.readUTF();   		           
		            if(clientGUI.upload) {
		            		clientGUI.display.append("Message Successfully Uploaded \n");
		            }
		            else if(displaytext.contentEquals("Max Client Count Reached.. Server Busy.. Try again Later")) {
		            	// Close Connections   
		            	   clientGUI.display.append("Max Client Count Reached.. Server Busy.. Try again Later \n");
		            	   try {
		            		   Thread.sleep(3000);
		            	   }
		            	   catch(Exception e) {
		            		   e.printStackTrace();
		            	   }
		   	               dis.close();
		                   dos.close();
		                   s.close();
		                   clientGUI.frame.dispose();
		            }
		            else {
		            	if(displaytext.equals("0")){
		            		clientGUI.display.append("The queue selected is empty \n");
		            	}
		            	else {
		            		clientGUI.display.append("Message in the queue: \n");
		            		for(int i=0;i<5;i++) {
		            			String[] data = displaytext.split(",");
		            			clientGUI.display.append(data[i] + "\n");
		            		}
		            	}
		            	
		            }
	        	   }
	        	   catch(Exception e){
	        		   // Close Connections           
	   	               dis.close();
	                   dos.close();
	                   s.close();
	        	   }
		           
	            }
	            // Close Connections           
	            dis.close();
                dos.close();
                s.close();
	        }catch(Exception e){ 
	        	e.printStackTrace();
	        }
	}
}