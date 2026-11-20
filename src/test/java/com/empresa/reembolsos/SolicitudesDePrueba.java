package com.empresa.reembolsos;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fabrica de solicitudes validas para las pruebas; cada prueba cambia solo lo que le interesa. */
public final class SolicitudesDePrueba {

    private SolicitudesDePrueba() { }

    public static SolicitudReembolso solicitud(CategoriaGasto categoria, String monto) {
        var s = new SolicitudReembolso();
        s.setEmpleadoId("E-001");
        s.setCategoria(categoria);
        s.setMontoSolicitado(new BigDecimal(monto));
        s.setFechaGasto(LocalDate.of(2026, 3, 15));
        s.setDiasViaje(categoria == CategoriaGasto.VIAJE ? 1 : 0);
        s.setDocumentosSoporte(true);
        return s;
    }
}
