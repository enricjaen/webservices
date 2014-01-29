package org.ibit.rol.sac.back.negocio.vuds;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TramiteVudsInvalidoTest {

	@Test
	public void testGetDetalles1campo() {
	
		List<String> sinTraducir = new ArrayList<String>();
		sinTraducir.add("nom del tramit");
		EstadoTramiteVuds estadoInvalido= new TramiteVudsInvalido("",sinTraducir);
		assertEquals("falta traduir: nom del tramit", estadoInvalido.getDetalles());
	}

	@Test
	public void testGetDetalles2campos() {

		List<String> sinTraducir = new ArrayList<String>();
		sinTraducir.add("nom del tramit");
		sinTraducir.add("descripcio");
		EstadoTramiteVuds estadoInvalido= new TramiteVudsInvalido("",sinTraducir);
		assertEquals("falta traduir: nom del tramit, descripcio", estadoInvalido.getDetalles());
	}
}
