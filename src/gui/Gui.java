package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import settings.Settings;
import downloader.Downloader;
import downloader.DownloaderListener;

public class Gui implements DownloaderListener {

	private JButton abortBtn;
	private JLabel currDownloadTitleLbl;
	private JLabel dlSpeedValue;
	private JButton downloadBtn;
	private JTextField downloadDirText;
	private JFileChooser fileChooser;
	private JLabel fileSizeValueLbl;
	private JFrame frmSongdownloader;
	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu mnAbout = new JMenu("About");
	private JMenu mnNewMenu;
	private JMenu mnNewMenu_1;
	private JMenu mnOption;
	private final JMenuItem mntmAbout = new JMenuItem("About");
	private final JMenuItem mntmLoadLastLog = new JMenuItem("Load Last Log");
	private JLabel nbOfSongsText;
	private JLabel notFoundNbText;
	private JMenuItem openDirectoryMenuItem;
	private JMenuItem openRecentLogMenuItem;
	private int progress = 0;
	private JProgressBar progressBar;
	private JMenuItem reloadListMenuItem;
	private JMenuItem resetPrefsMenuItem;
	private File selectedFile;
	private JList<String> songList;
	private JTextField songListFilePathText;
	private DefaultListModel<String> songs;
	private JLabel songsDownloadedText;
	private JLabel songSizeUnitLbl;
	private JLabel totalDownloadedValueLbl;
	private JLabel totalSongsText;
	private JLabel lblAlreadyOwn;
	private JLabel nbAlreadyOwnedValueLbl;

	/**
	 * Create the application.
	 */
	public Gui() {
		initialize();
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frmSongdownloader.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected void loadLastLog() {
		// TODO loadlastlog
	}

	/**
	 * choose Directory
	 */
	private void chooseOutputDirectory() {
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return false;
				}
			}

			@Override
			public String getDescription() {
				return "Directory";
			}
		});

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		String outPutDir = Settings.getOutPutDir();
		if (!outPutDir.isEmpty()) {
			fileChooser.setCurrentDirectory(new File(outPutDir));
		}
		int result = fileChooser.showOpenDialog(frmSongdownloader);
		if (result == JFileChooser.APPROVE_OPTION) {
			String dir = fileChooser.getSelectedFile().getAbsolutePath();
			downloadDirText.setText(dir);
			Downloader.setOutputDir(dir);
			downloadBtn.setEnabled(Downloader.isReady() && !songs.isEmpty());
			progressBar.setValue(Downloader.getProgress());
		}
	}

	/**
	 * chooseSongListFile
	 */
	private void chooseSongListFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".txt");
				}
			}

			@Override
			public String getDescription() {
				return "Text Files (*.txt)";
			}
		});
		;
		int result = fileChooser.showOpenDialog(frmSongdownloader);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			Settings.setInPutDir(selectedFile.getParent());
			songListFilePathText.setText(selectedFile.getAbsolutePath());
			nbOfSongsText.setText(String.valueOf(parseSongList()));
			Downloader.stop();
			Downloader.clearFails();
			loadList();
		}
	}

	/**
	 * download
	 */
	private void download() {
		totalSongsText.setText(String.valueOf(songs.getSize()));
		downloadBtn.setEnabled(false);
		Downloader.downLoadFromList(Arrays.asList(songs.toArray()));
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Downloader.setListener(this);

		frmSongdownloader = new JFrame();
		frmSongdownloader.setTitle("SongDownloader");
		frmSongdownloader.setBounds(100, 100, 487, 563);
		frmSongdownloader.setResizable(false);
		frmSongdownloader.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSongdownloader.getContentPane().setLayout(null);

		fileChooser = new JFileChooser();
		String inputDir = Settings.getInPutDir();
		if (!inputDir.isEmpty()) {
			fileChooser.setCurrentDirectory(new File(inputDir));
		} else {
			fileChooser.setCurrentDirectory(new File(System
					.getProperty("user.home")));
		}

		JLabel lblSongList = new JLabel("Song list:");
		lblSongList.setBounds(10, 8, 67, 35);
		frmSongdownloader.getContentPane().add(lblSongList);

		songListFilePathText = new JTextField();
		songListFilePathText.setAutoscrolls(true);
		songListFilePathText.setEditable(false);
		songListFilePathText.setBounds(88, 15, 306, 20);
		frmSongdownloader.getContentPane().add(songListFilePathText);
		songListFilePathText.setColumns(10);

		JButton btnFileChooser = new JButton("File");
		btnFileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooseSongListFile();
			}
		});
		btnFileChooser.setBounds(404, 15, 67, 20);
		frmSongdownloader.getContentPane().add(btnFileChooser);

		JLabel lblNewLabel = new JLabel("Number of songs:");
		lblNewLabel.setBounds(10, 46, 100, 14);
		frmSongdownloader.getContentPane().add(lblNewLabel);

		nbOfSongsText = new JLabel("0");
		nbOfSongsText.setBounds(120, 46, 71, 14);
		frmSongdownloader.getContentPane().add(nbOfSongsText);

		downloadBtn = new JButton("Download");
		downloadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (Downloader.getProgress() != 100) {
					download();
					downloadBtn.setText("Downloading...");
					abortBtn.setEnabled(true);
				} else {
					openOutputDir();
				}
			}
		});
		downloadBtn.setEnabled(false);
		downloadBtn.setBounds(10, 410, 145, 56);
		frmSongdownloader.getContentPane().add(downloadBtn);

		progressBar = new JProgressBar();
		progressBar.setBounds(165, 477, 306, 26);
		frmSongdownloader.getContentPane().add(progressBar);

		JLabel lblProgress = new JLabel("Progress:");
		lblProgress.setBounds(165, 452, 77, 14);
		frmSongdownloader.getContentPane().add(lblProgress);

		songsDownloadedText = new JLabel("0");
		songsDownloadedText.setHorizontalAlignment(SwingConstants.RIGHT);
		songsDownloadedText.setBounds(226, 452, 54, 14);
		frmSongdownloader.getContentPane().add(songsDownloadedText);

		JLabel slashLabel = new JLabel("/");
		slashLabel.setHorizontalAlignment(SwingConstants.LEFT);
		slashLabel.setBounds(282, 452, 15, 14);
		frmSongdownloader.getContentPane().add(slashLabel);

		totalSongsText = new JLabel("0");
		totalSongsText.setHorizontalAlignment(SwingConstants.LEFT);
		totalSongsText.setBounds(290, 452, 46, 14);
		frmSongdownloader.getContentPane().add(totalSongsText);

		songs = new DefaultListModel<String>();
		songs.ensureCapacity(100);
		songList = new JList<String>(songs);
		songList.setAutoscrolls(true);
		songList.setCellRenderer(new SongListRenderer());
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 71, 461, 248);
		scrollPane.setViewportView(songList);

		frmSongdownloader.getContentPane().add(scrollPane);

		JLabel downloadDirLabel = new JLabel("Save files to:");
		downloadDirLabel.setHorizontalAlignment(SwingConstants.LEFT);
		downloadDirLabel.setBounds(10, 330, 82, 24);
		frmSongdownloader.getContentPane().add(downloadDirLabel);

		downloadDirText = new JTextField("");
		downloadDirText.setAutoscrolls(true);
		downloadDirText.setEditable(false);
		downloadDirText.setBounds(88, 332, 241, 20);
		String outPutDir = Settings.getOutPutDir();
		if (!outPutDir.isEmpty()) {
			downloadDirText.setText(outPutDir);
			Downloader.setOutputDir(outPutDir);
		}
		frmSongdownloader.getContentPane().add(downloadDirText);

		abortBtn = new JButton("Stop");
		abortBtn.setBounds(10, 477, 145, 26);
		abortBtn.setEnabled(false);
		abortBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Downloader.stop();
				abortBtn.setEnabled(false);
				downloadBtn.setText("Download");
			}
		});
		frmSongdownloader.getContentPane().add(abortBtn);

		JLabel lblNotFound = new JLabel("Not found:");
		lblNotFound.setBounds(346, 452, 67, 14);
		frmSongdownloader.getContentPane().add(lblNotFound);

		notFoundNbText = new JLabel("0");
		notFoundNbText.setHorizontalAlignment(SwingConstants.RIGHT);
		notFoundNbText.setBounds(414, 452, 57, 14);
		notFoundNbText.setForeground(Color.black);
		frmSongdownloader.getContentPane().add(notFoundNbText);

		JLabel lblDownloadSpeed = new JLabel("Down speed:");
		lblDownloadSpeed.setHorizontalAlignment(SwingConstants.LEFT);
		lblDownloadSpeed.setBounds(165, 431, 100, 14);
		frmSongdownloader.getContentPane().add(lblDownloadSpeed);

		dlSpeedValue = new JLabel("0");
		dlSpeedValue.setHorizontalAlignment(SwingConstants.RIGHT);
		dlSpeedValue.setBounds(254, 431, 45, 14);
		frmSongdownloader.getContentPane().add(dlSpeedValue);

		JLabel lblKbs = new JLabel("kBps");
		lblKbs.setBounds(307, 431, 46, 14);
		frmSongdownloader.getContentPane().add(lblKbs);

		JLabel lblDownloading = new JLabel("Downloading:");
		lblDownloading.setBounds(10, 365, 77, 14);
		frmSongdownloader.getContentPane().add(lblDownloading);

		currDownloadTitleLbl = new JLabel("");
		currDownloadTitleLbl.setHorizontalAlignment(SwingConstants.LEFT);
		currDownloadTitleLbl.setBounds(88, 364, 383, 35);
		frmSongdownloader.getContentPane().add(currDownloadTitleLbl);

		JLabel lblFileSizeLbl = new JLabel("Song size:");
		lblFileSizeLbl.setBounds(165, 410, 67, 14);
		frmSongdownloader.getContentPane().add(lblFileSizeLbl);

		fileSizeValueLbl = new JLabel("0");
		fileSizeValueLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		fileSizeValueLbl.setBounds(254, 410, 46, 14);
		frmSongdownloader.getContentPane().add(fileSizeValueLbl);

		songSizeUnitLbl = new JLabel("kB");
		songSizeUnitLbl.setBounds(307, 410, 46, 14);
		frmSongdownloader.getContentPane().add(songSizeUnitLbl);

		JButton btnChooseDir = new JButton("Directory");
		btnChooseDir.setBounds(343, 331, 89, 23);
		btnChooseDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooseOutputDirectory();
			}
		});
		frmSongdownloader.getContentPane().add(btnChooseDir);

		JLabel lblTotal = new JLabel("Total:");
		lblTotal.setBounds(346, 410, 35, 14);
		frmSongdownloader.getContentPane().add(lblTotal);

		totalDownloadedValueLbl = new JLabel("0");
		totalDownloadedValueLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		totalDownloadedValueLbl.setBounds(391, 410, 57, 14);
		frmSongdownloader.getContentPane().add(totalDownloadedValueLbl);

		JLabel lblMbs = new JLabel("MB");
		lblMbs.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMbs.setBounds(436, 410, 35, 14);
		frmSongdownloader.getContentPane().add(lblMbs);

		lblAlreadyOwn = new JLabel("Already own:");
		lblAlreadyOwn.setBounds(346, 431, 86, 14);
		frmSongdownloader.getContentPane().add(lblAlreadyOwn);

		nbAlreadyOwnedValueLbl = new JLabel("0");
		nbAlreadyOwnedValueLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		nbAlreadyOwnedValueLbl.setBounds(425, 431, 46, 14);
		frmSongdownloader.getContentPane().add(nbAlreadyOwnedValueLbl);

		frmSongdownloader.setJMenuBar(menuBar);

		mnNewMenu = new JMenu("File");
		mnNewMenu.setHorizontalAlignment(SwingConstants.LEFT);
		menuBar.add(mnNewMenu);

		mnNewMenu_1 = new JMenu("Log");
		mnNewMenu.add(mnNewMenu_1);

		openRecentLogMenuItem = new JMenuItem("View Last Log");
		openRecentLogMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewLastLog();
			}
		});
		mnNewMenu_1.add(openRecentLogMenuItem);

		mntmLoadLastLog.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				loadLastLog();
			}
		});
		mnNewMenu_1.add(mntmLoadLastLog);

		reloadListMenuItem = new JMenuItem("Reload List");
		reloadListMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nbOfSongsText.setText(String.valueOf(parseSongList()));
				Downloader.stop();
				Downloader.clearFails();
				loadList();
			}
		});
		mnNewMenu.add(reloadListMenuItem);

		openDirectoryMenuItem = new JMenuItem("Open Directory");
		openDirectoryMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openOutputDir();
			}
		});
		mnNewMenu.add(openDirectoryMenuItem);

		mnOption = new JMenu("Option");
		menuBar.add(mnOption);

		resetPrefsMenuItem = new JMenuItem("Reset Preferences");
		resetPrefsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.reset();
			}
		});
		mnOption.add(resetPrefsMenuItem);

		menuBar.add(mnAbout);

		mnAbout.add(mntmAbout);
	}

	private void loadList() {
		progress = 0;
		songsDownloadedText.setText("0");
		nbOfSongsText.setText(String.valueOf(parseSongList()));
		downloadBtn.setText("Download");
		downloadBtn.setEnabled(Downloader.isReady() && !songs.isEmpty());
		Downloader.stop();
		progressBar.setValue(Downloader.getProgress());
		nbAlreadyOwnedValueLbl.setText(String.valueOf(Downloader
				.getNumberOwned()));
		nbAlreadyOwnedValueLbl.setForeground(Color.black);
		notFoundNbText.setText("0");
		notFoundNbText.setForeground(Color.black);
		onUpdateSpeed();
		onUpdateCurrentDownload("");
		songList.updateUI();
	}

	private void openOutputDir() {
		try {
			java.awt.Desktop.getDesktop().open(
					new File(Downloader.getOutputDir()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * parseSongList
	 */
	private int parseSongList() {
		if (selectedFile != null) {
			try {
				FileReader input = new FileReader(selectedFile);
				BufferedReader bufRead = new BufferedReader(input);
				String myLine = null;
				songs.clear();
				while ((myLine = bufRead.readLine()) != null) {
					if (!myLine.isEmpty()) {
						songs.addElement(myLine);
					}
				}
				bufRead.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return songs.getSize();
		}
		return 0;
	}

	private void viewLastLog() {
		File dir = new File(Downloader.getOutputDir());
		File[] logs = dir.listFiles(new java.io.FileFilter() {
			public boolean accept(File pathname) {
				String name = pathname.getName();
				String extension = name.substring(name.lastIndexOf(".") + 1,
						name.length());
				return pathname.isFile() && extension.equals("log");
			}
		});
		long lastMod = Long.MIN_VALUE;
		File theNewstLog = null;
		for (File log : logs) {
			if (log.lastModified() > lastMod) {
				theNewstLog = log;
				lastMod = log.lastModified();
			}
		}
		if (theNewstLog != null) {
			try {
				java.awt.Desktop.getDesktop().open(theNewstLog);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * callback from Downloader
	 */
	public void onFileDownloaded() {
		progress++;
		progressBar.setValue(Downloader.getProgress());
		if (Downloader.getProgress() == 100) {
			downloadBtn
					.setText("<HTML><center>Click here to view files.<center></HTML>");
			downloadBtn.setEnabled(true);
			abortBtn.setEnabled(false);
			onUpdateCurrentDownload("Download Done");
		} else {
			onUpdateCurrentDownload("");
		}
		songsDownloadedText.setText(String.valueOf(progress));
		int failedNumber = Downloader.getFailedNumber();
		notFoundNbText.setText(String.valueOf(failedNumber));
		if (failedNumber > 0) {
			notFoundNbText.setForeground(Color.red);
		}
		int alreadyOwn = Downloader.getNumberOwned();
		if (alreadyOwn > 0) {
			nbAlreadyOwnedValueLbl.setForeground(Color.green);
		}
		nbAlreadyOwnedValueLbl.setText(String.valueOf(alreadyOwn));
		songList.updateUI();
	}

	public void onUpdateCurrentDownload(String title) {
		currDownloadTitleLbl.setText("<html><div align=\"left\">" + title
				+ "</div></html>");
	}

	public void onUpdateSpeed() {
		dlSpeedValue.setText(String.valueOf(Downloader.getDownloadSpeed()));
		long currentSizeByte = Downloader.getCurrentFileSize();
		if (currentSizeByte / 1024 > 1000) { // in mB
			int remainder = (int) (currentSizeByte % 1048576 * 100 / 1048576);
			fileSizeValueLbl
					.setText(String.valueOf(currentSizeByte / 1048576)
							+ "."
							+ (remainder == 0 ? "00" : String
									.valueOf(remainder)));
			songSizeUnitLbl.setText("mB");
		} else {
			fileSizeValueLbl.setText(String.valueOf(Downloader
					.getCurrentFileSize() / 1024)); // in kB
			songSizeUnitLbl.setText("kB");
		}

		totalDownloadedValueLbl.setText(String.valueOf(Downloader
				.getTotalSize() / 1048576));
	}

	private class SongListRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -6027297447224041122L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if (Downloader.getFailedSongs().contains(value.toString())) {
				setForeground(Color.red);
			} else {
				setForeground(Color.black);
			}
			return (this);
		}
	}
}
