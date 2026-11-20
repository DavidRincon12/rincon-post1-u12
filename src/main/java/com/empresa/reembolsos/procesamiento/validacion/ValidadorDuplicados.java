package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

/**
 * Verifica que no exista otra solicitud pendiente del mismo empleado, categoria y periodo (mes calendario).
 * No consulta la base de datos: recibe las pendientes ya cargadas por el servicio de aplicacion.
 */
@Component
public class ValidadorDuplicados extends ValidadorSolicitud {
    private RepositorioSolicitudesJpa repositorio; // VIOLACION INTENCIONAL de la Regla 1

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        var periodo = YearMonth.from(solicitud.getFechaGasto());
        boolean existeDuplicado = pendientes.stream()
            .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && YearMonth.from(p.getFechaGasto()).equals(periodo));
        return existeDuplicado
            ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo")
            : ResultadoValidacion.ok();
    }
}
