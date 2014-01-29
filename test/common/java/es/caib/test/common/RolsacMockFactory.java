package es.caib.test.common;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

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
import org.ibit.rol.sac.model.Tramite.Operativa;
import org.ibit.rol.sac.model.UnidadAdministrativa;

public class RolsacMockFactory {

	public static Tramite crearMockTramite(String nomTramit, int ntaxes, int ndocsInf, int nforms,boolean vuds) {
	
		
		Tramite t=new Tramite();
		//t.setId(0);  si id=null -> hibernate=insert, sino hibernate=update
		
		t.setValidacio(1L);
		
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
		if(vuds) {
			procedimiento.setVentanillaUnica("1");
			t.setOperativa(Operativa.CREA);
		}
		else
			procedimiento.setVentanillaUnica("0");
		TraduccionProcedimientoLocal trapro_ca = new TraduccionProcedimientoLocal();
		trapro_ca.setResultat("un resultat");
		procedimiento.setTraduccion("ca", trapro_ca);

		TraduccionProcedimientoLocal trapro_es = new TraduccionProcedimientoLocal();
		trapro_es.setResultat("un resultado");
		procedimiento.setTraduccion("es", trapro_es);

		
		UnidadAdministrativa oc = new UnidadAdministrativa(202L);
		TraduccionUA toc = new TraduccionUA();
		toc.setNombre("govern illes balears");
		oc.setTraduccion("ca", toc);
		procedimiento.setUnidadAdministrativa(oc);

		t.setProcedimiento(procedimiento);
				
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
		if(vuds) {
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

}
