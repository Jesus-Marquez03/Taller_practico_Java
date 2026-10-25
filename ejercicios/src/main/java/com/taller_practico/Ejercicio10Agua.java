package com.taller_practico;

import java.util.Scanner;

// Registra lecturas de agua y calcula facturas progresivas por zonas.
public class Ejercicio10Agua {
    static final int MAX_SUSCRIPTORES = 40;
    static final int CARGO_FIJO = 12000;
    static final int TARIFA_1 = 1800;
    static final int TARIFA_2 = 2600;
    static final int TARIFA_3 = 3800;

    static String[] codigos = new String[MAX_SUSCRIPTORES];
    static String[] nombres = new String[MAX_SUSCRIPTORES];
    static int[] lecturasAnteriores = new int[MAX_SUSCRIPTORES];
    static int[] consumosPrevios = new int[MAX_SUSCRIPTORES];
    static int[] lecturasActuales = new int[MAX_SUSCRIPTORES];
    static boolean[] tieneLectura = new boolean[MAX_SUSCRIPTORES];
    static int cantidadSuscriptores = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        int opcion;
        do {
            System.out.println("\n1. Registrar suscriptor");
            System.out.println("2. Registrar lectura actual");
            System.out.println("3. Consultar factura");
            System.out.println("4. Listar lecturas pendientes");
            System.out.println("5. Generar informe del período");
            System.out.println("6. Salir");
            opcion = leerEntero(teclado, "Opción: ", 1, 6);
            switch (opcion) {
                case 1: registrarSuscriptor(teclado); break;
                case 2: registrarLectura(teclado); break;
                case 3: consultarFactura(teclado); break;
                case 4: listarPendientes(); break;
                case 5: generarInforme(); break;
                case 6: System.out.println("Programa finalizado."); break;
                default: break;
            }
        } while (opcion != 6);
        teclado.close();
    }

    // Valida todos los datos antes de ocupar una posición de suscriptor.
    static void registrarSuscriptor(Scanner teclado) {
        if (cantidadSuscriptores == MAX_SUSCRIPTORES) {
            System.out.println("Capacidad de suscriptores agotada.");
            return;
        }
        System.out.print("Código ACU-ZZ-NNN: ");
        String codigo = teclado.nextLine();
        if (!validarCodigo(codigo) || buscarSuscriptor(codigo) != -1) {
            System.out.println("Registro rechazado: código inválido o duplicado.");
            return;
        }
        System.out.print("Nombre: ");
        String nombre = teclado.nextLine().trim();
        if (nombre.equals("")) {
            System.out.println("Registro rechazado: nombre vacío.");
            return;
        }
        int anterior = leerEntero(teclado, "Lectura anterior (0 a 99999): ", 0, 99999);
        int previo = leerEntero(teclado, "Consumo del período previo: ", 0, 99999);
        int i = cantidadSuscriptores++;
        codigos[i] = codigo; nombres[i] = nombre;
        lecturasAnteriores[i] = anterior; consumosPrevios[i] = previo;
        System.out.println("Suscriptor registrado.");
    }

    // La lectura solo se almacena cuando pasa todas las reglas y, si aplica, se confirma.
    static void registrarLectura(Scanner teclado) {
        System.out.print("Código del suscriptor: ");
        int suscriptor = buscarSuscriptor(teclado.nextLine());
        if (suscriptor == -1) {
            System.out.println("Lectura rechazada: suscriptor inexistente.");
            return;
        }
        if (tieneLectura[suscriptor]) {
            System.out.println("Lectura rechazada: ya existe una lectura actual aceptada.");
            return;
        }
        int actual = leerEntero(teclado, "Lectura actual (0 a 99999): ", 0, 99999);
        if (actual < lecturasAnteriores[suscriptor]) {
            System.out.println("Lectura rechazada: es menor que la lectura anterior.");
            return;
        }
        int consumo = actual - lecturasAnteriores[suscriptor];
        boolean requiereConfirmacion = consumosPrevios[suscriptor] == 0
                ? consumo > 20 : consumo > consumosPrevios[suscriptor] * 2;
        if (requiereConfirmacion && !confirmarLectura(teclado)) {
            System.out.println("Lectura no registrada por decisión del usuario.");
            return;
        }
        lecturasActuales[suscriptor] = actual;
        tieneLectura[suscriptor] = true;
        System.out.println("Lectura registrada. Consumo: " + consumo + " m3.");
    }

    // Solicita S o N cuantas veces sea necesario y no distingue mayúsculas.
    static boolean confirmarLectura(Scanner teclado) {
        while (true) {
            System.out.print("El consumo requiere confirmación. ¿Aceptar? (S/N): ");
            String respuesta = teclado.nextLine();
            if (respuesta.equalsIgnoreCase("S")) return true;
            if (respuesta.equalsIgnoreCase("N")) return false;
            System.out.println("Respuesta inválida.");
        }
    }

    // Muestra una factura solo cuando el suscriptor tiene lectura aceptada.
    static void consultarFactura(Scanner teclado) {
        System.out.print("Código del suscriptor: ");
        int suscriptor = buscarSuscriptor(teclado.nextLine());
        if (suscriptor == -1 || !tieneLectura[suscriptor]) {
            System.out.println("No existe una factura disponible para ese código.");
            return;
        }
        mostrarFactura(suscriptor);
    }

    // Calcula cada tramo de manera independiente para no cobrar todo a una sola tarifa.
    static int calcularTramo1(int consumo) { return Math.min(consumo, 15) * TARIFA_1; }
    // Devuelve el cobro de los siguientes 15 m3, sin incluir los primeros 15.
    static int calcularTramo2(int consumo) { return Math.min(Math.max(consumo - 15, 0), 15) * TARIFA_2; }
    // Devuelve el cobro del consumo que supera los 30 m3.
    static int calcularTramo3(int consumo) { return Math.max(consumo - 30, 0) * TARIFA_3; }

    // Suma el cargo fijo y los tres tramos del suscriptor indicado.
    static int calcularTotal(int suscriptor) {
        int consumo = lecturasActuales[suscriptor] - lecturasAnteriores[suscriptor];
        return CARGO_FIJO + calcularTramo1(consumo) + calcularTramo2(consumo) + calcularTramo3(consumo);
    }

    // Detalla consumo, valores por tramo, cargo fijo y total.
    static void mostrarFactura(int suscriptor) {
        int consumo = lecturasActuales[suscriptor] - lecturasAnteriores[suscriptor];
        System.out.println(codigos[suscriptor] + " - " + nombres[suscriptor]);
        System.out.println("Consumo: " + consumo + " m3");
        System.out.println("Primer tramo: $" + calcularTramo1(consumo));
        System.out.println("Segundo tramo: $" + calcularTramo2(consumo));
        System.out.println("Tercer tramo: $" + calcularTramo3(consumo));
        System.out.println("Cargo fijo: $" + CARGO_FIJO);
        System.out.println("TOTAL: $" + calcularTotal(suscriptor));
    }

    // Recorre suscriptores y muestra los que todavía no tienen lectura actual.
    static void listarPendientes() {
        boolean hay = false;
        for (int i = 0; i < cantidadSuscriptores; i++) {
            if (!tieneLectura[i]) {
                System.out.println(codigos[i] + " - " + nombres[i]);
                hay = true;
            }
        }
        if (!hay) System.out.println("No hay lecturas pendientes.");
    }

    // Acumula solo facturas válidas y busca mayores consumidores por zona.
    static void generarInforme() {
        int[] consumosZona = new int[5];
        long[] facturacionZona = new long[5];
        int pendientes = 0;
        for (int i = 0; i < cantidadSuscriptores; i++) {
            if (!tieneLectura[i]) {
                pendientes++;
            } else {
                int zona = obtenerZona(codigos[i]) - 1;
                consumosZona[zona] += lecturasActuales[i] - lecturasAnteriores[i];
                facturacionZona[zona] += calcularTotal(i);
            }
        }
        for (int zona = 0; zona < 5; zona++) {
            System.out.println("Zona 0" + (zona + 1) + ": consumo " + consumosZona[zona]
                    + " m3, facturación $" + facturacionZona[zona]);
            int mayor = -1;
            for (int i = 0; i < cantidadSuscriptores; i++) {
                if (tieneLectura[i] && obtenerZona(codigos[i]) - 1 == zona) {
                    int consumo = lecturasActuales[i] - lecturasAnteriores[i];
                    if (consumo > mayor) mayor = consumo;
                }
            }
            if (mayor == -1) {
                System.out.println("  Sin facturas en esta zona.");
            } else {
                System.out.println("  Mayor consumidor(es), " + mayor + " m3:");
                for (int i = 0; i < cantidadSuscriptores; i++) {
                    if (tieneLectura[i] && obtenerZona(codigos[i]) - 1 == zona
                            && lecturasActuales[i] - lecturasAnteriores[i] == mayor) System.out.println("  " + codigos[i]);
                }
            }
        }
        System.out.println("Suscriptores pendientes: " + pendientes);
    }

    // Comprueba ACU-ZZ-NNN y que la zona esté entre 01 y 05.
    static boolean validarCodigo(String codigo) {
        if (codigo.length() != 10 || !codigo.substring(0, 4).equals("ACU-") || codigo.charAt(6) != '-') return false;
        for (int i = 4; i < 6; i++) if (!esDigito(codigo.charAt(i))) return false;
        for (int i = 7; i < 10; i++) if (!esDigito(codigo.charAt(i))) return false;
        int zona = obtenerZona(codigo);
        return zona >= 1 && zona <= 5;
    }

    // Extrae ZZ usando substring(), como exige el taller.
    static int obtenerZona(String codigo) { return Integer.parseInt(codigo.substring(4, 6)); }

    // Devuelve la posición del suscriptor o -1 cuando no existe.
    static int buscarSuscriptor(String codigo) {
        for (int i = 0; i < cantidadSuscriptores; i++) if (codigos[i].equals(codigo)) return i;
        return -1;
    }

    // Indica si el carácter recibido es un dígito.
    static boolean esDigito(char c) { return c >= '0' && c <= '9'; }

    // Solicita un entero dentro del intervalo y controla letras y desbordamientos.
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
}
