package com.empresa.reembolsos.procesamiento.calculo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.dominio.CategoriaGasto.CAPACITACION;
import static com.empresa.reembolsos.dominio.CategoriaGasto.VIAJE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectorCalculadoraTest {

    @Test
    void devuelveLaCalculadoraDeLaCategoria() {
        var selector = new SelectorCalculadora(List.of(new CalculadoraViaje(), new CalculadoraCapacitacion()));

        assertThat(selector.obtener(CAPACITACION)).isInstanceOf(CalculadoraCapacitacion.class);
    }

    @Test
    void fallaConUnaCategoriaSinCalculadora() {
        var selector = new SelectorCalculadora(List.of(new CalculadoraCapacitacion()));

        assertThatThrownBy(() -> selector.obtener(VIAJE))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Categoria no soportada: VIAJE");
    }
}
