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

## Decisiones de diseño

### Decisión 1 — Orden de la cadena de validadores

Orden elegido: **datos obligatorios → documentos soporte → monto máximo → duplicados** (`CadenaValidadores`).

- **Criterio de eficiencia:** la cadena corta en el primer rechazo, así que conviene poner primero lo barato y al final lo caro. Documentos y datos obligatorios leen un campo, el monto consulta un mapa y duplicados recorre toda la lista de pendientes del empleado, que es el único costo que crece con los datos.
- **Qué se gana:** una solicitud sin documentos o con un monto excesivo se rechaza sin recorrer la lista de pendientes.
- **Qué se pierde:** si duplicados fuera primero, el empleado vería antes el rechazo por duplicado (que suele ser el más informativo cuando reintenta), pero se pagaría el recorrido en todas las solicitudes, incluso las que iban a caer por documentos. Además, el servicio sigue cargando las pendientes antes de la cadena; mover esa consulta al eslabón la evitaría en los rechazos tempranos, pero metería persistencia en `procesamiento` (ver Regla 1).
- **Eslabón adicional:** `ValidadorDatosObligatorios` se agregó al inicio sin tocar los otros tres, que es justo la extensión que pide el RF1. Valida en el borde que lleguen empleado, categoría, monto positivo y fecha antes de que los demás eslabones los usen.

### Decisión 2 — Selección de la calculadora por categoría

Elegido: **mapa construido desde la `List<CalculadoraReembolso>` que inyecta Spring** (`SelectorCalculadora`), en lugar de un `switch` sobre `CategoriaGasto`.

- Con el mapa, una categoría nueva ("bienestar") se agrega con una clase nueva anotada con `@Component` y una constante nueva en el enum; `SelectorCalculadora` y las calculadoras existentes no cambian. Con el `switch`, cada categoría nueva obliga a editar el método del selector, que es justo lo que el RF2 pide evitar (principio abierto/cerrado).
- `Collectors.toMap` falla al arrancar si dos calculadoras declaran la misma categoría, así que un error de configuración se detecta en el arranque y no en una solicitud real.
- El costo: el flujo es menos explícito que un `switch` (hay que saber que Spring inyecta todas las implementaciones); lo compensa el Javadoc de la interfaz y la prueba `SelectorCalculadoraTest`.

## Qué se descartó y por qué

Criterio (Guía, sección 4.2): ¿la variabilidad que justificaría la alternativa es real y está documentada hoy, o es especulación sobre un futuro que aún no llegó? Cada descarte es una decisión vigente hoy, no definitiva: se indica la señal que haría reconsiderarla.

### 1. `ApplicationEventPublisher` en vez de una lista de observadores inyectada (referencia)

- **Qué se consideró:** publicar un evento de dominio con `ApplicationEventPublisher` y `@EventListener` (Guía, sección 2.3), en lugar de inyectar `List<ObservadorCambioEstado>` en el servicio.
- **Por qué se descartó:** con tres observadores fijos y conocidos en tiempo de compilación, un bus de eventos genérico no aporta desacoplamiento real (ya lo da la interfaz `ObservadorCambioEstado`) y sí dificulta rastrear en el código quién reacciona a qué cambio.
- **Señal para reconsiderar:** que algún interesado deba reaccionar de forma asíncrona o después del commit de la transacción (`@TransactionalEventListener`), por ejemplo cuando la API externa de Contabilidad sea lenta y no deba bloquear la respuesta.

### 2. Visitor o Abstract Factory para exportar reportes en varios formatos (referencia)

- **Qué se consideró:** anticipar exportaciones del historial en PDF y Excel además de CSV con una jerarquía de exportadores.
- **Por qué se descartó:** hoy solo existe el requisito de CSV; no hay una segunda solicitud real de otro formato, así que no se cumple la Rule of Three.
- **Señal para reconsiderar:** una segunda petición concreta de formato (por ejemplo, Excel para el reporte trimestral de auditoría).

### 3. Patrón State para las transiciones de `EstadoSolicitud` (propio)

- **Qué se consideró:** al escribir `cambiarEstado`, fue tentador modelar cada estado (`PENDIENTE`, `APROBADA`, `RECHAZADA`, `REQUIERE_CORRECCION`) como una clase con sus transiciones permitidas, para impedir, por ejemplo, aprobar una solicitud ya rechazada.
- **Por qué se descartó:** la especificación no define ninguna regla de transición; solo dice que, ante un cambio de estado, se notifica a los interesados. Inventar esas reglas sería resolver un problema que Finanzas no ha planteado (YAGNI), y aplicando el test de explicabilidad no hay hoy una oración que justifique cuatro clases de estado: "porque los estados podrían tener reglas" describe un futuro posible, no un requisito. Un `enum` y una asignación cubren el comportamiento pedido.
- **Señal para reconsiderar:** que Finanzas defina reglas de transición reales (por ejemplo, que solo el empleado pueda pasar de `REQUIERE_CORRECCION` a `PENDIENTE`, o que una solicitud aprobada no pueda volver a cambiar). Con dos o tres de esas reglas, primero bastaría una tabla de transiciones permitidas en el enum; State entraría si cada estado necesitara además comportamiento propio.

### 4. Puerto de persistencia `RepositorioSolicitudes` (propio, ver ADR-001)

- **Qué se consideró:** una interfaz de puerto entre el servicio y Spring Data, por simetría con una arquitectura Hexagonal.
- **Por qué se descartó:** una interfaz con una sola implementación y sin plan de cambiar de motor es una abstracción especulativa; el dominio ya quedó libre de JPA gracias a `orm.xml`, que era el beneficio real que se buscaba.
- **Señal para reconsiderar:** un cambio de motor o de tecnología de persistencia anunciado, o un segundo consumidor de la persistencia.
