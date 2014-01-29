package test.unitario.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EJBException;

import junit.framework.TestCase;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.easymock.classextension.EasyMock;
import org.ibit.rol.sac.model.Archivo;
import org.ibit.rol.sac.model.DocumentTramit;
import org.ibit.rol.sac.model.Familia;
import org.ibit.rol.sac.model.Iniciacion;
import org.ibit.rol.sac.model.ProcedimientoLocal;
import org.ibit.rol.sac.model.Taxa;
import org.ibit.rol.sac.model.TraduccionDocumento;
import org.ibit.rol.sac.model.TraduccionIniciacion;
import org.ibit.rol.sac.model.TraduccionProcedimientoLocal;
import org.ibit.rol.sac.model.TraduccionTaxa;
import org.ibit.rol.sac.model.TraduccionTramite;
import org.ibit.rol.sac.model.TraduccionUA;
import org.ibit.rol.sac.model.Tramite;
import org.ibit.rol.sac.model.UnidadAdministrativa;
import org.ibit.rol.sac.persistence.ejb.TramiteFacadeEJB;
import org.junit.runner.RunWith;


import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.ibit.rol.sac.persistence.intf.AccesoManagerLocal;
import org.ibit.rol.sac.persistence.ws.Actualizador;

import test.unitario.util.LogSpy;



import es.caib.vuds.ActualizacionVudsException;
import es.caib.vuds.TramiteValidado;
import es.caib.vuds.ValidateVudsException;
import es.caib.vuds.VentanillaUnica;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Actualizador.class, VentanillaUnica.class})
@PowerMockIgnore("org.apache.commons.logging")  // para q no salga duplicate visibility error

public class TramiteFacadeEJB_UTest extends TestCase {

	Session session;
	AccesoManagerLocal accesoManager;
	LogSpy log;

	@Override
	protected void setUp() throws Exception {
		
		//mock ua
		UnidadAdministrativa ua=new UnidadAdministrativa();
		
		//mock session
		session = EasyMock.createMock(Session.class);
		EasyMock.expect(session.load(UnidadAdministrativa.class, 202L)).andReturn(ua);
		session.saveOrUpdate(EasyMock.anyObject());
		EasyMock.expectLastCall().anyTimes();
		session.flush();
		EasyMock.expectLastCall().anyTimes();
		EasyMock.replay(session);
		
		//mock accesoManager
		accesoManager = EasyMock.createMock(AccesoManagerLocal.class);
		EasyMock.expect(accesoManager.tieneAccesoTramite(EasyMock.anyLong())).andReturn(true).times(1);
		EasyMock.replay(accesoManager);

		// no funciona en junit4 _("test name="+getName());

		//establim un espia pel log i fer procedural verification
		log=new LogSpy();
		TramiteFacadeEJB.setLog(log);
	}
	
	// OK 18.10.2010
	public void testGrabarTramiteInternoNoValidaVuds() throws HibernateException {
		_("testGrabarTramiteInternoNoValidaVuds");
		// el siguiente test comprueba que al grabar un tramite interno de ventanilla unica no se
		// valida los campos vuds y no se envia a la ventanilla
		
		//obtenim el pare
		String nomTramit="tramit test "+new Date();
		
		//mock tramite
		final Tramite t=creaTramite(nomTramit,1,0,0,true,true);
		t.setId(101L);
		t.setValidacio(Tramite.INTERNO);
	
		//mock tramiteFacade		
		TramiteFacadeEJB tramiteBean = crearMockTramiteFacadeEJB(t);
				
		//mock actualitzador
		PowerMock.mockStaticStrict(Actualizador.class);
		
		String[] arr={"-VUDS"};
		Actualizador.actualizar(EasyMock.anyObject(),EasyMock.aryEq(arr));
		EasyMock.expectLastCall();
		
		arr=new String[0];
		List<String> list=new ArrayList<String>();
		list.add("VUDS");
		EasyMock.expect(Actualizador.actualizarSync(EasyMock.anyObject(),EasyMock.aryEq(arr))).andReturn(list);
				
		PowerMock.replay(Actualizador.class);

		
		try {
			Long tid=tramiteBean.grabarTramite(t,202L);
		} catch (ValidateVudsException e) {
			fail();
		} catch (ActualizacionVudsException e) {
			fail();
		}

	}
	
	

	// OK 18.10.2010
	public void testGrabarTramitePublicoSiValidaVuds() {
		_("testGrabarTramitePublicoSiValidaVuds");
		// el siguiente test prueba que al grabar un tramite publico de ventanilla unica no se
		// valida los campos vuds y se genera una excepcion.

		//obtenim el pare
		String nomTramit="tramit test "+new Date();
		
		//mock tramite
		final Tramite t=creaTramite(nomTramit,1,0,0,true,true);
		t.setId(101L);
		t.setValidacio(Tramite.PUBLICO);
	
		//mock tramiteFacade		
		TramiteFacadeEJB tramiteBean = crearMockTramiteFacadeEJB(t);
				
		//mock actualitzador
		List<String> list=new ArrayList<String>();
		list.add("VUDS");
		crearMockActualitzador(list);
		
		//mock ventanilla unica
		crearMockValidarTramiteVuds(new String[]{"nombre"});
		
		try {
			tramiteBean.grabarTramite(t,202L);
		} catch (ValidateVudsException e) {
			assertEquals("nombre",e.getCampsSenseValidar()[0]);
			return; 
		} catch (ActualizacionVudsException e) {
			fail();
		}
		fail();
	}
	
	public void testGrabarTramitePublicoSiEnviaVentanilla() {
		// el siguiente test comprueba que solo se envia el tramite a la ventanilla cuando
		// este es publico.
		// se pasa el test si no se genera ninguna excepcion, y en los logs se verifica
		// que se ha enviado a la ventnialla
		

		_("testGrabarTramitePublicoSiEnviaVentanilla");
		//obtenim el pare
		String nomTramit="tramit test "+new Date();
		
		//mock tramite
		final Tramite t=creaTramite(nomTramit,1,0,0,true,true);
		t.setId(101L);
		t.setValidacio(Tramite.PUBLICO);
	
		//mock tramiteFacade		
		TramiteFacadeEJB tramiteBean = crearMockTramiteFacadeEJB(t);
				
		//mock actualitzador
		List<String> list=new ArrayList<String>();
		list.add("VUDS");
		crearMockActualitzador(list);
		
		//mock validar tramite OK
		crearMockValidarTramiteVuds(new String[0]);  
		
		try {
			tramiteBean.grabarTramite(t,202L);
		} catch (ValidateVudsException e) {
			fail(); 
		} catch (ActualizacionVudsException e) {
			fail();
		}
		
		List<String> infoMsgs = log.getInfoMsgs();
		//tramite validado
		//tramite enviado a la ventanilla
		
		assertTrue(log.containsInfoMsg("tramite "+t.getId()+ " validado para la ventanilla"));
		assertTrue(log.containsInfoMsg("tramite "+t.getId()+ " enviado a la ventanilla"));
		
	}

	//OK 18.10.2010
	public void testGrabarTramiteInternoNoEnviaVentanilla() {
		_("testGrabarTramiteInternoNoEnviaVentanilla");
		// el siguiente test comprueba que un tramite interno no se envia a la ventanilla. 
		//  Se comprueba mirando que el campo setDataActualitzacioVuds esta vacio. 

		String nomTramit="tramit test "+new Date();
		
		//mock tramite
		final Tramite t=creaTramite(nomTramit,1,0,0,true,true);
		t.setId(101L);
		t.setValidacio(Tramite.INTERNO);
	
		//mock tramiteFacade		
		TramiteFacadeEJB tramiteBean = crearMockTramiteFacadeEJB(t);
			
		//mock actualitzador
		List<String> list=new ArrayList<String>();
		list.add("VUDS");
		crearMockActualitzador(list);
		
		PowerMock.replay(Actualizador.class);
		
		try {
			t.setDataActualitzacioVuds(null);
			tramiteBean.grabarTramite(t,202L);
			assertNull(t.getDataActualitzacioVuds());
		} catch (ValidateVudsException e) {
			fail();
		} catch (ActualizacionVudsException e) {
			fail();
		}
	}

	public void testGrabarTramitePublicoNoLlegaAVentanillaPeroSiOtrasAdmins() {
		//el siguiente test comprueba que un tramite publico de ventanilla no llega a la ventanilla y se
		//genera una excepcion, pero si se envia a las otras administraciones.
		//se comprueba que no llega a ventanilla porque se genera una excepcion de ActualizacionVudsException
		//se comprueba que si se envia a las otras admins pq se ejecuta actualiza()

		String nomTramit="tramit test "+new Date();
		
		//mock tramite
		Tramite t=creaTramite(nomTramit,1,0,0,true,true);
		t.setId(101L);
		t.setValidacio(Tramite.PUBLICO);
	
		//mock tramiteFacade		
		TramiteFacadeEJB tramiteBean = crearMockTramiteFacadeEJB(t);
		
				
		//mock actualitzador
		crearMockActualitzador(new ArrayList<String>());
		
		//mock validar tramite
		crearMockValidarTramiteVuds(new String[0]);  //el param Sttring[0] indica que se valida bien.

		try {
			t.setDataActualitzacioVuds(null);
			tramiteBean.grabarTramite(t,202L);
			assertNull(t.getDataActualitzacioVuds());
		} catch (ValidateVudsException e) {
			fail();
		} catch (ActualizacionVudsException e) {
			return;
		}
		fail();
	}
	
	

	
	// private -------------------------------------------------------

	private  TramiteFacadeEJB crearMockTramiteFacadeEJB(final Tramite t) {
		return new TramiteFacadeEJB() {
			@Override
			protected AccesoManagerLocal getAccesoManager() {
				return accesoManager;
			}

			public void ejbActivate() throws EJBException, RemoteException {}

			public void ejbPassivate() throws EJBException, RemoteException {}
			
			@Override
			protected Session getSession() {
				return session;
			}
			
			@Override
			public Tramite obtenerTramite(Long id) {
				return t;
			}
			
			@Override
			protected void close(Session session) {
			}
		}; 
	}
	

	//mock validar ventanilla unica
	private void crearMockValidarTramiteVuds(String[] sinTraducir) {
		PowerMock.mockStatic(VentanillaUnica.class);
		TramiteValidado tramval= new TramiteValidado();
		tramval.setSinTraducir(sinTraducir);
		EasyMock.expect(VentanillaUnica.validarTramiteVuds((Tramite)EasyMock.anyObject(),EasyMock.matches("es"))).andReturn(tramval);
		PowerMock.replay(VentanillaUnica.class);
	}
	
	private void crearMockActualitzador(List<String> syncReturn) {
		//primer actualitza async a tots menys vuds
		//segon actualitza sync nomes a vuds

		PowerMock.mockStaticStrict(Actualizador.class);
		String[] arr={"-VUDS"};
		Actualizador.actualizar(EasyMock.anyObject(),EasyMock.aryEq(arr));
		EasyMock.expectLastCall();
		arr=new String[]{"VUDS"};
		EasyMock.expect(Actualizador.actualizarSync(EasyMock.anyObject(),EasyMock.aryEq(arr))).andReturn(syncReturn);
		PowerMock.replay(Actualizador.class);
		
	}
	
	//public pq es crida en altres test cases
	public Tramite creaTramite(String nomTramit, int ntaxes, int ndocsInf, int nforms,
			boolean vuds, boolean traduir) {
	
		
		Tramite t=new Tramite();
		//t.setId(0);  si id=null -> hibernate=insert, sino hibernate=update
		
		
		TraduccionTramite tt=new TraduccionTramite();
		tt.setNombre(nomTramit);
		tt.setDescripcion("la meva descripcio");
		tt.setObservaciones("les meves observacions");
		tt.setPlazos("3 mesos");
		tt.setLugar("el meu lloc");
		tt.setRequisits("ser major de 18 anys");
		tt.setDocumentacion("-DNI -Llibre de familia");
		
		t.setTraduccion("ca", tt);
		ProcedimientoLocal procedimiento = new ProcedimientoLocal();
		procedimiento.setId(247L);
		procedimiento.setFamilia(new Familia());
		Iniciacion ini=new Iniciacion();
		ini.setId(0L);
		TraduccionIniciacion ti=new TraduccionIniciacion();
		ti.setNombre("ofici");
		ini.setTraduccion("ca", ti);
		procedimiento.setIniciacion(ini);
		procedimiento.setTaxa("on");
		if(vuds) procedimiento.setVentanillaUnica("1");
		
		TraduccionProcedimientoLocal trapro = new TraduccionProcedimientoLocal();
		trapro.setResultat("un resultat");
		procedimiento.setTraduccion("ca", trapro);

		UnidadAdministrativa oc = new UnidadAdministrativa(202L);
		TraduccionUA toc = new TraduccionUA();
		toc.setNombre("govern illes balears");
		oc.setTraduccion("ca", toc);
		procedimiento.setUnidadAdministrativa(oc);

		t.setProcedimiento(procedimiento);

		t.setValidacio(Tramite.PUBLICO);
		
		t.setCodiVuds("0001"); //new Date().toString());
		t.setDescCodiVuds("el meu codi vuds");
		
		Calendar c=Calendar.getInstance();
		c.set(2010,2,10);
		t.setDataCaducitat(c.getTime());
		c.set(2009,12,10);
	    t.setDataPublicacio(c.getTime());
		c.set(2010,2,10);
	    t.setDataActualitzacio(c.getTime());
	    
		
		Set<DocumentTramit> formularios=new HashSet<DocumentTramit>();
		/*
		for(int i=0; i<nforms; i++) {
			DocumentTramit f=new DocumentTramit();
			f.setTraduccion(idioma, traduccion)
			f.se
			f.setId((long)i);
			f.setUrl("la meva url");
			f.setArchivo(creaArchivo("el meu arxiu"+i));
			
		}
		*/
		t.setFormularios(formularios);
		
		
		Set<DocumentTramit> dinf=new HashSet<DocumentTramit>();
		t.setDocsInformatius(dinf);

		
/*	collections se ponen desde otro action	
		Set<Formulario> formularios=new HashSet<Formulario>();
		Formulario f=new Formulario();
		//f.setId(123L);
		f.setUrl("URL1");
		formularios.add(f);
		t.setFormularios(formularios);
		
		Set<String> docsPresentar = new HashSet<String>();
		docsPresentar.add("DOC1");
		t.setDocsPresentar(docsPresentar);

		Set<Documento> docsInformatius = new HashSet<Documento>();
		Documento docinf=new Documento();
		docinf.setId(333L);
		docsInformatius.add(docinf);
		t.setDocsInformatius(docsInformatius);
*/
		
		UnidadAdministrativa ocr = new UnidadAdministrativa(202L);
		TraduccionUA tua = new TraduccionUA();
		tua.setNombre("govern illes balears");
		ocr.setTraduccion("ca", tua);
		t.setOrganCompetent(ocr);


	
		Set<Taxa> taxes=new HashSet<Taxa>();
		for (int i=0; i<ntaxes; i++) {
			Taxa tx=new Taxa();
			TraduccionTaxa ttx=new TraduccionTaxa();
			ttx.setCodificacio("cod-00"+i);
			ttx.setDescripcio("descripcio tasa "+i);
			ttx.setFormaPagament("pagament tasa "+i);
			tx.setTraduccion("ca", ttx);
			taxes.add(tx);
		}
		t.setTaxes(taxes);
		
		
		// es comenta perque aquest id es posa al fer la crida a grabar()
		//UnidadAdministrativa oc = new UnidadAdministrativa(202L); 
		//t.setOrganCompetent(oc);
		
		// si vuds, traduir els camps
		if(vuds && traduir) {
			tt=new TraduccionTramite();
			tt.setNombre(nomTramit);
			tt.setDescripcion("mi descripcion");
			tt.setObservaciones("mis observaciones");
			tt.setPlazos("3 meses");
			tt.setLugar("mi sitio");
			tt.setRequisits("ser mayor de 18 anys");
			tt.setDocumentacion("-DNI -Libro de familia");
			t.setTraduccion("es", tt);			
		}
		
		return t;
	}
	
	private DocumentTramit creaDocument(String tit)  {
		DocumentTramit d=new DocumentTramit();
		TraduccionDocumento td=new TraduccionDocumento();
		td.setTitulo(tit);
		d.setTraduccion("ca", td);
		
		td=new TraduccionDocumento();
		td.setTitulo("english title");
		d.setTraduccion("en", td);
		
		return d;
	}
	
	private Taxa creaTaxa(String codi) {
		Taxa t=new Taxa();
		TraduccionTaxa tt=new TraduccionTaxa(); 
		tt.setCodificacio(codi);
		tt.setDescripcio("taxa de prova");
		tt.setFormaPagament("es gratis");
		t.setTraduccion("ca", tt);
		
		tt=new TraduccionTaxa(); 
		tt.setCodificacio("codi-001");
		tt.setDescripcio("test tax");
		tt.setFormaPagament("free");
		t.setTraduccion("en", tt);
		
		return t;
		
		
	}

	public  DocumentTramit creaDocument(String tit, Archivo a)  {
		DocumentTramit d=new DocumentTramit();
		TraduccionDocumento td=new TraduccionDocumento();
		td.setTitulo(tit);
		td.setArchivo(a);
		d.setTraduccion("ca", td);
		return d;
	}
	
	public Archivo creaArchivo(String name) throws IOException {
		Archivo archivo=new Archivo();
		
		InputStream is = ClassLoader.getSystemResourceAsStream(name);
		byte[] data=new byte[is.available()];
		is.read(data);
		is.close();
		
		archivo.setMime("text/plain");
	    archivo.setNombre(name);
	    archivo.setPeso(data.length);
	    archivo.setDatos(data);
		
		return archivo;
	}
	private void _(Object o){ System.out.println(o); }
}
 