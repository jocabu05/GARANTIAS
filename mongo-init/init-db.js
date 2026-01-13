// ============================================
// Script de inicialización de MongoDB
// Crea la base de datos y las colecciones iniciales
// ============================================

db = db.getSiblingDB('garantias_db');

// Crear usuario para la aplicación
db.createUser({
    user: 'garantias_app',
    pwd: 'garantias_password_2024',
    roles: [
        { role: 'readWrite', db: 'garantias_db' }
    ]
});

// ============================================
// Colección: garantias
// ============================================
db.createCollection('garantias', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['numeroGarantia', 'cliente', 'aireAcondicionado', 'garantia'],
            properties: {
                numeroGarantia: {
                    bsonType: 'string',
                    description: 'Número único de garantía'
                },
                cliente: {
                    bsonType: 'object',
                    required: ['nombre', 'telefono'],
                    properties: {
                        nombre: { bsonType: 'string' },
                        telefono: { bsonType: 'string' },
                        email: { bsonType: 'string' },
                        direccion: { bsonType: 'string' }
                    }
                },
                aireAcondicionado: {
                    bsonType: 'object',
                    required: ['marca', 'modelo', 'numeroSerie'],
                    properties: {
                        marca: { bsonType: 'string' },
                        modelo: { bsonType: 'string' },
                        numeroSerie: { bsonType: 'string' },
                        tipoRefrigerante: { bsonType: 'string' },
                        potenciaBTU: { bsonType: 'int' },
                        fechaInstalacion: { bsonType: 'date' }
                    }
                },
                garantia: {
                    bsonType: 'object',
                    required: ['fechaInicio', 'fechaFin', 'duracionMeses', 'tipo', 'estado'],
                    properties: {
                        fechaInicio: { bsonType: 'date' },
                        fechaFin: { bsonType: 'date' },
                        duracionMeses: { bsonType: 'int' },
                        tipo: { enum: ['COMPLETA', 'LIMITADA', 'EXTENDIDA'] },
                        estado: { enum: ['ACTIVA', 'VENCIDA', 'RECLAMADA', 'ANULADA'] },
                        cobertura: { bsonType: 'array' }
                    }
                }
            }
        }
    }
});

// Crear índices para garantías
db.garantias.createIndex({ 'numeroGarantia': 1 }, { unique: true });
db.garantias.createIndex({ 'cliente.nombre': 'text', 'cliente.telefono': 'text' });
db.garantias.createIndex({ 'garantia.estado': 1 });
db.garantias.createIndex({ 'garantia.fechaFin': 1 });
db.garantias.createIndex({ 'aireAcondicionado.numeroSerie': 1 });

// ============================================
// Colección: facturas
// ============================================
db.createCollection('facturas', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['numeroFactura', 'cliente', 'fechaEmision', 'items', 'total', 'estado'],
            properties: {
                numeroFactura: {
                    bsonType: 'string',
                    description: 'Número único de factura'
                },
                garantiaId: { bsonType: 'objectId' },
                cliente: {
                    bsonType: 'object',
                    required: ['nombre'],
                    properties: {
                        nombre: { bsonType: 'string' },
                        nif: { bsonType: 'string' },
                        direccion: { bsonType: 'string' }
                    }
                },
                fechaEmision: { bsonType: 'date' },
                items: { bsonType: 'array' },
                subtotal: { bsonType: 'double' },
                totalIVA: { bsonType: 'double' },
                total: { bsonType: 'double' },
                estado: { enum: ['PENDIENTE', 'PAGADA', 'ANULADA'] },
                metodoPago: { bsonType: 'string' }
            }
        }
    }
});

// Crear índices para facturas
db.facturas.createIndex({ 'numeroFactura': 1 }, { unique: true });
db.facturas.createIndex({ 'garantiaId': 1 });
db.facturas.createIndex({ 'estado': 1 });
db.facturas.createIndex({ 'fechaEmision': 1 });

// ============================================
// Datos de ejemplo
// ============================================
db.garantias.insertMany([
    {
        numeroGarantia: 'GAR-2024-0001',
        cliente: {
            nombre: 'Juan García Pérez',
            telefono: '+34 612 345 678',
            email: 'juan.garcia@email.com',
            direccion: 'Calle Mayor 123, Madrid 28001'
        },
        aireAcondicionado: {
            marca: 'Samsung',
            modelo: 'AR12TXHQASINXEF',
            numeroSerie: 'SN-SAM-2024-001',
            tipoRefrigerante: 'R-32',
            potenciaBTU: 12000,
            fechaInstalacion: new Date('2024-01-15')
        },
        garantia: {
            fechaInicio: new Date('2024-01-15'),
            fechaFin: new Date('2026-01-15'),
            duracionMeses: 24,
            tipo: 'COMPLETA',
            estado: 'ACTIVA',
            cobertura: ['compresor', 'evaporador', 'condensador', 'mano_obra']
        },
        historialReparaciones: [],
        notas: 'Instalación en vivienda unifamiliar',
        creadoPor: 'admin',
        fechaCreacion: new Date(),
        fechaActualizacion: new Date()
    },
    {
        numeroGarantia: 'GAR-2024-0002',
        cliente: {
            nombre: 'María López Sánchez',
            telefono: '+34 623 456 789',
            email: 'maria.lopez@email.com',
            direccion: 'Avenida de la Constitución 45, Sevilla 41001'
        },
        aireAcondicionado: {
            marca: 'Daikin',
            modelo: 'FTXM35N',
            numeroSerie: 'SN-DAI-2024-002',
            tipoRefrigerante: 'R-32',
            potenciaBTU: 12000,
            fechaInstalacion: new Date('2024-03-20')
        },
        garantia: {
            fechaInicio: new Date('2024-03-20'),
            fechaFin: new Date('2026-03-20'),
            duracionMeses: 24,
            tipo: 'COMPLETA',
            estado: 'ACTIVA',
            cobertura: ['compresor', 'evaporador', 'condensador', 'mano_obra']
        },
        historialReparaciones: [],
        notas: 'Oficina comercial',
        creadoPor: 'admin',
        fechaCreacion: new Date(),
        fechaActualizacion: new Date()
    },
    {
        numeroGarantia: 'GAR-2023-0015',
        cliente: {
            nombre: 'Carlos Rodríguez Martín',
            telefono: '+34 634 567 890',
            email: 'carlos.rodriguez@email.com',
            direccion: 'Plaza España 8, Valencia 46001'
        },
        aireAcondicionado: {
            marca: 'Mitsubishi',
            modelo: 'MSZ-LN35VG',
            numeroSerie: 'SN-MIT-2023-015',
            tipoRefrigerante: 'R-32',
            potenciaBTU: 12000,
            fechaInstalacion: new Date('2023-06-10')
        },
        garantia: {
            fechaInicio: new Date('2023-06-10'),
            fechaFin: new Date('2025-06-10'),
            duracionMeses: 24,
            tipo: 'EXTENDIDA',
            estado: 'ACTIVA',
            cobertura: ['compresor', 'evaporador', 'condensador', 'mano_obra', 'desplazamiento']
        },
        historialReparaciones: [
            {
                fecha: new Date('2024-02-15'),
                descripcion: 'Revisión anual preventiva',
                tecnico: 'Antonio Ruiz',
                costo: 0
            }
        ],
        notas: 'Cliente preferente',
        creadoPor: 'admin',
        fechaCreacion: new Date('2023-06-10'),
        fechaActualizacion: new Date('2024-02-15')
    }
]);

db.facturas.insertMany([
    {
        numeroFactura: 'FAC-2024-0001',
        garantiaId: db.garantias.findOne({ numeroGarantia: 'GAR-2024-0001' })._id,
        cliente: {
            nombre: 'Juan García Pérez',
            nif: '12345678A',
            direccion: 'Calle Mayor 123, Madrid 28001'
        },
        fechaEmision: new Date('2024-01-15'),
        items: [
            {
                descripcion: 'Aire Acondicionado Samsung AR12TXHQASINXEF',
                cantidad: 1,
                precioUnitario: 899.99,
                iva: 21,
                total: 1088.99
            },
            {
                descripcion: 'Instalación profesional',
                cantidad: 1,
                precioUnitario: 200.00,
                iva: 21,
                total: 242.00
            }
        ],
        subtotal: 1099.99,
        totalIVA: 230.99,
        total: 1330.99,
        estado: 'PAGADA',
        metodoPago: 'TARJETA',
        notas: ''
    },
    {
        numeroFactura: 'FAC-2024-0002',
        garantiaId: db.garantias.findOne({ numeroGarantia: 'GAR-2024-0002' })._id,
        cliente: {
            nombre: 'María López Sánchez',
            nif: '87654321B',
            direccion: 'Avenida de la Constitución 45, Sevilla 41001'
        },
        fechaEmision: new Date('2024-03-20'),
        items: [
            {
                descripcion: 'Aire Acondicionado Daikin FTXM35N',
                cantidad: 1,
                precioUnitario: 1199.99,
                iva: 21,
                total: 1451.99
            },
            {
                descripcion: 'Instalación en altura',
                cantidad: 1,
                precioUnitario: 350.00,
                iva: 21,
                total: 423.50
            }
        ],
        subtotal: 1549.99,
        totalIVA: 325.49,
        total: 1875.49,
        estado: 'PAGADA',
        metodoPago: 'TRANSFERENCIA',
        notas: ''
    }
]);

print('✅ Base de datos garantias_db inicializada correctamente');
print('📊 Garantías insertadas: ' + db.garantias.countDocuments());
print('📄 Facturas insertadas: ' + db.facturas.countDocuments());
