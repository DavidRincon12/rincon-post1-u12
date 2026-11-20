package com.empresa.reembolsos.procesamiento.validacion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.CAPACITACION;
import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();

    @Test
    void rechazaSiHayPendienteDeLaMismaCategoriaEnElMismoMes() {
        var pendiente = solicitud(REPRESENTACION, "50000");
        pendiente.setFechaGasto(LocalDate.of(2026, 3, 2));

        var resultado = validador.validar(solicitud(REPRESENTACION, "100000"), List.of(pendiente));

        assertThat(resultado.valida()).isFalse();
        assertThat(resultado.motivoRechazo())
            .isEqualTo("Ya existe una solicitud pendiente de la misma categoria en el periodo");
    }

    @Test
    void apruebaSiLaPendienteEsDelMismoMesDeOtroAnio() {
        var pendiente = solicitud(REPRESENTACION, "50000");
        pendiente.setFechaGasto(LocalDate.of(2025, 3, 15));

        assertThat(validador.validar(solicitud(REPRESENTACION, "100000"), List.of(pendiente)).valida()).isTrue();
    }

    @Test
    void apruebaSiLaPendienteEsDeOtraCategoria() {
        var pendiente = solicitud(CAPACITACION, "50000");

        assertThat(validador.validar(solicitud(REPRESENTACION, "100000"), List.of(pendiente)).valida()).isTrue();
    }
}
