package bane.server;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ReportInterface extends Remote{

	boolean upload(byte[] bytes, String filename, String username) throws RemoteException;
	
	byte[] download(String filename) throws IOException;
	
	List<File> listFiles() throws RemoteException;
}
