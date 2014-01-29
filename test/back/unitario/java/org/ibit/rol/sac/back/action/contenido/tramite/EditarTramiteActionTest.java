package org.ibit.rol.sac.back.action.contenido.tramite;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.struts.action.RequestProcessor;
import org.easymock.classextension.EasyMock;
import org.ibit.rol.sac.back.form.DocumentoForm;
import org.ibit.rol.sac.back.form.ProcedimientoForm;
import org.ibit.rol.sac.back.form.TraDynaValidatorForm;
import org.ibit.rol.sac.back.form.TramiteForm;
import org.ibit.rol.sac.back.negocio.GrabadorProcedimiento;
import org.ibit.rol.sac.back.negocio.vuds.TramiteVudsInvalido;
import org.ibit.rol.sac.back.negocio.vuds.TramiteVudsSinEnviar;
import org.ibit.rol.sac.model.Documento;
import org.ibit.rol.sac.model.Materia;
import org.ibit.rol.sac.model.ProcedimientoLocal;
import org.ibit.rol.sac.model.Taxa;
import org.ibit.rol.sac.model.TraduccionMateria;
import org.ibit.rol.sac.model.TraduccionTaxa;
import org.ibit.rol.sac.model.TraduccionTramite;
import org.ibit.rol.sac.model.Tramite;
import org.ibit.rol.sac.persistence.delegate.DelegateException;
import org.ibit.rol.sac.persistence.delegate.IdiomaDelegate;
import org.springframework.mock.web.MockHttpServletRequest;

import es.caib.test.common.LogSpy;
import es.caib.test.common.RolsacMockFactory;

import servletunit.struts.MockStrutsTestCase;
import test.unitario.back.mock.MockGrabadorTramite;
import test.unitario.back.mock.MockMateriaDelegate;
import test.unitario.back.mock.MockProcedimientoDelegate;
import test.unitario.back.mock.MockTramiteDelegate;


/**
 * INFO: utiliza el sac-back-messages.properties por defecto (o sea "en");
 * @author u92770
 *
 */

public class EditarTramiteActionTest extends MockStrutsTestCase {

    LogSpy spy = new LogSpy();
	private Tramite tramite;
	private MockHttpServletRequest request;

	
	public EditarTramiteActionTest(String s) {
		super(s);
		}
	
	public void setUp() { try {
		super.setUp();
        setInitParameter("validating","false");
    	establecerMockIdiomaDelegate();
    	System.setProperty("es.caib.rolsac.idiomaDefault", "ca");

        spy = new LogSpy();
        //SeleccionarProcedimientoAction.log = spy;
  

	} catch (Exception e) {
		e.printStackTrace();
	} }

	private void establecerMockIdiomaDelegate() throws DelegateException {
		IdiomaDelegate idiomaDelegate = EasyMock.createMock(IdiomaDelegate.class);
    	String[] langs={"ca","es","en","de"};
		EasyMock.expect(idiomaDelegate.listarLenguajes()).andReturn(Arrays.asList(langs)).times(6);
		EasyMock.replay(idiomaDelegate);
    	DocumentoForm.idiomaDelegate = idiomaDelegate;
    	
    	TraDynaValidatorForm.idiomaDelegate = idiomaDelegate;
	}

    public void tearDown() { try {
		super.tearDown();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} }

   class ParametersBase {
	   String action;
   }

	class ParametresSeleccionarProcediment extends ParametersBase {
		String idSelect;
	}
    
    class AfegirTramiteParameters extends ParametersBase {
    	String idProcedimiento;
	}
    
    public class ParametresRelacionarMateria extends ParametersBase {
		public String procedimiento;
		public String materia;
		public String operacion;
	}


    public void test01CrearTramiteNoVUDSNoActualizaVUDS() throws DelegateException {

    	AfegirTramiteParameters params = prepararTestVerificarEstado(false); 

		    	
        actionPerform(); 
        verifyNoActionErrors();
        verifyForward("success");
        
        
        //verificaParamsFormularioRespuesta(params, request);
        
        
     }

	

    /*
     * en aquest test, tots els camps del tramite estan traduits al castella
     */
    public void test02VerificarEstadoPendienteAlCrearTramiteVUDSActualizaVUDS() throws DelegateException {

    	
    	AfegirTramiteParameters params = prepararTestVerificarEstado(true); 
    	    	
        actionPerform(); 
        verifyNoActionErrors();
        verifyForward("success");
        
        
        verificaCodigoPendiente(params, request);
        
        
     }


    public void test03VerificarEstadoInvalidoPQFaltaTraducirNombreNouTramiteVUDS() throws DelegateException {

    	AfegirTramiteParameters params = prepararTestVerificarEstado(true);
		
		TraduccionTramite tramiteCastella=new TraduccionTramite();
		tramite.setTraduccion("es", tramiteCastella);
    	
        actionPerform(); 
        verifyNoActionErrors();
        verifyForward("success");
        
        verificaCodigoInvalidoFaltanCampos(params,request);
        
     }

	


    public void test04VerificarEstadoInvalidoPQSoloTraducidoNombreTramiteVUDS() throws DelegateException {

    	AfegirTramiteParameters params = prepararTestVerificarEstado(true);
		
		TraduccionTramite tramiteCastella=new TraduccionTramite();
		tramiteCastella.setNombre("nombre ficticio");
		tramite.setTraduccion("es", tramiteCastella);
    	
        actionPerform(); 
        verifyNoActionErrors();
        verifyForward("success");
        
        verificaCodigoInvalidoNoFaltaNombre(params,request);
        
     }

    
    public void test05VerificarDescripcionTasaNoTraducida() throws DelegateException {

    	AfegirTramiteParameters params = prepararTestVerificarEstado(true);
		
    	Set<Taxa> taxes=new HashSet<Taxa>();
    	Taxa tx=new Taxa();
    	TraduccionTaxa taxaCatala=new TraduccionTaxa();
    	taxaCatala.setCodificacio("cod00");
    	taxaCatala.setDescripcio("descripcio tasa");
    	taxaCatala.setFormaPagament("pagament tasa");
    	tx.setTraduccion("ca", taxaCatala);
    	taxes.add(tx);
    	tramite.setTaxes(taxes);
 

        actionPerform(); 
        verifyNoActionErrors();
        verifyForward("success");
        
        verificaCodigoInvalidoFaltaTraducirTasa(params,request);
        
     }
    
    
    public void test06VerificarDescripcionTasaSiTraducida() throws DelegateException {

    	AfegirTramiteParameters params = prepararTestVerificarEstado(true);
		
    	Set<Taxa> taxes=new HashSet<Taxa>();
    	Taxa tx=new Taxa();
    	TraduccionTaxa taxaCatala=new TraduccionTaxa();
    	taxaCatala.setCodificacio("cod00");
    	taxaCatala.setDescripcio("descripcio taxa");
    	taxaCatala.setFormaPagament("pagament taxa");
    	tx.setTraduccion("ca", taxaCatala);
    	taxes.add(tx);
    	
    	TraduccionTaxa taxaCastella=new TraduccionTaxa();
    	taxaCastella.setCodificacio("cod00");
    	taxaCastella.setDescripcio("descripcion tasa");
    	taxaCastella.setFormaPagament("pagamiento tasa");
    	tx.setTraduccion("es", taxaCastella);
    	taxes.add(tx);
    	tramite.setTaxes(taxes);
 

        actionPerform(); 
        verifyNoActionErrors();
        verifyForward("success");
        
        verificaCodigoPendiente(params,request);
        
     }

    //test validacio formularis
    public void test07verificarDescripcioFormulariSiTraduida() {
    	fail();
    }

    public void test08verificarArxiuFormulariSiTraduit() {
    	fail();
    }


    public void test09verificarArxiuFormulariNoTraduit() {
    	fail();
    }
    
    
    public void test10verificarArxiuDocumentNoTraduit() {
    	fail();
    }

    
    
    //test validacio documents

    public void test11verificarArxiuDocumentSiTraduit() {
    	fail();
    }

    public void test12verificarDescripcioDocumentSiTraduida() {
    	fail();
    }

    
    public void test13verificarArxiuDocumentNoTraduit() {
    	fail();
    }


    public void test14verificarDescripcioDocumentNoTraduida() {
    	fail();
    }




    
    private void verificaCodigoInvalidoFaltaTraducirTasa(AfegirTramiteParameters params, MockHttpServletRequest request2) {
    	TramiteForm dform = obtenerFormularioDeRespuesta(request);
		assertEquals(TramiteVudsInvalido.CODIGO,dform.get("estatVuds"));
		String detalls= (String)dform.get("detallEstatVuds");
		String expected="falta traduir: descripcio taxa cod00, pagament taxa cod00";
		assertEquals(expected,detalls);
		
	}

	private AfegirTramiteParameters prepararTestVerificarEstado(boolean vuds) {
		setConfigFile("/WEB-INF/struts-config.xml");
    	request=createMockRequestWithPath("/contenido/tramite/editar");
    	AfegirTramiteParameters params = creaFormularioAfegirTramite(); 

    	rellenaParametrosEnRequestAfegirTramite(params, request);

		crearConnectarMockTramite(vuds);
		return params;
	}
    
    private void verificaCodigoInvalidoFaltanCampos(AfegirTramiteParameters params, MockHttpServletRequest request) {
    	TramiteForm dform = obtenerFormularioDeRespuesta(request);
		assertEquals(TramiteVudsInvalido.CODIGO,dform.get("estatVuds"));
		String detalls= (String)dform.get("detallEstatVuds");
		String expected="falta traduir: nom del tramit, descripció, documentació, requisits, plaços, lloc";
		assertEquals(expected,detalls);
    }
    
   private void verificaCodigoInvalidoNoFaltaNombre(AfegirTramiteParameters params, MockHttpServletRequest request) {
		
    	TramiteForm dform = obtenerFormularioDeRespuesta(request);
		assertEquals(TramiteVudsInvalido.CODIGO,dform.get("estatVuds"));	
		String detalls= (String)dform.get("detallEstatVuds");
		_(detalls);

		String expected="falta traduir: descripció, documentació, requisits, plaços, lloc";
		assertEquals(expected,detalls);

    }

	private void crearConnectarMockTramite(boolean vuds) {
		String nomTramit = "tramit test MockGrabadorTramite "+new Date();
		tramite = RolsacMockFactory.crearMockTramite(nomTramit, 0, 0, 0, vuds);
		tramite.setId(12345L);
		if(vuds)
			tramite.setEstadoVuds(TramiteVudsSinEnviar.CODIGO);
		MockGrabadorTramite.tramite =tramite;
		MockTramiteDelegate.tramite =tramite;
	}

    
   


    
	private void verificaCodigoPendiente(AfegirTramiteParameters params, MockHttpServletRequest request) {
		 
		TramiteForm dform = obtenerFormularioDeRespuesta(request);
		
		assertEquals("tramiteVuds.pendiente",dform.get("estatVuds"));
		
	}
 
	private TramiteForm obtenerFormularioDeRespuesta(MockHttpServletRequest request) {
		TramiteForm dform=(TramiteForm)request.getAttribute("tramiteForm");
		return dform;
	}



    private AfegirTramiteParameters creaFormularioAfegirTramite() {
    	AfegirTramiteParameters params = new AfegirTramiteParameters();
    	params.action="Insertar";
    	params.idProcedimiento="247";
    	return params;
	}


    
    private void rellenaParametrosEnRequestAfegirTramite(AfegirTramiteParameters params, MockHttpServletRequest request) {
  	  	  request.addParameter("action",params.action);
  	  	  request.addParameter("idProcedimiento",params.idProcedimiento);
	}
    


	private MockHttpServletRequest createMockRequestWithPath(String path) {
    	 setRequestPathInfo(path);
         MockHttpServletRequest request=new MockHttpServletRequest();
         HttpServletRequestWrapper wrapper =new HttpServletRequestWrapper(request);
         wrapper.setAttribute(RequestProcessor.INCLUDE_SERVLET_PATH, path);
         setRequestWrapper(wrapper);
         return request;
	}

	private void _(Object o){System.out.println(o);}
	
}
