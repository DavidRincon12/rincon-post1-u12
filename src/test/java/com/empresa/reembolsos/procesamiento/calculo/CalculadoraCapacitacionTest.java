package com.empresa.reembolsos.procesamiento.calculo;

import org.junit.jupiter.api.Test;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.CAPACITACION;
import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraCapacitacionTest {

    private final CalculadoraCapacitacion calculadora = new CalculadoraCapacitacion();

    @Test
    void reembolsaElOchentaPorCiento() {
        assertThat(calculadora.calcular(solicitud(CAPACITACION, "1000000"))).isEqualByComparingTo("800000");
    }

    @Test
    void limitaAlTopeAnual() {
        assertThat(calculadora.calcular(solicitud(CAPACITACION, "5000000"))).isEqualByComparingTo("3000000");
    }
}
