package bane.server;

public class SOAPServisProxy implements bane.server.SOAPServis {
  private String _endpoint = null;
  private bane.server.SOAPServis sOAPServis = null;
  
  public SOAPServisProxy() {
    _initSOAPServisProxy();
  }
  
  public SOAPServisProxy(String endpoint) {
    _endpoint = endpoint;
    _initSOAPServisProxy();
  }
  
  private void _initSOAPServisProxy() {
    try {
      sOAPServis = (new bane.server.SOAPServisServiceLocator()).getSOAPServis();
      if (sOAPServis != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sOAPServis)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sOAPServis)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sOAPServis != null)
      ((javax.xml.rpc.Stub)sOAPServis)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public bane.server.SOAPServis getSOAPServis() {
    if (sOAPServis == null)
      _initSOAPServisProxy();
    return sOAPServis;
  }
  
  public boolean login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException{
    if (sOAPServis == null)
      _initSOAPServisProxy();
    return sOAPServis.login(username, password);
  }
  
  public boolean logout(java.lang.String username) throws java.rmi.RemoteException{
    if (sOAPServis == null)
      _initSOAPServisProxy();
    return sOAPServis.logout(username);
  }
  
  public bane.model.User[] listOnlineUsers() throws java.rmi.RemoteException{
    if (sOAPServis == null)
      _initSOAPServisProxy();
    return sOAPServis.listOnlineUsers();
  }
  
  public int getUserStationID(java.lang.String username) throws java.rmi.RemoteException{
    if (sOAPServis == null)
      _initSOAPServisProxy();
    return sOAPServis.getUserStationID(username);
  }
  
  
}