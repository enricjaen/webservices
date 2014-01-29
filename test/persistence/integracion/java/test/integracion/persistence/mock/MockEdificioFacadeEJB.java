package test.integracion.persistence.mock;

import java.rmi.RemoteException;

import javax.ejb.EJBException;

import net.sf.hibernate.SessionFactory;

import org.ibit.rol.sac.persistence.ejb.EdificioFacadeEJB;
import org.ibit.rol.sac.persistence.intf.AccesoManagerLocal;

public class MockEdificioFacadeEJB extends EdificioFacadeEJB {

	public void ejbActivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub
		
	}
	public void ejbPassivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub
		
	}


	public void setSessionFactory(SessionFactory sf) { this.sessionFactory=sf;} 
	
	public void setAccesoManager(AccesoManagerLocal manager) { accesoManager=manager;}
	
	@Override
	public AccesoManagerLocal getAccesoManager() {
		// TODO Auto-generated method stub
		return accesoManager;
	}

	
	AccesoManagerLocal accesoManager;

}