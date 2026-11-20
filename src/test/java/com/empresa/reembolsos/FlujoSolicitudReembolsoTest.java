package com.empresa.reembolsos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo completo por HTTP: crear, validar, calcular y notificar, contra H2 en memoria. */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FlujoSolicitudReembolsoTest {

    @Autowired
    private MockMvc mvc;

    private ResultActions crear(String empleado, String categoria, String monto, int dias, boolean documentos)
            throws Exception {
        var json = """
            {"empleadoId":"%s","categoria":"%s","montoSolicitado":%s,"fechaGasto":"2026-03-15",
             "diasViaje":%d,"documentosSoporte":%b}""".formatted(empleado, categoria, monto, dias, documentos);
        return mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void calculaElMontoAprobadoDeLasTresCategorias() throws Exception {
        crear("E-10", "VIAJE", "400000", 3, true).andExpect(status().isOk())
            .andExpect(jsonPath("$.montoAprobado").value(400000))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        crear("E-10", "REPRESENTACION", "750000", 0, true).andExpect(jsonPath("$.montoAprobado").value(750000));
        crear("E-10", "CAPACITACION", "1000000", 0, true).andExpect(jsonPath("$.montoAprobado").value(800000.0));
    }

    @Test
    void rechazaSinDocumentosSoporteConMensajeClaro() throws Exception {
        crear("E-20", "REPRESENTACION", "100000", 0, false)
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("Faltan documentos soporte"));
    }

    @Test
    void rechazaUnaSegundaPendienteDeLaMismaCategoriaYPeriodo() throws Exception {
        crear("E-30", "CAPACITACION", "100000", 0, true).andExpect(status().isOk());
        crear("E-30", "CAPACITACION", "200000", 0, true)
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("Ya existe una solicitud pendiente de la misma categoria en el periodo"));
    }

    @Test
    void noPermiteSobrescribirUnaSolicitudExistenteEnviandoSuId() throws Exception {
        var original = crear("E-40", "REPRESENTACION", "100000", 0, true).andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(original, "$.id");

        var json = """
            {"id":%d,"empleadoId":"E-41","categoria":"VIAJE","montoSolicitado":1000,"fechaGasto":"2026-03-15",
             "diasViaje":1,"documentosSoporte":true}""".formatted(id);
        mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.not(id)));
    }

    @Test
    void cambiarEstadoNotificaALosTresInteresados(CapturedOutput salida) throws Exception {
        var creada = crear("E-50", "REPRESENTACION", "300000", 0, true).andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(creada, "$.id");

        mvc.perform(put("/api/solicitudes/{id}/estado", id).contentType(MediaType.APPLICATION_JSON).content("\"RECHAZADA\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("RECHAZADA"));
        assertThat(salida).contains("Notificando a E-50: su solicitud paso de PENDIENTE a RECHAZADA")
                          .contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a RECHAZADA")
                          .doesNotContain("Contabilidad: programar pago");

        mvc.perform(put("/api/solicitudes/{id}/estado", id).contentType(MediaType.APPLICATION_JSON).content("\"APROBADA\""))
            .andExpect(status().isOk());
        assertThat(salida).contains("Contabilidad: programar pago de 300000");
    }

    @Test
    void cambiarEstadoDeUnaSolicitudInexistenteResponde404() throws Exception {
        mvc.perform(put("/api/solicitudes/{id}/estado", 999999).contentType(MediaType.APPLICATION_JSON).content("\"APROBADA\""))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Solicitud no encontrada: 999999"));
    }
}
