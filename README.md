# Sistema de Solicitudes de Reembolso de Gastos Corporativos

Post-contenido de la Unidad 12 (Patrones de Diseño de Software): proyecto integrador `reembolsos-integrado`, con Spring Boot 3, Java 17, Spring Data JPA y H2.

Los empleados registran una solicitud de reembolso con la categoría del gasto, el monto, la fecha y sus documentos soporte. El sistema valida la solicitud, calcula el monto aprobado según la categoría y avisa a los interesados cada vez que la solicitud cambia de estado.

## Cómo ejecutar

```bash
mvn spring-boot:run
```

```bash
mvn verify
```

## Diseño Propuesto

Este análisis se escribió antes de implementar cualquier patrón. Para cada requisito se identifica qué varía, qué patrón lo resuelve y por qué ese y no otro (test de explicabilidad: una oración).

| Requisito | Qué varía | Patrón | Justificación (una oración) |
|---|---|---|---|
| RF1 — Validación en varias etapas | El conjunto y el orden de las verificaciones que debe pasar una solicitud; Finanzas agregará verificaciones nuevas (p. ej., límite anual acumulado). | **Chain of Responsibility** | Cada verificación es un eslabón que decide por sí solo si rechaza o deja pasar al siguiente, así que una verificación nueva se agrega como un eslabón más sin tocar las existentes. |
| RF2 — Cálculo distinto por categoría | La fórmula del monto aprobado, que depende de la categoría del gasto; vendrán categorías nuevas (p. ej., "bienestar"). | **Strategy** | Cada categoría encapsula su fórmula en una clase intercambiable detrás de la misma interfaz, de modo que una categoría nueva es una clase nueva y no una rama más en un `switch` compartido. |
| RF3 — Notificación simultánea | Quiénes reaccionan a un cambio de estado y cómo reacciona cada uno; ya se anunció un cuarto interesado (panel de métricas). | **Observer** | El servicio solo publica "la solicitud cambió de estado" a una lista de observadores, y cada interesado decide su reacción, así que sumar el panel de métricas es agregar un observador sin modificar el servicio. |

Por qué no otras opciones:

- **RF1 no es Strategy:** las verificaciones no son alternativas excluyentes, sino pasos que se ejecutan en secuencia y que cortan al primer rechazo.
- **RF2 no es Template Method:** las tres fórmulas no comparten un esqueleto de pasos; lo único común es la firma `calcular(solicitud)`.
- **RF3 no es Chain of Responsibility:** todos los interesados deben enterarse siempre, mientras que en una cadena un eslabón puede detener la propagación.

### Observaciones sobre la especificación

- **Tope de viáticos.** El RF2 define el tope de viaje como un tope diario multiplicado por los días del viaje. Por coherencia, la validación de monto máximo usa ese mismo tope (150.000 × días) para la categoría VIAJE; con un tope fijo de 150.000, un viaje legítimo de 3 días por 400.000 sería rechazado.
- **Período de duplicados.** El período se interpreta como el mes calendario del gasto (año y mes, `YearMonth`), para que enero de 2025 no se confunda con enero de 2026.
- **Dominio sin frameworks.** `SolicitudReembolso` no importa Spring ni JPA; su mapeo a la base de datos se declara en `META-INF/orm.xml`.
