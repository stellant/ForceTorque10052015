package ft;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;

import com.atiia.automation.sensors.NetFTRDTPacket;
import com.atiia.automation.sensors.NetFTSensor;

@SuppressWarnings("serial")
public class FT_Main extends JFrame {
	// Dimension variable to locate the frame
	private Dimension dimension;
	// Integer variable to dimension X
	private int x;
	// Integer variable to dimension Y
	private int y;
	// Label variable for IP Address
	private JLabel label_ipaddress;
	// Label variable for Low/Speed data reading
	private JLabel label_speed;
	// Label variable for file name
	private JLabel label_filename;
	// Button variable to connect, read data from the sensor
	private JButton button_connect;
	// Button variable to stop reading data and disconnect from the sensor.
	private JButton button_close;
	// Button variable to open file choose dialog
	private JButton button_new;
	// Text field variable to hold first part of IP Address
	private JTextField textfield_ipaddress1;
	// Text field variable to hold second part of IP Address
	private JTextField textfield_ipaddress2;
	// Text field variable to hold third part of IP Address
	private JTextField textfield_ipaddress3;
	// Text field variable to hold fourth part of IP Address
	private JTextField textfield_ipaddress4;
	// Checkbox variable to choose whether to read data in Low/High speed
	private JCheckBox checkbox_speed;
	private JCheckBox checkbox_logs;
	// Textarea variable to show the status in the UI
	private JTextArea textarea_status;
	// Scrollpane variable to hold textarea
	private JScrollPane scrollpane_status;
	// FileChooser variable to create a file chooser dialog
	private JFileChooser fileChooser;
	// NetFTSensor class variable to connect, create sockets, read data and stop
	// reading data
	private NetFTSensor sensor;
	// NetFTRDTPacket array variable to hold packets of data from the sensor
	private NetFTRDTPacket[] packets;
	// NetFTRDTPacket variable to hold single packet of data from the sensor
	private NetFTRDTPacket packet;
	// Datagram socket to read data in low speed
	private DatagramSocket slowDataSocket;
	// Datagram socket to read data in high speed
	private DatagramSocket fastDataSocket;
	// Thread variable to read data in low speed
	private Thread threadLowSpeed;
	// Thread variable to read data in high speed
	private Thread threadHighSpeed;
	// Thread variable to check file size to create new file
	private Thread fileWatcher;
	//Stringbuilder variable to construct ipaddress
	private StringBuilder ipaddress;
	//File Writer variable to write low speed into file
	private FileWriter fileWriter;
	//File Writer variable to write high speed into file
	private FileWriter fileWriterHighSpeed;
	//File Writer variable to write logs into file
	private FileWriter logWriter;
	//String variable to hold file path chosen using file chooser
	private String filePathDefault;
	//String variable to hold the file name with path
	private String filePath;
	//String variable holds file name chosen using file chooser
	private String fileName;
	//String variable holds new file name with date time with timezone
	private String fileNameNew;
	//Integer variable holds sample rate or number of packets sensor generates
	private int m_iRDTSampleRate;
	//Counts for Force and Torque
	private double[] m_daftCountsPerUnit = { 1, 1, 1, 1, 1, 1 }; 
	//Simple Date Format variable to format date time
	private SimpleDateFormat dateTimeZoneFormat;
	//Textfield to hold file name
	private JTextField textField_filename;
	//Textfield holds number of samples user wants
	private JTextField textfield_samples;
	private JLabel label;
	private JTextField textfield_frequency;

	/**
	 * @return Offset
	 */
	private String getCurrentTimezoneOffset() {

		TimeZone tz = TimeZone.getDefault();
		Calendar cal = GregorianCalendar.getInstance(tz);
		int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
		int hour = Integer.parseInt(String.format("%02d", Math.abs(offsetInMillis / 3600000)));
		int minute = Integer.parseInt(String.format("%02d", Math.abs((offsetInMillis / 60000) % 60)));
		String offset = (minute < 1) ? (hour + "") : (hour + "" + minute + "");
		offset = (offsetInMillis >= 0 ? "" : "-") + offset;
		return offset;
	}

	
	/**
	 * @return Date Time with Offset
	 */
	private String getDateTimeWithZone() {
		return dateTimeZoneFormat.format(new Date()) + "Tz" + getCurrentTimezoneOffset();
	}

	
	/**
	 *
	 */
	private void EnableConnect() {
		button_connect.setEnabled(true);
		button_close.setEnabled(false);
		checkbox_logs.setEnabled(true);
	}

	private void EnableClose() {
		button_connect.setEnabled(false);
		button_close.setEnabled(true);
		checkbox_logs.setEnabled(false);
	}

	/**
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws UnsupportedLookAndFeelException
	 */
	public FT_Main() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		dateTimeZoneFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		dateTimeZoneFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		getContentPane().setLayout(null);
		initComponents();
		getContentPane().add(label_ipaddress);
		getContentPane().add(label_speed);
		getContentPane().add(checkbox_speed);
		getContentPane().add(textfield_ipaddress1);
		getContentPane().add(textfield_ipaddress2);
		getContentPane().add(textfield_ipaddress3);
		getContentPane().add(textfield_ipaddress4);
		getContentPane().add(button_new);
		getContentPane().add(button_connect);
		getContentPane().add(button_close);
		getContentPane().add(label_filename);
		getContentPane().add(textField_filename);
		getContentPane().add(scrollpane_status);
		getContentPane().add(textfield_samples);
		textfield_samples.setText("1000");
		getContentPane().add(checkbox_logs);
		dimension = Toolkit.getDefaultToolkit().getScreenSize();
		x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
		y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
		checkbox_logs.setSelected(false);
		
		label = new JLabel("Frequency (ms)");
		label.setBounds(267, 35, 56, 10);
		getContentPane().add(label);
		
		
		getContentPane().add(textfield_frequency);
		if(!checkbox_logs.isSelected())
		{
			textarea_status.setEnabled(false);
		}
		button_close.setEnabled(false);
		this.setSize(398, 294);
		this.setTitle("Force / Torque Sensor - ATI");
		this.setLocation(x, y);
		this.setVisible(true);
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
	}

	/**
	 * @param filePath
	 * @return fileWriterHighSpeed
	 */
	private FileWriter getHighSpeedWriter(String filePath) {
		if (fileWriterHighSpeed == null) {
			try {
				fileWriterHighSpeed = new FileWriter(filePath, true);
			} catch (IOException e) {
				WriteLog(e.toString() + " at " + getDateTimeWithZone() + "\n");
			}
		}
		return fileWriterHighSpeed;
	}

	/**
	 * @param data
	 * @param date
	 */
	private void WriteHighSpeedData(String data, String date) {
		try {
			getHighSpeedWriter(filePath).write(data + "," + date + "\n");
			getHighSpeedWriter(filePath).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void CloseHighSpeedData() {
		try {
			getHighSpeedWriter(filePath).close();
			fileWriterHighSpeed = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param filePath
	 * @return fileWriter
	 */
	private FileWriter getWriter(String filePath) {
		if (fileWriter == null) {
			try {
				fileWriter = new FileWriter(filePath, true);
			} catch (IOException e) {
				if(checkbox_logs.isSelected())
				{
					WriteLog(e.toString() + " at " + getDateTimeWithZone() + "\n");
				}
			}
		}
		return fileWriter;
	}

	/**
	 * @param filePath
	 * @return logWriter
	 */
	private FileWriter getLogWriter(String filePath) {
		if (logWriter == null) {
			try {
				logWriter = new FileWriter(filePath, true);
			} catch (IOException e) {
				if(checkbox_logs.isSelected())
				{
					WriteLog(e.toString() + " at " + getDateTimeWithZone() + "\n");
				}
			}
		}
		return logWriter;
	}

	/**
	 * @param status
	 */
	private void WriteLog(String status) {
		try {
			getLogWriter("log.log").write(status + "\n");
			getLogWriter("log.log").flush();
			CloseLog();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void CloseLog() {
		try {
			getLogWriter("log.log").close();
			logWriter = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param data
	 * @param date
	 */
	private void WriteData(String data, String date) {
		try {
			getWriter(filePath).write(data + "," + date + "\n");
			getWriter(filePath).flush();
			CloseData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void CloseData() {
		try {
			getWriter(filePath).close();
			fileWriter = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();

		// kill threads here
	}

	/**
	 * 
	 */
	private void initComponents() {
		textfield_frequency = new JTextField();
		textfield_frequency.setText("100");
		textfield_frequency.setColumns(10);
		textfield_frequency.setBounds(273, 56, 41, 20);
		textfield_frequency.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent arg0) {

				if (textfield_frequency.getText().trim().equals("")) {
					textfield_frequency.setText("100");
				} 
			}

			public void focusGained(FocusEvent arg0) {

				textfield_frequency.setText("");
			}
		});
		checkbox_logs = new JCheckBox("Logs");
		checkbox_logs.setBounds(329, 55, 47, 23);
		checkbox_logs.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) 
			{
				if(checkbox_logs.isSelected())
				{
					textarea_status.setEnabled(true);
					textarea_status.setText("");
				}
				else
				{
					textarea_status.setEnabled(false);
					textarea_status.setText("");
				}
				
			}
		});
		label_ipaddress = new JLabel("Enter IP Address");
		label_ipaddress.setBounds(32, 35, 150, 10);
		label_speed = new JLabel("Samples");
		label_speed.setBounds(210, 35, 47, 10);
		checkbox_speed = new JCheckBox("High Speed");
		checkbox_speed.setBounds(32, 7, 79, 20);
		checkbox_speed.setSelected(true);
		checkbox_speed.setVisible(false);
		textfield_samples = new JTextField();
		textfield_samples.setBounds(210, 56, 41, 20);
		textfield_samples.setColumns(10);
		textfield_samples.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent arg0) {

				if (textfield_samples.getText().trim().equals("")) {
					textfield_samples.setText("1000");
				} else if (Integer.parseInt(textfield_samples.getText().trim()) > 1000) {
					textfield_samples.setText("1000");
				}
			}

			public void focusGained(FocusEvent arg0) {

				textfield_samples.setText("");
			}
		});

		textfield_ipaddress1 = new JTextField();
		textfield_ipaddress1.setBounds(32, 56, 30, 20);
		textfield_ipaddress1.setText("192");
		textfield_ipaddress1.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent arg0) {

				if (textfield_ipaddress1.getText().trim().equals("")) {
					textfield_ipaddress1.setText("192");
				} else if (Integer.parseInt(textfield_ipaddress1.getText().trim()) > 255) {
					textfield_ipaddress1.setText("192");
				}
			}

			public void focusGained(FocusEvent arg0) {

				textfield_ipaddress1.setText("");
			}
		});
		textfield_ipaddress2 = new JTextField();
		textfield_ipaddress2.setBounds(72, 56, 30, 20);
		textfield_ipaddress2.setText("168");
		textfield_ipaddress2.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent arg0) {

				if (textfield_ipaddress2.getText().trim().equals("")) {
					textfield_ipaddress2.setText("168");
				} else if (Integer.parseInt(textfield_ipaddress2.getText().trim()) > 255) {
					textfield_ipaddress2.setText("168");
				}
			}

			public void focusGained(FocusEvent arg0) {

				textfield_ipaddress2.setText("");
			}
		});
		textfield_ipaddress3 = new JTextField();
		textfield_ipaddress3.setBounds(112, 56, 30, 20);
		textfield_ipaddress3.setText("0");
		textfield_ipaddress3.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent arg0) {

				if (textfield_ipaddress3.getText().trim().equals("")) {
					textfield_ipaddress3.setText("0");
				} else if (Integer.parseInt(textfield_ipaddress3.getText().trim()) > 255) {
					textfield_ipaddress3.setText("0");
				}
			}

			public void focusGained(FocusEvent arg0) {

				textfield_ipaddress3.setText("");
			}
		});
		textfield_ipaddress4 = new JTextField();
		textfield_ipaddress4.setBounds(152, 56, 30, 20);
		textfield_ipaddress4.setText("8");
		textfield_ipaddress4.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent arg0) {

				if (textfield_ipaddress4.getText().trim().equals("")) {
					textfield_ipaddress4.setText("8");
				} else if (Integer.parseInt(textfield_ipaddress4.getText().trim()) > 255) {
					textfield_ipaddress4.setText("8");
				}
			}

			public void focusGained(FocusEvent arg0) {

				textfield_ipaddress4.setText("");
			}
		});
		button_new = new JButton("Browse...");
		button_new.setBounds(273, 117, 89, 23);
		button_new.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Enter File Name to Save...");
				int status = fileChooser.showSaveDialog(FT_Main.this);
				if (status == JFileChooser.APPROVE_OPTION) {
					filePathDefault = fileChooser.getCurrentDirectory().toString();
					fileName = fileChooser.getSelectedFile().getName();
					fileNameNew = fileName + "_" + getDateTimeWithZone() + ".csv";
					filePath = filePathDefault + "\\" + fileNameNew;
					WriteData("RDTSequence,FTSequence,Status,Fx,Fy,Fz,Tx,Ty,Tz", "TimeStamp");
					textField_filename.setText(filePath);
				}
			}
		});
		button_connect = new JButton("Connect");
		button_connect.setBounds(273, 152, 90, 37);
		button_connect.addActionListener(new ActionListener() {
			private boolean createFile;
			private int samples = 0;

			public void actionPerformed(ActionEvent arg0) {
				try {
					EnableClose();
					//checkbox_speed.setEnabled(false);
					if (!textfield_ipaddress1.getText().trim().equals("")
							&& !textfield_ipaddress2.getText().trim().equals("")
							&& !textfield_ipaddress3.getText().trim().equals("")
							&& !textfield_ipaddress4.getText().trim().equals("")
							&& !textField_filename.getText().trim().equals("")) {
						ipaddress = new StringBuilder();
						ipaddress.append(textfield_ipaddress1.getText().trim());
						ipaddress.append(".");
						ipaddress.append(textfield_ipaddress2.getText().trim());
						ipaddress.append(".");
						ipaddress.append(textfield_ipaddress3.getText().trim());
						ipaddress.append(".");
						ipaddress.append(textfield_ipaddress4.getText().trim());
						fileWatcher = new Thread() {

							public synchronized void run() {
								try {
									
									while (true && !this.isInterrupted()) {
										try {
											File f = new File(filePath);
											if (f.exists()) {
												long bytes = f.length();
												long kb = bytes / 1024;
												long mb = kb / 1024;
												if (mb > 50) {
													createFile = true;
//													fileNameNew = fileName + "_" + getDateTimeWithZone() + ".csv";
//													filePath = filePathDefault + "\\" + fileNameNew;
													// if(checkbox_speed.isSelected())
													// fileWriterHighSpeed = new
													// FileWriter(filePath,true);
													// else
													// fileWriter = new
													// FileWriter(filePath,true);
													// WriteData("RDTSequence,FTSequence,Status,Fx,Fy,Fz,Tx,Ty,Tz",
													// "TimeStamp");
												}
											}
										} catch (Exception ex) {
											System.out.println(ex.toString());
										}
										Thread.sleep(10000);
									}
								} catch (Exception ex) {
									System.out.println(ex.toString());
								}
							}
						};
						
						fileWatcher.start();
						if (!readConfigurationInfo(ipaddress.toString())) {
							if(checkbox_logs.isSelected())
							{
								textarea_status.replaceRange("", 0, textarea_status.getLineEndOffset(0));
								textarea_status.append("Cannot Read Configurations" + "\n");
							}
							EnableConnect();
							try
							{
								fileWatcher.interrupt();
							}
							catch(Exception ex)
							{
								
							}
							return;
						}
						
						if (checkbox_speed.isSelected() == false) {
							// For Low Speed
							if(checkbox_logs.isSelected())
							{
							textarea_status.removeAll();
							textarea_status.append("Initiating Read with Low Speed...\n");
							}
							sensor = new NetFTSensor(InetAddress.getByName(ipaddress.toString()), 49152);
							if (sensor != null) {
								if(checkbox_logs.isSelected())
								{
								textarea_status.append("Sensor Object Initiated...\n");
								}
							}
							slowDataSocket = sensor.initLowSpeedData();
							if (slowDataSocket != null) {
								if(checkbox_logs.isSelected())
								{
								textarea_status.append("Datagram Socket Created...\n");
								}
							}
							if(checkbox_logs.isSelected())
							{
							textarea_status.append("Starting Data Read...\n");
							}
							threadLowSpeed = new Thread() {

								@Override
								public synchronized void run() {

									synchronized (sensor) {
										while (true) {
											try {
												if (createFile) {
													fileNameNew = fileName + "_" + getDateTimeWithZone() + ".csv";
													filePath = filePathDefault + "\\" + fileNameNew;
													fileWriter = new FileWriter(filePath, true);
													WriteData("RDTSequence,FTSequence,Status,Fx,Fy,Fz,Tx,Ty,Tz",
															"TimeStamp");
													createFile = Boolean.FALSE;
												}
												packet = sensor.readLowSpeedData(slowDataSocket);
												if (packet != null) {
													String data = packet.toString(m_daftCountsPerUnit);
													WriteData(data, getDateTimeWithZone());
												}
											} catch (Exception ex) {
												try {
													if(checkbox_logs.isSelected())
													{
													textarea_status.replaceRange("", 0,
															textarea_status.getLineEndOffset(0));
													textarea_status.append("Cannot Read Data from Sensor...\n");
													}
												} catch (BadLocationException e) {
													e.printStackTrace();
												}
											} finally {
												if (fileWriter != null) {
													try {
														fileWriter.close();
													} catch (IOException e) {
														e.printStackTrace();
													}
												}
											}
											try {
												// keep it less
												this.sleep(100);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
									}

								}
							};
							if(checkbox_logs.isSelected())
							{
							textarea_status.append("Low Speed Reading Starts...");
							}
							threadLowSpeed.start();
						} else {
							// For High Speed
							if(checkbox_logs.isSelected())
							{
							textarea_status.removeAll();
							textarea_status.append("Initiating Read with High Speed...\n");
							}
							//NetFT sensor object to create read data from the 
							sensor = new NetFTSensor(InetAddress.getByName(ipaddress.toString()), 49152);
							if (sensor != null) {
								if(checkbox_logs.isSelected())
								{
								textarea_status.append("Sensor Object Initiated...\n");
								}
							}
							fastDataSocket = sensor.startHighSpeedDataCollection(0);
							if (fastDataSocket != null) {
								if(checkbox_logs.isSelected())
								{
								textarea_status.append("Datagram Socket Created...\n");
								}
							}
							if(checkbox_logs.isSelected())
							{
							textarea_status.append("Starting Data Read...\n");
							}
							threadHighSpeed = new Thread() {

								@Override
								public synchronized void run() {
									synchronized (sensor) {
										while (true && !this.isInterrupted()) {
											try {
												//int count = Math.max(m_iRDTSampleRate / 10, 1);
												int count = Integer.parseInt(textfield_samples.getText().toString());
												if (createFile) {
													fileNameNew = fileName + "_" + getDateTimeWithZone() + ".csv";
													filePath = filePathDefault + "\\" + fileNameNew;
													fileWriterHighSpeed = new FileWriter(filePath, true);
													WriteData("RDTSequence,FTSequence,Status,Fx,Fy,Fz,Tx,Ty,Tz",
															"TimeStamp");
													createFile = Boolean.FALSE;
												}
												packets = new NetFTRDTPacket[count];
												packets = sensor.readHighSpeedData(fastDataSocket, count);
												//System.out.println(packets.length);

												for (int i = 0; i < count; i++) {
													String data = packets[i].toString(m_daftCountsPerUnit);
													String date = getDateTimeWithZone();
													//System.out.println(data);
													//System.out.println(date);
													//System.out.println(i);
													WriteHighSpeedData(data, date);
												}
												CloseHighSpeedData();
											} catch (Exception ex) {
												try {
													if(checkbox_logs.isSelected())
													{
													textarea_status.replaceRange("", 0,
															textarea_status.getLineEndOffset(0));
													textarea_status.append("Cannot Read Data from Sensor...\n");
													}
												} catch (BadLocationException e) {
													if(checkbox_logs.isSelected())
													{
													WriteLog(e.toString() + " at " + getDateTimeWithZone() + "\n");
													}
												}
											} finally {

											}
											try {
												this.sleep(Integer.parseInt(textfield_frequency.getText().toString()));
											} catch (InterruptedException e) {
												if(checkbox_logs.isSelected())
												{
												WriteLog(e.toString() + " at " + getDateTimeWithZone() + "\n");
												}
											}
										}
									}

								}
							};
							if(checkbox_logs.isSelected())
							{
							textarea_status.append("High Speed Reading Starts...");
							}
							threadHighSpeed.start();
						}
					} else {
						if(checkbox_logs.isSelected())
						{
						textarea_status.append("IP Address or File Name Should Not Be Empty..." + "\n");
						}
						EnableConnect();
						//checkbox_speed.setEnabled(true);
						return;
					}
				} catch (Exception ex) {
					EnableConnect();
					//checkbox_speed.setEnabled(true);
					if (fileWatcher.isAlive()) {
						try {
							fileWatcher.interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				//	if (threadLowSpeed.isAlive()) {
						try {
							threadLowSpeed.interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
				//	}
				//	if (threadHighSpeed.isAlive()) {
						try {
							threadHighSpeed.interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
				//	}
						if(checkbox_logs.isSelected())
						{
					textarea_status.append(ex.toString() + "\n");
						}
				}
			}
		});
		button_close = new JButton("Close");
		button_close.setBounds(273, 200, 90, 39);
		button_close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					button_close.setEnabled(false);
					button_connect.setEnabled(true);
					//checkbox_speed.setEnabled(true);
					if (fileWatcher != null) {
						try {
							fileWatcher.interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (!checkbox_speed.isSelected()) {
						if (slowDataSocket != null && !slowDataSocket.isClosed()) {
							sensor.stopDataCollection(slowDataSocket);
							if(checkbox_logs.isSelected())
							{
							textarea_status.append("Socket Closed...\n");
							}
						}
						if (fileWriter != null) {
							fileWriter.close();
						}
						if (threadLowSpeed != null) {
							threadLowSpeed.interrupt();
						}
					} else {
						if (fastDataSocket != null && !fastDataSocket.isClosed()) {
							sensor.stopDataCollection(fastDataSocket);
							if(checkbox_logs.isSelected())
							{
							textarea_status.append("Socket Closed...\n");
							}
						}
						if (fileWriter != null) {
							fileWriter.close();
						}
						if (threadHighSpeed != null) {
							threadHighSpeed.interrupt();
						}
					}
				} catch (Exception ex) {
					button_close.setEnabled(true);
					button_connect.setEnabled(false);
					//checkbox_speed.setEnabled(false);
					if(checkbox_logs.isSelected())
					{
					textarea_status.append(ex.toString() + "\n");
					}
				}
			}
		});
		label_filename = new JLabel("Choose File Name");
		label_filename.setBounds(32, 97, 150, 10);
		textField_filename = new JTextField();
		textField_filename.setBounds(32, 118, 231, 20);
		textField_filename.setColumns(10);
		textField_filename.setEditable(false);
		scrollpane_status = new JScrollPane();
		scrollpane_status.setBounds(32, 152, 231, 87);
		textarea_status = new JTextArea();
		scrollpane_status.setViewportView(textarea_status);
		textarea_status.setLineWrap(true);
	}

	/**
	 * @param strUrlSuffix
	 * @param m_strSensorAddress
	 * @return strPageText
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private String readWebPageText(String strUrlSuffix, String m_strSensorAddress)
			throws MalformedURLException, IOException {
		BufferedReader cBufferedReader;
		String strURL = "http://" + m_strSensorAddress + "/" + strUrlSuffix;
		cBufferedReader = new BufferedReader(new InputStreamReader(new URL(strURL).openConnection().getInputStream()));
		String strPageText = "";
		String strCurLine;
		while (null != (strCurLine = cBufferedReader.readLine())) {
			strPageText += strCurLine;
		}
		return strPageText;
	}

	/**
	 * @param index
	 * @param ipAddress
	 * @return strXML
	 */
	private String readNetFTCalAPI(int index, String ipAddress) {
		try {
			String strXML = readWebPageText("netftcalapi.xml?index=" + index, ipAddress);
			return strXML;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * @param index
	 * @param ipAddress
	 * @return strXML
	 */
	private String readNetFTAPI(int index, String ipAddress) {
		try {
			String strXML = readWebPageText("netftapi2.xml?index=" + index, ipAddress);
			return strXML;
		} catch (Exception e) {
			WriteLog(e.toString());
			return "";
		}
	}

	/**
	 * @param xmlText
	 * @return activeConfig
	 */
	private int findActiveCFG(String xmlText) {
		String[] strret = xmlText.split("<setcfgsel>");
		String[] strret2 = strret[1].split("</setcfgsel>");
		int activeConfig = Integer.parseInt(strret2[0]);
		return activeConfig;
	}

	/**
	 * @param counts
	 */
	private void setCountsPerForce(double counts) {
		double dCountsPerForce = counts;
		if (0 == dCountsPerForce) {
			dCountsPerForce = 1;
		}
		int i;
		for (i = 0; i < 3; i++) {
			m_daftCountsPerUnit[i] = dCountsPerForce;
		}
	}

	/**
	 * @param counts
	 */
	private void setCountsPerTorque(double counts) {
		double dCountsPerTorque = counts;
		if (0 == dCountsPerTorque) {
			dCountsPerTorque = 1;
		}
		int i;
		for (i = 0; i < 3; i++) {
			m_daftCountsPerUnit[i + 3] = dCountsPerTorque;
		}
	}

	/**
	 * @param ipAddress
	 * @return status 
	 */
	private boolean readConfigurationInfo(String ipAddress) {
		try {
			String mDoc = readNetFTAPI(0, ipAddress);
			int activeConfig = findActiveCFG(mDoc);
			mDoc = readNetFTAPI(activeConfig, ipAddress);
			String[] parseStep1 = mDoc.split("<cfgcalsel>");
			String[] parseStep2 = parseStep1[1].split("</cfgcalsel>");
			String mCal = readNetFTCalAPI(Integer.parseInt(parseStep2[0]), ipAddress);
			mDoc = readNetFTAPI(activeConfig, ipAddress);
			parseStep1 = mDoc.split("<cfgcpf>");
			parseStep2 = parseStep1[1].split("</cfgcpf>");
			setCountsPerForce(Double.parseDouble(parseStep2[0]));
			parseStep1 = mDoc.split("<cfgcpt>");
			parseStep2 = parseStep1[1].split("</cfgcpt>");
			setCountsPerTorque(Double.parseDouble(parseStep2[0]));
			parseStep1 = mDoc.split("<comrdtrate>");
			parseStep2 = parseStep1[1].split("</comrdtrate>");
			m_iRDTSampleRate = (Integer.parseInt(parseStep2[0]));
		} catch (Exception e) {
			if(checkbox_logs.isSelected())
			{
			WriteLog(e.toString());
			}
			return false;
		}
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		FT_Main ftmain;
		try {
			ftmain = new FT_Main();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
}
