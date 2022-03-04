package bane.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import bane.model.ReportMetadata;

//RMI sistem za upload/download i pregled izvjestaja
public class ReportServer implements ReportInterface {

	private static String REPORTS_FOLDER;
	private static String SERCER_POLICY_FILE;
	private static int PORT;
	private static String PDF_EXTENSION;
	private static String RMI_SERVER_NAME;

	public static String ROOT_RESOURCE = "./resources/";

	private File rootFolder;

	public ReportServer() {

		rootFolder = new File(REPORTS_FOLDER);
		if (!rootFolder.exists())
			rootFolder.mkdir();
	}

	//ucitavanje konfiguracionih parametara iz fajla
	public static void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "config.properties");

		prop.load(input);

		String value = prop.getProperty("REPORTS_FOLDER");
		if (value != null) {
			REPORTS_FOLDER = value;
		} else {
			throw new MissingFormatArgumentException("REPORTS_FOLDER nije definisan u config fajlu.");
		}

		value = prop.getProperty("SERCER_POLICY_FILE");
		if (value != null) {
			SERCER_POLICY_FILE = value;
		} else {
			throw new MissingFormatArgumentException("SERCER_POLICY_FILE nije definisan u config fajlu.");
		}

		value = prop.getProperty("PORT");
		if (value != null) {
			PORT = Integer.parseInt(value);
		} else {
			throw new MissingFormatArgumentException("PORT nije definisan u config fajlu.");
		}

		value = prop.getProperty("PDF_EXTENSION");
		if (value != null) {
			PDF_EXTENSION = value;
		} else {
			throw new MissingFormatArgumentException("PDF_EXTENSION nije definisan u config fajlu.");
		}

		value = prop.getProperty("RMI_SERVER_NAME");
		if (value != null) {
			RMI_SERVER_NAME = value;
		} else {
			throw new MissingFormatArgumentException("RMI_SERVER_NAME nije definisan u config fajlu.");
		}

		input.close();
	}

	//upload PDF fajla
	@Override
	public boolean upload(byte[] bytes, String filename, String username) throws RemoteException, Exception {
		if (!filename.endsWith(PDF_EXTENSION))
			throw new IllegalArgumentException("Format fajla nije ispravan!");
		
		if (new File(rootFolder + File.separator + filename).exists())
			throw new Exception("Fajl sa datim imenom vec postoji!");

		try (FileOutputStream out = new FileOutputStream(new File(rootFolder + File.separator + filename))) {
			out.write(bytes);
			serializeGsonInfoFile(username, filename, bytes.length);
			return true;
		} catch (IOException e) {
			Logger.getLogger(ReportServer.class.getName()).log(Level.WARNING, null, e);
			return false;
		}
	}

	//download fajla
	@Override
	public byte[] download(String filename) throws IOException {
		return Files.readAllBytes(Paths.get(rootFolder + File.separator + filename));
	}

	//serijalizacija metapodataka predstavljenih klasom ReportMetadata u json fajl: imafajla_datumVrijeme.json
	private void serializeGsonInfoFile(String username, String filename, int length) throws IOException {
		Gson gson = new Gson();

		LocalDateTime dateTime = LocalDateTime.now();
		String formattedDate = dateTime.format(ReportMetadata.dateTimeFormatter);
		// reports/imefajla_datum_vrijeme.json
		FileWriter out = new FileWriter(
				new File(REPORTS_FOLDER + File.separator + getFileName(filename) + "_" + formattedDate + ".json"));

		ReportMetadata metadata = new ReportMetadata(username, formattedDate, length);
		out.write(gson.toJson(metadata));
		out.close();
	}

	//ime fajla bez ekstenzije
	private String getFileName(String filename) {
		if (filename.indexOf(".") > 0) {
			return filename.substring(0, filename.lastIndexOf("."));
		} else {
			return filename;
		}
	}

	@Override
	public List<File> listFiles() throws RemoteException {
		return Arrays.asList(rootFolder.listFiles());
	}

	public static void main(String args[]) {
		try {
			loadConfig();
		} catch (MissingFormatArgumentException | IOException e) {
			Logger.getLogger(ReportServer.class.getName()).log(Level.WARNING, null, e);
		}
		//podesavanje Security parametara
		System.setProperty("java.security.policy", SERCER_POLICY_FILE);
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			//"objavljivanje"
			ReportServer server = new ReportServer();
			ReportInterface stub = (ReportInterface) UnicastRemoteObject.exportObject(server, 0);
			Registry registry = LocateRegistry.createRegistry(PORT);
			registry.rebind(RMI_SERVER_NAME, stub);
			System.out.println("AZSMDP server started...");
		} catch (Exception e) {
			Logger.getLogger(ReportServer.class.getName()).log(Level.WARNING, null, e);
		}
	}
}
