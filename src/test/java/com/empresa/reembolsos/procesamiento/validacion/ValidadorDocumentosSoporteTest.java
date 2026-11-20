package com.empresa.reembolsos.procesamiento.validacion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    @Test
    void rechazaCuandoFaltanDocumentosSoporte() {
        var s = solicitud(REPRESENTACION, "100000");
        s.setDocumentosSoporte(false);

        var resultado = validador.validar(s, List.of());

        assertThat(resultado.valida()).isFalse();
        assertThat(resultado.motivoRechazo()).isEqualTo("Faltan documentos soporte");
    }

    @Test
    void apruebaCuandoTieneDocumentosSoporte() {
        assertThat(validador.validar(solicitud(REPRESENTACION, "100000"), List.of()).valida()).isTrue();
    }
}
