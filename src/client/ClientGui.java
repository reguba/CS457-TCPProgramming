package client;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class ClientGui {

	public static int usercount = 0;

	public static boolean connected = false;

	public static String ipaddress = "127.0.01";

	public static int portaddress = 9876;

	public static String username = "";


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		makeConnectionWindow();
	}


	private static void makeConnectionWindow() {
		JFrame connectionFrame = new JFrame("Welcome!");
		connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();

		final JLabel ip = new JLabel("IP Address:");
		final JLabel port = new JLabel("Port Number:");
		final JLabel user = new JLabel("Username:");
		final JTextField ipNum = new JTextField();
		final JTextField portNum = new JTextField();
		final JTextField userName = new JTextField();
		final JButton connectButton = new JButton("Connect");
		final JButton quitButton = new JButton("Quit");

		connectButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ipaddress = ipNum.getText().toString();
				try {
					portaddress = Integer.parseInt(portNum.getText());
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null,
							"Incorrect Port Number", null, 0);
					portNum.setText("");
					return;
				}
				username = userName.getText().toString();
				checkConnection();
			}
		});

		quitButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		panel.setLayout(new GridLayout(4, 2));
		panel.add(ip);
		panel.add(ipNum);
		panel.add(port);
		panel.add(portNum);
		panel.add(user);
		panel.add(userName);
		panel.add(connectButton);
		panel.add(quitButton);

		connectionFrame.getContentPane().add(panel);
		connectionFrame.setSize(300, 90);
		connectionFrame.setResizable(false);
		connectionFrame.setLocationRelativeTo(null);
		connectionFrame.setVisible(true);
	}


	private static void checkConnection() {

		try {
			Socket clientSocket = new Socket(ipaddress, portaddress);
			connected = true;
			makeClientWindow();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Server at: " + ipaddress + ":" + portaddress
							+ " was not found!", null, 0);
		}

	}


	private static void makeClientWindow() {
		JComponent redComp = new JPanel();
		redComp.setBackground(Color.RED);

		JComponent greenComp = new JPanel();
		greenComp.setBackground(Color.GREEN);

		JComponent blueComp = new JPanel();
		blueComp.setBackground(Color.WHITE);

		JComponent whiteComp = new JPanel();
		whiteComp.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		// we'll use this anchor/fill for all components
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;

		JPanel panel = new JPanel(new GridBagLayout());

		JTextArea chatWindow = new JTextArea();
		JScrollPane chatWindowScroll = new JScrollPane(chatWindow);
		chatWindowScroll.setEnabled(false);
		chatWindow.setHighlighter(null);
		chatWindow.setEditable(false);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.9; // use 80% of the overall width
		gbc.weighty = 0.9; // use 80% of the overall height
		panel.add(chatWindowScroll, gbc);
		// panel.add(redComp, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.weightx = 0.1; // use 20% of the overall width
		gbc.weighty = 1.0; // use 100% of the overall height
		panel.add(blueComp, gbc);

		JLabel UsersTitle = new JLabel("Users Online" + " ("
				+ usercount + ")");

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 0;
		gbc.gridheight = 2;
		gbc.weightx = 0.1; // use 20% of the overall width of
							// UsersCount
		gbc.weighty = 1.0; // use 100% of the overall height
							// UserCounter

		blueComp.add(UsersTitle);

		final JTextPane chatArea = new JTextPane();
		JScrollPane chatAreaScroll = new JScrollPane(chatArea);
		chatArea.setText("You are not connected yet.");

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.9; // use 80% of the width used by green/white
							// comps
		gbc.weighty = 0.1; // use 20% of the overall height
		panel.add(chatAreaScroll, gbc);
		// panel.add(greenComp, gbc);

		final JButton sendMessage = new JButton("Connect");
		sendMessage.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				int userNum = (int) Math.random();
				String username = "user " + userNum;
				username = JOptionPane.showInputDialog("Username");

				String getPortNum = "0";
				getPortNum = JOptionPane.showInputDialog("Port Number");

				try {
					int portNum = Integer.parseInt(getPortNum);
					sendMessage.setText("Send");
					chatArea.setText("Connecting...");
				} catch (IllegalArgumentException IAE) {
					JOptionPane.showMessageDialog(null,
							"Invalid port number.");
				}

				System.out.println(username);

			}
		});

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.1; // use 20% of the width used by green/white
							// comps
		gbc.weighty = 0.1; // use 20% of the overall height
		panel.add(sendMessage, gbc);
		// panel.add(whiteComp, gbc);

		JFrame frame = new JFrame("Client GUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.setSize(600, 400);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
