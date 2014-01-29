package es.caib.persistence.actualizador.vuds;

import java.util.ArrayList;
import java.util.List;


public class ValidacionTramiteVuds {
	
	List<String> sinTraducir = new ArrayList<String>();  
	
	public List<String> getSinTraducir() {
		return sinTraducir;
	}

	public boolean esValido() {
		if(0==sinTraducir.size()) 
			return true;
		else 
			return false;
	}
	public void addCampoSinTraducir(String campo) {
		sinTraducir.add(campo);
		
	}
}
