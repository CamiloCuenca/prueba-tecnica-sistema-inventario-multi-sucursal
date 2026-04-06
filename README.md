# OptiPlant - Sistema de Inventario Multi-Sucursal

> **Autor:** Juan Camilo Cuenca Sepúlveda

Solución robusta para la gestión de inventario en red, diseñada para permitir la autonomía operativa de cada sede mientras se mantiene visibilidad total y coherencia de datos centralizada.

---

## 🚀 Funcionalidades Destacadas

Además de los módulos base, el sistema integra capacidades de alto valor operativo:

| Funcionalidad | Descripción |
|---|---|
| **Auditoría con AOP** | Programación Orientada a Aspectos para registrar cada movimiento sin contaminar el código de negocio |
| **Alertas Inteligentes** | Notificaciones por correo a administradores y gerentes cuando el stock cae por debajo del umbral mínimo |
| **Reportes PDF** | Generación automática de recibos de venta en PDF para control comercial |
| **Gestión Integral** | Módulos maestros para administración de Proveedores, Sucursales y Usuarios |

---

## 🛠️ Requisitos Previos

- Docker y Docker Compose instalados

---

## 📦 Levantar la Solución

El proyecto cumple con la regla técnica obligatoria de contenedorización completa.

**1. Abre una terminal en la raíz del proyecto**

**2. Ejecuta:**
```bash
docker compose up
```

### Puertos y Acceso

| Servicio | URL |
|---|---|
| Frontend (Vite/React) | http://localhost:5173 |
| Backend (Spring Boot) | http://localhost:8080 |
| API Docs (Swagger) | http://localhost:8080/swagger-ui/index.html |
| Base de Datos (PostgreSQL) | puerto `5432` — User: `postgres` DB: `PruebaTecnica` |

---

## 🏗️ Arquitectura y Diseño

El sistema implementa una separación estricta de capas comunicadas exclusivamente mediante una API RESTful:

- **Presentación** → React + Vite
- **Negocio** → Spring Boot (API REST)
- **Datos** → PostgreSQL

### Principios clave

- **Trazabilidad completa:** cada transacción genera un registro auditable con responsable, motivo y fecha.
- **Coherencia en red:** las sucursales consultan el stock de otros nodos en tiempo real, habilitando el módulo de Transferencias.

---

## 📝 Notas Técnicas

- Para detener los servicios y limpiar volúmenes:
```bash
docker compose down
```
- La configuración de la base de datos puede ajustarse en `application.properties` si se requiere una instancia externa.