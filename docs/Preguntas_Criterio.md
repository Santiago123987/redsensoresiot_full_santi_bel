# Criterios de Decisión: Estructuras de Datos en la Red de Sensores

Este documento es la parte más importante de tu entrega. No basta con que el código funcione; debes justificar **por qué** elegiste una colección de Java sobre otra.

Responde a las siguientes preguntas basándote en la teoría de nodos y el rendimiento del JCF.

### 1. El dilema del Historial
Para el `historialEventos`, podrías usar un `ArrayList` o un `LinkedList`. 
- Si la plataforma registra miles de eventos por segundo y necesitas eliminar los más antiguos frecuentemente para ahorrar memoria, ¿cuál de las dos colecciones es más eficiente y por qué? (Pista: piensa en el costo de eliminar el primer elemento).
- Resouesta: Usé ArrayList porque el historial se llena agregando al final y se lee por posición, y ahí es rápido. Si es verdad que borrar el primer elemento es lento en un arrayList, porque todos se corren pero para eso la limpieza la hago por bloques con subList().clear(), así que ese desplazamiento ocurre una sola vez. Con LinkedList borraría el primero más rápido, pero leer los últimos eventos sería más lento y gastaría más memoria.

### 2. Navegación Bidireccional
En el `navegadorAlertas`, utilizamos un `Deque` (Double Ended Queue). 
- ¿Qué ventaja ofrece un `Deque` sobre una `Queue` simple para implementar la funcionalidad de "Atrás" y "Adelante"? 
- ¿Cómo se relaciona esto con el concepto de un `NodoDoble`?
- Respuesta: La Queue solo permite insertar por el final y sacar por el frente, o sea un solo sentido, mientras que el Deque permite agregar, sacar y consultar por ambos extremos. Se relaciona con el NodoDoble porque este tiene referencias siguiente y anterior, así que desde cualquier nodo se puede avanzar o retroceder, y el Deque ofrece ese mismo comportamiento en dos sentidos. Por eso lo usé en el navegador de alertas: “siguiente” saca del frente y lo pasa al final, y “anterior” hace lo contrario, ambas en O(1).

### 3. El Ciclo de Monitoreo
Para la `colaMonitoreo`, implementaste una rotación (sacar del frente y meter al final).
- ¿Por qué una `Queue` es la estructura natural para este problema? 
- ¿Qué pasaría con la equidad del sistema si usaras una `Stack` (Pila) en su lugar?
- Respuesta: La cola es apropiada porque es FIFO: el primer sensor programado es el primero en consultarse, y con la rotación (sacar del frente y volver a meter al final) todos reciben turnos por igual, en round-robin. Con una pila (LIFO) saldría siempre el último en entrar, que sería el sensor recién reinsertado, y los del fondo nunca serían consultados (starvation), así que se perdería la equidad.

### 4. Topología de Red (El Grafo)
Representaste la red de sensores como un `Map<String, List<String>>`.
- ¿Por qué usamos un `Map` y no un arreglo bidimensional (matriz de adyacencia) considerando que la mayoría de los sensores solo están conectados a 2 o 3 vecinos?
- ¿Cómo se traduce esta estructura de `Map` a la idea de un `NodoGrafo` con una lista de adyacencias?
- Respuesta: El Map con listas ocupa O(V + E), proporcional a las conexiones que realmente existen, mientras que la matriz siempre ocupa V×V aunque casi todo sean ceros. Además es fácil agregar sensores nuevos, se accede directamente por el id y listar los vecinos cuesta O(grado). Su desventaja es que saber si dos sensores concretos están conectados toca recorrer la lista, mientras que en la matriz es un acceso directo O(1), y en grafos muy densos la matriz no gasta más memoria. Es una lista de adyacencia porque cada clave es un nodo (un sensor) y su List guarda sus adyacentes, y como la conexión es bidireccional queda registrada en las dos listas.

### 5. Buffer de Ingesta y Rendimiento
El `bufferIngesta` procesa datos en orden FIFO.
- Si el volumen de datos aumenta drásticamente y necesitas procesar las lecturas en paralelo usando múltiples hilos, ¿seguiría siendo adecuado un `ArrayDeque` o deberías cambiar a una `ConcurrentLinkedQueue`? Justifica tu respuesta basándote en la seguridad de hilos (Thread-safety).
- Respuesta: ArrayDeque no es thread-safe y ConcurrentLinkedQueue sí, es decir, esta última funciona correctamente aunque varios hilos la usen a la vez. Si dos hilos hacen offer o poll sobre un ArrayDeque pueden dañar sus índices internos, perder lecturas o procesar la misma dos veces, mientras que ConcurrentLinkedQueue lo evita con operaciones atómicas sin bloqueos, a cambio de ser más lenta por operación. Como mi proyecto usa un solo hilo, ArrayDeque es lo adecuado, pero si las lecturas llegaran desde varios hilos cambiaría a ConcurrentLinkedQueue.

### Uso de IA
Usé la IA para que me ayudara en la redacción del código y ponerles los comentarios y la comprensión de los temas que se necesitaban aprender y para mejorar la redacción de mis respuestas tras haberlas escrito personalmente.