# **Arquitectura y Diseño Técnico**

> **Autor:** Juan Camilo Cuenca Sepúlveda

## **1\. Separación de Responsabilidades**

El sistema implementa una arquitectura de tres capas con responsabilidades claramente delimitadas. Esta decisión responde al principio de separación de preocupaciones (Separation of Concerns), que facilita el mantenimiento, la escalabilidad independiente de cada componente y la claridad del flujo de datos.

| Capa | Tecnología elegida | Descripción |
| ----- | ----- | ----- |
| Presentación (Frontend) | React \+ Vite \+ Tailwind CSS | Interfaz de usuario responsiva. Toda comunicación con el backend se realiza exclusivamente a través de la API REST mediante una instancia global de Axios. No existe lógica de negocio en el cliente. |
| Negocio (Backend) | Java \+ Spring Boot 3 | API RESTful que centraliza toda la lógica de negocio: validaciones, reglas de transferencia, cálculo de Costo Promedio Ponderado, generación de reportes PDF y envío de alertas por correo. |
| Datos (Base de datos) | PostgreSQL 16 | Almacenamiento persistente relacional. Centraliza la información de todas las sucursales, garantizando coherencia de datos y soporte para consultas complejas entre nodos. |
| Infraestructura | Docker \+ Docker Compose | Toda la solución se levanta con un único comando (`docker compose up --build`), con servicios independientes y correctamente aislados para frontend, backend y base de datos. |

## **2\. Justificación de Decisiones Tecnológicas**

### **2.1 Backend — Java con Spring Boot 3**

**¿Por qué Java?:**  Java es un lenguaje fuertemente tipado con un ecosistema maduro para el desarrollo de aplicaciones empresariales. En el contexto de este sistema, donde la integridad de los datos, la trazabilidad de movimientos y las reglas de negocio complejas son críticas, un lenguaje tipado reduce significativamente los errores en tiempo de ejecución y facilita el mantenimiento del código a largo plazo.

**¿Por qué Spring Boot?** Spring Boot ofrece un conjunto de ventajas directamente aplicables a este proyecto:

| Característica | Aplicación en el proyecto |
| ----- | ----- |
| Spring Security | Implementación de autenticación JWT y control de acceso por roles (ADMIN, MANAGER, OPERATOR) con mínima configuración. |
| Spring Data JPA | Abstracción del acceso a datos con soporte para Specifications, paginación nativa y consultas derivadas, reduciendo el código repetitivo en repositorios. |
| Spring AOP | Permite implementar la auditoría como una responsabilidad transversal sin contaminar la lógica de negocio, usando la anotación `@Auditable`. |
| Spring Mail | Integración directa con servidores SMTP para el envío de alertas automáticas de stock mínimo. |
| Swagger / OpenAPI | Documentación automática de la API generada desde las anotaciones del código, accesible en `/swagger-ui/index.html`. |

### **2.2 Frontend — React con Vite y Tailwind CSS**

**¿Por qué React?:** React es una biblioteca basada en componentes que permite construir interfaces modulares y reutilizables. En un sistema con múltiples módulos (inventario, compras, ventas, transferencias, dashboard), la arquitectura por componentes de React facilita mantener cada vista independiente, con su propio estado y lógica de presentación, sin acoplamiento entre módulos.

**¿Por qué Tailwind CSS?**: Tailwind permite aplicar estilos directamente en el JSX mediante clases utilitarias, eliminando la necesidad de archivos CSS separados por componente. Esto acelera el desarrollo de interfaces consistentes y responsivas sin sacrificar flexibilidad de diseño.

Adicionalmente, React cuenta con un ecosistema amplio de librerías complementarias que se integraron en este proyecto:

| Librería | Uso en el proyecto |
| ----- | ----- |
| React Router | Navegación entre módulos y protección de rutas por rol. |
| Axios (instancia global) | Comunicación centralizada con el backend, con interceptores para adjuntar el token JWT y manejar errores 401/403 de forma global. |
| Recharts | Visualización de métricas del dashboard (ventas por mes, rotación de inventario). |
| React Hook Form | Gestión de formularios con validaciones en el cliente. |

### **2.3 Base de Datos — PostgreSQL 16**

**¿Por qué una base de datos relacional?** El dominio del problema es inherentemente relacional: productos, sucursales, usuarios, movimientos de inventario, compras, ventas y transferencias tienen relaciones bien definidas entre sí. Un modelo relacional garantiza integridad referencial mediante claves foráneas, soporte para transacciones ACID y consistencia de datos en operaciones concurrentes de múltiples sucursales.

### **2.4 Estrategia de Autenticación y Autorización**

Se implementó autenticación stateless basada en JWT (JSON Web Tokens). Esta decisión se justifica porque:

* No requiere almacenar sesiones en el servidor, lo que simplifica el escalado horizontal.  
* El token contiene el rol y la sucursal del usuario, permitiendo que el backend tome decisiones de autorización sin consultas adicionales a la base de datos en cada request.  
* Spring Security intercepta cada request, valida el token y establece el contexto de seguridad antes de que llegue a la capa de negocio.

Los tres roles definidos y sus alcances son:

| Rol | Alcance |
| ----- | ----- |
| ADMIN | Acceso total a todos los módulos y todas las sucursales, etc. |
| MANAGER | Acceso a su sucursal, aprobación de transferencias y dashboards comparativos, etc. |
| OPERATOR | Ejecución de transacciones diarias en su sucursal asignada, etc. |

### **2.5 Patrón de Auditoría con AOP**

La auditoría se implementó como una responsabilidad transversal usando Programación Orientada a Aspectos (AOP). La decisión de no implementarla dentro de cada servicio responde al principio de responsabilidad única: un servicio de ventas debe ocuparse de registrar ventas, no de auditar sus propias acciones.

Con la anotación `@Auditable` sobre cualquier método de servicio, el aspecto intercepta automáticamente la ejecución, registra el usuario autenticado, el método invocado, los argumentos y el resultado (SUCCESS o ERROR) en la tabla `audit_log`, sin que el servicio sea consciente de ello.

