Proyecto: Sistema de Inventario Multi-Sucursal (Backend)
1. Objetivo del Sistema
   Desarrollar una API robusta para la gestión de inventario donde múltiples sucursales operan con autonomía pero comparten visibilidad del stock general en tiempo real
   .
2. Stack Tecnológico y Reglas Técnicas
   Lenguaje: Java 17+ con Spring Boot.
   Arquitectura: Basada en capas para garantizar la separación de responsabilidades:
   Controller: Endpoints REST con validaciones.
   Service: Centralización de la lógica de negocio (punto más crítico).
   Repository: Abstracción de datos con Spring Data JPA.
   Entity (Model): Mapeo de tablas de PostgreSQL.
   DTO: Objetos para transferencia de datos y desacoplamiento de la base de datos
   Enums: Para estados de transacciones, roles de usuario, etc
   .
   Base de Datos: PostgreSQL con llaves primarias tipo UUID para escalabilidad
   .
   Seguridad: Implementación de JWT para autenticación y autorización basada en roles (Admin, Gerente, Operador)
   .
   Infraestructura: Todo debe estar configurado para correr en un contenedor Docker vía docker-compose.yml
   .
3. Lógica de Negocio Crítica (Reglas de Oro)
   Al generar código o sugerencias, priorizar:
   Trazabilidad (Auditoría): Todo movimiento de inventario (venta, compra, ajuste, transferencia) DEBE registrarse en la tabla inventory_transaction con: responsable, fecha, motivo y referencia
   .
   Costo Promedio: El módulo de compras debe recalcular automáticamente el costo promedio ponderado del inventario
   .
   Ciclo de Transferencias: Implementar el flujo completo: Solicitud → Preparación → Envío → Recepción (gestionar faltantes en recepciones parciales)
   .
   Visibilidad Multi-sucursal: Un operador puede ver el stock de otras sucursales, pero solo realizar transacciones en la suya
   .
4. Entidades Principales (Basadas en Modelo ER)
   Branch: Sedes de la organización.
   Product: Catálogo maestro de productos.
   Inventory: Relación Many-to-Many entre productos y sucursales (stock actual).
   Purchase / Sale: Cabeceras de transacciones comerciales.
   Transfer: Registro de movimientos logísticos entre nodos
   .

### Reglas para el desarollo del código:

1.  cada servicio que sea una lista de objetos, debe tener paginación y con los filtors corresondientes
2. cada servicio que sea una lista de objetos, debe tener paginación y con los filtros correspondientes
3. cada servicio ya sea su interface y su implementacion debe de estar correctamente comentada y documentada con los entanderres de java
4. cada dto debe de tener sus validaciones correspondientes con las anotaciones de javax.validation
5. cada endpoint debe de tener su respectiva seguridad con JWT y roles si es necesario.
6. cada enpoint debe tener su respectiva documentación.
8. en la logica de negocio, se deben de manejar las excepciones de manera adecuada, lanzando errores personalizados con mensajes claros para el usuario.
9. en la logica de negocio se debe seguir el pricipio SOLID, especialmente el de responsabilidad única, asegurando que cada clase y método tenga una única razón para cambiar.
10. el código debe ser limpio y legible, siguiendo las convenciones de codificación de Java y Spring Boot, con nombres de variables y métodos descriptivos.