// Script para insertar facturas de muestra en MongoDB

db = db.getSiblingDB('garantias_db');

db.facturas.insertMany([
    {
        numeroFactura: "FAC-2024-001",
        garantiaId: null,
        cliente: {
            nombre: "Juan Pérez García",
            nif: "12345678A",
            direccion: "Calle Mayor 123, Madrid"
        },
        fechaEmision: new Date("2024-01-15"),
        items: [
            {
                descripcion: "Aire Acondicionado Split 3000 frigorías",
                cantidad: 1,
                precioUnitario: 650.00,
                iva: 21,
                total: 786.50
            },
            {
                descripcion: "Instalación completa",
                cantidad: 1,
                precioUnitario: 350.00,
                iva: 21,
                total: 423.50
            }
        ],
        subtotal: 1000.00,
        totalIVA: 210.00,
        total: 1210.00,
        estado: "PAGADA",
        metodoPago: "TARJETA",
        notas: "Cliente satisfecho. Instalación completada sin incidencias.",
        fechaCreacion: new Date("2024-01-15T10:30:00"),
        fechaActualizacion: new Date("2024-01-15T10:30:00")
    },
    {
        numeroFactura: "FAC-2024-002",
        garantiaId: null,
        cliente: {
            nombre: "María González López",
            nif: "87654321B",
            direccion: "Avenida Constitución 45, Barcelona"
        },
        fechaEmision: new Date("2024-02-20"),
        items: [
            {
                descripcion: "Aire Acondicionado Multi-split 2x1",
                cantidad: 1,
                precioUnitario: 1250.00,
                iva: 21,
                total: 1512.50
            },
            {
                descripcion: "Instalación Multi-split",
                cantidad: 1,
                precioUnitario: 550.00,
                iva: 21,
                total: 665.50
            }
        ],
        subtotal: 1800.00,
        totalIVA: 378.00,
        total: 2178.00,
        estado: "PENDIENTE",
        metodoPago: "TRANSFERENCIA",
        notas: "Pendiente de pago. Cliente solicitó factura en PDF.",
        fechaCreacion: new Date("2024-02-20T14:15:00"),
        fechaActualizacion: new Date("2024-02-20T14:15:00")
    },
    {
        numeroFactura: "FAC-2024-003",
        garantiaId: null,
        cliente: {
            nombre: "Carlos Rodríguez Sánchez",
            nif: "11223344C",
            direccion: "Plaza España 8, Valencia"
        },
        fechaEmision: new Date("2024-03-10"),
        items: [
            {
                descripcion: "Aire Acondicionado Portátil 2500 frigorías",
                cantidad: 2,
                precioUnitario: 350.00,
                iva: 21,
                total: 847.00
            }
        ],
        subtotal: 700.00,
        totalIVA: 147.00,
        total: 847.00,
        estado: "PAGADA",
        metodoPago: "EFECTIVO",
        notas: "Venta de equipos portátiles. Sin instalación.",
        fechaCreacion: new Date("2024-03-10T09:00:00"),
        fechaActualizacion: new Date("2024-03-10T09:00:00")
    },
    {
        numeroFactura: "FAC-2024-004",
        garantiaId: null,
        cliente: {
            nombre: "Ana Martínez Ruiz",
            nif: "99887766D",
            direccion: "Calle Libertad 67, Sevilla"
        },
        fechaEmision: new Date("2024-04-05"),
        items: [
            {
                descripcion: "Aire Acondicionado Cassette 4500 frigorías",
                cantidad: 1,
                precioUnitario: 980.00,
                iva: 21,
                total: 1185.80
            },
            {
                descripcion: "Instalación tipo cassette",
                cantidad: 1,
                precioUnitario: 420.00,
                iva: 21,
                total: 508.20
            }
        ],
        subtotal: 1400.00,
        totalIVA: 294.00,
        total: 1694.00,
        estado: "PENDIENTE",
        metodoPago: "FINANCIADO",
        notas: "Financiación a 12 meses sin intereses. Pendiente primera cuota.",
        fechaCreacion: new Date("2024-04-05T11:45:00"),
        fechaActualizacion: new Date("2024-04-05T11:45:00")
    },
    {
        numeroFactura: "FAC-2024-005",
        garantiaId: null,
        cliente: {
            nombre: "Luis Fernández Torres",
            nif: "55443322E",
            direccion: "Paseo Marítimo 12, Málaga"
        },
        fechaEmision: new Date("2024-05-18"),
        items: [
            {
                descripcion: "Sistema VRV 4 unidades interiores",
                cantidad: 1,
                precioUnitario: 2500.00,
                iva: 21,
                total: 3025.00
            },
            {
                descripcion: "Instalación sistema VRV completo",
                cantidad: 1,
                precioUnitario: 850.00,
                iva: 21,
                total: 1028.50
            }
        ],
        subtotal: 3350.00,
        totalIVA: 703.50,
        total: 4053.50,
        estado: "PAGADA",
        metodoPago: "TRANSFERENCIA",
        notas: "Proyecto comercial. Instalación en local comercial.",
        fechaCreacion: new Date("2024-05-18T16:20:00"),
        fechaActualizacion: new Date("2024-05-18T16:20:00")
    },
    {
        numeroFactura: "FAC-2024-006",
        garantiaId: null,
        cliente: {
            nombre: "Isabel Moreno Díaz",
            nif: "66778899F",
            direccion: "Calle Sol 34, Zaragoza"
        },
        fechaEmision: new Date("2024-06-22"),
        items: [
            {
                descripcion: "Aire Acondicionado Inverter 3500 frigorías",
                cantidad: 1,
                precioUnitario: 750.00,
                iva: 21,
                total: 907.50
            },
            {
                descripcion: "Instalación estándar",
                cantidad: 1,
                precioUnitario: 300.00,
                iva: 21,
                total: 363.00
            },
            {
                descripcion: "Mantenimiento anual",
                cantidad: 1,
                precioUnitario: 120.00,
                iva: 21,
                total: 145.20
            }
        ],
        subtotal: 1170.00,
        totalIVA: 245.70,
        total: 1415.70,
        estado: "PAGADA",
        metodoPago: "BIZUM",
        notas: "Incluye contrato de mantenimiento anual.",
        fechaCreacion: new Date("2024-06-22T10:15:00"),
        fechaActualizacion: new Date("2024-06-22T10:15:00")
    }
]);

print("✅ Facturas insertadas correctamente");
