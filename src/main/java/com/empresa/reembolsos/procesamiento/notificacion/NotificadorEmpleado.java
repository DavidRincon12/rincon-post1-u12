package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Avisa al empleado el resultado de su solicitud en cada cambio de estado. */
@Component
public class NotificadorEmpleado implements ObservadorCambioEstado {
    private static final Logger log = LoggerFactory.getLogger(NotificadorEmpleado.class);

    @Override
    public void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior) {
        log.info("Notificando a {}: su solicitud paso de {} a {}",
            solicitud.getEmpleadoId(), estadoAnterior, solicitud.getEstado());
    }
}
