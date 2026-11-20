package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Interesado en los cambios de estado de una solicitud (Observer).
 * Cada implementacion decide como reacciona; un interesado nuevo se agrega con otra implementacion
 * anotada con {@code @Component}, sin tocar el servicio que publica el cambio.
 */
public interface ObservadorCambioEstado {

    /**
     * Se invoca despues de guardar el nuevo estado.
     *
     * @param solicitud      solicitud con su estado nuevo
     * @param estadoAnterior estado que tenia antes del cambio
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
