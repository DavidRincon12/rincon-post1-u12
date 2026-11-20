package com.empresa.reembolsos.procesamiento.validacion;

/** Resultado de un eslabon de validacion: valido, o rechazado con su motivo. */
public record ResultadoValidacion(boolean valida, String motivoRechazo) {

    public static ResultadoValidacion ok() {
        return new ResultadoValidacion(true, null);
    }

    public static ResultadoValidacion rechazar(String motivo) {
        return new ResultadoValidacion(false, motivo);
    }
}
