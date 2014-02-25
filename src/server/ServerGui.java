package server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/**
 * ServerGui
 * 
 * User interface code for server.
 * 
 * @author Eric Ostrowski, Austin Anderson, Alex Schuitema
 *
 */
public class ServerGui extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static JTextArea diagLog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		diagLog = new JTextArea();
		((DefaultCaret)diagLog.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGui frame = new ServerGui();
					frame.setVisible(true);
					new ServerController(diagLog).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ServerGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		diagLog.setEditable(false);
		diagLog.setBackground(Color.BLACK);
		diagLog.setForeground(Color.GREEN);
		scrollPane.setViewportView(diagLog);
	}

}
