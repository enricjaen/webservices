package org.ibit.rol.sac.back.negocio;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.DynaProperty;
import org.easymock.EasyMock;
import org.ibit.rol.sac.back.form.TramiteForm;
import org.ibit.rol.sac.back.negocio.GrabadorTramite.GrabarTramiteParameter;
import org.ibit.rol.sac.model.DocumentTramit;
import org.ibit.rol.sac.model.Familia;
import org.ibit.rol.sac.model.Iniciacion;
import org.ibit.rol.sac.model.ProcedimientoLocal;
import org.ibit.rol.sac.model.Taxa;
import org.ibit.rol.sac.model.TraduccionIniciacion;
import org.ibit.rol.sac.model.TraduccionProcedimientoLocal;
import org.ibit.rol.sac.model.TraduccionTaxa;
import org.ibit.rol.sac.model.TraduccionTramite;
import org.ibit.rol.sac.model.TraduccionUA;
import org.ibit.rol.sac.model.Tramite;
import org.ibit.rol.sac.model.UnidadAdministrativa;
import org.ibit.rol.sac.persistence.delegate.DelegateException;
import org.ibit.rol.sac.persistence.ejb.ProcedimientoFacadeEJB;
import org.ibit.rol.sac.persistence.ws.Actualizador;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import es.caib.test.common.RolsacMockFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Actualizador.class)
@PowerMockIgnore("org.apache.commons.logging") 
public class GrabadorTramiteTest  extends 
									//AbstractDependencyInjectionSpringContextTests {
									AbstractTransactionalSpringContextTests {

	
	
	GrabadorTramite grabadorTramite;
	ProcedimientoFacadeEJB procBean;
	
	protected String[] getConfigLocations() {
		return new String[] {
				"dataSource.xml",
				"dao.xml"
				};
	}

	public void onSetUp() throws Exception {
		grabadorTramite = (GrabadorTramite) applicationContext.getBean("grabadorTramite");
	
		procBean=(ProcedimientoFacadeEJB)applicationContext.getBean("procedimientoFacadeEJB");
		crearMockActualizador();

	}

	private void crearMockActualizador() {
		PowerMock.mockStatic(Actualizador.class);
		Actualizador.actualizar(EasyMock.anyObject());
    	PowerMock.expectLastCall();
    	PowerMock.replay(Actualizador.class);
	}
	
	@Test
	public void testGrabarTramite() throws Exception {
		assertNotNull(grabadorTramite);

		ProcedimientoLocal proc = procBean.obtenerProcedimiento(247L);
		System.out.println(proc.getTramites().size());
		Tramite tramite = crearMockTramite("tramit de test GrabadorTramiteTest", 0, 0, 0, false);
		TramiteForm tForm = crearTramiteForm(); 
		tramite = grabadorTramite.grabarTramite(new GrabarTramiteParameter(tForm, 247L, true));
		System.out.println(tramite.getId()+ ", "+  tramite.getOrden());
		proc = procBean.obtenerProcedimiento(247L);
		System.out.println(proc.getTramites().size());
		
	}

	private TramiteForm crearTramiteForm() {
		TramiteForm tramiteForm = new TramiteForm() {
			@Override
			protected DynaProperty getDynaProperty(String name) {
				return  new DynaProperty(name); 
			}
		};
		
		tramiteForm.setDataActualitzacio(new Date());
		tramiteForm.setDataPublicacio(new Date());
		tramiteForm.setDataCaducitat(new Date());
		tramiteForm.set("idOrganCompetent",202L);
		return tramiteForm;
	}

	private Tramite crearMockTramite(String nomTramit, int ntaxes, int ndocsInf, int nforms,boolean vuds) {
		return RolsacMockFactory.crearMockTramite(nomTramit, ntaxes, ndocsInf, nforms, vuds);
	}
	
}
