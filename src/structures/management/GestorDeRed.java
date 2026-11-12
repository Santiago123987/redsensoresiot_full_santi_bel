package structures.management; // carpeta (paquete) donde vive esta clase

import model.LecturaSensor; // importa la clase que representa una lectura de un sensor
import java.util.*; // importa List, ArrayList, Deque, ArrayDeque, Queue, Map, HashMap, Date, Collections
import java.util.stream.Collectors; // importa Collectors, que se usa al final del stream

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos
 * Cada atributo es una colección distinta, elegida según el problema que resuelve.
 */
public class GestorDeRed { // clase que agrupa toda la gestión de la red de sensores
    // 1. Historial de Eventos: registro lineal de todas las actividades
    private List<String> historialEventos; // lista de textos: cada evento con su fecha y hora

    // 2. Navegador de Alertas: permite moverse adelante y atrás entre alertas
    private Deque<String> navegadorAlertas; // cola de dos puntas: se entra y sale por el frente y por el final
    private String alertaActual; // guarda cuál es la alerta en la que estamos parados

    // 3. Ciclo de Monitoreo: gestión de la rotación de sensores a consultar
    private Queue<String> colaMonitoreo; // fila de ids de sensores esperando su turno (FIFO)

    // 4. Topología de la Red: grafo de conexiones entre estaciones (lista de adyacencia)
    private Map<String, List<String>> topologiaRed; // diccionario: id del sensor -> lista de sus vecinos

    // 5. Buffer de Ingesta: cola de espera para procesar lecturas entrantes
    private Queue<LecturaSensor> bufferIngesta; // fila de lecturas que llegaron y aún no se procesan (FIFO)

    public GestorDeRed() { // constructor: se ejecuta al crear un GestorDeRed
        // Aquí se crea (inicializa) cada colección con su implementación concreta
        this.historialEventos = new ArrayList<>(); // ArrayList: se agrega al final y se lee por posición
        this.navegadorAlertas = new ArrayDeque<>(); // ArrayDeque: operaciones O(1) en ambos extremos
        this.colaMonitoreo = new ArrayDeque<>(); // ArrayDeque usado como cola simple (FIFO)
        this.topologiaRed = new HashMap<>(); // HashMap: busca un sensor por su id en O(1)
        this.bufferIngesta = new ArrayDeque<>(); // ArrayDeque usado como cola de lecturas (FIFO)
    } // fin del constructor

    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================

    public void agregarEvento(String evento) { // TODO 1: registra un evento con fecha y hora
        String marca = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()); // toma la hora actual y la escribe como texto, ej. 2026-11-05 10:00
        historialEventos.add("[" + marca + "] " + evento); // une "[fecha] texto" y lo agrega al final de la lista
    } // fin de agregarEvento

    public List<String> obtenerEventosRecientes(int limite) { // devuelve los últimos 'limite' eventos
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos); // si hay pocos eventos, devuelve una copia de todos
        return historialEventos.subList(historialEventos.size() - limite, historialEventos.size()); // si no, devuelve solo el tramo final (los más nuevos)
    } // fin de obtenerEventosRecientes

    public void limpiarHistorialAntiguo(int mantener) { // TODO 10: deja solo los últimos 'mantener' eventos
        int sobrantes = historialEventos.size() - mantener; // cuántos eventos hay de más (los viejos)
        if (sobrantes > 0) { // solo si de verdad sobran
            historialEventos.subList(0, sobrantes).clear(); // borra de golpe los más viejos (están al inicio), un solo desplazamiento
        } // fin del if
    } // fin de limpiarHistorialAntiguo

    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    public void registrarAlerta(String alerta) { // agrega una alerta nueva (este método ya venía hecho)
        navegadorAlertas.addLast(alerta); // la pone al final del Deque
        alertaActual = alerta; // y la marca como la actual: la actual siempre es el último elemento
    } // fin de registrarAlerta

    public String navegarAlSiguiente() { // TODO 2: pasa a la alerta siguiente (circular)
        String siguiente = navegadorAlertas.pollFirst(); // saca la que está al frente (la que sigue a la actual)
        if (siguiente == null) { // si no había ninguna alerta
            return null; // no hay a dónde ir: devuelve null
        } // fin del if
        navegadorAlertas.addLast(siguiente); // la manda al final para que sea la nueva actual (y se mantenga la regla)
        alertaActual = siguiente; // actualiza cuál es la alerta actual
        return siguiente; // devuelve la alerta a la que llegamos
    } // fin de navegarAlSiguiente

    public String navegarAlAnterior() { // TODO 3: vuelve a la alerta anterior (circular)
        String actual = navegadorAlertas.pollLast(); // saca la actual, que está al final
        if (actual == null) { // si no había ninguna alerta
            return null; // no hay a dónde ir: devuelve null
        } // fin del if
        navegadorAlertas.addFirst(actual); // la manda al frente; así la que queda al final es la anterior
        alertaActual = navegadorAlertas.peekLast(); // mira (sin sacar) el último: esa es la nueva actual
        return alertaActual; // devuelve la alerta a la que volvimos
    } // fin de navegarAlAnterior

    public String getAlertaActual() { return alertaActual; } // devuelve cuál es la alerta actual

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    public void programarConsulta(String idSensor) { // TODO 4: mete un sensor a la fila de consultas
        if (!colaMonitoreo.contains(idSensor)) { // si el sensor NO está ya en la fila
            colaMonitoreo.offer(idSensor); // lo agrega al final de la fila
        } // fin del if (si ya estaba, no se hace nada: así no hay duplicados)
    } // fin de programarConsulta

    public String obtenerSiguienteSensor() { // TODO 5: entrega el sensor que toca consultar y lo rota
        String sensor = colaMonitoreo.poll(); // saca el primero de la fila (null si está vacía)
        if (sensor == null) { // si la fila estaba vacía
            return null; // devuelve null en vez de fallar
        } // fin del if
        colaMonitoreo.offer(sensor); // lo vuelve a meter al final: así espera su próximo turno (rotación)
        return sensor; // devuelve el sensor que toca consultar ahora
    } // fin de obtenerSiguienteSensor

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED
    // =========================================================================

    public void registrarConexion(String sensorA, String sensorB) { // TODO 6: conecta dos sensores en ambos sentidos
        List<String> vecinosA = topologiaRed.computeIfAbsent(sensorA, k -> new ArrayList<>()); // obtiene la lista de vecinos de A (la crea vacía si A es nuevo)
        List<String> vecinosB = topologiaRed.computeIfAbsent(sensorB, k -> new ArrayList<>()); // lo mismo para B
        if (!vecinosA.contains(sensorB)) vecinosA.add(sensorB); // anota a B en la ficha de A, si no estaba ya
        if (!vecinosB.contains(sensorA)) vecinosB.add(sensorA); // anota a A en la ficha de B, si no estaba ya (conexión bidireccional)
    } // fin de registrarConexion

    public List<String> obtenerVecinos(String idSensor) { // TODO 7: devuelve los vecinos de un sensor
        return new ArrayList<>(topologiaRed.getOrDefault(idSensor, Collections.emptyList())); // lista del sensor o lista vacía si no existe; se devuelve una copia para proteger la red
    } // fin de obtenerVecinos

    public List<String> detectarEstacionesAisladas() { // TODO 9: encuentra los sensores sin ninguna conexión
        List<String> aisladas = new ArrayList<>(); // lista donde se irán guardando las aisladas
        for (Map.Entry<String, List<String>> entrada : topologiaRed.entrySet()) { // recorre cada par (sensor, sus vecinos) del mapa
            if (entrada.getValue().isEmpty()) { // si su lista de vecinos está vacía
                aisladas.add(entrada.getKey()); // ese sensor está aislado: se agrega a la lista
            } // fin del if
        } // fin del for
        return aisladas; // devuelve todas las estaciones aisladas encontradas
    } // fin de detectarEstacionesAisladas

    // Método extra: con solo registrarConexion nunca puede existir una estación sin vecinos,
    // porque cada conexión agrega a los dos extremos. Sin esto el TODO 9 no tendría casos reales.

    public void registrarEstacion(String idSensor) { // registra un sensor sin conectarlo a nadie
        topologiaRed.computeIfAbsent(idSensor, k -> new ArrayList<>()); // crea su ficha con lista de vecinos vacía (si no existía)
    } // fin de registrarEstacion

    // =========================================================================
    // SECCIÓN 5: BUFFER DE INGESTA
    // =========================================================================

    public void recibirLectura(LecturaSensor lectura) { // recibe una lectura nueva (este método ya venía hecho)
        bufferIngesta.add(lectura); // la pone al final de la fila de espera
    } // fin de recibirLectura

    public LecturaSensor procesarSiguienteLectura() { // TODO 8: entrega la lectura más antigua
        return bufferIngesta.poll(); // saca y devuelve la del frente (FIFO), o null si no hay ninguna
    } // fin de procesarSiguienteLectura

    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) { // EXTRA: lecturas con contaminación por encima del límite
        return bufferIngesta.stream() // convierte el buffer en un flujo de lecturas
                .filter(l -> l.getPm25() > limitePm25) // deja pasar solo las lecturas cuyo PM2.5 supera el límite
                .collect(Collectors.toList()); // junta las que pasaron en una lista nueva (el buffer no se modifica)
    } // fin de filtrarAlertasCriticas
} // fin de la clase GestorDeRed