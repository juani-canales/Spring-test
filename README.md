# Sistema de Facturación Electrónica
 
Aplicación web para gestionar **facturas de venta** y emitirlas ante **ARCA (ex AFIP)**, obteniendo el **CAE** mediante el servicio WSFE v1 (entorno de homologación).
 
- **Backend:** Java 21 + Spring Boot 4 (API REST, JPA/JPQL, MySQL).
- **Frontend:** TypeScript + HTML5 + CSS (sin frameworks), consumiendo la API con `fetch`.

---
 
## 👥 Integrantes del grupo
 
| N.º | Apellido y Nombre | 
|:---:|-------------------|
| 1 | Beltran Nadia |
| 2 | Canales Juan Ignacio | 
| 3 | Lopez Noelia | 
| 4 | Ochoa Melanie |
| 5 | Santibañez Guadalupe |
 
**Materia:** Desarrollo de Software

**Comisión:** 3k9

**Año:** 2026

---
 
## 🧾 ¿Qué hace el sistema?
 
| Funcionalidad | Descripción |
|---------------|-------------|
| **Listar facturas** | Tabla paginada con filtros: número, cliente, fechas, condición IVA y estado. |
| **Crear factura** | Se elige punto de venta, cliente, condición IVA y lista de precios, y se cargan renglones dinámicamente. |
| **Guardar sin informar** | Guarda la factura como `PENDIENTE` (todavía sin CAE). |
| **Guardar e informar** | Envía la factura a ARCA; si la aprueba, queda `EMITIDA` con su **CAE** y vencimiento. |
| **Editar** | Solo facturas `PENDIENTE` (se modifican los renglones). |
| **Anular** | Baja **lógica**: cambia el estado a `ANULADA` y guarda la fecha de anulación. |
| **Eliminar** | Baja **física**: borra la factura y sus renglones. Solo `PENDIENTE`. |
| **Descargar PDF** | Genera el comprobante A4 de una factura `EMITIDA`. |
| **Buscadores (popovers)** | Ventanas de búsqueda con filtros y paginación para elegir clientes y artículos. |
 
---
 
## 🛠 Tecnologías
 
**Backend**
 
- Java 21, Spring Boot 4.1.1 (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`)
- MySQL + Hibernate (`ddl-auto=update` crea las tablas solo)
- Lombok (getters, setters, constructores)
- OpenPDF (PDF) y Apache POI (Excel)
- BouncyCastle (firma CMS/PKCS#7 para autenticarse en ARCA)
- JAX-WS (cliente SOAP de WSFE v1, clases generadas en `com.afip.wsfe`)
- Maven (incluye Maven Wrapper: `mvnw` / `mvnw.cmd`)
**Frontend**
 
- TypeScript (modo `strict`), HTML5, CSS3
- Módulos ES2020 (`type="module"`), `fetch` con `async/await`
- Elemento nativo `<dialog>` para los popovers

---

## 🚀 Cómo ejecutar el proyecto
 
### Requisitos previos
 
- **JDK 21**
- **MySQL 8+** en ejecución
- **Node.js y npm** (solo si se va a modificar el TypeScript)
- TypeScript instalado globalmente: `npm install -g typescript`
### Base de datos
 
Crear la base vacía (Hibernate crea las tablas automáticamente al iniciar):
 
```sql
CREATE DATABASE springGrupal;
```
 
Revisar usuario y contraseña en `src/main/resources/application.properties`:
 
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/springGrupal
spring.datasource.username=root
spring.datasource.password=1234
```
 
**Datos iniciales necesarios.** Las tablas se crean vacías, así que para probar hay que cargar:
 
- Un **usuario con `id = 1`** (se usa para la auditoría).
- Un **tipo de moneda con `id = 1`** (ARS; el frontend lo envía fijo).
- Al menos un **punto de venta**, una **condición IVA** (incluida *Consumidor Final*, código ARCA `5`), una **lista de precios** con **artículos** (con su precio) y algunos **clientes**.


### 🔐 Configuración de ARCA (homologación)
 
La emisión usa dos servicios de ARCA:
 
1. **WSAA** (autenticación): se firma un pedido (TRA) con el certificado y se obtiene un *Token* y un *Sign*. Se guardan en un archivo de caché para reutilizarlos mientras no venzan.
2. **WSFE v1** (facturación): se consulta el último comprobante autorizado (`FECompUltimoAutorizado`) y se solicita el CAE (`FECAESolicitar`).
### Pasos
 
1. Generar la clave privada y el CSR con OpenSSL y tramitar el certificado en el portal de ARCA (servicio **WSASS**), asociándolo al servicio `wsfe` en el *Administrador de Relaciones*.
2. Completar en `application.properties`:
```properties
afip.cuit=20XXXXXXXXX                 # CUIT sin guiones
afip.key-path=ruta/a/privada.key      # clave privada (formato PKCS#8)
afip.cert-path=ruta/a/certificado.crt # certificado devuelto por ARCA
afip.tipo-comprobante=11              # 11 = Factura C | 6 = Factura B | 1 = Factura A
 
emisor.razon-social=Mi Empresa S.R.L.
emisor.domicilio=Av. Siempre Viva 742
emisor.condicion-iva=IVA Responsable Inscripto
```
 
