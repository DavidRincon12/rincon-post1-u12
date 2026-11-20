package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Verifica que la solicitud traiga los datos minimos para evaluar las demas reglas. */
@Component
public class ValidadorDatosObligatorios extends ValidadorSolicitud {

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        if (solicitud.getEmpleadoId() == null || solicitud.getEmpleadoId().isBlank()
            || solicitud.getCategoria() == null || solicitud.getMontoSolicitado() == null
            || solicitud.getFechaGasto() == null) {
            return ResultadoValidacion.rechazar("Empleado, categoria, monto y fecha del gasto son obligatorios");
        }
        return solicitud.getMontoSolicitado().compareTo(BigDecimal.ZERO) > 0
            ? ResultadoValidacion.ok()
            : ResultadoValidacion.rechazar("El monto solicitado debe ser mayor que cero");
    }
}
