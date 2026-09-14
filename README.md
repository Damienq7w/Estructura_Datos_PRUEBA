# Estructura_Datos_PRUEBA

# Tower Defense — Estructura de Datos (Java)

# Grupo - 04

Prueba práctica de Estructura de Datos (UTA): simulador Tower Defense con las tres
estructuras exigidas, implementadas manualmente (sin colecciones de `java.util`).

## Estructura del repositorio

```
Estructura_Datos_PRUEBA/
├── .vscode/
│   └── settings.json                    → indica a VS Code que el código fuente empieza en TowerDefense/
├── Capturas_Ejecucion/
│   ├── Ejecucion_01.jpeg                → capturas de pantalla de la ejecución
│   ├── Ejecucion_02.jpeg
│   └── tablero_gui.png                  → captura de la interfaz gráfica opcional
├── Diagrama de clases/
│   └── Diagrama de Clases.png           → diagrama de clases del proyecto
├── Documento/
│   └── Documentacion_Examen.pdf         → informe y documentación escrita del examen
├── TowerDefense/
│   └── Scr_java/                        → código fuente (.java), paquete `Scr_java`
│       ├── Enemigo.java
│       ├── Jugador.java
│       ├── ListaCircularOleadas.java
│       ├── ListaDobleEnemigos.java
│       ├── ListaSecuencialTorres.java
│       ├── Main.java                   → menú de consola
│       ├── NodoEnemigo.java
│       ├── NodoOleada.java
│       ├── Oleada.java
│       ├── ReglasJuego.java             → reglas compartidas por la consola y la interfaz gráfica
│       ├── TableroPanel.java            → dibuja el tablero de la interfaz gráfica
│       ├── Torre.java
│       ├── TowerDefenseApp.java         
│       └── TowerDefenseGUI.java         → interfaz gráfica (Swing, tablero animado)
├── TrabajoEnEquipoPruebas/              → evidencia del trabajo en equipo en GitHub
│   ├── Evidencia_GitHub_cunalata.docx   
│   ├── Evidencia_GitHub_tisalema.docx
│   ├── Evidencia_GitHub_silva.docx
│   ├── Evidencia_GitHub_chalco.docx
│   └── Evidencia_GitHub_camacho.docx
├── .gitignore
└── README.md
```

## Compilar y ejecutar

Todas las clases pertenecen al paquete `Scr_java`, por eso se compila y ejecuta desde la
carpeta `TowerDefense` usando el nombre completo de la clase:

```bash
cd TowerDefense
javac -encoding UTF-8 -d bin Scr_java/*.java
java -cp bin Scr_java.TowerDefenseApp     # menú de consola
java -cp bin Scr_java.TowerDefenseGUI     # interfaz gráfica opcional
```

`Main.java` es un punto de entrada alterno: solo invoca `TowerDefenseApp.main()`, por si el
entorno de ejecución espera una clase `Main` (`java -cp bin Scr_java.Main`). El `main` exigido
por el enunciado sigue estando en `TowerDefenseApp`.

### Desde VS Code

Abrir la carpeta `Estructura_Datos_PRUEBA` completa. El archivo `.vscode/settings.json` le indica
a VS Code que el código fuente empieza en `TowerDefense/`, así el paquete `Scr_java` coincide con
su carpeta y las clases se compilan en `TowerDefense/bin`. Si al ejecutar aparece
`ClassNotFoundException`, usar `Ctrl+Shift+P` → **Java: Force Java Compilation** → **Full** y
volver a ejecutar.

## Reparto del equipo (6 integrantes)

| Integrante                        | Rol                         | Clases a cargo                                                                                                                                                                                       | Rama              |
| --------------------------------- | --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------- |
| Cunalata Mendoza Damian Alexander | Líder técnico e integración | `Torre`, `TowerDefenseApp` (menú de consola y lógica de "avanzar turno") y `TowerDefenseGUI` (interfaz gráfica opcional); integración de las tres estructuras, compilación final y prueba del caso sugerido | `Damian_Cunalata` |
| Tisalema Guashco Darwin Joel      | Desarrollo                  | `ListaSecuencialTorres` (insertar, eliminar por id, buscar, mostrar y contar torres activas), `Main` y `NodoEnemigo`                                                                                  | `Rama-Joel`       |
| Silva Camuendo Luis Alexander     | Desarrollo                  | `NodoOleada`, `Oleada` y `TableroPanel` (panel Swing que dibuja el camino, las torres, los enemigos y los efectos de combate)                                                                        | `Rama-Luis-Silva` |
| Chalco Tasna Kenneth Mateo        | Desarrollo                  | `Enemigo` (avanzar, recibir daño, destrucción), `Jugador` (vidas, derrota), `ListaCircularOleadas` (registrar, avanzar y reiniciar el ciclo) y `ListaDobleEnemigos` (insertar, buscar por id, eliminar y recorrido bidireccional) | `Mateo-Chalco`    |
| Camacho Monta Josue Jampier       | Documentación               | Este README (generación y mantenimiento), diagrama de clases, explicación de cada estructura y capturas de pantalla de la ejecución                                                                  | `rama---Josue`    |
| Tacuri Santillan Mónica Sara      | Desarrollo (no completado)  | Tenía asignadas las validaciones de `ListaSecuencialTorres` (lista vacía, llena, id repetido), pero no llegó a tiempo a la prueba práctica y no realizó su parte, por lo que no registra commits    | —                 |

Cada integrante desarrolló sus clases en su propia rama y las subió al repositorio; el líder
integró todo en `main` a través de `TowerDefenseApp` y validó que el caso de prueba sugerido
funcione antes de la entrega; Josue documentó el proyecto completo en este mismo README.

## Breve explicación del uso de cada estructura de datos

| Estructura                                                                    | Dónde                   | Por qué                                                                                                                                            |
| ----------------------------------------------------------------------------- | ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Lista secuencial** (`Torre[] datos` + `cantidad`)                           | `ListaSecuencialTorres` | Pocas torres, se recorren completas cada turno; O(1) insertar al final.                                                                            |
| **Lista doble con primero/último** (`NodoEnemigo` con `anterior`/`siguiente`) | `ListaDobleEnemigos`    | Los enemigos se destruyen o retiran constantemente; eliminar un nodo ya ubicado es O(1) sin desplazar nada, y permite recorrido en ambos sentidos. |
| **Lista circular simple con referencia a último** (`NodoOleada`)              | `ListaCircularOleadas`  | El `siguiente` del último apunta al primero: el ciclo de oleadas avanza y se reinicia sin lógica especial.                                         |

### Lista secuencial de torres (`ListaSecuencialTorres`)

Guarda las torres defensivas en un arreglo `Torre[] datos` de capacidad fija, con un contador
`cantidad` que marca cuántas posiciones están ocupadas (siempre `datos[0 .. cantidad-1]`).
`insertarTorre` agrega al final en O(1) si hay espacio y el id no existe; `buscarTorrePorId`
recorre linealmente hasta encontrar el id (O(n)); `eliminarTorrePorId` localiza la posición y
desplaza los elementos siguientes una posición a la izquierda para no dejar huecos (O(n));
`mostrarTodasLasTorres` recorre la zona ocupada e imprime cada torre; `contarTorresActivas`
simplemente devuelve `cantidad`, porque toda torre presente en el arreglo está activa. Se eligió
por ser el número de torres siempre pequeño y por recorrerse por completo en cada turno (para
verificar rango), donde el costo de un arreglo compacto es despreciable frente a su simplicidad.

### Lista doblemente enlazada de enemigos (`ListaDobleEnemigos`)

Cada enemigo activo vive en un `NodoEnemigo` con referencias a `anterior` y `siguiente`, y la
lista mantiene `primero` y `ultimo` para insertar y recorrer sin recorrer todo el camino.
`insertarEnemigoAlFinal` engancha el nuevo nodo tras `ultimo` en O(1); `buscarEnemigoPorId`
recorre desde `primero` hasta encontrar el id (O(n)); `eliminarEnemigoDestruido` reenlaza
`anterior.siguiente` y `siguiente.anterior` alrededor del nodo eliminado, sin desplazar ningún
otro elemento (O(1) una vez localizado el nodo); `recorrerHaciaAdelante` y `recorrerHaciaAtras`
recorren la lista desde `primero` o desde `ultimo` respectivamente, usando las dos referencias
del nodo. Se eligió esta estructura porque los enemigos se agregan y eliminan constantemente en
cada turno, y un arreglo obligaría a desplazar elementos en cada baja; el doble enlace también
es indispensable para exigir el recorrido bidireccional que pide el enunciado.

### Lista circular simple de oleadas (`ListaCircularOleadas`)

Los `NodoOleada` solo tienen `siguiente`, pero el `siguiente` del último nodo apunta siempre al
primero, formando un círculo; la lista guarda la referencia `ultimo` (no hace falta `primero`,
porque `ultimo.siguiente` ya lo es). `registrarOleada` inserta el nuevo nodo justo después de
`ultimo` y lo convierte en el nuevo `ultimo`, preservando la circularidad; `mostrarOleadas`
recorre el círculo una sola vez con un `do-while` que se detiene al volver al nodo de inicio;
`avanzarSiguienteOleada` mueve un puntero `actual` al siguiente nodo (al primero, la primera
vez) para saber qué oleada toca iniciar; `reiniciarCiclo` limpia ese puntero para volver a
empezar desde la primera oleada registrada. Se eligió esta estructura porque las oleadas se
repiten en ciclo por diseño del juego, y la circularidad evita tener que programar aparte la
lógica de "volver al inicio" cuando se termina la última oleada.

## Lógica de "Avanzar turno" (opción 7)

1. Mueve enemigos según su velocidad.
2. Cada torre daña a los enemigos dentro de su rango (`Torre.enRango`, distancia absoluta).
3. Elimina los enemigos cuya vida llegó a 0.
4. Descuenta una vida al jugador por cada enemigo que llegó al final del camino (`LONGITUD_RUTA = 20`) y lo retira.
5. Imprime el resumen del turno.

La partida termina por derrota (0 vidas) o victoria (todas las oleadas iniciadas y sin enemigos activos).

## Caso de prueba sugerido — verificado

Torre Arquero (pos 3, daño 20, rango 2), Torre Cañón (pos 8, daño 35, rango 3), Oleada de
3 básicos (vida 50, vel 1), Oleada de 2 rápidos (vida 40, vel 2), jugador con 3 vidas.
Compilado y ejecutado: el Arquero entra en rango desde el primer turno y destruye a los
tres enemigos básicos en el turno 3 (50 → 30 → 10 → 0), sin pérdida de vidas.

## Checklist de entregables

- [x] Archivos `.java` compilables y ejecutables — carpeta `TowerDefense/Scr_java/` — **Damian, Joel, Luis y Mateo**.
- [x] Capturas de pantalla de la ejecución — carpeta `Capturas_Ejecucion/` — **Josue**.
- [x] Explicación breve de cada estructura (arriba) — **Josue**.
- [x] Diagrama de clases — carpeta `Diagrama de clases/` — **Josue**.
- [x] Invitar al docente (`joseru82@hotmail.com`) como colaborador del repositorio — **Damian**.

## Restricciones respetadas

Sin `ArrayList`, `LinkedList`, `Queue`, `Deque` ni colecciones de `java.util` (de ese paquete
solo se usa `Scanner` para leer el teclado). Listas implementadas manualmente con arreglos y
referencias entre objetos. Clases separadas en archivos `.java`, con `TowerDefenseApp`
conteniendo `main`.
