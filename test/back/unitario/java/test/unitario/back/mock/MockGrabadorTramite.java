package test.unitario.back.mock;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ibit.rol.sac.back.action.contenido.tramite.GuardarTramiteAction;
import org.ibit.rol.sac.back.negocio.GrabadorTramite;
import org.ibit.rol.sac.model.Tramite;

public class MockGrabadorTramite extends GrabadorTramite {

	protected static Log log = LogFactory.getLog(MockGrabadorTramite.class);

	public static Tramite tramite;
	
	@Override
	public Tramite grabarTramite(GrabarTramiteParameter grabarParams)
			throws Exception {
		
			log.info("grabarTramite");
			return tramite;

	}
}
