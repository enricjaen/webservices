package es.caib.persistence.actualizador.vuds;

import java.util.Iterator;

import org.ibit.rol.sac.model.Archivo;
import org.ibit.rol.sac.model.DocumentTramit;
import org.ibit.rol.sac.model.Taxa;
import org.ibit.rol.sac.model.TraduccionDocumentTramit;
import org.ibit.rol.sac.model.TraduccionDocumento;
import org.ibit.rol.sac.model.TraduccionProcedimiento;
import org.ibit.rol.sac.model.TraduccionTaxa;
import org.ibit.rol.sac.model.TraduccionTramite;
import org.ibit.rol.sac.model.Tramite;

import es.map.vuds.si.service.webservice.Tasa;




public class TramiteVUDSValidator {

	

	public ValidacionTramiteVuds validarTramiteEstaTraducidoCastellano(Tramite t_rolsac)
	{
		tramiteCatala =   t_rolsac.getTramiteCatala();
		tramiteCastella = t_rolsac.getTramiteCastella();
		procCatala =  	  t_rolsac.getProcedimentCatala();
		procCastella = 	  t_rolsac.getProcedimentCastella();
		
		validarNombre();
		validarDescripcion();
		validarDocumentacion();
		validarRequisits();
		validarPlazos();
		validarLugar();
		validarObservaciones();
		validarResultat();
		validarTiempoResolucion();
		validarTasas(t_rolsac);
		validarFormularis(t_rolsac); 
		validarDocuments(t_rolsac); 

		return validacion;
		
	}

	private void validarFormularis(Tramite t_rolsac) {
		validarDocuments(t_rolsac,"formulari");
	}

	
	private void validarDocuments(Tramite t_rolsac) {
		validarDocuments(t_rolsac,"document");
	}

	private void validarDocuments(Tramite t_rolsac, String tipusDoc) {
		Iterator<DocumentTramit> it= t_rolsac.getFormularios().iterator();
		while(it.hasNext()) {
			DocumentTramit form = it.next();
			validarDescripcioDocument(form, tipusDoc);
			validarArxiuDocument(form, tipusDoc);
		}
	}
	
	
	private void validarArxiuDocument(DocumentTramit form, String tipusDoc) {
		TraduccionDocumento formCatala   =form.getDocumentCatala();
		TraduccionDocumento formCastella =form.getDocumentCastella();
		
		if(arxiuEnCatalaPeroNoCastella(formCatala.getArchivo(),
										formCastella.getArchivo()))
			validacion.addCampoSinTraducir("arxiu "+tipusDoc+" "+formCatala.getTitulo());				
	}

	private boolean arxiuEnCatalaPeroNoCastella(Archivo arxiuCat, Archivo arxiuCast) {
		if(arxiuBuit(arxiuCat)) 
			return false;
		
		if(!arxiuBuit(arxiuCast)) 
			return false;
		
		return true;
	}

	private boolean arxiuBuit(Archivo arxiu) {
		return null==arxiu;
	}

	private void validarDescripcioDocument(DocumentTramit form, String tipusDoc) {
		TraduccionDocumento formCatala   =form.getDocumentCatala();
		TraduccionDocumento formCastella =form.getDocumentCastella();
				
		if(campoRellenadoCatalaPeroNoCastella(formCatala.getDescripcion(), 
											  formCastella.getDescripcion()))
			validacion.addCampoSinTraducir("descripcio "+tipusDoc+" "+formCatala.getTitulo());				
	}



	private void validarTasas(Tramite t_rolsac) {
		if(!t_rolsac.taxesHabilitades()) return;

		Iterator<Taxa> it= t_rolsac.getTaxes().iterator();
		while(it.hasNext()) {
			Taxa taxa=it.next();
			validarDescripcioTaxa(taxa);
			validarFormaPagament(taxa);
		}
	}

		
	private void validarFormaPagament(Taxa taxa) {
			TraduccionTaxa taxaCatala=taxa.getTaxaCatala();
			TraduccionTaxa taxaCastella=taxa.getTaxaCastella();
			
			if(campoRellenadoCatalaPeroNoCastella(taxaCatala.getFormaPagament(), 
												  taxaCastella.getFormaPagament()))
				validacion.addCampoSinTraducir("pagament taxa "+taxaCatala.getCodificacio());		
		
	}

	private void validarDescripcioTaxa(Taxa taxa) {
			TraduccionTaxa taxaCatala=taxa.getTaxaCatala();
			TraduccionTaxa taxaCastella=taxa.getTaxaCastella();
			
			if(campoRellenadoCatalaPeroNoCastella(taxaCatala.getDescripcio(), 
												  taxaCastella.getDescripcio()))
				validacion.addCampoSinTraducir("descripcio taxa "+taxaCatala.getCodificacio());		
	}

	private void validarTiempoResolucion() {
		if(  campoRellenadoCatalaPeroNoCastella(procCatala.getResolucion(), 
												procCastella.getResolucion()))
			validacion.addCampoSinTraducir("resolucion(proc)");
		
	}
	
	private void validarObservaciones() {
		if(  campoRellenadoCatalaPeroNoCastella(procCatala.getObservaciones(), 
												procCastella.getObservaciones()))
			validacion.addCampoSinTraducir("observacions(proc)");
	}

	private void validarResultat() {
		if(  campoRellenadoCatalaPeroNoCastella(procCatala.getResultat(), 
												procCastella.getResultat()))
			validacion.addCampoSinTraducir("resultat(proc)");
	}

	private void validarLugar() {
		if(  campoRellenadoCatalaPeroNoCastella(tramiteCatala.getLugar(), 
												tramiteCastella.getLugar()))
			validacion.addCampoSinTraducir("lloc");
	}

	private void validarPlazos() {
		if(  campoRellenadoCatalaPeroNoCastella(tramiteCatala.getPlazos(), 
												tramiteCastella.getPlazos()))
			validacion.addCampoSinTraducir("plaços");
	}

	private void validarRequisits() {
		if(  campoRellenadoCatalaPeroNoCastella(tramiteCatala.getRequisits(), 
												tramiteCastella.getRequisits()))
			validacion.addCampoSinTraducir("requisits");		
	}

	private void validarDocumentacion() {
		if(  campoRellenadoCatalaPeroNoCastella(tramiteCatala.getDocumentacion(), 
												tramiteCastella.getDocumentacion()))
			validacion.addCampoSinTraducir("documentació");		
	}

	private void validarDescripcion() {
		if(  campoRellenadoCatalaPeroNoCastella(tramiteCatala.getDescripcion(), 
												tramiteCastella.getDescripcion()))
			validacion.addCampoSinTraducir("descripció");		
	}

	private void validarNombre() {
		if(  campoRellenadoCatalaPeroNoCastella(tramiteCatala.getNombre(), 
												tramiteCastella.getNombre()))
			validacion.addCampoSinTraducir("nom del tramit");
				
	}

	private boolean campoRellenadoCatalaPeroNoCastella(String campoCatala, String campoCastella) {
		return campoRellenado(campoCatala) && 
			!campoRellenado(campoCastella);
	}
	
	private boolean campoRellenado(String campo) {
		return null!=campo && !"".equals(campo);
	}


	ValidacionTramiteVuds validacion=new ValidacionTramiteVuds();
	TraduccionTramite tramiteCatala;
	TraduccionTramite tramiteCastella;
	TraduccionProcedimiento procCatala;
	TraduccionProcedimiento procCastella;	
	
}
