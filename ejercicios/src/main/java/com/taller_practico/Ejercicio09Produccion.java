package com.taller_practico;

import java.util.Scanner;

// Registra mediciones de lotes y los clasifica según conformidad y defectos críticos.
public class Ejercicio09Produccion {
    static final int MAX_LOTES = 20;
    static final int MAX_MEDICIONES = 300;
    // Todas las mediciones se guardan en este único arreglo unidimensional.
    static double[] mediciones = new double[MAX_MEDICIONES];
    static int totalMediciones = 0;
    static String[] codigos = new String[MAX_LOTES];
    static int[] inicios = new int[MAX_LOTES];
    static int[] cantidades = new int[MAX_LOTES];
    static double[] promedios = new double[MAX_LOTES];
    static double[] minimos = new double[MAX_LOTES];
    static double[] maximos = new double[MAX_LOTES];
    static double[] porcentajes = new double[MAX_LOTES];
    static String[] clasificaciones = new String[MAX_LOTES];
    static int cantidadLotes = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        int opcion;
        do {
            System.out.println("\n1. Registrar lote");
            System.out.println("2. Consultar un lote");
            System.out.println("3. Listar lotes por clasificación");
            System.out.println("4. Consultar el resumen de producción");
            System.out.println("5. Salir");
            opcion = leerEntero(teclado, "Opción: ", 1, 5);
            switch (opcion) {
                case 1: registrarLote(teclado); break;
                case 2: consultarLote(teclado); break;
                case 3: listarPorClasificacion(teclado); break;
                case 4: mostrarResumen(); break;
                case 5: System.out.println("Programa finalizado."); break;
                default: break;
            }
        } while (opcion != 5);
        teclado.close();
    }

    // Lee todas las mediciones válidas y guarda dónde comienza el segmento del lote.
    static void registrarLote(Scanner teclado) {
        if (cantidadLotes == MAX_LOTES) {
            System.out.println("Capacidad de lotes agotada.");
            return;
        }
        System.out.print("Código LOT-T-NNN: ");
        String codigo = teclado.nextLine();
        if (!validarCodigo(codigo) || buscarLote(codigo) != -1) {
            System.out.println("Registro rechazado: código inválido o duplicado.");
            return;
        }
        int cantidad = leerEntero(teclado, "Cantidad de mediciones (5 a 15): ", 5, 15);
        if (totalMediciones + cantidad > MAX_MEDICIONES) {
            System.out.println("No hay espacio para las mediciones del lote.");
            return;
        }

        int lote = cantidadLotes;
        codigos[lote] = codigo;
        inicios[lote] = totalMediciones;
        cantidades[lote] = cantidad;
        for (int i = 0; i < cantidad; i++) {
            mediciones[totalMediciones++] = leerDouble(teclado, "Peso " + (i + 1) + " (450 a 550): ", 450, 550);
        }
        calcularResultados(lote);
        cantidadLotes++;
        System.out.println("Lote registrado y clasificado como " + clasificaciones[lote] + ".");
    }

    // Recorre solamente el segmento que pertenece al lote recibido.
    static void calcularResultados(int lote) {
        double suma = 0;
        double minimo = mediciones[inicios[lote]];
        double maximo = minimo;
        int conformes = 0;
        boolean critico = false;
        for (int i = inicios[lote]; i < inicios[lote] + cantidades[lote]; i++) {
            double peso = mediciones[i];
            suma += peso;
            if (peso < minimo) minimo = peso;
            if (peso > maximo) maximo = peso;
            if (peso >= 490 && peso <= 510) conformes++;
            if (peso < 480 || peso > 520) critico = true;
        }
        promedios[lote] = suma / cantidades[lote];
        minimos[lote] = minimo;
        maximos[lote] = maximo;
        porcentajes[lote] = conformes * 100.0 / cantidades[lote];
        // La prioridad evita que un lote crítico sea aprobado por su porcentaje.
        if (critico) clasificaciones[lote] = "RECHAZADO";
        else if (porcentajes[lote] >= 90.0) clasificaciones[lote] = "APROBADO";
        else if (porcentajes[lote] >= 70.0) clasificaciones[lote] = "REVISIÓN";
        else clasificaciones[lote] = "RECHAZADO";
    }

    // Busca un lote y recorre solo su segmento de mediciones.
    static void consultarLote(Scanner teclado) {
        System.out.print("Código del lote: ");
        int lote = buscarLote(teclado.nextLine());
        if (lote == -1) {
            System.out.println("Lote inexistente.");
            return;
        }
        mostrarLote(lote);
        System.out.println("Mediciones:");
        for (int i = inicios[lote]; i < inicios[lote] + cantidades[lote]; i++) System.out.println(mediciones[i]);
    }

    // Filtra los lotes de acuerdo con la clasificación seleccionada.
    static void listarPorClasificacion(Scanner teclado) {
        System.out.println("1. APROBADO  2. REVISIÓN  3. RECHAZADO");
        int opcion = leerEntero(teclado, "Clasificación: ", 1, 3);
        String buscada = opcion == 1 ? "APROBADO" : (opcion == 2 ? "REVISIÓN" : "RECHAZADO");
        boolean hay = false;
        for (int i = 0; i < cantidadLotes; i++) {
            if (clasificaciones[i].equals(buscada)) {
                mostrarLote(i);
                hay = true;
            }
        }
        if (!hay) System.out.println("No hay lotes con esa clasificación.");
    }

    // Acumula resultados por clasificación y por turno, controlando turnos vacíos.
    static void mostrarResumen() {
        int aprobados = 0, revision = 0, rechazados = 0;
        int[] piezasTurno = new int[3];
        int[] conformesTurno = new int[3];
        double[] pesoTurno = new double[3];
        double mayorPorcentaje = -1;
        for (int lote = 0; lote < cantidadLotes; lote++) {
            if (clasificaciones[lote].equals("APROBADO")) aprobados++;
            else if (clasificaciones[lote].equals("REVISIÓN")) revision++;
            else rechazados++;
            int turno = indiceTurno(codigos[lote].charAt(4));
            for (int i = inicios[lote]; i < inicios[lote] + cantidades[lote]; i++) {
                piezasTurno[turno]++;
                pesoTurno[turno] += mediciones[i];
                if (mediciones[i] >= 490 && mediciones[i] <= 510) conformesTurno[turno]++;
            }
            if (porcentajes[lote] > mayorPorcentaje) mayorPorcentaje = porcentajes[lote];
        }
        System.out.println("Aprobados: " + aprobados + " | Revisión: " + revision + " | Rechazados: " + rechazados);
        char[] turnos = {'M', 'T', 'N'};
        for (int i = 0; i < 3; i++) {
            if (piezasTurno[i] == 0) {
                System.out.println("Turno " + turnos[i] + ": sin mediciones.");
            } else {
                System.out.printf("Turno %c: %.2f%% conformes, peso promedio %.2f g%n", turnos[i],
                        conformesTurno[i] * 100.0 / piezasTurno[i], pesoTurno[i] / piezasTurno[i]);
            }
        }
        if (cantidadLotes > 0) {
            System.out.println("Lotes con mayor conformidad (" + mayorPorcentaje + "%):");
            for (int i = 0; i < cantidadLotes; i++) if (porcentajes[i] == mayorPorcentaje) System.out.println(codigos[i]);
        }
    }

    // Presenta los indicadores calculados del lote indicado.
    static void mostrarLote(int lote) {
        System.out.printf("%s | promedio %.2f | mínimo %.2f | máximo %.2f | conformidad %.2f%% | %s%n",
                codigos[lote], promedios[lote], minimos[lote], maximos[lote], porcentajes[lote], clasificaciones[lote]);
    }

    // Verifica el formato LOT-T-NNN y los tres turnos permitidos.
    static boolean validarCodigo(String codigo) {
        if (codigo.length() != 9 || !codigo.substring(0, 4).equals("LOT-") || codigo.charAt(5) != '-') return false;
        char turno = codigo.charAt(4);
        if (turno != 'M' && turno != 'T' && turno != 'N') return false;
        for (int i = 6; i < 9; i++) if (!esDigito(codigo.charAt(i))) return false;
        return true;
    }

    // Convierte M, T o N en la posición usada por los acumulados de turno.
    static int indiceTurno(char turno) { return turno == 'M' ? 0 : (turno == 'T' ? 1 : 2); }

    // Devuelve la posición del lote o -1 cuando no existe.
    static int buscarLote(String codigo) {
        for (int i = 0; i < cantidadLotes; i++) if (codigos[i].equals(codigo)) return i;
        return -1;
    }

    // Indica si el carácter es un dígito decimal.
    static boolean esDigito(char c) { return c >= '0' && c <= '9'; }

    // Lee un entero válido dentro del intervalo señalado.
    static int leerEntero(Scanner teclado, String mensaje, int minimo, int maximo) {
        while (true) {
            System.out.print(mensaje);
            try {
                int valor = Integer.parseInt(teclado.nextLine());
                if (valor >= minimo && valor <= maximo) return valor;
            } catch (NumberFormatException e) { }
            System.out.println("Entrada inválida.");
        }
    }

    // Lee un peso finito dentro de 450 a 550 y repite ante errores.
    static double leerDouble(Scanner teclado, String mensaje, double minimo, double maximo) {
        while (true) {
            System.out.print(mensaje);
            try {
                double valor = Double.parseDouble(teclado.nextLine());
                if (!Double.isNaN(valor) && !Double.isInfinite(valor) && valor >= minimo && valor <= maximo) return valor;
            } catch (NumberFormatException e) { }
            System.out.println("Entrada inválida.");
        }
    }
}
