import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
public class ClientApp implements ActionListener{
    JFrame window;
    JLabel east;
    JPanel north,south;
    JButton connect,submit,submit2;
    JTextArea client;
    JTextField input,alias;
    JPanel[] grid;
    JScrollPane pane;
    DataOutputStream outStream;
    BufferedReader inStream;
    Socket connection = null;
    public ClientApp(){
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
        window.setTitle("CLIENT window - A 1172 product.");
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
    public void setLabels(){
    	north = new JPanel();
        north.setLayout(new FlowLayout());
        south = new JPanel();
        south.setLayout(new FlowLayout());
        east = new JLabel("--------------------");
        east.setLayout(new GridLayout(10,1));
        east.setOpaque(false);
    }
    public void setButtons(){
        connect = new JButton("CONNECT");   
        connect.addActionListener(new Client());
        connect.setPreferredSize(new Dimension(200,50));
        connect.setEnabled(true);
        submit = new JButton("SUBMIT");
        submit.addActionListener(this);
        submit.setSize(200, 50);
        submit.setEnabled(true);
        submit2 = new JButton("SubmitAlias");
        submit2.addActionListener(new Action());
        submit2.setSize(200, 50);
        submit2.setEnabled(true);
    }
    class Action implements ActionListener{
    	public void actionPerformed(ActionEvent ae){
    		try{
    		outStream = new DataOutputStream(connection.getOutputStream());
            outStream.writeBytes("%%%%" + alias.getText());
            outStream.write(13);
            outStream.write(10);
            outStream.flush();
            alias.setText("");
    		}catch(Exception e){client.append(e.getMessage());}
    	}
    }
    public void setTextAreas(){
        client = new JTextArea();
        client.setFont(new Font("Arial", Font.PLAIN, 18));
        client.setBackground(Color.DARK_GRAY);
        client.setForeground(Color.LIGHT_GRAY);
        client.setEditable(false);
        pane = new JScrollPane(client);
        input = new JTextField(25);
        input.setFont(new Font("Arial",Font.PLAIN,16));
        alias = new JTextField(25);
        alias.setFont(new Font("Arial",Font.BOLD,10));
        alias.setText("ALIAS");
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
        window.add(pane, BorderLayout.CENTER);
        grid[4].add(connect);
        north.add(alias);
        north.add(submit2);
        window.add(north, BorderLayout.NORTH);
        window.add(east, BorderLayout.EAST);
        south.add(input);
        south.add(submit);
        window.add(south, BorderLayout.SOUTH);
    }
    @Override
    public void actionPerformed(ActionEvent ae){
        try{
        inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        outStream = new DataOutputStream(connection.getOutputStream());
        if(ae.getActionCommand().equals("SUBMIT"))
        {
                String sendLine = input.getText();
                outStream.writeBytes(sendLine);
                outStream.write(13);
                outStream.write(10);
                outStream.flush();
                if(input.getText().equalsIgnoreCase("QUIT"))
                {
                	do{
                		  String s = inStream.readLine();
                		  if(s.endsWith("has disconnected.")){
    		              client.append("Disconnected from Server.");
    		              connection.close();
    		              submit.setEnabled(false);
    		              connect.setText("CONNECT");
    		              break;
                		  }
                	}while(true);
                }
                input.setText("");
        }
        }
        catch(Exception e)
        {
            client.append(e.toString());
        }
    }
    class Client implements ActionListener,Runnable{
        PortTalk portTalk;
        public void actionPerformed(ActionEvent ae){
            if(ae.getActionCommand().equals("CONNECT")){
                connect.setText("TERMINATE");
            try{
                portTalk = new PortTalk();
                portTalk.displayDestinationParameters();
                portTalk.displayLocalParameters();
                Thread t = new Thread(this,"APPENDER");
                t.start();
            }
            catch(Exception e){
                    client.append("Connection error.");
                    connect.setEnabled(false);
                }
            }
            else{
                connect.setText("Connect");
                portTalk.shutdown();
            }
        }
        public void run(){
        	while(true){
        		try{
        			String s = inStream.readLine();
        			if(s!=""){
        				client.append("***");
        				client.append(s);
        				client.append("\n");
        			}
        		}catch(Exception e){
        			client.append(e.getMessage());
        		}
        	}
        }
    }
    class PortTalk{
        String[] args = {"http://192.168.137.1","8080"};
        public PortTalk(){
            try{
                connection = new Socket(args[0],Integer.parseInt(args[1]));
                connect.setEnabled(true);
            }
            catch(IOException ex){
                error("IO error getting streams");
            }
            catch(Exception ex){
                error("Unable to process client request");
            }
        }
        public void displayDestinationParameters(){
            client.append("Connected to " + args[0] + " at port " + args[1] + ".\n");
            InetAddress destAddress = connection.getInetAddress();
            String name = destAddress.getHostName();
            byte ipAddress[] = destAddress.getAddress();
            int port = connection.getPort();
            displayParameters("Destination ",name,ipAddress,port);
        }
        public void displayLocalParameters(){
            InetAddress localAddress = null;
            try{
                localAddress = InetAddress.getLocalHost();
            }
            catch(UnknownHostException ex){
                error("Error getting local host information");
            }
            String name = localAddress.getHostName();
            byte ipAddress[] = localAddress.getAddress();
            int port = connection.getLocalPort();
            displayParameters("Local ",name,ipAddress,port);
        }
        public void displayParameters(String s,String name,byte ipAddress[],int port){
            client.append(s + "host is " + name + ".\n");
            client.append(s + "IP adress is ");
            for(int i=0;i<ipAddress.length;i++)
                client.append((ipAddress[i]+256)%256+".");
            client.append("\n" + s + "port number is " + port + ".\n");
        }
        public void shutdown(){
            try{
                connection.close();
                client.append("Disconnected from Server.");
                connect.setEnabled(false);
            }
            catch(IOException ex){
                error("IO error closing socket");
                
            }
        }
        public void error(String s){
            client.append(s+"\n");
                    
        }
    }
    public static void main(String[] args) {
        new ClientApp();
    }
}

