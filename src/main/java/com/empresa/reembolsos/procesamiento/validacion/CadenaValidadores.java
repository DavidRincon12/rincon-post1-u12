package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Arma la cadena de validacion. El orden va de lo mas barato a lo mas caro:
 * datos obligatorios y documentos (un campo), monto (un mapa) y por ultimo duplicados (recorre la lista de pendientes).
 */
@Component
public class CadenaValidadores {
    private final ValidadorSolicitud primero;

    public CadenaValidadores(ValidadorDatosObligatorios datos,
                             ValidadorDocumentosSoporte documentos,
                             ValidadorMontoMaximo monto,
                             ValidadorDuplicados duplicados) {
        datos.siguiente(documentos).siguiente(monto).siguiente(duplicados);
        this.primero = datos;
    }

    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        return primero.validar(solicitud, pendientes);
    }
}
