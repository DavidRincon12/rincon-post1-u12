package com.empresa.reembolsos.procesamiento.calculo;

import org.junit.jupiter.api.Test;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.VIAJE;
import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    @Test
    void limitaAlTopeDiarioPorLosDiasDelViaje() {
        var s = solicitud(VIAJE, "500000");
        s.setDiasViaje(3);

        assertThat(calculadora.calcular(s)).isEqualByComparingTo("450000");
    }

    @Test
    void reembolsaElMontoCompletoSiEstaBajoElTope() {
        var s = solicitud(VIAJE, "200000");
        s.setDiasViaje(2);

        assertThat(calculadora.calcular(s)).isEqualByComparingTo("200000");
    }
}
