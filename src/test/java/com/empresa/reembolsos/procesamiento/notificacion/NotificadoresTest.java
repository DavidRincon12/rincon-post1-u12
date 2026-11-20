package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.math.BigDecimal;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static com.empresa.reembolsos.dominio.EstadoSolicitud.APROBADA;
import static com.empresa.reembolsos.dominio.EstadoSolicitud.PENDIENTE;
import static com.empresa.reembolsos.dominio.EstadoSolicitud.RECHAZADA;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class NotificadoresTest {

    private static SolicitudReembolso conEstado(EstadoSolicitud estado) {
        var s = solicitud(REPRESENTACION, "100000");
        s.setId(7L);
        s.setMontoAprobado(new BigDecimal("100000"));
        s.setEstado(estado);
        return s;
    }

    @Test
    void elEmpleadoRecibeElResultado(CapturedOutput salida) {
        new NotificadorEmpleado().onCambioEstado(conEstado(RECHAZADA), PENDIENTE);

        assertThat(salida).contains("Notificando a E-001: su solicitud paso de PENDIENTE a RECHAZADA");
    }

    @Test
    void contabilidadSoloActuaEnAprobaciones(CapturedOutput salida) {
        var contabilidad = new NotificadorContabilidad();

        contabilidad.onCambioEstado(conEstado(RECHAZADA), PENDIENTE);
        assertThat(salida).doesNotContain("Contabilidad");

        contabilidad.onCambioEstado(conEstado(APROBADA), PENDIENTE);
        assertThat(salida).contains("Contabilidad: programar pago de 100000 para E-001");
    }

    @Test
    void auditoriaRegistraTodoCambio(CapturedOutput salida) {
        var auditoria = new NotificadorAuditoria();

        auditoria.onCambioEstado(conEstado(RECHAZADA), PENDIENTE);
        auditoria.onCambioEstado(conEstado(APROBADA), RECHAZADA);

        assertThat(salida).contains("Auditoria: solicitud 7 cambio de PENDIENTE a RECHAZADA")
                          .contains("Auditoria: solicitud 7 cambio de RECHAZADA a APROBADA");
    }
}
