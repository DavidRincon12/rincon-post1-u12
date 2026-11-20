package com.empresa.reembolsos.aplicacion;

/** La solicitud no paso la cadena de validacion; el mensaje es el motivo del rechazo. */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String motivo) {
        super(motivo);
    }
}
