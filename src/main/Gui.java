package main;

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

public class Gui implements DownloaderListener {

	private JFrame frmSongdownloader;
	private JTextField songListFilePathText;
	private JFileChooser fileChooser;
	private JLabel nbOfSongsText;
	private File selectedFile;
	private DefaultListModel<String> songs;
	private JButton downloadBtn;
	private JList<String> songList;
	private JLabel songsDownloadedText;
	private JLabel totalSongsText;
	private JTextField downloadDirText;
	private JButton abortBtn;
	private JProgressBar progressBar;
	private JLabel notFoundNbText;
	private int progress = 0;

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
	 * Create the application.
	 */
	public Gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Downloader.setListener(this);
		
		frmSongdownloader = new JFrame();
		frmSongdownloader.setTitle("SongDownloader");
		frmSongdownloader.setBounds(100, 100, 487, 508);
		frmSongdownloader.setResizable(false);
		frmSongdownloader.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSongdownloader.getContentPane().setLayout(null);

		fileChooser = new JFileChooser(); 
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

		JLabel lblSongList = new JLabel("Song list:");
		lblSongList.setBounds(10, 8, 67, 35);
		frmSongdownloader.getContentPane().add(lblSongList);

		songListFilePathText = new JTextField();
		songListFilePathText.setAutoscrolls(true);
		songListFilePathText.setEditable(false);
		songListFilePathText.setBounds(87, 15, 265, 20);
		frmSongdownloader.getContentPane().add(songListFilePathText);
		songListFilePathText.setColumns(10);

		JButton btnFileChooser = new JButton("File");
		btnFileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooseSongListFile();
			}
		});
		btnFileChooser.setBounds(363, 11, 71, 32);
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
						Runtime.getRuntime().exec("explorer " + downloadDirText.getText());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		downloadBtn.setEnabled(false);
		downloadBtn.setBounds(10, 365, 145, 64);
		frmSongdownloader.getContentPane().add(downloadBtn);

		progressBar = new JProgressBar();
		progressBar.setBounds(169, 455, 302, 14);
		frmSongdownloader.getContentPane().add(progressBar);

		JLabel lblProgress = new JLabel("Progress:");
		lblProgress.setBounds(169, 437, 77, 14);
		frmSongdownloader.getContentPane().add(lblProgress);
		
		songsDownloadedText = new JLabel("0");
		songsDownloadedText.setHorizontalAlignment(SwingConstants.RIGHT);
		songsDownloadedText.setBounds(223, 437, 46, 14);
		frmSongdownloader.getContentPane().add(songsDownloadedText);
		
		JLabel slashLabel = new JLabel("/");
		slashLabel.setHorizontalAlignment(SwingConstants.LEFT);
		slashLabel.setBounds(273, 437, 46, 14);
		frmSongdownloader.getContentPane().add(slashLabel);
		
		totalSongsText = new JLabel("0");
		totalSongsText.setHorizontalAlignment(SwingConstants.LEFT);
		totalSongsText.setBounds(290, 437, 46, 14);
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
		
		JLabel downloadDirLabel = new JLabel("Download directory:");
		downloadDirLabel.setBounds(15, 330, 151, 24);
		frmSongdownloader.getContentPane().add(downloadDirLabel);
		
		downloadDirText = new JTextField("");
		downloadDirText.setAutoscrolls(true);
		downloadDirText.setEditable(false);
		downloadDirText.setBounds(151, 332, 320, 20);
		frmSongdownloader.getContentPane().add(downloadDirText);
		
		abortBtn = new JButton("Abort");
		abortBtn.setBounds(10, 440, 145, 29);
		abortBtn.setEnabled(false);
		abortBtn.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				Downloader.stop();	
				abortBtn.setEnabled(false);
				downloadBtn.setEnabled(true);
				downloadBtn.setText("Download");
			}
		});
		frmSongdownloader.getContentPane().add(abortBtn);
		
		JLabel lblNotFound = new JLabel("Not found:");
		lblNotFound.setBounds(365, 437, 81, 14);
		frmSongdownloader.getContentPane().add(lblNotFound);
		
		notFoundNbText = new JLabel("0");
		notFoundNbText.setHorizontalAlignment(SwingConstants.LEFT);
		notFoundNbText.setBounds(425, 437, 46, 14);
		notFoundNbText.setForeground(Color.black);
		frmSongdownloader.getContentPane().add(notFoundNbText);
	}

	/**
	 * download
	 */
	private void download() {
		totalSongsText.setText(String.valueOf(songs.getSize()));
		downloadBtn.setEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {

			public String getDescription() {
				return "Directory";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return false;
				}
			}
		});
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = fileChooser.showOpenDialog(frmSongdownloader);
		if (result == JFileChooser.APPROVE_OPTION) {
			String dir = fileChooser.getSelectedFile().getAbsolutePath();
			downloadDirText.setText(dir);			
			Downloader.downLoad(Arrays.asList(songs.toArray()), dir);
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

			public String getDescription() {
				return "Text Files (*.txt)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".txt");
				}
			}
		});;
		int result = fileChooser.showOpenDialog(frmSongdownloader);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			songListFilePathText.setText(selectedFile.getAbsolutePath());
			songsDownloadedText.setText(String.valueOf(progress));
			nbOfSongsText.setText(String.valueOf(parseSongList()));			
			downloadBtn.setEnabled(true);
			downloadBtn.setText("Download");
			Downloader.stop();
			progressBar.setValue(Downloader.getProgress());
			notFoundNbText.setText("0");
			notFoundNbText.setForeground(Color.black);
			songList.updateUI();
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
				while ( (myLine = bufRead.readLine()) != null) {
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
			downloadBtn.setText("<HTML><center>Done!<P>Downloaded files here.<center></HTML>");
			downloadBtn.setEnabled(true);
			abortBtn.setEnabled(false);
		} 
		songsDownloadedText.setText(String.valueOf(progress));
		int failedNumber = Downloader.getFailedNumber();
		notFoundNbText.setText(String.valueOf(failedNumber));
		if (failedNumber > 0) {
			notFoundNbText.setForeground(Color.red);
		}
		songList.updateUI();
	}
	
	private class SongListRenderer extends DefaultListCellRenderer {
		
		private static final long serialVersionUID = -6027297447224041122L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
														boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (Downloader.getFailedSongs().contains(value.toString())) {
            	setForeground(Color.red);
            } else {
            	setForeground(Color.black);
            }
            return(this);
        }
	}
}
