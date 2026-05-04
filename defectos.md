# Registro de Defectos

Este archivo documenta defectos detectados en el taller de pruebas de integracion y sistema.

## Defecto 01 - Cobertura global por debajo del umbral minimo

- **Caso probado:** Ejecucion del pipeline completo de calidad con `mvn clean verify`.
- **Resultado esperado:** Cobertura global JaCoCo >= 80%.
- **Resultado obtenido:** Cobertura global inferior al umbral (Instrucciones: 72.58%, Lineas: 69.17%).
- **Causa probable:** Faltan pruebas para componentes con baja cobertura, principalmente en `infrastructure.persistence` y clases de modelo/arranque.
- **Estado:** Abierto.
- **Evidencia:**
  - Reporte: `registraduria/target/site/jacoco/index.html`
  - Datos crudos: `registraduria/target/site/jacoco/jacoco.csv`
  - Comando ejecutado: `mvn clean verify`

## Convencion de estados

- **Abierto:** Defecto detectado y pendiente de correccion.
- **En progreso:** Defecto en proceso de correccion.
- **Resuelto:** Defecto corregido y validado en pruebas.
