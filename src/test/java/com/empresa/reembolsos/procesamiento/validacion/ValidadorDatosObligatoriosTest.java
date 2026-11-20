package com.empresa.reembolsos.procesamiento.validacion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDatosObligatoriosTest {

    private final ValidadorDatosObligatorios validador = new ValidadorDatosObligatorios();

    @Test
    void rechazaSinCategoria() {
        var s = solicitud(REPRESENTACION, "100");
        s.setCategoria(null);

        assertThat(validador.validar(s, List.of()).motivoRechazo())
            .isEqualTo("Empleado, categoria, monto y fecha del gasto son obligatorios");
    }

    @Test
    void rechazaMontoCeroONegativo() {
        assertThat(validador.validar(solicitud(REPRESENTACION, "0"), List.of()).motivoRechazo())
            .isEqualTo("El monto solicitado debe ser mayor que cero");
    }

    @Test
    void apruebaUnaSolicitudCompleta() {
        assertThat(validador.validar(solicitud(REPRESENTACION, "100"), List.of()).valida()).isTrue();
    }
}
