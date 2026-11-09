package structures.management;

import model.LecturaSensor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos
 * VERSION ESTUDIANTE
 *
 * Objetivo: Implementar la gestión de la red utilizando las colecciones de Java (JCF).
 * El éxito de este módulo no es solo que el código compile, sino que elijas
 * la estructura correcta para cada problema.
 */
public class GestorDeRed {
    // 1. Historial de Eventos: Registro lineal de todas las actividades
    private List<String> historialEventos;

    // 2. Navegador de Alertas: Permite moverse adelante y atrás entre alertas
    private Deque<String> navegadorAlertas;
    private String alertaActual;

    // 3. Ciclo de Monitoreo: Gestión de la rotación de sensores a consultar
    private Queue<String> colaMonitoreo;

    // 4. Topología de la Red: Grafo de conexiones entre estaciones (Lista de adyacencia)
    private Map<String, List<String>> topologiaRed;

    // 5. Buffer de Ingesta: Cola de espera para procesar lecturas entrantes
    private Queue<LecturaSensor> bufferIngesta;

    public GestorDeRed() {
        // TODO: Inicializa las colecciones usando las implementaciones más adecuadas de Java
        // (ArrayList, ArrayDeque, HashMap, etc.)
        this.historialEventos = new ArrayList<>();
        this.navegadorAlertas = new ArrayDeque<>();
        this.colaMonitoreo = new ArrayDeque<>();
        this.topologiaRed = new HashMap<>();
        this.bufferIngesta = new ArrayDeque<>();
    }

    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================

    public void agregarEvento(String evento) {
        String marca = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
        historialEventos.add("[" + marca + "] " + evento);
    }

    public List<String> obtenerEventosRecientes(int limite) {
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);
        return historialEventos.subList(historialEventos.size() - limite, historialEventos.size());
    }

    public void limpiarHistorialAntiguo(int mantener) {
        int sobrantes = historialEventos.size() - mantener;
        if (sobrantes > 0) {
            historialEventos.subList(0, sobrantes).clear();
        }
    }

    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    public void registrarAlerta(String alerta) {
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
    }

    public String navegarAlSiguiente() {
        // TODO 2: Implementar la lógica de desplazamiento hacia adelante.
        // Debe devolver la siguiente alerta o null si no hay más.
        // Recuerda actualizar el estado de 'alertaActual'.
        return null;
    }

    public String navegarAlAnterior() {
        // TODO 3: Implementar la lógica de desplazamiento hacia atrás.
        // Debe devolver la alerta anterior o null si es la primera.
        // Recuerda actualizar el estado de 'alertaActual'.
        return null;
    }

    public String getAlertaActual() { return alertaActual; }

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    public void programarConsulta(String idSensor) {
        if (!colaMonitoreo.contains(idSensor)) {
            colaMonitoreo.offer(idSensor);
        }
    }

    public String obtenerSiguienteSensor() {
        String sensor = colaMonitoreo.poll();
        if (sensor == null) {
            return null;
        }
        colaMonitoreo.offer(sensor);
        return sensor;
    }

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED
    // =========================================================================

    public void registrarConexion(String sensorA, String sensorB) {
        List<String> vecinosA = topologiaRed.computeIfAbsent(sensorA, k -> new ArrayList<>());
        List<String> vecinosB = topologiaRed.computeIfAbsent(sensorB, k -> new ArrayList<>());
        if (!vecinosA.contains(sensorB)) vecinosA.add(sensorB);
        if (!vecinosB.contains(sensorA)) vecinosB.add(sensorA);
    }

    public List<String> obtenerVecinos(String idSensor) {
        return new ArrayList<>(topologiaRed.getOrDefault(idSensor, Collections.emptyList()));
    }

    public List<String> detectarEstacionesAisladas() {
        List<String> aisladas = new ArrayList<>();
        for (Map.Entry<String, List<String>> entrada : topologiaRed.entrySet()) {
            if (entrada.getValue().isEmpty()) {
                aisladas.add(entrada.getKey());
            }
        }
        return aisladas;
    }

    // Me toco añadir este metodo extra pq con solo registrarConexion,
    // nunca puede existir una estación sin vecinos, porque cada conexión agrega a los dos extremos.
    // Entonces el TODO 9 no tendría casos reales.

    public void registrarEstacion(String idSensor) {
        topologiaRed.computeIfAbsent(idSensor, k -> new ArrayList<>());
    }

    // =========================================================================
    // SECCIÓN 5: BUFFER DE INGESTA
    // =========================================================================

    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    public LecturaSensor procesarSiguienteLectura() {
        // TODO 8: Implementar la extracción del buffer.
        // Debe remover y devolver la lectura más antigua del buffer (FIFO).
        return null;
    }

    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        // TODO EXTRA: Usar Streams para filtrar lecturas con PM2.5 superior al límite.
        // Debe retornar una lista con las lecturas filtradas.
        return null;
    }
}
