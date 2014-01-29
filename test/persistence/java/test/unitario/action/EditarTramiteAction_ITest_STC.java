package test.unitario.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.RequestProcessor;
import org.easymock.classextension.EasyMock;
import org.ibit.rol.sac.back.form.DocumentoForm;
import org.ibit.rol.sac.back.form.ProcedimientoForm;
import org.ibit.rol.sac.back.form.TraDynaValidatorForm;
import org.ibit.rol.sac.back.form.TramiteForm;
import org.ibit.rol.sac.model.Destinatario;
import org.ibit.rol.sac.model.DocumentTramit;
import org.ibit.rol.sac.model.Taxa;
import org.ibit.rol.sac.model.TraduccionTramite;
import org.ibit.rol.sac.persistence.delegate.DelegateException;
import org.ibit.rol.sac.persistence.delegate.DestinatarioDelegate;
import org.ibit.rol.sac.persistence.delegate.IdiomaDelegate;
import org.ibit.rol.sac.persistence.ws.Actualizador;
import org.ibit.rol.sac.persistence.ws.ReportarFallo;
import org.ibit.rol.sac.persistence.ws.invoker.WSInvocatorException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import es.caib.vuds.DestinatarioVuds;
import es.map.vuds.si.service.webservice.Formulario;
import es.map.vuds.si.service.webservice.TramiteVuds;

import servletunit.struts.MockStrutsTestCase;
import test.integracion.mock.MockUnidadAdministrativaDelegate;


/**
 * INFO: utiliza el sac-back-messages.properties por defecto (o sea "en");
 * @author u92770
 *
 */


@RunWith(PowerMockRunner.class)
@PrepareForTest(ReportarFallo.class)
@PowerMockIgnore("org.apache.commons.logging")  // para q no salga duplicate visibility error

public class EditarTramiteAction_ITest_STC extends MockStrutsTestCase {

	IdiomaDelegate idiomaDelegate;
	
	public EditarTramiteAction_ITest_STC(String s) {
		super(s);
	}
	
	public void setUp() { try {
		super.setUp();
        setInitParameter("validating","false");
        //_(Thread.currentThread().getContextClassLoader());
        
    	idiomaDelegate = EasyMock.createMock(IdiomaDelegate.class);
    	String[] langs={"ca","es","en","de"};
		EasyMock.expect(idiomaDelegate.listarLenguajes()).andReturn(Arrays.asList(langs)).times(6);
		EasyMock.replay(idiomaDelegate);
    	DocumentoForm.idiomaDelegate = idiomaDelegate;


	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} }

    public void tearDown() { try {
		super.tearDown();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} }

    
    // OK 15/10/2010
    public void testH84CrearTramitEnVentanilla() throws DelegateException {
    	//este test unitario demuestra que un tramite se ha modificado en el caib y 
    	// que se ha creado en la ventanilla
    	// se pasa el test si el mensaje contiene "modificado correctamente" y
    	// "creado en la ventanilla"
    	
    	
    	//creamos un mock del detinatarioDeletage aqui porque Actualizador es static, 
    	//y spring no lo enlaza con los otros beans del applicationContext.
/*    	
		List<Destinatario> destinatarios=new ArrayList<Destinatario>();
		DestinatarioDelegate destDelegate = EasyMock.createMock(DestinatarioDelegate.class);
		Destinatario d=new DestinatarioVuds();
		d.setNombre("VUDS");
		destinatarios.add(d);
		try {
			EasyMock.expect(destDelegate.listarDestinatarios()).andReturn(destinatarios);

		} catch (DelegateException e) {
			e.printStackTrace();
		}
		EasyMock.replay(destDelegate);
		Actualizador.setDestDelegate(destDelegate);
		
*/	
    	//ponemos a mano la referencia a idiomaDelegate porque el Form al no ser una action, spring
		//no lo puede conectar automaticamente.
    	TraDynaValidatorForm.idiomaDelegate = idiomaDelegate;
    	
    	String path="/contenido/tramite/editar";
    	String action="Insertar";
    	MockHttpServletRequest request=createMockRequest(path);

        request.addParameter("action", action);

        String validacio="1";  //public
        String requisits="tenir mes de 18 anys";
        String documentacion="document 1";
        String requisits_cst="tener mas de 18 anys";
        String documentacion_cst="documento 1";

        ompleParametresTramit(request,true);
        
        request.addParameter("validacio",validacio);
        request.addParameter("traducciones[0].requisits",requisits);
        request.addParameter("traducciones[0].documentacion",documentacion);
        
        request.addParameter("traducciones[1].requisits",requisits_cst);
        request.addParameter("traducciones[1].documentacion",documentacion_cst);

        
        actionPerform();
        verifyNoActionErrors();
        verifyForward("success");

        TramiteForm df = (TramiteForm)request.getAttribute("tramiteForm");
        assertEquals(Integer.parseInt(validacio),df.get("validacio"));
        
        List<TraduccionTramite> t=(List<TraduccionTramite>)df.getMap().get("traducciones");
        TraduccionTramite tt=(TraduccionTramite)t.get(0);
        assertEquals(requisits,tt.getRequisits());
        assertEquals(documentacion,tt.getDocumentacion());
        
        assertEquals("codigo1",df.get("idTraTel"));
        assertEquals("1",df.get("versio").toString());
        assertEquals("http://urlExterna",df.get("urlExterna"));

     }


    //area privada
    
    private void ompleParametresTramit(MockHttpServletRequest request, boolean traduir) {
    	  request.addParameter("idProcedimiento","247");
          request.addParameter("textoFechaCaducidad","10/02/2010");	
          request.addParameter("textoFechaPublicacion","10/12/2009");
          request.addParameter("textoFechaActualizacion","11/12/2009");
          request.addParameter("textoFechaCaducidad","10/02/2010");	
          request.addParameter("fase", "Instrucción");
          request.addParameter("idOrganCompetent","1"); //1 = "Govern de les Illes Balears");
          request.addParameter("nomOrganCompetent","Govern de les Illes Balears"); 
          request.addParameter("descTaxa","descripcion taxa"); 
          request.addParameter("formaPagamentTaxa","en efectiu");
          request.addParameter("codiTaxa","123456");
          request.addParameter("idTraTel","codigo1");
          request.addParameter("versio","1");
          request.addParameter("urlExterna","http://urlExterna");
          //request.addParameter("traducciones[0].nombre","test tramit");
          request.addParameter("traducciones[0].plazos","3 mesos");
          request.addParameter("codiVuds","C0002");
          if(traduir) {
        	  request.addParameter("traducciones[1].plazos","3 meses");
          }
        	  

	}
    
    private void ompleParametresDocument(MockHttpServletRequest request) {
        request.addParameter("traducciones[0].titulo","nombre doc");
        request.addParameter("traducciones[0].descripcion","descripcion doc ");
	}


    private void assertValidacioFormOK(MockHttpServletRequest request) {
		ActionErrors errors= (ActionErrors)request.getAttribute("org.apache.struts.action.ERROR");
		if(null!=errors) {
			Iterator<ActionError> it=errors.get();
			while(it.hasNext())  _(Arrays.toString(it.next().getValues()));
			fail();
		}
	}
	private MockHttpServletRequest createMockRequest(String path) {
    	 setRequestPathInfo(path);
         MockHttpServletRequest request=new MockHttpServletRequest();
         HttpServletRequestWrapper wrapper =new HttpServletRequestWrapper(request);
         wrapper.setAttribute(RequestProcessor.INCLUDE_SERVLET_PATH, path);
         setRequestWrapper(wrapper);
         return request;
	}

	private void _(Object o){System.out.println(o);}
	
}
