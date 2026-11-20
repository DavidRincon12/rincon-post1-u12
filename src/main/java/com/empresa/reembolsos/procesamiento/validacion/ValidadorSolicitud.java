package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.util.List;

/**
 * Eslabon de la cadena de validacion (Chain of Responsibility).
 * Cada subclase implementa una sola verificacion; si aprueba, la solicitud pasa al siguiente eslabon
 * sin que ninguno sepa cuantos hay despues de el.
 */
public abstract class ValidadorSolicitud {
    private ValidadorSolicitud siguiente;

    /**
     * Encadena el siguiente eslabon.
     *
     * @return el eslabon recibido, para encadenar con fluidez: {@code a.siguiente(b).siguiente(c)}
     */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * Ejecuta esta verificacion y, si aprueba, las siguientes.
     *
     * @param solicitud              solicitud a validar
     * @param pendientesDelEmpleado  solicitudes pendientes del mismo empleado, ya cargadas por el servicio
     * @return el primer rechazo encontrado, o {@link ResultadoValidacion#ok()} si todas aprueban
     */
    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    /** Verificacion propia de este eslabon. */
    protected abstract ResultadoValidacion validarPropio(
        SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado);
}
