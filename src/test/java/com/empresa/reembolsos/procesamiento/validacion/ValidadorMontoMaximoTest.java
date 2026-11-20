package com.empresa.reembolsos.procesamiento.validacion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.CAPACITACION;
import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static com.empresa.reembolsos.dominio.CategoriaGasto.VIAJE;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo();

    @Test
    void rechazaRepresentacionQueSuperaElTope() {
        var resultado = validador.validar(solicitud(REPRESENTACION, "2000001"), List.of());

        assertThat(resultado.valida()).isFalse();
        assertThat(resultado.motivoRechazo()).isEqualTo("Monto excede el tope de la categoria REPRESENTACION");
    }

    @Test
    void apruebaMontoIgualAlTope() {
        assertThat(validador.validar(solicitud(CAPACITACION, "3000000"), List.of()).valida()).isTrue();
    }

    @Test
    void elTopeDeViajeEsDiarioPorDiasDeViaje() {
        var tresDias = solicitud(VIAJE, "450000");
        tresDias.setDiasViaje(3);
        var excedido = solicitud(VIAJE, "450001");
        excedido.setDiasViaje(3);

        assertThat(validador.validar(tresDias, List.of()).valida()).isTrue();
        assertThat(validador.validar(excedido, List.of()).valida()).isFalse();
    }
}
