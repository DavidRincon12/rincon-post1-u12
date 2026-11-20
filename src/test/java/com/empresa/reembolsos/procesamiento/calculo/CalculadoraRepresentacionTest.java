package com.empresa.reembolsos.procesamiento.calculo;

import org.junit.jupiter.api.Test;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    @Test
    void reembolsaElMontoCompletoBajoElTopeMensual() {
        assertThat(calculadora.calcular(solicitud(REPRESENTACION, "750000"))).isEqualByComparingTo("750000");
    }

    @Test
    void limitaAlTopeMensual() {
        assertThat(calculadora.calcular(solicitud(REPRESENTACION, "2500000"))).isEqualByComparingTo("2000000");
    }
}
