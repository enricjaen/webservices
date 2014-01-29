package test.unitario.ws;

import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.easymock.classextension.EasyMock;
import org.ibit.rol.sac.model.Destinatario;
import org.ibit.rol.sac.model.Edificio;
import org.ibit.rol.sac.model.Familia;
import org.ibit.rol.sac.model.Formulario;
import org.ibit.rol.sac.model.ProcedimientoLocal;
import org.ibit.rol.sac.model.TraduccionTramite;
import org.ibit.rol.sac.model.Tramite;
import org.ibit.rol.sac.model.UnidadAdministrativa;
import org.ibit.rol.sac.model.Tramite.Operativa;
import org.ibit.rol.sac.persistence.delegate.DestinatarioDelegate;
import org.ibit.rol.sac.persistence.ws.Actualizador;
import org.ibit.rol.sac.persistence.ws.ReportarFallo;
import org.ibit.rol.sac.persistence.ws.Actualizador.ActualizadorThread;
import org.ibit.rol.sac.persistence.ws.invoker.WSInvocatorException;
import org.powermock.api.mockito.PowerMockito;

import test.unitario.util.LogSpy;



import es.caib.vuds.DestinatarioVuds;
import es.caib.vuds.VentanillaUnica;


/**
 * Esta clase es un test unitario del Actualizador, asi que el resto de clases dependendintes (ReportarFallo, web services..)
 * se simulan. Para testear todo hay que ejecutar un test de integracion.
 * @author enric
 *
 */
public class ActualizadorTest extends junit.framework.TestCase {

	DestinatarioDelegate destDelegateMock = EasyMock.createMock(DestinatarioDelegate.class);
	Actualizador actualitzador=new Actualizador();

	VentanillaUnica vuds = EasyMock.createMock(VentanillaUnica.class);
	
	List<Destinatario> destinatarios=new ArrayList<Destinatario>();
	Destinatario dest;
	
	@Override
	protected void setUp() throws Exception {
		
		String vudsEP = "http://epreinf45:18080/axis2/services/sistemaInformacionWS";
		dest=new DestinatarioVuds();
		dest.setNombre("VUDS");
		dest.setEndpoint(vudsEP);
		dest.setEmail("bustia@rolsac");
		destinatarios.add(dest);
		
		dest=new Destinatario();
		dest.setNombre("otro");
		destinatarios.add(dest);
		 
		dest=new Destinatario();
		dest.setNombre("otromas");
		destinatarios.add(dest);
		
		EasyMock.expect(destDelegateMock.listarDestinatarios()).andReturn(destinatarios).times(2);
		EasyMock.replay(destDelegateMock);
		Actualizador.setDestDelegate(destDelegateMock);
		Actualizador.setVuds(vuds);		// FIXME setVuds??
	}
	
	
	public void testActualizarTramiteSinParamsAsincronamentATodosDestinatarios() throws InterruptedException  {
		//el siguiente test comprueba que se envia un tramite a todos los destinatarios
		//	se pasa el test si el array devuelto contiene a todos los destinatarios

		Tramite t=new Tramite();
		Actualizador.actualizar(t);
		while(null==Actualizador.getThreadActualizador());
		Actualizador.getThreadActualizador().join();
		List<String>destOK = Actualizador.getThreadActualizador().getDestinatarisOK();
		assertEquals(3,destOK.size());
	}
	

	public void testActualizarTramiteConParamsAsincronamentATodosDestinatarios() throws InterruptedException  {
		//el siguiente test comprueba que se envia un tramite a todos los destinatarios 
		//pasando un parametro.
		//	se pasa el test si el array devuelto contiene a todos los destinatarios

		//PowerMockito.spy(org.apache.commons.logging.Log.class);
		//PowerMockito.replace(PowerMockito.method(org.apache.commons.logging.Log.class, "info")).
		//				with(PowerMockito.method(MyLog.class, "info"));

		LogSpy log=new LogSpy();
		Actualizador.setLog(log);
		
		Tramite t=new Tramite();
		long ua=202;
		Actualizador.actualizar(t,ua);
		while(null==Actualizador.getThreadActualizador());
		Actualizador.getThreadActualizador().join();
		List<String>destOK = Actualizador.getThreadActualizador().getDestinatarisOK();
		assertEquals(3,destOK.size());
		
		_(Arrays.toString(log.getInfoMsgs().toArray()));
	}

	
	public void testBorrarEdificioConParamsATodosDestinatarios()  throws InterruptedException  {
		//el siguiente test comprueba que se envia un borrar edificio a todos los destinatarios 
		//pasando un parametro.
		//	se pasa el test si el array de destinatarios enviados contiene a todos los destinatarios

		
			
		Edificio e=new Edificio();
		long ua=202;
		Actualizador.borrar(e,ua);
		while(null==Actualizador.getThreadActualizador());
		Actualizador.getThreadActualizador().join();
		List<String>destOK = Actualizador.getThreadActualizador().getDestinatarisOK();
		assertEquals(3,destOK.size());
	}
	
	public void testTODO_ActualizarEdificioConParamsAsincronamentATodos() throws InterruptedException  {
		//TODO
	}


	
	public void testActualizarTramiteAsincronamentATodosMenosVentanilla() throws InterruptedException  {
		//el siguiente test comprueba que se envia un tramite a todos los destinatarios menos VUDS
		//	se pasa el test si el array devuelto contiene a todos menos VUDS

		Tramite t=new Tramite();
		Actualizador.actualizar(t,new String[]{"-VUDS"});
		while(null==Actualizador.getThreadActualizador());
		Actualizador.getThreadActualizador().join();
		List<String>destOK = Actualizador.getThreadActualizador().getDestinatarisOK();
		assertEquals(2,destOK.size());
	}

	
	
	public void testActualizarTramiteAsincronamentSoloVentanilla() throws InterruptedException  {
		//el siguiente test comprueba que se envia un tramite solo a VUDS
		// se test pasa si el array devuelto contiene solo a VUDS
		
		Tramite t=new Tramite();
		Actualizador.actualizar(t,new String[]{"VUDS"});
		while(null==Actualizador.getThreadActualizador());
		Actualizador.getThreadActualizador().join();
		List<String>destOK = Actualizador.getThreadActualizador().getDestinatarisOK();
		assertEquals(1,destOK.size());
		assertSame("VUDS", destOK.get(0));
	}

	
	//el siguiente test comprueba que se envia sincrono un tramite a todos los destinatarios
	//	se pasa el test si el array devuelto contiene a todos los destinatarios 
	
	//el siguiente test comprueba que se envia sincrono un tramite a todos menos al destinatario VUDS
	//	se pasa el test si el array devuelto contiene a todos menos VUDS 
	
	//el siguiente test comprueba que se envia sincrono un tramite solo a VUDS
	// se pasa el test si el array devuelto contiene solo a VUDS
	
	
	
	public void testFiltreDestinatariosNomesVuds() {
		List<Destinatario> list=Actualizador.filtreDestinataris(destinatarios,new String[]{"VUDS"});
		assertEquals(1,list.size());
		Destinatario d = new Destinatario();
		d.setNombre("VUDS");
		assertEquals(d,list.get(0));
	}
	
	public void testFiltreDestinatariosTodosMenosVuds() {
		List<Destinatario> list=Actualizador.filtreDestinataris(destinatarios,new String[]{"-VUDS"});
		assertEquals(2,list.size());

		Destinatario d = new Destinatario();
		d.setNombre("VUDS");
		assertTrue(0>Collections.binarySearch(list, d));

	}
	
	public void testFiltreDestinatariosSoloOtroyNoVuds() {
		List<Destinatario> list=Actualizador.filtreDestinataris(destinatarios,new String[]{"otro","-VUDS"});
		assertEquals(1,list.size());
		Destinatario d = new Destinatario();
		d.setNombre("otro");
		assertEquals(d,list.get(0));
	}
	
	
	
	public void testFiltreDestinatariosTodos() {
		List<Destinatario> list=Actualizador.filtreDestinataris(destinatarios,new String[0]);
		assertEquals(3,list.size());
	}
	
	public void testFiltreDestinatariosTodos2() {
		List<Destinatario> list=Actualizador.filtreDestinataris(destinatarios,null);
		assertEquals(3,list.size());
	}
	
	private void _(Object o)  { System.out.println(o); }
	
}
