package com.empresa.reembolsos.procesamiento.validacion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.SolicitudesDePrueba.solicitud;
import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static org.assertj.core.api.Assertions.assertThat;

class CadenaValidadoresTest {

    private final CadenaValidadores cadena = new CadenaValidadores(new ValidadorDatosObligatorios(),
        new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados());

    @Test
    void cortaEnElPrimerEslabonQueRechaza() {
        var s = solicitud(REPRESENTACION, "9000000");
        s.setDocumentosSoporte(false);

        assertThat(cadena.validar(s, List.of(solicitud(REPRESENTACION, "1"))).motivoRechazo())
            .isEqualTo("Faltan documentos soporte");
    }

    @Test
    void llegaAlUltimoEslabonCuandoLosAnterioresAprueban() {
        assertThat(cadena.validar(solicitud(REPRESENTACION, "100000"), List.of(solicitud(REPRESENTACION, "1")))
            .motivoRechazo()).startsWith("Ya existe");
    }

    @Test
    void apruebaUnaSolicitudQuePasaTodasLasVerificaciones() {
        assertThat(cadena.validar(solicitud(REPRESENTACION, "100000"), List.of()).valida()).isTrue();
    }
}
