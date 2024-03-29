package es.caib.persistence.actualizador.vuds;

//FIXME import es.caib.persistence.vuds.GestorWebserviceBeanServiceStub;
import es.map.vuds.si.service.webservice.CanalTramitacion;
import es.map.vuds.si.service.webservice.CargarTramitesVuds;
import es.map.vuds.si.service.webservice.CargarTramitesVudsE;
import es.map.vuds.si.service.webservice.CargarTramitesVudsResponse;
import es.map.vuds.si.service.webservice.CargarTramitesVudsResponseE;
import es.map.vuds.si.service.webservice.FormaIniciacion;
import es.map.vuds.si.service.webservice.Formulario;
import es.map.vuds.si.service.webservice.GuardarTramites;
import es.map.vuds.si.service.webservice.GuardarTramitesE;
//FIXME import es.map.vuds.si.service.webservice.Idioma;
import es.map.vuds.si.service.webservice.OrganismoCompetente;
import es.map.vuds.si.service.webservice.Tasa;
import es.map.vuds.si.service.webservice.TipoRegistro;
import es.map.vuds.si.service.webservice.TipologiaTramite;
import es.map.vuds.si.service.webservice.Tramite;
import es.map.vuds.si.service.webservice.TramiteVuds;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ibit.rol.sac.model.DocumentTramit;
import org.ibit.rol.sac.model.Taxa;
import org.ibit.rol.sac.model.TraduccionDocumentTramit;
import org.ibit.rol.sac.model.TraduccionDocumento;
import org.ibit.rol.sac.model.TraduccionIniciacion;
import org.ibit.rol.sac.model.TraduccionNormativa;
import org.ibit.rol.sac.model.TraduccionProcedimiento;
import org.ibit.rol.sac.model.TraduccionProcedimientoLocal;
import org.ibit.rol.sac.model.TraduccionTaxa;
import org.ibit.rol.sac.model.TraduccionTramite;
import org.ibit.rol.sac.model.TraduccionUA;
import org.ibit.rol.sac.model.UnidadAdministrativa;
import org.ibit.rol.sac.model.Tramite.Operativa;
import org.ibit.rol.sac.persistence.ws.invoker.WSInvocatorException;


/**
 * @author u92770
 * He escollit el patr� Adapter perque cal adaptar un tramit rolsac a un tramit vuds
 * (patr� Facade no seria adecuat perque cal adaptar el tramit)  
 *
 */
public class VentanillaUnicaAdapter {
	protected static Log log = LogFactory.getLog(VentanillaUnicaAdapter.class);
	//private static String endpoint = "http://epreinf45:18080/axis2/services/GestorWebserviceBeanService";
	String endpoint = "http://89.140.20.218:65003/ServiciosExternos/sistemaInformacionWS";  //TODO parametritzar 

	public static final String CATALA="ca";
	public static final String CASTELLA="es";
	
	//codigos forma inciacion devueltos por el webservice:
	private static final long PRESENCIAL = 1L;   
	private static final long TELEMATICO = 2L;
	private static final String PRESENCIAL_STR = "presencial";   
	private static final String TELEMATICO_STR = "presencial y telematico";
	private static final String NOREQS_STR = "No hay requisitos";
	private static final String NODOCS_STR = "No hay documentos";
	private static final String NOFORM_STR = "No hay formularios";
	private static final String VACIO_STR = "-vacio-";
	private static final String TAXAMULTIPLE = "Ver campo descripci�n.";
	private static final Long INICIACION_SOLICITANTE = 1L;
	private static final Long INICIACION_OFICIO = 2L;
	private static final Long INICIACION_AMBAS = 3L;
	
	private static final String ALTA = "alta";
	private static final String MODIFICACION = "modificacion";
	private static final String BAJA = "baja";
	
	
	
	//private final String endpoint_webcaib = "http://www.caib.es";
	private final static String endpoint_webcaib = "https://proves.caib.es";

	private HashMap<String,String> idiomes=new HashMap<String, String>();

	{
		idiomes.put(CATALA, "ca_ES Catala");
		idiomes.put(CASTELLA, "es_ES Castellano");

		/*	altres idiomes:
		  		eu_ES Euskera	
				gl_ES Galego
				va_ES Valenci�
				en_US English
				fr_FR Fran�ais
				de_DE Deutsch
				
		*/
	}
	
	
	public VentanillaUnicaAdapter(){}
	
	public VentanillaUnicaAdapter(String ep){ endpoint=ep;	}

	org.ibit.rol.sac.model.Tramite t_rolsac;
	public String idioma;

	
	public Tramite convertirTramitRolsacl2TramiteVUDS(org.ibit.rol.sac.model.Tramite t_rolsac, String idioma)
	{
		this.t_rolsac=t_rolsac;
		this.idioma=idioma;
		
		ValidacionTramiteVuds traval = new ValidacionTramiteVuds();
		
		List<String> sinTraducir = new ArrayList<String>();
		
		Tramite t = new Tramite();
		
		//ENVY
		long canal = (null == t_rolsac.getId() || null == t_rolsac.getVersio()) && null == t_rolsac.getUrlExterna() ? PRESENCIAL : TELEMATICO;
		
		CanalTramitacion canalTramitacion = new CanalTramitacion();
		String descCT=TELEMATICO==canal? TELEMATICO_STR : PRESENCIAL_STR;
		canalTramitacion.setDescripcionCanal(descCT);
		canalTramitacion.setIdCanal(canal);
		TraduccionTramite tratra = (TraduccionTramite)t_rolsac.getTraduccion(idioma);
		
		//ENCADENACION MENSAJES
		TraduccionProcedimientoLocal trapro = (TraduccionProcedimientoLocal)t_rolsac.getProcedimiento().getTraduccion(idioma);

		String denominacionTramite = "";
		String descripcionTramite = "";
		String plazosLegales = "";
		String docsPresentar = "";
		String requisits = "";
		String observaciones = "";
		String resultat= "";
		String tiempoResolucion = "";

		if(null!=tratra) {
		if (null!=tratra.getNombre()) denominacionTramite = tratra.getNombre();
		if (null!=tratra.getDescripcion())	descripcionTramite =  tratra.getDescripcion();
		if (null!=tratra.getPlazos()) plazosLegales = tratra.getPlazos();
		if (null!=tratra.getDocumentacion()) docsPresentar=tratra.getDocumentacion();
		if (null!=tratra.getRequisits())  requisits =tratra.getRequisits();
		}
		if(null!=trapro) {
		if (null!=trapro.getObservaciones())observaciones =trapro.getObservaciones();
		if (null!=trapro.getResultat())		  	 resultat=trapro.getResultat();
		if (null!=trapro.getResolucion()) 	 tiempoResolucion =trapro.getResolucion();
		}
		
		//validacio
		TraduccionTramite tratra_ca = (TraduccionTramite)t_rolsac.getTraduccion("ca");
		TraduccionProcedimientoLocal trapro_ca = (TraduccionProcedimientoLocal)t_rolsac.getProcedimiento().getTraduccion("ca");
		
		//SIMPLIF CONDICIONAL
		if("".equals(denominacionTramite)&& null!= tratra_ca.getNombre() && !"".equals(tratra_ca.getNombre())) sinTraducir.add("nom del tr�mit");
		
		if("".equals(descripcionTramite) && null!= tratra_ca.getDescripcion() && !"".equals(tratra_ca.getDescripcion())) sinTraducir.add("descripci� del tr�mit");
		if("".equals(plazosLegales) && null!= tratra_ca.getPlazos() && !"".equals(tratra_ca.getPlazos())) sinTraducir.add("pla� m�xim de presentaci�");
		if("".equals(docsPresentar) && null!= tratra_ca.getDocumentacion() && !"".equals(tratra_ca.getDocumentacion())) sinTraducir.add("documentaci�");
		if("".equals(requisits) && null!= tratra_ca.getRequisits() && !"".equals(tratra_ca.getRequisits())) sinTraducir.add("requisits");
		if("".equals(observaciones) && null!= trapro_ca.getObservaciones() && !"".equals(trapro_ca.getObservaciones())) sinTraducir.add("observacions (procediment)");
		
		if("".equals(resultat) && null!= trapro_ca.getResultat() && !"".equals(trapro_ca.getResultat())) 
			sinTraducir.add("resultat (procedimient)");
		
		//FORMA INICIACION
		
		FormaIniciacion formaIniciacion = new FormaIniciacion();
		formaIniciacion.setDescripcionFormaIniciacion(VACIO_STR);
		TraduccionIniciacion iniciacion = (TraduccionIniciacion)t_rolsac.getProcedimiento().getIniciacion().getTraduccion(idioma);
		
		String regex="(?i).*"+"ofici"+".*";
		if(null!=iniciacion && iniciacion.getNombre().matches(regex)) {
			formaIniciacion.setIdFormaIniciacion(INICIACION_OFICIO);
			formaIniciacion.setDescripcionFormaIniciacion("de oficio");
		}
		regex="(?i).*"+"instancia"+".*";
		if(null!=iniciacion && iniciacion.getNombre().matches(regex)) {
			formaIniciacion.setIdFormaIniciacion(INICIACION_SOLICITANTE);
			formaIniciacion.setDescripcionFormaIniciacion("de instancia");
		}
 	
		//TASA
		Tasa tasa = null;
		if("on".equals(t_rolsac.getProcedimiento().getTaxa()) &&
				null!=t_rolsac.getTaxes()) {
			tasa = new Tasa();
			Iterator<Taxa> it= t_rolsac.getTaxes().iterator();
			
			//cas A) nomes hi ha 1 taxa. S'omplen tots els camps
			if(1==t_rolsac.getTaxes().size()) {
				Taxa taxa=it.next();
				TraduccionTaxa tt=(TraduccionTaxa)taxa.getTraduccion(idioma);
				String cod="";
				String desc="";
				String forpag="";

				if(null!=tt) {
					cod=tt.getCodificacio();
					desc=tt.getDescripcio();
					forpag=tt.getFormaPagament();

					//tasa.setCodificacion(cod);
					tasa.setDescripcionTasa(desc);
					tasa.setModoPago(forpag);
				}

				//validacio
				TraduccionTaxa tt_ca=(TraduccionTaxa)taxa.getTraduccion("ca"); 
				if((null==tt || null==desc || "".equals(desc)) && null!=tt_ca.getDescripcio() && !"".equals(tt_ca.getDescripcio())) sinTraducir.add("descripci� taxa");
				if((null==tt || null==forpag || "".equals(forpag)) && null!=tt_ca.getFormaPagament()&&!"".equals(tt_ca.getFormaPagament())) sinTraducir.add("forma de pagament taxa");
			}
			
			//cas B) hi ha +1 taxa. Es posa tot en el camp descripci�.
			else {
				StringBuilder descMultiple = new StringBuilder();
				while(it.hasNext()) {
					TraduccionTaxa tt=(TraduccionTaxa)it.next().getTraduccion();
					descMultiple.append("c�digo: "+tt.getCodificacio()+"\n");
					descMultiple.append("descripci�n: "+tt.getDescripcio()+"\n");
					descMultiple.append("forma de pago: "+tt.getFormaPagament()+"\n");
					
					//validacio
					TraduccionTaxa tt_ca=(TraduccionTaxa)it.next().getTraduccion("ca"); 
					if("".equals(tt.getDescripcio()) && null!=tt_ca.getDescripcio() && !"".equals(tt_ca.getDescripcio())) sinTraducir.add("descripci� taxa "+tt_ca.getCodificacio());
					if("".equals(tt.getFormaPagament()) && null!=tt_ca.getFormaPagament() && !"".equals(tt_ca.getFormaPagament())) sinTraducir.add("forma de pagament taxa "+tt_ca.getCodificacio());
					
				}
				
				//tasa.setCodificacion(TAXAMULTIPLE);
				tasa.setDescripcionTasa(descMultiple.toString());
				tasa.setModoPago(TAXAMULTIPLE);
			}
		}
		
		
		//TIPO DE REGISTRO
		String tipoRegistro;
		switch(t_rolsac.getOperativa())
		{
		case CREA : // '\001'
		default:
			tipoRegistro = ALTA;
			break;

		case MODIFICA: // '\002'
			tipoRegistro = MODIFICACION;
			break;

		case BORRA: // '\003'
			tipoRegistro = BAJA;
			break;
		}
		TipologiaTramite tipologia = new TipologiaTramite();
		tipologia.setIdTipologiaTra(123L);

		//FORMULARIOS	
		Set<DocumentTramit> rolsac_forms = t_rolsac.getFormularios();
		Formulario f;
		List<Formulario> forms_l = new ArrayList<Formulario>();

		for(Iterator<DocumentTramit> it = rolsac_forms.iterator(); it.hasNext(); ){
			DocumentTramit rolsac_f=it.next();
			f = new Formulario();
			f.setIdCodificacion( Long.toString(rolsac_f.getId().longValue()));
			TraduccionDocumento trafor =(TraduccionDocumento)rolsac_f.getTraduccion(idioma);

			//validacio
			TraduccionDocumento trafor_ca =(TraduccionDocumento)rolsac_f.getTraduccion("ca"); 
			if(null!=trafor_ca.getDescripcion() && !"".equals(trafor_ca.getDescripcion()) && 
					(null==trafor || null!=trafor && null!=trafor.getDescripcion() && "".equals(trafor.getDescripcion()))) { 
				sinTraducir.add("descripci� formulari "+trafor_ca.getTitulo());
			}
			if(null!=trafor_ca.getArchivo() && !"".equals(trafor_ca.getArchivo()) && 
					(null==trafor || (null!=trafor && null== trafor.getArchivo()))) {
				sinTraducir.add("arxiu formulari "+trafor_ca.getTitulo());
			}
			
			
//			long arxiuId=trafor.getArchivo().getId();
//			String url=endpoint_webcaib+"/govern/archivo.do?id="+arxiuId;
//			f.setUrlDescarga(url);	
//			f.setDescripcionFormulario(trafor.getDescripcion());
			
			forms_l.add(f);
		}
		
		//si no hi ha cap, s'indica.
		if(0==forms_l.size()) {
			f=new Formulario();
			f.setDescripcionFormulario(NOFORM_STR);
			f.setIdCodificacion(VACIO_STR);
			f.setUrlDescarga(VACIO_STR);
			forms_l.add(f);
		}
		
		Formulario forms_a[] = new Formulario[forms_l.size()];
		forms_l.toArray(forms_a);
		
		
		//NORMATIVAS
		Set<org.ibit.rol.sac.model.Normativa> norms = t_rolsac.getProcedimiento().getNormativas();
		List<String> normsVuds = new ArrayList<String>(); 
		
		if(null!=norms)
			for(Iterator<org.ibit.rol.sac.model.Normativa> it = norms.iterator(); it.hasNext(); ) {
				//Normativa nVuds = new Normativa();
				org.ibit.rol.sac.model.Normativa norm=it.next();

				TraduccionNormativa tranor=(TraduccionNormativa)norm.getTraduccion(idioma); 
				normsVuds.add(tranor.getTitulo());

				//validacio
				TraduccionNormativa tranor_ca =(TraduccionNormativa)norm.getTraduccion("ca"); 
				if("".equals(tranor.getTitulo()) && null!=tranor_ca.getTitulo() && !"".equals(tranor_ca.getTitulo())) sinTraducir.add("normativa "+tranor_ca.getTitulo());

			}
		
		t.setTramiteVuds(obtenerTramiteVuds());
		t.setCanalTramitacion(canalTramitacion);
		t.setDenominacionTramite(denominacionTramite);
		t.setDescripcionTramite(descripcionTramite);
		t.setFormaIniciacion(formaIniciacion);
		t.setObservaciones(observaciones);
		t.setPlazosLegales(plazosLegales);
		t.setTasa(tasa);
		t.setTiempoResolucion(tiempoResolucion);
		t.setTipoRegistro(tipoRegistro);
		t.setTipologia(tipologia);
		t.setFormulario(forms_a);
		t.setRequisitosPrevios(new String[]{requisits});
		t.setDocumento(new String[]{docsPresentar});
		t.setNormativa(normsVuds.toArray(new String[normsVuds.size()]));
		

		String url=endpoint_webcaib+"/govern/sac/visor_proc.do?codi="+t_rolsac.getProcedimiento().getId();
		t.setEnlaceConsulta(url);
		t.setAreaTramitadora(obtenerAreaTramitadora());

		t.setOrganismoCompetente(obtenerOrganismoCompetente());
		t.setResultado(new String[]{resultat});

		
		return t;
	}

	private OrganismoCompetente obtenerOrganismoCompetente() {
		String descOC = t_rolsac.getNombreUnidadAdministrativa(idioma);
		if(null==descOC) 
			descOC = t_rolsac.getNombreUnidadAdministrativa("ca");

		OrganismoCompetente organismoCompetente = new OrganismoCompetente();
		organismoCompetente.setIdOrganismo(t_rolsac.obtenerIdUnidadAdministrativa().toString());
		organismoCompetente.setDescripcionOrganismo( descOC );
		
		return organismoCompetente;
	}

	private TramiteVuds obtenerTramiteVuds() {
		//establecer tramite vuds
		TramiteVuds tramiteVuds = new TramiteVuds();
		tramiteVuds.setDescripcionTramiteVuds(this.t_rolsac.getDescCodiVuds());
		tramiteVuds.setIdTramiteVuds(this.t_rolsac.getCodiVuds());
		return tramiteVuds;
	}

	private String obtenerAreaTramitadora() {
		String areaTramitadora = t_rolsac.getNombreOrganCompetent(idioma);
		if(null!=areaTramitadora) return areaTramitadora;
		return t_rolsac.getNombreOrganCompetent("ca");
	}

	
}
