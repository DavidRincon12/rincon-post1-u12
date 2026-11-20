package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

/**
 * Estrategia de calculo del monto aprobado para una categoria de gasto (Strategy).
 * Una categoria nueva se agrega con una implementacion nueva anotada con {@code @Component}.
 */
public interface CalculadoraReembolso {

    /** Categoria que atiende esta estrategia; debe ser unica entre todas las calculadoras. */
    CategoriaGasto getCategoria();

    /** Calcula el monto aprobado de la solicitud segun la formula de la categoria. */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
