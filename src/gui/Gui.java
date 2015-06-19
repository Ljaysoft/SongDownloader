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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import downloader.Downloader;
import downloader.DownloaderListener;
import settings.Settings;

public class Gui implements DownloaderListener {

	private JButton abortBtn;
	private JLabel currDownloadTitleLbl;
	private JLabel dlSpeedValue;
	private JButton downloadBtn;
	private JTextField downloadDirText;
	private JFileChooser fileChooser;
	private JLabel fileSizeValueLbl;
	private JFrame frmSongdownloader;
	private JLabel nbOfSongsText;
	private JLabel notFoundNbText;
	private int progress = 0;
	private JProgressBar progressBar;
	private File selectedFile;
	private JList<String> songList;
	private JTextField songListFilePathText;
	private DefaultListModel<String> songs;
	private JLabel songsDownloadedText;
	private JLabel songSizeUnitLbl;
	private JLabel totalDownloadedValueLbl;
	private JLabel totalSongsText;

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
		progress = 0;
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
			songsDownloadedText.setText(String.valueOf(progress));
			nbOfSongsText.setText(String.valueOf(parseSongList()));
			downloadBtn.setText("Download");
			downloadBtn.setEnabled(Downloader.isReady() && !songs.isEmpty());
			Downloader.stop();
			progressBar.setValue(Downloader.getProgress());
			notFoundNbText.setText("0");
			notFoundNbText.setForeground(Color.black);
			songList.updateUI();
		}
	}

	/**
	 * download
	 */
	private void download() {
		totalSongsText.setText(String.valueOf(songs.getSize()));
		downloadBtn.setEnabled(false);
		Downloader.downLoad(Arrays.asList(songs.toArray()));
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Downloader.setListener(this);

		frmSongdownloader = new JFrame();
		frmSongdownloader.setTitle("SongDownloader");
		frmSongdownloader.setBounds(100, 100, 487, 522);
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
					try {
						Runtime.getRuntime().exec(
								"explorer " + downloadDirText.getText());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		downloadBtn.setEnabled(false);
		downloadBtn.setBounds(10, 390, 145, 56);
		frmSongdownloader.getContentPane().add(downloadBtn);

		progressBar = new JProgressBar();
		progressBar.setBounds(165, 457, 306, 26);
		frmSongdownloader.getContentPane().add(progressBar);

		JLabel lblProgress = new JLabel("Progress:");
		lblProgress.setBounds(165, 432, 77, 14);
		frmSongdownloader.getContentPane().add(lblProgress);

		songsDownloadedText = new JLabel("0");
		songsDownloadedText.setHorizontalAlignment(SwingConstants.RIGHT);
		songsDownloadedText.setBounds(226, 432, 54, 14);
		frmSongdownloader.getContentPane().add(songsDownloadedText);

		JLabel slashLabel = new JLabel("/");
		slashLabel.setHorizontalAlignment(SwingConstants.LEFT);
		slashLabel.setBounds(282, 432, 15, 14);
		frmSongdownloader.getContentPane().add(slashLabel);

		totalSongsText = new JLabel("0");
		totalSongsText.setHorizontalAlignment(SwingConstants.LEFT);
		totalSongsText.setBounds(290, 432, 46, 14);
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
		abortBtn.setBounds(10, 457, 145, 26);
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
		lblNotFound.setBounds(346, 432, 67, 14);
		frmSongdownloader.getContentPane().add(lblNotFound);

		notFoundNbText = new JLabel("0");
		notFoundNbText.setHorizontalAlignment(SwingConstants.RIGHT);
		notFoundNbText.setBounds(414, 432, 57, 14);
		notFoundNbText.setForeground(Color.black);
		frmSongdownloader.getContentPane().add(notFoundNbText);

		JLabel lblDownloadSpeed = new JLabel("Download speed:");
		lblDownloadSpeed.setHorizontalAlignment(SwingConstants.LEFT);
		lblDownloadSpeed.setBounds(165, 411, 107, 14);
		frmSongdownloader.getContentPane().add(lblDownloadSpeed);

		dlSpeedValue = new JLabel("0");
		dlSpeedValue.setHorizontalAlignment(SwingConstants.RIGHT);
		dlSpeedValue.setBounds(261, 411, 55, 14);
		frmSongdownloader.getContentPane().add(dlSpeedValue);

		JLabel lblKbs = new JLabel("kBps");
		lblKbs.setBounds(317, 411, 46, 14);
		frmSongdownloader.getContentPane().add(lblKbs);

		JLabel lblDownloading = new JLabel("Downloading:");
		lblDownloading.setBounds(10, 365, 77, 14);
		frmSongdownloader.getContentPane().add(lblDownloading);

		currDownloadTitleLbl = new JLabel("");
		currDownloadTitleLbl.setHorizontalAlignment(SwingConstants.LEFT);
		currDownloadTitleLbl.setBounds(88, 364, 383, 22);
		frmSongdownloader.getContentPane().add(currDownloadTitleLbl);

		JLabel lblFileSizeLbl = new JLabel("Song size:");
		lblFileSizeLbl.setBounds(165, 390, 67, 14);
		frmSongdownloader.getContentPane().add(lblFileSizeLbl);

		fileSizeValueLbl = new JLabel("0");
		fileSizeValueLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		fileSizeValueLbl.setBounds(270, 390, 46, 14);
		frmSongdownloader.getContentPane().add(fileSizeValueLbl);

		songSizeUnitLbl = new JLabel("kB");
		songSizeUnitLbl.setBounds(318, 390, 46, 14);
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
		lblTotal.setBounds(346, 390, 35, 14);
		frmSongdownloader.getContentPane().add(lblTotal);

		totalDownloadedValueLbl = new JLabel("0");
		totalDownloadedValueLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		totalDownloadedValueLbl.setBounds(391, 390, 46, 14);
		frmSongdownloader.getContentPane().add(totalDownloadedValueLbl);

		JLabel lblMbs = new JLabel("MBps");
		lblMbs.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMbs.setBounds(436, 390, 35, 14);
		frmSongdownloader.getContentPane().add(lblMbs);
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
					songs.addElement(myLine);
				}
				bufRead.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return songs.getSize();
		}
		return 0;
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
		songList.updateUI();
	}

	public void onUpdateCurrentDownload(String title) {
		currDownloadTitleLbl.setText("<html>" + title + "</html>");
	}

	public void onUpdateSpeed() {
		dlSpeedValue.setText(String.valueOf(Downloader.getDownloadSpeed()));
		long currentSizeByte = Downloader.getCurrentFileSize();
		if (currentSizeByte / 1024 > 1000) {
			fileSizeValueLbl.setText(String.valueOf(currentSizeByte / 1048576)); // in mB																					// mB
			songSizeUnitLbl.setText("mB");
		} else {
			fileSizeValueLbl.setText(String.valueOf(Downloader.getCurrentFileSize() / 1024)); // in kB
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
