import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.concurrent.*;
public class ServerMain {
	JFrame window;
	JLabel east, information, message;
	JButton run;
	JTextArea host;
	JPanel[] grid;
	JScrollPane pane;
	Socket connections[] = new Socket[10];
	int no_of_clients = 0;
	public ServerMain(){
		setWindow();
		setLabels();
		setButtons();
		setTextAreas();
		setGrid();
		setComponents();
	}
	public void setWindow(){
		window = new JFrame();
		window.setSize(700,500);
		window.setTitle("SERVER window - A 1172 product.");
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
	public void setLabels(){
		east = new JLabel("--------------------");
		east.setLayout(new GridLayout(10,1));
		east.setOpaque(false);
		information = new JLabel("Hello, welcome to server window");
		information.setFont(new Font("Sans Serif",Font.PLAIN,20));
		message = new JLabel();
		message.setFont(new Font("Sans Serif",Font.BOLD + Font.ITALIC,15));
		message.setForeground(Color.RED);
	}
	public void setButtons(){
		run = new JButton("RUN");	
		run.addActionListener(new Server());
		run.setPreferredSize(new Dimension(200,50));
		run.setEnabled(true);
	}
	public void setTextAreas(){
		host = new JTextArea();
		host.setFont(new Font("Arial", Font.PLAIN, 18));
		host.setBackground(Color.BLACK);
		host.setForeground(Color.WHITE);
		host.setEditable(false);
		pane = new JScrollPane(host);
	}
	public void setGrid(){
		grid = new JPanel[10];
		for(int i=0; i<10; i++)
		{
			grid[i] = new JPanel();
			grid[i].setOpaque(false);
			east.add(grid[i]);
 		}
	}
	public void setComponents(){
		window.add(information,BorderLayout.NORTH);
		window.add(pane, BorderLayout.CENTER);
		grid[4].add(run);
		window.add(east, BorderLayout.EAST);
		window.add(message, BorderLayout.SOUTH);
	}
	class Server implements ActionListener{
		ServerSocket serverSocket = null;
		public void actionPerformed(ActionEvent ae) {
			if(run.getText().equals("RUN")){
				run.setText("TERMINATE");
				message.setText("SERVER ENGINE STARTED.");
				new Server().startServer();
			}
			else{
				run.setText("RUN");
				message.setText("SERVER ENGINE STOPPED.");
				try{
					serverSocket.close();
				}
				catch(Exception e)
				{
					host.append("Please restart the program to continue.\n");
				}
			}
		}
		public void startServer(){
			final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
			Runnable serverTask = new Runnable(){
				@Override 
				public void run(){
					try {
	                    serverSocket = new ServerSocket(8080);
	                    host.append("Server listening on port " + serverSocket.getLocalPort() + ".\n");
	                    while (true) {
	                        Socket clientSocket = serverSocket.accept();
	                        clientProcessingPool.submit(new ClientTask(clientSocket));
                    	} 
					}
                    catch(Exception e) {
                    host.append("Unable to process client request.\n");
                    e.printStackTrace();
                    }
				}
	        };
	        Thread serverThread = new Thread(serverTask);
	        serverThread.start();
		}
		private class ClientTask implements Runnable{
			private final Socket clientSocket;
			String alias = "";
			DataOutputStream outStream = null;
			String inLine = "";
			private ClientTask(Socket clientSocket) {
	            this.clientSocket = clientSocket;
	            alias = clientSocket.getInetAddress().getHostName();
	            connections[no_of_clients] = clientSocket; 
	            no_of_clients++;
	            }
			@Override
	        public void run() {
	            host.append("Accepted connection to " + alias + " on port " + clientSocket.getPort() + ".\n");
	            boolean finished = false;
	            try{
	    		BufferedReader inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    		do{
	    			inLine = inStream.readLine();
	    			if(inLine.length()>4){
	    			if(inLine.substring(0,4).equals("%%%%")){ 
	    				display(alias + " has changed it's name to " + inLine.substring(4,inLine.length()));
	    				alias = inLine.substring(4,inLine.length());
	    				continue;
	    			}
	    			}
	    			if(inLine.equalsIgnoreCase("quit")) {
	    				display(alias + "has disconnected.");
	    				finished = true;
	    				break;
	    			}
	    			host.append("Received: "+inLine+"\n");
	    			display(alias + " says: " + inLine);
	    		}
	    		while(!finished);
	    		inStream.close();
	    		outStream.close();
	    		host.append("Client: " + alias + " has deactivated.\n");
	    		clientSocket.close();
	    		message.setText(clientSocket.getInetAddress().getHostName() + " has disconnected");
	            }
 	             catch (Exception e) {
	                host.append(e.toString());
	            }
	            finally{
	            	if(serverSocket.isClosed())
	            		host.setText("Client(s) disconnected.");
	            }
			}
			public void display(String str){
				try{
					for(int index=0;index<no_of_clients;index++){
    				outStream = new DataOutputStream(connections[index].getOutputStream());
    				String outLine = "";
    				outLine = (str.trim());
    				for(int i=0; i<outLine.length();i++)
    					outStream.write((byte)outLine.charAt(i));
    					outStream.write(13);
    					outStream.write(10);
    					outStream.flush();
    					host.append("Sent: " + outLine + "\n");
    			}}catch(Exception e){host.append(e.getMessage());}
			}
		}
	}
	public static void main(String[] args) {
	new ServerMain();
	}
}
