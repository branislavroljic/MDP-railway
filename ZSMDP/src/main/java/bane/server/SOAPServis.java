/**
 * SOAPServis.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package bane.server;

public interface SOAPServis extends java.rmi.Remote {
    public boolean login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public boolean logout(java.lang.String username) throws java.rmi.RemoteException;
    public bane.model.User[] listOnlineUsers() throws java.rmi.RemoteException;
    public int getUserStationID(java.lang.String username) throws java.rmi.RemoteException;
}
