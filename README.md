Pruebas Automatizadas - Sistema Bancario Descripción En este proyecto desarrollamos pruebas automatizadas para un sistema de gestión de transacciones bancarias usando RestAssured y JUnit 5. Comenzando Requisitos

Java 11+ Maven o Gradle JDK instalado

Dependencias

RestAssured JUnit 5 Hamcrest Matchers

Endpoint de Pruebas https://67e48bae2ae442db76d4c670.mockapi.io/api/bancoegg/Transacciones Características Principales

Generación de transacciones con datos aleatorios Validación de emails únicos Pruebas de depósito y retiro Control de saldos bancarios

Tests Implementados

Limpieza del endpoint antes de cada prueba Creación de transacciones Verificación de emails únicos Actualización de número de cuenta Gestión de saldos Control de retiros

Ejecución de Pruebas

Con Maven bash mvn clean test
