package de.fkoeberle.tcpbuffer;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.dyno.visual.swing.layouts.Bilateral;
import org.dyno.visual.swing.layouts.Constraints;
import org.dyno.visual.swing.layouts.GroupLayout;
import org.dyno.visual.swing.layouts.Leading;

//VS4E -- DO NOT REMOVE THIS LINE!
public class ApplicationFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel0;
	private JPanel jPanel0;
	private JPanel otherStepsPanel;
	private JLabel jLabel2;
	private JLabel jLabel1;
	private JPanel serverQuestionsPanel;
	private JRadioButton radioButtonNo;
	private JPanel clientQuestionsPanel;
	private JLabel jLabel6;
	private JTextField clientServerToConnect;
	private JTextField clientPortToConnect;
	private JLabel jLabel3;
	private JTextField serverMinecraftPort;
	private JLabel jLabel4;
	private JTextField serverProxyPort;
	private JLabel jLabel5;
	private JButton startButton;
	private JLabel jLabel7;
	private JLabel jLabel8;
	private JList list;
	private JScrollPane jScrollPane0;
	private JPanel jPanel4;
	private JPanel jPanel3;
	private ButtonGroup buttonGroup1;
	private JRadioButton radioButtonYes;
	private static final String MINECRAFT_DEFAULT_PORT_STRING = "25565";
	private final Server server;
	private JButton stopButton;
	private static final String PREFERRED_LOOK_AND_FEEL = "javax.swing.plaf.metal.MetalLookAndFeel";

	public ApplicationFrame() {
		initComponents();
		this.server = new Server();
		final DefaultListModel listModel = new DefaultListModel();
		list.setModel(listModel);
		server.addEventListener(new EventListener() {

			@Override
			public void handleEvent(String event) {
				listModel.add(0, event);
				if (listModel.size() > 4)
					listModel.remove(4);
			}
		});
		server.addServerStateListener(new ServerStateListener() {

			@Override
			public void handleServerStopped() {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						startButton.setEnabled(true);
						stopButton.setEnabled(false);
					}
				});
			}

			@Override
			public void handleServerStarted() {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						startButton.setEnabled(false);
						stopButton.setEnabled(true);
					}
				});
			}
		});
		server.addHostingListener(new HostingListener() {

			@Override
			public void handleHostingChanged() {
				clientQuestionsPanel.setVisible(!server.isHosting());
				serverQuestionsPanel.setVisible(server.isHosting());
			}
		});
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				server.stop();
			}
		});
	}

	private void initComponents() {
		setTitle("tcpbuffer - Package Count Reducing Proxy");
		setLayout(new GroupLayout());
		add(getJPanel0(), new Constraints(new Bilateral(0, 0, 277),
				new Leading(0, 77, 61, 61)));
		add(getOtherStepsPanel(), new Constraints(new Bilateral(0, 0, 426),
				new Leading(83, 112, 10, 10)));
		add(getJPanel3(), new Constraints(new Bilateral(0, 0, 0),
				new Bilateral(201, 0, 0)));
		initButtonGroup1();
		setSize(449, 430);
	}

	private JButton getStopButton() {
		if (stopButton == null) {
			stopButton = new JButton();
			stopButton.setText("Stop");
			stopButton.setEnabled(false);
			stopButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					stopButtonActionActionPerformed(event);
				}
			});
		}
		return stopButton;
	}

	private JRadioButton getJRadioButtonYes() {
		if (radioButtonYes == null) {
			radioButtonYes = new JRadioButton();
			radioButtonYes.setSelected(true);
			radioButtonYes.setText("Yes");
			radioButtonYes.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					radioButtonYesActionActionPerformed(event);
				}
			});
		}
		return radioButtonYes;
	}

	private void initButtonGroup1() {
		buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(getJRadioButtonYes());
		buttonGroup1.add(getJRadioButton1());
	}

	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GroupLayout());
			jPanel3.add(getJLabel5(), new Constraints(new Leading(12, 12, 12),
					new Leading(0, 12, 12)));
			jPanel3.add(getJButton0(), new Constraints(new Leading(24, 12, 12),
					new Leading(21, 12, 12)));
			jPanel3.add(getJLabel7(), new Constraints(new Leading(12, 12, 12),
					new Leading(62, 10, 10)));
			jPanel3.add(getJLabel8(), new Constraints(new Leading(12, 12, 12),
					new Leading(95, 12, 12)));
			jPanel3.add(getJPanel4(), new Constraints(
					new Bilateral(24, 12, 33), new Leading(122, 101, 10, 10)));
			jPanel3.add(getStopButton(), new Constraints(new Leading(106, 12,
					12), new Leading(21, 12, 12)));
		}
		return jPanel3;
	}

	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jPanel4 = new JPanel();
			jPanel4.setBorder(BorderFactory.createTitledBorder(null,
					"Last four events:", TitledBorder.LEADING,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel4.setLayout(new GroupLayout());
			jPanel4.add(getJScrollPane0(), new Constraints(new Bilateral(1, 0,
					22), new Leading(-2, 76, 10, 10)));
		}
		return jPanel4;
	}

	private JScrollPane getJScrollPane0() {
		if (jScrollPane0 == null) {
			jScrollPane0 = new JScrollPane();
			jScrollPane0.setViewportView(getJList0());
		}
		return jScrollPane0;
	}

	private JList getJList0() {
		if (list == null) {
			list = new JList();
			DefaultListModel listModel = new DefaultListModel();
			listModel.addElement(" ");
			listModel.addElement(" ");
			listModel.addElement(" ");
			listModel.addElement(" ");
			list.setModel(listModel);
			list.setPreferredSize(new Dimension(10, 40));
		}
		return list;
	}

	private JLabel getJLabel8() {
		if (jLabel8 == null) {
			jLabel8 = new JLabel();
			jLabel8.setText("6. If something doesn't work, check this log:");
		}
		return jLabel8;
	}

	private JLabel getJLabel7() {
		if (jLabel7 == null) {
			jLabel7 = new JLabel();
			jLabel7.setText("5. In Minecraft connect to 'localhost'.");
		}
		return jLabel7;
	}

	private JButton getJButton0() {
		if (startButton == null) {
			startButton = new JButton();
			startButton.setText("Start");
			startButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					startButtonActionActionPerformed(event);
				}
			});
		}
		return startButton;
	}

	private JLabel getJLabel5() {
		if (jLabel5 == null) {
			jLabel5 = new JLabel();
			jLabel5.setText("4. Start this proxy:");
		}
		return jLabel5;
	}

	private JTextField getJTextField3() {
		if (serverProxyPort == null) {
			serverProxyPort = new JTextField();
			serverProxyPort.setText("25500");
		}
		return serverProxyPort;
	}

	private JLabel getJLabel4() {
		if (jLabel4 == null) {
			jLabel4 = new JLabel();
			jLabel4.setText("3. On which port should this program listening?");
		}
		return jLabel4;
	}

	private JTextField getJTextField2() {
		if (serverMinecraftPort == null) {
			serverMinecraftPort = new JTextField();
			serverMinecraftPort.setText("25565");
		}
		return serverMinecraftPort;
	}

	private JLabel getJLabel3() {
		if (jLabel3 == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("(if you haven't changed it, it is 25565)");
		}
		return jLabel3;
	}

	private JTextField getJTextField1() {
		if (clientPortToConnect == null) {
			clientPortToConnect = new JTextField();
			clientPortToConnect.setText("25500");
		}
		return clientPortToConnect;
	}

	private JTextField getJTextField0() {
		if (clientServerToConnect == null) {
			clientServerToConnect = new JTextField();
		}
		return clientServerToConnect;
	}

	private JLabel getJLabel6() {
		if (jLabel6 == null) {
			jLabel6 = new JLabel();
			jLabel6.setText("3. On which port is this program running on the server?");
		}
		return jLabel6;
	}

	private JPanel getClientQuestionsPanel() {
		if (clientQuestionsPanel == null) {
			clientQuestionsPanel = new JPanel();
			clientQuestionsPanel.setVisible(false);
			clientQuestionsPanel.setLayout(new GroupLayout());
			clientQuestionsPanel.add(getJLabel2(), new Constraints(new Leading(
					12, 12, 12), new Leading(0, 12, 12)));
			clientQuestionsPanel.add(getJTextField0(), new Constraints(
					new Leading(24, 209, 10, 10), new Leading(21, 12, 12)));
			clientQuestionsPanel.add(getJTextField1(), new Constraints(
					new Leading(24, 12, 12), new Leading(79, 12, 12)));
			clientQuestionsPanel.add(getJLabel6(), new Constraints(new Leading(
					12, 12, 12), new Leading(58, 12, 12)));
		}
		return clientQuestionsPanel;
	}

	private JRadioButton getJRadioButton1() {
		if (radioButtonNo == null) {
			radioButtonNo = new JRadioButton();
			radioButtonNo.setText("No");
			radioButtonNo.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					radioButtonNoActionActionPerformed(event);
				}
			});
		}
		return radioButtonNo;
	}

	private JPanel getOtherStepsPanel() {
		if (otherStepsPanel == null) {
			otherStepsPanel = new JPanel();
			otherStepsPanel.setLayout(new CardLayout());
			otherStepsPanel.add(getJPanel1(), "serverQuestionsPanel");
			otherStepsPanel.add(getClientQuestionsPanel(),
					"clientQuestionsPanel");
		}
		return otherStepsPanel;
	}

	private JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("2. On which port is the Minecraft server listening?");
		}
		return jLabel1;
	}

	private JLabel getJLabel2() {
		if (jLabel2 == null) {
			jLabel2 = new JLabel();
			jLabel2.setText("2. What is the address (without port) of the server?");
		}
		return jLabel2;
	}

	private JPanel getJPanel1() {
		if (serverQuestionsPanel == null) {
			serverQuestionsPanel = new JPanel();
			serverQuestionsPanel.setLayout(new GroupLayout());
			serverQuestionsPanel.add(getJLabel1(), new Constraints(new Leading(
					12, 12, 12), new Leading(0, 12, 12)));
			serverQuestionsPanel.add(getJLabel3(), new Constraints(new Leading(
					80, 12, 12), new Leading(23, 12, 12)));
			serverQuestionsPanel.add(getJTextField2(), new Constraints(
					new Leading(24, 12, 12), new Leading(21, 12, 12)));
			serverQuestionsPanel.add(getJLabel4(), new Constraints(new Leading(
					12, 12, 12), new Leading(58, 12, 12)));
			serverQuestionsPanel.add(getJTextField3(), new Constraints(
					new Leading(24, 12, 12), new Leading(79, 12, 12)));
		}
		return serverQuestionsPanel;
	}

	private JPanel getJPanel0() {
		if (jPanel0 == null) {
			jPanel0 = new JPanel();
			jPanel0.setLayout(new GroupLayout());
			jPanel0.add(getJLabel0(), new Constraints(new Leading(12, 12, 12),
					new Leading(15, 12, 10, 10)));
			jPanel0.add(getJRadioButtonYes(), new Constraints(new Leading(24,
					8, 8), new Leading(29, 8, 8)));
			jPanel0.add(getJRadioButton1(), new Constraints(new Leading(24, 8,
					8), new Leading(52, 8, 8)));
		}
		return jPanel0;
	}

	private JLabel getJLabel0() {
		if (jLabel0 == null) {
			jLabel0 = new JLabel();
			jLabel0.setText("1. Is the server running on this PC?");
		}
		return jLabel0;
	}

	private static void installLnF() {
		try {
			String lnfClassname = PREFERRED_LOOK_AND_FEEL;
			if (lnfClassname == null)
				lnfClassname = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(lnfClassname);
		} catch (Exception e) {
			System.err.println("Cannot install " + PREFERRED_LOOK_AND_FEEL
					+ " on this platform:" + e.getMessage());
		}
	}

	/**
	 * Main entry of the class. Note: This class is only created so that you can
	 * easily preview the result at runtime. It is not expected to be managed by
	 * the designer. You can modify it as you like.
	 */
	public static void main(String[] args) {
		installLnF();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ApplicationFrame frame = new ApplicationFrame();
				frame.setDefaultCloseOperation(ApplicationFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setPreferredSize(frame.getSize());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	private void startButtonActionActionPerformed(ActionEvent event) {
		server.setHosting(radioButtonYes.isSelected());
		final String target;
		final String targetPortString;
		final String portString;
		if (server.isHosting()) {
			target = "localhost";
			targetPortString = serverMinecraftPort.getText();
			portString = serverProxyPort.getText();
		} else {
			target = clientServerToConnect.getText();
			targetPortString = clientPortToConnect.getText();
			portString = MINECRAFT_DEFAULT_PORT_STRING;
		}
		server.startServer(target, targetPortString, portString);
	}

	private void stopButtonActionActionPerformed(ActionEvent event) {
		server.stop();
	}

	private void radioButtonYesActionActionPerformed(ActionEvent event) {
		server.setHosting(true);
	}

	private void radioButtonNoActionActionPerformed(ActionEvent event) {
		server.setHosting(false);
	}
}
