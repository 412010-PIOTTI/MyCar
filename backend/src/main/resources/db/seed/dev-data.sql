-- ═══════════════════════════════════════════════════════════════════════════
-- MyCar — seed data para desarrollo local (SQL Server)
-- Usuario de prueba : test@mycar.app
-- Contraseña        : Test1234!
--
-- Ejecutar manualmente contra el SQL Server del Docker:
--   docker exec -i <container> /opt/mssql-tools/bin/sqlcmd -S localhost -U sa \
--     -P 'MyCar_Dev_2024!' -d mycar_db -i /ruta/dev-data.sql
-- O pegarlo directo en DBeaver / Azure Data Studio.
-- Es idempotente: se puede correr varias veces sin duplicar datos.
-- ═══════════════════════════════════════════════════════════════════════════

-- ── 1. Usuario de prueba ─────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'test@mycar.app')
    INSERT INTO users (name, email, password_hash, role, active, two_factor_enabled, created_at)
    VALUES ('Usuario Demo', 'test@mycar.app',
            '$2a$10$P92roXHnF2sIz9.ZkYe5zei7d6vc3KkiJkZ0WY3S7Gky1H6FBDPvm',
            'USER', 1, 0, GETDATE());

-- ── 2. Vehículos ─────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM vehicles WHERE plate = 'ABC123')
    INSERT INTO vehicles (owner_id, plate, brand, model, [year], color, current_km, active, created_at)
    SELECT id, 'ABC123', 'Toyota', 'Corolla', 2020, 'Blanco', 48500, 1, GETDATE()
    FROM users WHERE email = 'test@mycar.app';

IF NOT EXISTS (SELECT 1 FROM vehicles WHERE plate = 'DEF456')
    INSERT INTO vehicles (owner_id, plate, brand, model, [year], color, current_km, active, created_at)
    SELECT id, 'DEF456', 'Ford', 'Focus', 2018, 'Gris Oscuro', 72000, 1, GETDATE()
    FROM users WHERE email = 'test@mycar.app';

-- ── 3. Gastos ─────────────────────────────────────────────────────────────
-- Se insertan solo si el usuario no tiene ningún gasto todavía.
DECLARE @u  BIGINT = (SELECT id FROM users    WHERE email = 'test@mycar.app');
DECLARE @v1 BIGINT = (SELECT id FROM vehicles WHERE plate = 'ABC123');
DECLARE @v2 BIGINT = (SELECT id FROM vehicles WHERE plate = 'DEF456');

IF NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = @u)
BEGIN

    -- ── Toyota Corolla (ABC123) ───────────────────────────────────────────

    -- Ene 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'OPERATIVO', 'Combustible', '2026-01-08', 14800.00, 'Nafta súper — YPF Av. Colón', 43000, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'IMPUESTO_SEGURO', 'Seguro', '2026-01-15', 22500.00, 'Cuota mensual Federación Patronal', NULL, '2026-12-31', GETDATE());

    -- Feb 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'OPERATIVO', 'Combustible', '2026-02-05', 15200.00, 'Nafta súper full tank', 44200, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'MANTENIMIENTO', 'Service', '2026-02-20', 38000.00, 'Service 45.000 km — cambio aceite, filtros y correa', 45000, NULL, GETDATE());

    -- Mar 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'OPERATIVO', 'Combustible', '2026-03-10', 16400.00, 'Nafta súper', 45800, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'IMPUESTO_SEGURO', 'Patente', '2026-03-20', 18700.00, '1ª cuota patente 2026', NULL, '2026-03-31', GETDATE());

    -- Abr 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'OPERATIVO', 'Combustible', '2026-04-07', 15900.00, 'Nafta súper', 46500, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'OPERATIVO', 'Peaje', '2026-04-12', 2800.00, 'Autopista Córdoba-Carlos Paz ida y vuelta', NULL, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'MEJORA', 'Accesorios', '2026-04-25', 28500.00, 'Alfombrillas 3D y parasol trasero', NULL, NULL, GETDATE());

    -- May 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'OPERATIVO', 'Combustible', '2026-05-09', 16800.00, 'Nafta súper', 47200, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'INFRACCION', 'Multa', '2026-05-14', 31200.00, 'Exceso de velocidad Av. Rafael Núñez', NULL, '2026-06-10', GETDATE());
    -- ^ expiryDate pasado → VENCIDO

    -- Jun 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'OPERATIVO', 'Combustible', '2026-06-03', 17200.00, 'Nafta súper', 48000, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'MANTENIMIENTO', 'Neumáticos', '2026-06-10', 62000.00, '4 cubiertas Bridgestone Turanza 195/65 R15', 48500, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v1, @u, 'IMPUESTO_SEGURO', 'VTV', '2026-06-14', 9800.00, 'Verificación técnica vehicular', NULL, '2026-07-05', GETDATE());
    -- ^ vence en ~19 días → POR_VENCER


    -- ── Ford Focus (DEF456) ──────────────────────────────────────────────

    -- Ene 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'OPERATIVO', 'Combustible', '2026-01-12', 13500.00, 'Nafta súper', 70000, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'IMPUESTO_SEGURO', 'Seguro', '2026-01-15', 19800.00, 'Cuota mensual Zurich', NULL, '2026-12-31', GETDATE());

    -- Feb 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'OPERATIVO', 'Combustible', '2026-02-11', 14000.00, 'Nafta súper', 70600, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'MANTENIMIENTO', 'Frenos', '2026-02-18', 54000.00, 'Cambio pastillas y discos delanteros', 70800, NULL, GETDATE());

    -- Mar 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'OPERATIVO', 'Combustible', '2026-03-06', 13800.00, 'Nafta súper', 71200, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'IMPUESTO_SEGURO', 'Patente', '2026-03-22', 16200.00, '1ª cuota patente 2026', NULL, '2026-03-31', GETDATE());

    -- Abr 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'OPERATIVO', 'Combustible', '2026-04-09', 14500.00, 'Nafta súper', 71700, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'MEJORA', 'Audio', '2026-04-18', 85000.00, 'Sistema de audio Pioneer: head unit + parlantes', NULL, NULL, GETDATE());

    -- May 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'OPERATIVO', 'Combustible', '2026-05-07', 15000.00, 'Nafta súper', 72000, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'MANTENIMIENTO', 'Aceite', '2026-05-22', 12500.00, 'Cambio de aceite y filtro', 72000, NULL, GETDATE());

    -- Jun 2026
    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'OPERATIVO', 'Combustible', '2026-06-05', 15400.00, 'Nafta súper', NULL, NULL, GETDATE());

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'INFRACCION', 'Estacionamiento', '2026-06-12', 8500.00, 'Estacionamiento en zona prohibida', NULL, '2026-07-08', GETDATE());
    -- ^ vence en ~22 días → POR_VENCER

    INSERT INTO expenses (vehicle_id, user_id, category, subcategory, date, amount, description, km_at_expense, expiry_date, created_at)
    VALUES (@v2, @u, 'OPERATIVO', 'Estacionamiento', '2026-06-15', 1200.00, 'Playa de estacionamiento centro', NULL, NULL, GETDATE());

END
GO

-- ── 4. Mantenimiento ─────────────────────────────────────────────────────
-- Corrige columnas legacy que quedaron NOT NULL por ddl-auto=update:
--   'type'          → viejo MaintenanceType, reemplazado por 'system'
--   'km_at_service' → renombrado a 'km_at_maintenance'
--   'registered_by' → reemplazado por 'user_id'
-- También elimina constraints generadas por Hibernate que usan valores de
-- enums viejos (SISTEMA_ELECTRICO en lugar de ELECTRICO, etc.).

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK__maintenanc__type__14270015'
           AND parent_object_id = OBJECT_ID('maintenance_logs'))
    ALTER TABLE maintenance_logs DROP CONSTRAINT CK__maintenanc__type__14270015;

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK__maintenan__syste__412EB0B6'
           AND parent_object_id = OBJECT_ID('maintenance_logs'))
    ALTER TABLE maintenance_logs DROP CONSTRAINT CK__maintenan__syste__412EB0B6;

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'maintenance_logs' AND COLUMN_NAME = 'type' AND IS_NULLABLE = 'NO')
    ALTER TABLE maintenance_logs ALTER COLUMN [type] NVARCHAR(30) NULL;

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'maintenance_logs' AND COLUMN_NAME = 'km_at_service' AND IS_NULLABLE = 'NO')
    ALTER TABLE maintenance_logs ALTER COLUMN km_at_service INT NULL;

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'maintenance_logs' AND COLUMN_NAME = 'registered_by' AND IS_NULLABLE = 'NO')
    ALTER TABLE maintenance_logs ALTER COLUMN registered_by BIGINT NULL;
GO

DECLARE @u2  BIGINT = (SELECT id FROM users    WHERE email = 'test@mycar.app');
DECLARE @v1b BIGINT = (SELECT id FROM vehicles WHERE plate = 'ABC123');
DECLARE @v2b BIGINT = (SELECT id FROM vehicles WHERE plate = 'DEF456');

IF NOT EXISTS (SELECT 1 FROM maintenance_logs WHERE vehicle_id = @v1b)
BEGIN

    -- ── Toyota Corolla (ABC123) ─────────────────────────────────────────────

    -- Service 40.000 km
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v1b, @u2, 'MOTOR', '2025-08-10', 40000, 'Centro Integral Toyota Córdoba',
            'Service 40.000 km — cambio de aceite 5W30, filtro de aceite y filtro de aire', 32000.00, 45000, NULL, GETDATE());

    -- Frenos
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v1b, @u2, 'FRENOS', '2025-11-04', 43500, 'Taller Díaz — Especialistas en Frenos',
            'Cambio pastillas traseras Brembo y ajuste de mordaza', 18500.00, NULL, '2026-11-01', GETDATE());

    -- Service 45.000 km (coincide con gasto registrado en Feb 2026)
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v1b, @u2, 'MOTOR', '2026-02-20', 45000, 'Centro Integral Toyota Córdoba',
            'Service 45.000 km — aceite, filtros, correa de distribución y bujías', 38000.00, 50000, NULL, GETDATE());

    -- Suspensión
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v1b, @u2, 'SUSPENSION', '2026-04-15', 46800, 'Autoservicio Norte',
            'Cambio de amortiguadores delanteros KYB y revisión de bujes', 27500.00, NULL, '2027-04-01', GETDATE());

    -- Neumáticos (coincide con gasto Jun 2026)
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v1b, @u2, 'CARROCERIA', '2026-06-10', 48500, 'Neumáticos Córdoba Sur',
            '4 cubiertas Bridgestone Turanza 195/65 R15 + balanceo y alineación', 62000.00, NULL, '2030-01-01', GETDATE());

END

IF NOT EXISTS (SELECT 1 FROM maintenance_logs WHERE vehicle_id = @v2b)
BEGIN

    -- ── Ford Focus (DEF456) ─────────────────────────────────────────────────

    -- Eléctrico
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v2b, @u2, 'ELECTRICO', '2025-07-20', 68000, 'Electromecánica Rodríguez',
            'Cambio de batería Bosch 60Ah y revisión del alternador', 24000.00, NULL, '2028-07-01', GETDATE());

    -- Frenos (coincide con gasto Feb 2026)
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v2b, @u2, 'FRENOS', '2026-02-18', 70800, 'Taller Díaz — Especialistas en Frenos',
            'Cambio pastillas y discos delanteros, limpieza de pinzas traseras', 54000.00, 80000, NULL, GETDATE());

    -- Transmisión
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v2b, @u2, 'TRANSMISION', '2026-03-12', 71200, 'Transmisiones Del Sur',
            'Cambio de aceite de caja automática y filtro interno', 16800.00, 80000, NULL, GETDATE());

    -- Service aceite (coincide con gasto May 2026)
    INSERT INTO maintenance_logs (vehicle_id, user_id, system, date, km_at_maintenance, workshop, description, cost, next_service_km, next_service_date, created_at)
    VALUES (@v2b, @u2, 'MOTOR', '2026-05-22', 72000, 'Taller Oficial Ford Córdoba',
            'Cambio de aceite sintético 5W40 y filtro de aceite', 12500.00, 77000, NULL, GETDATE());

END
GO

-- ── 5. Alertas ───────────────────────────────────────────────────────────────
DECLARE @u3  BIGINT = (SELECT id FROM users    WHERE email = 'test@mycar.app');
DECLARE @v1c BIGINT = (SELECT id FROM vehicles WHERE plate = 'ABC123');
DECLARE @v2c BIGINT = (SELECT id FROM vehicles WHERE plate = 'DEF456');

IF NOT EXISTS (SELECT 1 FROM alerts WHERE user_id = @u3)
BEGIN

    -- ── Toyota Corolla ABC123 (48.500 km actual) ──────────────────────────

    -- URGENTE: VTV vencida hace 20 días
    INSERT INTO alerts (vehicle_id, user_id, title, alert_type, alert_date, alert_km, advance_days, urgency_level, notified, notified_at, active, created_at)
    VALUES (@v1c, @u3, 'VTV / Revisión Técnica', 'DATE', '2026-06-10', NULL, 30, 'URGENTE', 0, NULL, 1, GETDATE());

    -- URGENTE: Aceite excedido (límite 48.000 km, actual 48.500)
    INSERT INTO alerts (vehicle_id, user_id, title, alert_type, alert_date, alert_km, advance_days, urgency_level, notified, notified_at, active, created_at)
    VALUES (@v1c, @u3, 'Cambio de Aceite', 'KM', NULL, 48000, 30, 'URGENTE', 0, NULL, 1, GETDATE());

    -- ADVERTENCIA: Seguro vence en 15 días (advance_days=30)
    INSERT INTO alerts (vehicle_id, user_id, title, alert_type, alert_date, alert_km, advance_days, urgency_level, notified, notified_at, active, created_at)
    VALUES (@v1c, @u3, 'Renovación de Seguro', 'DATE', '2026-07-15', NULL, 30, 'ADVERTENCIA', 0, NULL, 1, GETDATE());

    -- INFORMATIVA: Próximo service a 50.000 km (1.500 km libres)
    INSERT INTO alerts (vehicle_id, user_id, title, alert_type, alert_date, alert_km, advance_days, urgency_level, notified, notified_at, active, created_at)
    VALUES (@v1c, @u3, 'Service 50.000 km', 'KM', NULL, 50000, 30, 'INFORMATIVA', 0, NULL, 1, GETDATE());

    -- ── Ford Focus DEF456 (72.000 km actual) ─────────────────────────────

    -- URGENTE: Seguro vencido hace 25 días
    INSERT INTO alerts (vehicle_id, user_id, title, alert_type, alert_date, alert_km, advance_days, urgency_level, notified, notified_at, active, created_at)
    VALUES (@v2c, @u3, 'Póliza de Seguro Vencida', 'DATE', '2026-06-05', NULL, 30, 'URGENTE', 0, NULL, 1, GETDATE());

    -- ADVERTENCIA: Service a sólo 500 km
    INSERT INTO alerts (vehicle_id, user_id, title, alert_type, alert_date, alert_km, advance_days, urgency_level, notified, notified_at, active, created_at)
    VALUES (@v2c, @u3, 'Service Motor 72.500 km', 'KM', NULL, 72500, 30, 'ADVERTENCIA', 0, NULL, 1, GETDATE());

    -- ADVERTENCIA: Patente vence en 10 días
    INSERT INTO alerts (vehicle_id, user_id, title, alert_type, alert_date, alert_km, advance_days, urgency_level, notified, notified_at, active, created_at)
    VALUES (@v2c, @u3, 'Pago de Patente', 'DATE', '2026-07-10', NULL, 30, 'ADVERTENCIA', 0, NULL, 1, GETDATE());

    -- INFORMATIVA: RTO vence en 3 meses
    INSERT INTO alerts (vehicle_id, user_id, title, alert_type, alert_date, alert_km, advance_days, urgency_level, notified, notified_at, active, created_at)
    VALUES (@v2c, @u3, 'RTO / Revisión Técnica Obligatoria', 'DATE', '2026-10-15', NULL, 30, 'INFORMATIVA', 0, NULL, 1, GETDATE());

END
GO
