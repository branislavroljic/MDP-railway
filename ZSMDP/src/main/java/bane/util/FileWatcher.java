//package bane.util;
//
//import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.nio.file.FileSystems;
//import java.nio.file.Path;
//import java.nio.file.WatchEvent;
//import java.nio.file.WatchKey;
//import java.nio.file.WatchService;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import javafx.scene.control.TreeItem;
///*
// * FileWatcher prati promjene u korisnickom direktorijumu
// * Prilikom dodavanja novog fajla(od strane ReceiverThread-a) provjerava se ekstenzija
// * Ako je ektenzija .chat znaci da je fajl poruka, te se sadrzaj fajla upisuje u odgovorajuci fajl 
// * 	sa ekstenzijom .hchat koji sadrzi sve poruke primljene od datog korisnika
// */
//public class FileWatcher implements Runnable {
//
//	private WatchService watcher;
//	TreeItem<File> root;
//	private File userFolder;
//
//	public FileWatcher(File folder) throws IOException {
//		userFolder = folder;
//		this.watcher = FileSystems.getDefault().newWatchService();
//		folder.toPath().register(watcher, ENTRY_CREATE);
//		System.out.println("File watcher registred for dir: " + folder.getName());
//	}
//
//	@Override
//	public void run() {
//
//		while (true) {
//			WatchKey key;
//			try {
//				key = watcher.take();
//			} catch (InterruptedException ex) {
//				return;
//			}
//
//			for (WatchEvent<?> event : key.pollEvents()) {
//				Path filename = (Path) event.context();
//				System.out.println("Registred creation of file: " + filename);
//
//				// MainForm.receivedFiles.add(userFolder.toPath().resolve(filename).toFile());
//
//				if (filename.toFile().getName().endsWith(".chat")) {
//					try {
//						processFile(filename.toFile());
//					} catch (IOException e) {
//						Logger.getLogger(FileWatcher.class.getName()).log(Level.WARNING, null, e);
//					}
//					new File(userFolder + File.separator + filename.toString()).delete();
//				}
//			}
//
//			boolean valid = key.reset();
//			if (!valid) {
//				break;
//			}
//		}
//	}
//
//	private void processFile(File file) throws IOException {
//		String sender = file.getName().split("_")[0];
//
//		LocalDateTime time = LocalDateTime.now();
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH:mm:ss");
//		String dateTime = time.format(formatter);
//
//		// citam iz pridoslog fajla i sve upisujem u jedan fajl
//		BufferedReader in = new BufferedReader(
//				new InputStreamReader(new FileInputStream(userFolder + File.separator + file.getName())));
//		PrintWriter out = new PrintWriter(new BufferedWriter(
//				new OutputStreamWriter(new FileOutputStream(userFolder + File.separator + sender + ".hchat", true))));
//		String line = "";
//		boolean firstLine = true;
////		while ((line = in.readLine()) != null) {
////			out.println(line + "   " + (firstLine ? "[" + dateTime + "]" : ""));
////			firstLine = false;
////		}
//		while ((line = in.readLine()) != null) {
//			out.println(sender + "#" + line);
//			firstLine = false;
//		}
//		in.close();
//		out.close();
//	}
//
//}