# **Ingeniería de Software**

**Autor:** Juan Camilo Cuenca Sepúlveda

# **Levantamiento de Requerimientos** 

## **1\. Requerimientos Funcionales**

### **1.1 Gestión de Inventario**

* El sistema debe permitir crear, consultar, actualizar y eliminar productos del inventario (CRUD completo).  
* Cada sucursal debe poder visualizar su propio catálogo de productos con paginación y filtros.  
* Cada sucursal debe poder consultar el stock de cualquier otra sucursal de la red.  
* El sistema debe registrar ingresos de productos (compras, devoluciones, ajustes) y retiros (ventas, mermas).  
* El sistema debe controlar un stock mínimo configurable por producto y sucursal.  
* Cada movimiento de inventario queda registrado en `inventory_transaction` con fecha, responsable, motivo y cantidad.  
* El sistema debe soportar múltiples unidades de medida por producto.

### **1.2 Módulo de Compras**

* El sistema debe permitir crear órdenes de compra asociadas a un proveedor.  
* Debe registrar precio unitario y descuentos por línea de compra.  
* Al confirmar la recepción, el inventario de la sucursal se actualiza automáticamente.  
* El sistema calcula el Costo Promedio Ponderado del inventario tras cada compra.

### **1.3 Módulo de Ventas**

* El sistema debe registrar transacciones de venta por producto, cantidad y precio.  
* Cada venta se asocia a una sucursal, fecha y usuario responsable.  
* El sistema valida disponibilidad de stock antes de confirmar la venta.  
* El sistema genera un recibo de venta en formato PDF automáticamente al confirmar.

### **1.4 Transferencias entre Sucursales**

El ciclo de vida de una transferencia contempla los siguientes estados: `PENDING → PREPARING → SHIPPED → IN_TRANSIT → RECEIVED` (con soporte para recepciones parciales).

* La sucursal destino solicita una transferencia indicando producto, cantidad y sucursal origen.  
* La sucursal origen confirma o ajusta la cantidad disponible para enviar.  
* Se registra el despacho con transportista y fecha estimada de llegada.  
* Al confirmar recepción completa, el inventario destino se actualiza automáticamente.  
* El sistema soporta recepción parcial.

### **1.5 Módulo de Logística**

* El sistema registra tiempos estimados y reales de entrega por transferencia.  
* Permite consultar el estado actual de cada traslado en curso.  
* Calcula el cumplimiento logístico comparando `estimatedArrival` vs `receivedAt`.

### **1.6 Dashboard y Análisis**

* Muestra volumen de ventas del mes en curso comparado con meses anteriores.  
* Presenta indicadores de rotación de inventario y productos de alta/baja demanda.  
* Muestra estado de transferencias activas y su impacto sobre el stock.  
* Indica productos próximos a alcanzar el stock mínimo.  
* Los perfiles Administrador y Gerente acceden a comparativas de rendimiento entre sucursales.

### **1.7 Funcionalidades Adicionales Implementadas**

* **Auditoría con AOP:** registro automático y transparente de cada acción sobre el sistema mediante anotación `@Auditable`, sin modificar la lógica de negocio.  
* **Alertas de stock:** notificaciones Toast en interfaz y correo electrónico automático a Administradores y Gerentes cuando el stock cae por debajo del mínimo configurado.  
* **Reportes PDF:** generación automática de recibos de venta al confirmar una transacción.  
* **Importación CSV:** carga masiva de productos mediante archivo `.csv` con validación de formato.  
* **Gestión de proveedores:** registro de proveedores, asociación a productos y seguimiento de condiciones comerciales.

## **2\. Requerimientos No Funcionales**

### **2.1 Rendimiento**

* Las consultas de inventario y listados paginados deben responder en tiempos razonables bajo carga normal de uso.  
* Todas las listas implementan paginación obligatoria para evitar consultas sin límite sobre la base de datos.

### **2.2 Seguridad**

* La autenticación se implementa mediante JWT con expiración configurable.  
* El acceso a recursos está controlado por tres roles: Administrador General, Gerente de Sucursal y Operador.  
* Las contraseñas se almacenan hasheadas con BCrypt.  
* Los endpoints protegidos retornan `401` o `403` según corresponda cuando el acceso no está autorizado.

### **2.3 Escalabilidad**

* La separación en tres capas independientes (frontend, backend, base de datos) permite escalar cada componente por separado si el proyecto lo requiere en el futuro.  
* El modelo de datos soporta agregar nuevas sucursales sin cambios estructurales en el esquema.

### **2.4 Usabilidad**

* La interfaz es responsiva y funciona en dispositivos de escritorio y móviles.  
* Todas las operaciones asíncronas muestran indicadores de carga.  
* Los errores se comunican al usuario mediante notificaciones Toast con mensajes descriptivos.

### **2.5 Mantenibilidad**

* La lógica transversal de auditoría está separada mediante AOP, evitando duplicación en los servicios.  
* La API está documentada con Swagger, accesible en `/swagger-ui/index.html`.  
* El código sigue convenciones de nomenclatura consistentes en paquetes, clases y endpoints.

## **3\. Restricciones Técnicas y de Negocio**

### **3.1 Restricciones Técnicas**

* El sistema debe levantarse completamente con `docker compose up --build` sin configuración manual adicional.  
* El frontend se comunica con el backend exclusivamente a través de la API REST. No existe lógica de negocio en el cliente.  
* La solución mantiene separación estricta de tres capas: presentación, negocio y datos.

### **3.2 Restricciones de Negocio**

* Un operador solo puede gestionar el inventario de su sucursal asignada.  
* Las transferencias requieren confirmación explícita de la sucursal origen antes del despacho.  
* Todo ajuste o retiro de inventario debe registrar un motivo obligatorio.  
* Los dashboards comparativos entre sucursales son visibles únicamente para Administrador y Gerente.

## **4\. Supuestos y Dependencias**

### **4.1 Supuestos**

* Cada usuario pertenece a una única sucursal, excepto el Administrador General que tiene visibilidad sobre toda la red.  
* Los productos tienen un SKU único a nivel de toda la organización.  
* El volumen de datos del proyecto es manejable con PostgreSQL sin necesidad de estrategias adicionales de particionamiento.

### **4.2 Dependencias del Sistema**

| Componente | Tecnología |
| ----- | ----- |
| Backend | Java \+ Spring Boot 3 |
| Frontend | React \+ Vite \+ Tailwind CSS |
| Base de datos | PostgreSQL 16 |
| Infraestructura | Docker \+ Docker Compose |
| Autenticación | Spring Security \+ JWT |
| Documentación API | Swagger / OpenAPI |

# 

# **Casos de Uso**

## **1\. Actores del Sistema**

| Actor | Responsabilidades |
| ----- | ----- |
| **Administrador General** | Visibilidad total de la red. Gestiona usuarios, sucursales, configuraciones globales y consulta reportes de todas las sedes. |
| **Gerente de Sucursal** | Supervisa las operaciones de su sucursal, aprueba o rechaza transferencias y consulta dashboards comparativos de rendimiento. |
| **Operador de Inventario** | Ejecuta las transacciones diarias: registra compras, ventas, ingresos, retiros y solicita transferencias entre sucursales. |

---

## **2\. Casos de Uso por Módulo**

### **2.1 Dashboard**

| ID | Caso de Uso | Actores | Descripción |
| ----- | ----- | ----- | ----- |
| CU-01 | Ver reportes y métricas | Administrador, Gerente | Consulta indicadores de ventas, rotación de inventario, transferencias activas y productos próximos a agotarse. El Administrador tiene visibilidad de todas las sucursales; el Gerente solo de la suya. |

---

### **2.2 Usuarios**

| ID | Caso de Uso | Actores | Descripción |
| ----- | ----- | ----- | ----- |
| CU-02 | Gestionar usuarios | Administrador | Crear, editar, desactivar y asignar roles y sucursal a los usuarios del sistema. |

---

### **2.3 Sucursales**

| ID | Caso de Uso | Actores | Descripción |
| ----- | ----- | ----- | ----- |
| CU-03 | Gestionar sucursales | Administrador | Crear y administrar las sucursales de la red, incluyendo nombre y ubicación. |

---

### **2.4 Inventario**

| ID | Caso de Uso | Actores | Descripción |
| ----- | ----- | ----- | ----- |
| CU-04 | Consultar inventario propio | Administrador, Gerente, Operador | Visualizar el catálogo de productos y stock disponible en la sucursal del usuario autenticado. |
| CU-05 | Consultar inventario de otras sucursales `<<extend>>` | Administrador, Gerente | Extensión de CU-04. Permite consultar el stock de cualquier otro nodo de la red para facilitar decisiones de transferencia. |

---

### **2.5 Compras**

| ID | Caso de Uso | Actores | Descripción |
| ----- | ----- | ----- | ----- |
| CU-06 | Solicitar compra de productos | Operador | Crear una orden de compra a un proveedor indicando productos, cantidades y condiciones comerciales. |
| CU-07 | Registrar recepción de mercancía `<<include>>` | Operador | Incluido en CU-06. Al confirmar la llegada de la mercancía, el inventario de la sucursal se actualiza automáticamente y se recalcula el Costo Promedio Ponderado. |

---

### **2.6 Ventas**

| ID | Caso de Uso | Actores | Descripción |
| ----- | ----- | ----- | ----- |
| CU-08 | Registrar venta de productos | Operador | Registrar una transacción de venta asociada a la sucursal, validando stock disponible antes de confirmar. |
| CU-09 | Generar comprobante de venta `<<include>>` | Sistema | Incluido en CU-08. Al confirmar la venta se genera automáticamente un recibo en formato PDF. |
| CU-10 | Aplicar descuento `<<extend>>` | Operador | Extensión de CU-08. Permite aplicar un descuento porcentual o por valor fijo antes de confirmar la venta. |

---

### **2.7 Transferencias**

| ID | Caso de Uso | Actores | Descripción |
| ----- | ----- | ----- | ----- |
| CU-11 | Solicitar transferencia | Operador, Gerente | La sucursal destino genera una solicitud indicando producto, cantidad y sucursal origen. La transferencia queda en estado `PENDING`. |
| CU-12 | Confirmar transferencia | Gerente, Operador | La sucursal origen revisa disponibilidad y confirma o ajusta la cantidad a enviar. Incluye `<<include>>` la verificación de disponibilidad. |
| CU-13 | Revisar disponibilidad `<<include>>` | Sistema | Incluido en CU-12. El sistema valida automáticamente que haya stock suficiente en la sucursal origen antes de permitir la confirmación. |
| CU-14 | Registrar recepción de mercancía de la transferencia `<<include>>` | Operador | Incluido en CU-12. La sucursal destino confirma la llegada (total o parcial). Si es parcial, se registran los faltantes y se genera una alerta. |

---

## **3\. Diagrama de Casos de Uso**

El diagrama adjunto ilustra las relaciones entre actores y módulos del sistema, incluyendo las relaciones `<<include>>` y `<<extend>>` entre casos de uso relacionados.

<img width="985" height="2201" alt="Diagrama ER BD-Casos de Uso drawio (2)" src="https://github.com/user-attachments/assets/baf75c6c-e39e-4fac-b53e-46d2b9d3cdc7" />


# **Historias de Usuario**

| ID | Rol | Historia |
| ----- | ----- | ----- |
| HU-01 | Operador de inventario | Quiero registrar el ingreso de productos con su precio de compra, para mantener el costo promedio del inventario actualizado y generar órdenes de pago a proveedores. |
| HU-02 | Gerente de sucursal | Quiero ver en el dashboard la comparativa de ventas entre el mes actual y los tres meses anteriores, para identificar tendencias y tomar decisiones de compra anticipadas. |
| HU-03 | Operador de inventario | Quiero solicitar la transferencia de un producto desde otra sucursal con indicación de urgencia, para que la sucursal origen pueda priorizar el despacho según disponibilidad. |
| HU-04 | Administrador general | Quiero gestionar usuarios y asignarles un rol y una sucursal, para controlar qué operaciones puede realizar cada persona en el sistema. |
| HU-05 | Operador de inventario | Quiero registrar una venta y que el sistema valide el stock disponible antes de confirmarla, para evitar registrar ventas de productos que no hay en inventario. |
| HU-06 | Operador de inventario | Quiero recibir una alerta cuando el stock de un producto baje del mínimo configurado, para reaccionar a tiempo y generar una orden de compra o solicitar una transferencia. |
| HU-07 | Gerente de sucursal | Quiero aprobar o ajustar una transferencia solicitada por otra sucursal, para confirmar que hay disponibilidad real antes de despachar la mercancía. |
| HU-08 | Operador de inventario | Quiero confirmar la recepción parcial de una transferencia e indicar los faltantes, para que el sistema registre la diferencia y genere una alerta para su tratamiento. |
| HU-09 | Administrador general | Quiero consultar el log de auditoría filtrando por usuario, acción o fecha, para tener trazabilidad completa de quién hizo qué y cuándo en el sistema. |
| HU-10 | Operador de inventario | Quiero importar productos masivamente desde un archivo CSV, para ahorrar tiempo al registrar grandes volúmenes de productos sin hacerlo uno por uno. |
| HU-11 | Operador de inventario | Quiero que se genere automáticamente un recibo en PDF al confirmar una venta, para tener un comprobante descargable para el control comercial de la sucursal. |
| HU-12 | Gerente de sucursal | Quiero consultar el inventario de otras sucursales de la red, para identificar si otra sede tiene disponibilidad antes de hacer un pedido a proveedor. |

