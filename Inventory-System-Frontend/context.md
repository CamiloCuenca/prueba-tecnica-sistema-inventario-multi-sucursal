Proyecto: Sistema de Inventario Multi-Sucursal (frontend)
1. Objetivo del Sistema

Desarrollar el frontend para la gestión de inventario donde múltiples sucursales operan con autonomía pero comparten visibilidad del stock general en tiempo real.

2. Stack Tecnológico y Reglas Técnicas
Lenguaje: JavaScript
Framework/Librería: React
Arquitectura: Feature-Based Architecture
Librerías principales: TailwindCSS, Axios
3. Sistema de Diseño
Los colores y temas están definidos en index.js
No se deben usar colores hardcodeados en los componentes
4. Estructura del Proyecto

src/
│
├── api/ # Configuración global de Axios
├── components/ # Componentes reutilizables (UI genérica)
├── features/ # Módulos del sistema (lógica + UI específica)
├── pages/ # Pantallas (solo composición)
├── hooks/ # Hooks reutilizables
├── context/ # Estado global (ej: auth)
├── routes/ # Definición de rutas
├── utils/ # Funciones auxiliares

5. Reglas de Arquitectura (MUY IMPORTANTE)
5.1 Pages
NO deben contener lógica de negocio
Solo se encargan de:
Componer componentes y features
Estructurar la UI de la pantalla
No hacer llamadas HTTP aquí
No manejar estado complejo

✔ Ejemplo correcto:

<LoginForm />

❌ Incorrecto:

axios.get(...)
useState(...)
5.2 Features
Contienen TODA la lógica de negocio
Cada feature representa un módulo del sistema (auth, products, inventory, etc)

Cada feature puede incluir:

Componentes propios (NO reutilizables globalmente)
Hooks específicos
Servicios (llamadas HTTP con Axios)
Manejo de estado

✔ Ejemplo:
features/auth/

LoginForm.jsx
useLogin.js
authApi.js
5.3 Components
Solo componentes reutilizables en toda la aplicación
NO deben tener lógica de negocio
Solo lógica visual o de interacción básica

✔ Ejemplos:

Button
Input
Modal
Card

❌ NO poner aquí:

lógica de login
lógica de inventario
5.4 API
Aquí se configura Axios globalmente
Las llamadas específicas van dentro de cada feature
5.5 Hooks
Hooks reutilizables globales
Si un hook es específico de un feature → va dentro del feature
6. Principio clave

👉 Separación de responsabilidades:

pages → UI (pantalla)
features → lógica + comportamiento
components → UI reutilizable

Toda la aplicación debe de res responsiva. (MUY IMPORTANTE)