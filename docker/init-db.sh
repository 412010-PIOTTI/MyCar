#!/bin/bash
# Inicia SQL Server y crea la base de datos mycar_db si no existe.
# Este script corre como entrypoint del contenedor.

set -e

# Arrancar SQL Server en background
/opt/mssql/bin/sqlservr &
PID=$!

echo "Esperando que SQL Server esté listo..."
until /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT 1" -No 2>/dev/null; do
    sleep 2
done

echo "Creando base de datos mycar_db si no existe..."
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -No -Q "
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'mycar_db')
BEGIN
    CREATE DATABASE mycar_db;
    PRINT 'Base de datos mycar_db creada.';
END
ELSE
BEGIN
    PRINT 'Base de datos mycar_db ya existe.';
END
"

echo "SQL Server listo en localhost:1433"
wait $PID
