package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

/**
 * ClientGui
 * 
 * User interface code for the client.
 * 
 * @author Eric Ostrowski, Austin Anderson, Alex Schuitema
 *
 */
public class ClientGui extends JFrame {

	private static final long serialVersionUID = 1L;
	private static JFrame connectionFrame;
	private static ClientController controller;
	private JPanel contentPane;
	private static JTextField txtSendArea;
	private static JTextArea txtChatArea;
	private static JList<String> lstGroups;
	private static JList<String> lstUsers;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		makeConnectionWindow();
	}

	/**
	 * Create the frame.
	 */
	public ClientGui() {
		
		setVisible(true);
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 672, 490);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel occupancyPanel = new JPanel();
		occupancyPanel.setBackground(Color.BLACK);
		occupancyPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(occupancyPanel, BorderLayout.EAST);
		occupancyPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setOpaque(false);
		scrollPane.setBackground(Color.BLACK);
		scrollPane.setBorder(null);
		occupancyPanel.add(scrollPane);
		
		lstGroups.setBorder(null);
		lstGroups.setFixedCellWidth(120);
		lstGroups.setForeground(Color.GREEN);
		lstGroups.setBackground(Color.BLACK);
		scrollPane.setViewportView(lstGroups);
		
		JLabel lblGroups = new JLabel("Groups");
		lblGroups.setOpaque(true);
		lblGroups.setForeground(Color.GREEN);
		lblGroups.setBackground(Color.BLACK);
		scrollPane.setColumnHeaderView(lblGroups);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setOpaque(false);
		scrollPane_1.setBackground(Color.BLACK);
		scrollPane_1.setBorder(null);
		occupancyPanel.add(scrollPane_1);
		
		lstUsers.setBorder(null);
		lstUsers.setForeground(Color.GREEN);
		lstUsers.setBackground(Color.BLACK);
		lstUsers.setFixedCellWidth(120);
		scrollPane_1.setViewportView(lstUsers);
		
		JLabel lblUsers = new JLabel("Users");
		lblUsers.setOpaque(true);
		lblUsers.setBorder(null);
		lblUsers.setForeground(Color.GREEN);
		lblUsers.setBackground(Color.BLACK);
		scrollPane_1.setColumnHeaderView(lblUsers);
		
		JPanel chatPanel = new JPanel();
		contentPane.add(chatPanel, BorderLayout.SOUTH);
		chatPanel.setLayout(new BorderLayout(0, 0));
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!txtSendArea.getText().equals("")) {
					try {
						controller.sendMessage(txtSendArea.getText());
						txtSendArea.setText("");
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null,
								"Error sending message", null, 0);
						e.printStackTrace();
					}
				}
			}
		});
		btnSend.setPreferredSize(new Dimension(120, 25));
		chatPanel.add(btnSend, BorderLayout.EAST);
		
		chatPanel.add(txtSendArea, BorderLayout.CENTER);
		txtSendArea.setColumns(10);
		
		Component verticalStrut = Box.createVerticalStrut(10);
		chatPanel.add(verticalStrut, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_2 = new JScrollPane();
		panel.add(scrollPane_2);
		scrollPane_2.setBorder(null);
		
		txtChatArea.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtChatArea.setForeground(Color.GREEN);
		txtChatArea.setBackground(Color.BLACK);
		txtChatArea.setEditable(false);
		scrollPane_2.setViewportView(txtChatArea);
		
		Component horizontalStrut = Box.createHorizontalStrut(10);
		panel.add(horizontalStrut, BorderLayout.EAST);
	}
	
	private static void makeConnectionWindow() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		connectionFrame = new JFrame("TCP Chat");
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
				
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							
							txtSendArea = new JTextField();
							txtChatArea = new JTextArea();
							((DefaultCaret)txtChatArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
							lstGroups = new JList<String>();
							lstUsers = new JList<String>();
							
							InetAddress ip = InetAddress.getByName(ipNum.getText());
							int port = Integer.parseInt(portNum.getText());
							
							controller = new ClientController(ip, port, userName.getText(), txtChatArea, txtSendArea, lstGroups, lstUsers);
							ClientGui frame = new ClientGui();
							frame.setVisible(true);
							connectionFrame.setVisible(false);
						
						} catch (UnknownHostException e) {
							JOptionPane.showMessageDialog(null,
									"Invalid IP address", null, 0);
							e.printStackTrace();
							
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Invalid port number", null, 0);
							
						} catch (IllegalArgumentException e) {
							JOptionPane.showMessageDialog(null,
									e.getMessage(), null, 0);
							e.printStackTrace();
						
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null,
									"Unable to connect to server!", null, 0);
							e.printStackTrace();
						}
					}
				});
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
		connectionFrame.setSize(300, 120);
		connectionFrame.setResizable(false);
		connectionFrame.setLocationRelativeTo(null);
		connectionFrame.setVisible(true);
	}
}
