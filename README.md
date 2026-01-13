# 🌬️ Sistema de Gestión de Garantías - Aires Acondicionados

<div align="center">

![Logo](https://img.shields.io/badge/JavaFX-17-blue?style=for-the-badge&logo=java)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?style=for-the-badge&logo=mongodb)
![Odoo](https://img.shields.io/badge/Odoo-17-purple?style=for-the-badge&logo=odoo)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-Educational-yellow?style=for-the-badge)

**Sistema empresarial de gestión integral para garantías de equipos de climatización**

[Características](#-características) • [Tecnologías](#️-stack-tecnológico) • [Instalación](#-instalación) • [Arquitectura](#-arquitectura) • [Uso](#-uso)

</div>

---

## 📋 Descripción del Proyecto

Sistema de escritorio desarrollado en **JavaFX** para la gestión completa de garantías de aires acondicionados, integrando autenticación centralizada con **Odoo** mediante XML-RPC y almacenamiento de datos en **MongoDB**. La aplicación está diseñada para empresas de instalación y mantenimiento de equipos de climatización, permitiendo un control exhaustivo del ciclo de vida de las garantías y la emisión de facturas.

## ✨ Características Principales

### 🔐 Autenticación y Seguridad
- **Integración con Odoo 17** via XML-RPC para gestión centralizada de usuarios
- Sistema de roles y permisos
- Modo demo para pruebas sin dependencias externas
- Sesiones persistentes con logout seguro

### 📊 Dashboard Interactivo
- **Estadísticas en tiempo real**: garantías activas, vencidas, facturación total
- **Gráficas dinámicas** con JFreeChart:
  - Distribución de garantías por estado (Pie Chart)
  - Garantías por marca de equipo (Bar Chart)
  - Evolución de facturación mensual (Line Chart)
- **Alertas automáticas** de garantías próximas a vencer (≤ 30 días)
- **Acciones rápidas** para crear garantías y facturas directamente

### 🔧 Gestión de Garantías
- **CRUD completo** con validación de datos
- **Filtros avanzados** por estado (Activa, Vencida, Reclamada, Anulada)
- **Búsqueda en tiempo real** por número de garantía, cliente o equipo
- **Historial de reparaciones** con registro de fechas y descripciones
- **Campos completos**:
  - Datos del cliente (nombre, NIF, dirección, teléfono, email)
  - Datos del equipo (marca, modelo, serie, BTU, tipo)
  - Fechas y duración de garantía
  - Estado con indicadores visuales por colores

### 📄 Gestión de Facturas
- **Emisión de facturas** con múltiples items y cálculo automático de IVA
- **Control de estado de pago** (Pagada, Pendiente, Vencida)
- **Métodos de pago** configurables (Efectivo, Tarjeta, Transferencia)
- **Exportación a PDF** con formato profesional (iText7)
- **Vista previa** antes de exportar
- Asociación automática con garantías

### 🎨 Diseño Profesional
- **Tema oscuro moderno** con paleta de colores corporativa
- **Interfaz responsiva** con JavaFX FXML
- **Iconos FontAwesome** mediante Ikonli
- **Animaciones suaves** y feedback visual
- **Notificaciones** con ControlsFX

## 🛠️ Stack Tecnológico

### Backend y Core

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 | Lenguaje principal |
| **JavaFX** | 17.0.9 | Framework de interfaz gráfica |
| **Maven** | 3.9.6 | Gestión de dependencias y build |
| **MongoDB** | 7 | Base de datos NoSQL para persistencia |
| **Odoo** | 17 | Sistema ERP para gestión de usuarios |

### Librerías Principales

| Librería | Propósito |
|----------|-----------|
| **MongoDB Driver Sync** (4.11.1) | Cliente MongoDB para Java |
| **Apache XML-RPC** (3.1.3) | Comunicación con Odoo via XML-RPC |
| **ControlsFX** (11.2.0) | Controles avanzados de JavaFX |
| **Ikonli FontAwesome** (12.3.1) | Iconos vectoriales |
| **iText7** (8.0.2) | Generación de PDFs |
| **Gson** (2.10.1) | Procesamiento JSON |
| **Logback** (1.4.14) | Sistema de logging |

### Infraestructura

- **Docker Compose**: Orquestación de servicios
  - PostgreSQL 15 (base de datos de Odoo)
  - MongoDB 7 (almacenamiento de garantías y facturas)
  - Odoo 17 (gestión de usuarios)
  - Mongo Express (interfaz web para MongoDB)

## 📁 Estructura del Proyecto

```
GARANTIAS/
├── docker-compose.yml              # Configuración de contenedores
├── pom.xml                         # Dependencias Maven
├── mongo-init/                     # Scripts de inicialización MongoDB
│   └── init-mongo.js
├── src/main/
│   ├── java/com/garantias/
│   │   ├── App.java               # Punto de entrada de la aplicación
│   │   ├── config/
│   │   │   ├── MongoDBConfig.java # Conexión MongoDB
│   │   │   └── OdooConfig.java    # Conexión Odoo XML-RPC
│   │   ├── controller/
│   │   │   ├── LoginController.java
│   │   │   ├── MainController.java
│   │   │   ├── DashboardContentController.java
│   │   │   ├── GarantiasController.java
│   │   │   ├── FacturasController.java
│   │   │   └── GraficasController.java
│   │   ├── model/
│   │   │   ├── Garantia.java
│   │   │   ├── Factura.java
│   │   │   ├── Usuario.java
│   │   │   ├── Reparacion.java
│   │   │   └── ItemFactura.java
│   │   ├── service/
│   │   │   ├── GarantiaService.java
│   │   │   ├── FacturaService.java
│   │   │   ├── OdooAuthService.java
│   │   │   ├── ChartService.java
│   │   │   └── PDFService.java
│   │   └── util/
│   │       ├── SessionManager.java
│   │       ├── DateUtils.java
│   │       └── ValidationUtils.java
│   └── resources/
│       ├── views/                 # Archivos FXML
│       │   ├── login.fxml
│       │   ├── main.fxml
│       │   ├── dashboard_content.fxml
│       │   ├── garantias.fxml
│       │   ├── facturas.fxml
│       │   └── graficas.fxml
│       └── styles/                # Hojas de estilo CSS
│           ├── styles.css
│           └── styles_premium.css
└── README.md
```

## 🚀 Instalación

### Requisitos Previos

Asegúrate de tener instalado:

- ✅ **Java Development Kit (JDK) 17 o superior**
  ```bash
  java -version
  ```
- ✅ **Maven 3.9+** (incluido en el proyecto en `/apache-maven-3.9.6`)
- ✅ **Docker Desktop** para levantar los servicios
  ```bash
  docker --version
  docker-compose --version
  ```

### Instalación Paso a Paso

#### 1️⃣ Clonar el Repositorio

```bash
git clone https://github.com/jocabu05/sistema-garantias-aires.git
cd sistema-garantias-aires
```

#### 2️⃣ Levantar Servicios Docker

```bash
docker-compose up -d
```

Esto iniciará:
- 🐘 **PostgreSQL** (puerto 5432) - Base de datos para Odoo
- 🗄️ **MongoDB** (puerto 27018) - Almacenamiento de garantías y facturas
- 🔧 **Odoo** ([http://localhost:8070](http://localhost:8070)) - Gestión de usuarios
- 📊 **Mongo Express** ([http://localhost:8081](http://localhost:8081)) - UI para MongoDB

#### 3️⃣ Configurar Odoo (Primera vez)

1. Accede a [http://localhost:8070](http://localhost:8070)
2. **Crear base de datos**:
   - Database Name: `odoo`
   - Email: tu email
   - Password: tu contraseña
   - Seleccionar idioma: Español
3. Completa la configuración inicial
4. **Crear usuarios adicionales** si es necesario desde el menú de Usuarios

#### 4️⃣ Compilar y Ejecutar la Aplicación

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn javafx:run
```

O si Maven no está en el PATH:

```bash
# Windows
.\apache-maven-3.9.6\bin\mvn.cmd javafx:run

# Linux/Mac
./apache-maven-3.9.6/bin/mvn javafx:run
```

#### 5️⃣ Iniciar Sesión

**Opción 1 - Con Odoo:**
- URL: `http://localhost:8070`
- Base de datos: `odoo`
- Usuario: el que creaste en Odoo
- Contraseña: la que configuraste

**Opción 2 - Modo Demo:**
- Usuario: `admin`
- Contraseña: `admin`

## 🏗️ Arquitectura

### Diagrama de Arquitectura

```mermaid
flowchart TB
    subgraph Frontend["🖥️ Frontend - JavaFX"]
        UI[Views FXML]
        Controllers[Controllers]
        CSS[Estilos CSS]
    end
    
    subgraph Backend["⚙️ Backend - Java Services"]
        Auth[OdooAuthService]
        GarService[GarantiaService]
        FacService[FacturaService]
        ChartSrv[ChartService]
        PDFSrv[PDFService]
    end
    
    subgraph Data["🗄️ Capa de Datos"]
        Mongo[(MongoDB)]
        Odoo[(Odoo + PostgreSQL)]
    end
    
    UI --> Controllers
    Controllers --> Auth
    Controllers --> GarService
    Controllers --> FacService
    Controllers --> ChartSrv
    Controllers --> PDFSrv
    
    Auth -.XML-RPC.-> Odoo
    GarService --> Mongo
    FacService --> Mongo
    ChartSrv --> Mongo
    
    style Frontend fill:#2d4263
    style Backend fill:#1e3a5f
    style Data fill:#16213e
```

### Flujo de Datos

1. **Autenticación**: LoginController → OdooAuthService → Odoo (XML-RPC)
2. **CRUD Garantías**: GarantiasController → GarantiaService → MongoDB
3. **CRUD Facturas**: FacturasController → FacturaService → MongoDB
4. **Generación de Gráficas**: GraficasController → ChartService → MongoDB
5. **Exportación PDF**: FacturasController → PDFService → Sistema de archivos

## 📊 Modelos de Datos

### Garantía

```java
{
  "_id": ObjectId,
  "numeroGarantia": String (único),
  "cliente": {
    "nombre": String,
    "nif": String,
    "direccion": String,
    "telefono": String,
    "email": String
  },
  "equipo": {
    "marca": String,
    "modelo": String,
    "numeroSerie": String (único),
    "btu": Integer,
    "tipo": String ("Split", "Ventana", "Central", "Portátil")
  },
  "fechaCompra": LocalDate,
  "fechaInicio": LocalDate,
  "fechaFin": LocalDate,
  "duracionMeses": Integer,
  "estado": String ("Activa", "Vencida", "Reclamada", "Anulada"),
  "reparaciones": [
    {
      "fecha": LocalDate,
      "descripcion": String,
      "tecnico": String
    }
  ]
}
```

### Factura

```java
{
  "_id": ObjectId,
  "numeroFactura": String (único),
  "fecha": LocalDate,
  "cliente": {
    "nombre": String,
    "nif": String,
    "direccion": String
  },
  "items": [
    {
      "concepto": String,
      "cantidad": Integer,
      "precioUnitario": Double,
      "iva": Double (porcentaje: 21, 10, 4)
    }
  ],
  "subtotal": Double,
  "totalIva": Double,
  "total": Double,
  "estadoPago": String ("Pagada", "Pendiente", "Vencida"),
  "metodoPago": String ("Efectivo", "Tarjeta", "Transferencia"),
  "garantiaAsociada": String (numeroGarantia, opcional)
}
```

## 🔧 Configuración Avanzada

### Variables de Entorno MongoDB

Edita `docker-compose.yml` si necesitas cambiar credenciales:

```yaml
mongodb:
  environment:
    - MONGO_INITDB_ROOT_USERNAME=admin
    - MONGO_INITDB_ROOT_PASSWORD=tu_password_aqui
```

Luego actualiza `MongoDBConfig.java`:

```java
private static final String CONNECTION_STRING = 
    "mongodb://admin:tu_password_aqui@localhost:27018";
```

### Configuración de Odoo

En `OdooConfig.java`:

```java
private static final String ODOO_URL = "http://localhost:8070";
```

## 🐳 Gestión de Contenedores Docker

```bash
# Ver estado de los servicios
docker-compose ps

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (⚠️ BORRA DATOS)
docker-compose down -v

# Reiniciar un servicio específico
docker-compose restart mongodb
```

## 📸 Capturas de Pantalla

### Pantalla de Login
- Diseño moderno con gradientes
- Validación de campos en tiempo real
- Opción de modo demo

### Dashboard
- Vista general con KPIs
- Gráficas interactivas
- Alertas de vencimiento
- Acciones rápidas

### Gestión de Garantías
- Tabla con búsqueda y filtros
- Formulario de creación/edición
- Estados visuales con colores
- Historial de reparaciones

### Gestión de Facturas
- Tabla de facturas con filtros
- Formulario con items dinámicos
- Cálculo automático de IVA
- Exportación a PDF

### Gráficas y Reportes
- Pie Chart: Distribución por estado
- Bar Chart: Garantías por marca
- Line Chart: Facturación mensual

## 🧪 Pruebas y Validación

### Datos de Prueba

La aplicación incluye datos de ejemplo para facilitar las pruebas:

```java
// Modo demo incluye:
- 5 garantías de ejemplo (activas y vencidas)
- 3 facturas de prueba
- Usuario admin predefinido
```

### Testing Manual

1. **Login**: Probar ambos modos (Odoo y Demo)
2. **CRUD Garantías**: Crear, listar, editar, eliminar
3. **CRUD Facturas**: Crear con múltiples items, exportar PDF
4. **Gráficas**: Verificar actualización en tiempo real
5. **Búsqueda y Filtros**: Probar todas las combinaciones
6. **Exportación PDF**: Verificar formato y contenido

## 🚨 Solución de Problemas

### Error: "Cannot connect to MongoDB"

```bash
# Verificar que MongoDB esté corriendo
docker ps | grep mongodb

# Ver logs de MongoDB
docker-compose logs mongodb

# Verificar puerto y credenciales en MongoDBConfig.java
```

### Error: "Odoo authentication failed"

- Verificar que Odoo esté corriendo en `http://localhost:8070`
- Confirmar que la base de datos `odoo` existe
- Verificar credenciales
- Usar modo demo como alternativa

### La aplicación no inicia (JavaFX)

```bash
# Verificar versión de Java
java -version  # Debe ser 17 o superior

# Limpiar y recompilar
mvn clean compile
mvn javafx:run
```

## 📚 Documentación Adicional

- 📘 [JavaFX Documentation](https://openjfx.io/)
- 📗 [MongoDB Java Driver](https://www.mongodb.com/docs/drivers/java/sync/current/)
- 📙 [Odoo XML-RPC API](https://www.odoo.com/documentation/17.0/developer/reference/external_api.html)
- 📕 [Docker Compose](https://docs.docker.com/compose/)

## 🤝 Contribuciones

Este proyecto es de uso educativo y demostrativo. Para sugerencias o mejoras:

1. Fork el repositorio
2. Crea una rama con tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto es de uso **educativo** y fue desarrollado como parte de un proyecto académico.

## 👨‍💻 Autor

**Jorge Castillo Bueno** - [@jocabu05](https://github.com/jocabu05)

Desarrollado como proyecto de gestión empresarial para empresas de instalación y mantenimiento de aires acondicionados.

---

<div align="center">

**⭐ Si te ha gustado el proyecto, dale una estrella ⭐**

Hecho con ❤️ y ☕ usando JavaFX

</div>
