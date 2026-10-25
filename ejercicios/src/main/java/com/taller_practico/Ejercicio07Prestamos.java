package com.taller_practico;

import java.util.Scanner;

// Administra equipos tecnológicos, préstamos activos, devoluciones e historial.
public class Ejercicio07Prestamos {
    static final int MAX_EQUIPOS = 30;
    static final int MAX_PRESTAMOS = 100;
    static final int MULTA_DIARIA = 8000;
    static final double LIMITE_MULTA = 0.30;

    static String[] codigosEquipo = new String[MAX_EQUIPOS];
    static String[] nombresEquipo = new String[MAX_EQUIPOS];
    static long[] valoresEquipo = new long[MAX_EQUIPOS];
    static int[] ultimaDevolucion = new int[MAX_EQUIPOS];
    static int cantidadEquipos = 0;

    // Estos arreglos paralelos conservan préstamos activos y ya devueltos.
    static String[] documentos = new String[MAX_PRESTAMOS];
    static int[] equipoPrestamo = new int[MAX_PRESTAMOS];
    static int[] diaPrestamo = new int[MAX_PRESTAMOS];
    static int[] diaPrevisto = new int[MAX_PRESTAMOS];
    static int[] diaReal = new int[MAX_PRESTAMOS];
    static boolean[] activo = new boolean[MAX_PRESTAMOS];
    static long[] multas = new long[MAX_PRESTAMOS];
    static int cantidadPrestamos = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero(teclado, "Opción: ", 1, 7);
            switch (opcion) {
                case 1: registrarEquipo(teclado); break;
                case 2: registrarPrestamo(teclado); break;
                case 3: registrarDevolucion(teclado); break;
                case 4: consultarActivos(); break;
                case 5: consultarHistorial(teclado); break;
                case 6: generarInforme(teclado); break;
                case 7: System.out.println("Programa finalizado."); break;
                default: break;
            }
        } while (opcion != 7);
        teclado.close();
    }

    // Presenta exactamente las siete operaciones disponibles.
    static void mostrarMenu() {
        System.out.println("\n1. Registrar equipo");
        System.out.println("2. Registrar préstamo");
        System.out.println("3. Registrar devolución");
        System.out.println("4. Consultar préstamos activos");
        System.out.println("5. Consultar historial de un usuario");
        System.out.println("6. Generar informe del laboratorio");
        System.out.println("7. Salir");
    }

    // Lee y valida por completo un equipo antes de ocupar una posición del arreglo.
    static void registrarEquipo(Scanner teclado) {
        if (cantidadEquipos == MAX_EQUIPOS) {
            System.out.println("No hay espacio para más equipos.");
            return;
        }
        System.out.print("Código (COM-NNN, TAB-NNN o PRO-NNN): ");
        String codigo = teclado.nextLine();
        if (!validarCodigoEquipo(codigo) || buscarEquipo(codigo) != -1) {
            System.out.println("Registro rechazado: código inválido o duplicado.");
            return;
        }
        System.out.print("Nombre: ");
        String nombre = teclado.nextLine().trim();
        if (nombre.length() < 3 || nombre.length() > 40) {
            System.out.println("Registro rechazado: el nombre debe tener entre 3 y 40 caracteres.");
            return;
        }
        long valor = leerLong(teclado, "Valor de reposición: ", 1, Long.MAX_VALUE);
        codigosEquipo[cantidadEquipos] = codigo;
        nombresEquipo[cantidadEquipos] = nombre;
        valoresEquipo[cantidadEquipos] = valor;
        cantidadEquipos++;
        System.out.println("Equipo registrado.");
    }

    // Comprueba capacidad, usuario, equipo y fechas antes de crear el préstamo.
    static void registrarPrestamo(Scanner teclado) {
        if (cantidadPrestamos == MAX_PRESTAMOS) {
            System.out.println("No hay espacio para más préstamos históricos.");
            return;
        }
        System.out.print("Documento del usuario: ");
        String documento = teclado.nextLine();
        System.out.print("Código del equipo: ");
        String codigo = teclado.nextLine();
        int dia = leerEntero(teclado, "Día del préstamo (1 a 365): ", 1, 365);
        int duracion = leerEntero(teclado, "Duración autorizada (1 a 7 días): ", 1, 7);

        int equipo = buscarEquipo(codigo);
        if (!validarDocumento(documento)) {
            System.out.println("Préstamo rechazado: documento inválido.");
        } else if (equipo == -1) {
            System.out.println("Préstamo rechazado: equipo inexistente.");
        } else if (!comprobarDisponibilidad(equipo)) {
            System.out.println("Préstamo rechazado: equipo ocupado.");
        } else if (contarActivosUsuario(documento) >= 2) {
            System.out.println("Préstamo rechazado: el usuario ya tiene dos préstamos activos.");
        } else if (dia + duracion > 365) {
            System.out.println("Préstamo rechazado: la fecha prevista supera el día 365.");
        } else if (dia < ultimaDevolucion[equipo]) {
            System.out.println("Préstamo rechazado: fecha anterior a la última devolución del equipo.");
        } else {
            int i = cantidadPrestamos++;
            documentos[i] = documento;
            equipoPrestamo[i] = equipo;
            diaPrestamo[i] = dia;
            diaPrevisto[i] = dia + duracion;
            activo[i] = true;
            System.out.println("Préstamo registrado. Devolución prevista: día " + diaPrevisto[i] + ".");
        }
    }

    // Cierra un préstamo activo después de validar el día y calcular la multa.
    static void registrarDevolucion(Scanner teclado) {
        System.out.print("Código del equipo: ");
        String codigo = teclado.nextLine();
        int equipo = buscarEquipo(codigo);
        int prestamo = equipo == -1 ? -1 : buscarPrestamoActivo(equipo);
        if (prestamo == -1) {
            System.out.println("Devolución rechazada: no existe un préstamo activo para ese equipo.");
            return;
        }
        int dia = leerEntero(teclado, "Día real de devolución (1 a 365): ", 1, 365);
        if (dia < diaPrestamo[prestamo]) {
            System.out.println("Devolución rechazada: no puede ser anterior al préstamo.");
            return;
        }
        long multa = calcularMulta(prestamo, dia);
        diaReal[prestamo] = dia;
        multas[prestamo] = multa;
        activo[prestamo] = false;
        ultimaDevolucion[equipo] = dia;
        System.out.println("Devolución registrada. Multa: $" + multa + ".");
    }

    // Calcula días de retraso y limita el cobro al 30 % del valor de reposición.
    static long calcularMulta(int prestamo, int devolucion) {
        int retraso = devolucion - diaPrevisto[prestamo];
        if (retraso <= 0) return 0;
        long calculada = (long) retraso * MULTA_DIARIA;
        long limite = Math.round(valoresEquipo[equipoPrestamo[prestamo]] * LIMITE_MULTA);
        return calculada < limite ? calculada : limite;
    }

    // Recorre el historial y muestra solamente los préstamos que siguen activos.
    static void consultarActivos() {
        System.out.println("\nPRÉSTAMOS ACTIVOS");
        boolean hay = false;
        for (int i = 0; i < cantidadPrestamos; i++) {
            if (activo[i]) {
                mostrarPrestamo(i);
                hay = true;
            }
        }
        if (!hay) System.out.println("No hay préstamos activos.");
    }

    // Muestra préstamos activos y devueltos del documento solicitado.
    static void consultarHistorial(Scanner teclado) {
        System.out.print("Documento del usuario: ");
        String documento = teclado.nextLine();
        if (!validarDocumento(documento)) {
            System.out.println("Documento inválido.");
            return;
        }
        boolean hay = false;
        for (int i = 0; i < cantidadPrestamos; i++) {
            if (documentos[i].equals(documento)) {
                mostrarPrestamo(i);
                hay = true;
            }
        }
        if (!hay) System.out.println("El usuario no tiene historial.");
    }

    // Resume disponibilidad, vencimientos, multas y equipos más prestados.
    static void generarInforme(Scanner teclado) {
        int diaConsulta = leerEntero(teclado, "Día de consulta (1 a 365): ", 1, 365);
        int disponibles = 0, activos = 0, vencidos = 0;
        long multasAcumuladas = 0;
        int[] prestamosPorEquipo = new int[MAX_EQUIPOS];
        for (int i = 0; i < cantidadEquipos; i++) if (comprobarDisponibilidad(i)) disponibles++;
        for (int i = 0; i < cantidadPrestamos; i++) {
            prestamosPorEquipo[equipoPrestamo[i]]++;
            if (activo[i]) {
                activos++;
                if (diaPrevisto[i] < diaConsulta) vencidos++;
            }
            multasAcumuladas += multas[i];
        }
        System.out.println("Equipos disponibles: " + disponibles);
        System.out.println("Préstamos activos: " + activos);
        System.out.println("Préstamos vencidos al día " + diaConsulta + ": " + vencidos);
        System.out.println("Multas acumuladas: $" + multasAcumuladas);
        int mayor = 0;
        for (int i = 0; i < cantidadEquipos; i++) if (prestamosPorEquipo[i] > mayor) mayor = prestamosPorEquipo[i];
        if (mayor == 0) {
            System.out.println("Aún no se han registrado préstamos.");
        } else {
            System.out.println("Equipos con mayor cantidad de préstamos (" + mayor + "):");
            for (int i = 0; i < cantidadEquipos; i++) if (prestamosPorEquipo[i] == mayor) System.out.println(codigosEquipo[i]);
        }
    }

    // Presenta los datos del préstamo ubicado en la posición recibida.
    static void mostrarPrestamo(int i) {
        System.out.println(documentos[i] + " | " + codigosEquipo[equipoPrestamo[i]] + " | día " + diaPrestamo[i]
                + " | previsto " + diaPrevisto[i] + " | " + (activo[i] ? "ACTIVO" : "DEVUELTO día " + diaReal[i] + ", multa $" + multas[i]));
    }

    // Acepta los tipos COM, TAB y PRO seguidos por tres dígitos.
    static boolean validarCodigoEquipo(String codigo) {
        if (codigo.length() != 7 || codigo.charAt(3) != '-') return false;
        String tipo = codigo.substring(0, 3);
        if (!tipo.equals("COM") && !tipo.equals("TAB") && !tipo.equals("PRO")) return false;
        for (int i = 4; i < 7; i++) if (!esDigito(codigo.charAt(i))) return false;
        return true;
    }

    // Comprueba que el documento tenga de 6 a 12 dígitos.
    static boolean validarDocumento(String documento) {
        if (documento.length() < 6 || documento.length() > 12) return false;
        for (int i = 0; i < documento.length(); i++) if (!esDigito(documento.charAt(i))) return false;
        return true;
    }

    // Devuelve la posición del equipo o -1 cuando no está registrado.
    static int buscarEquipo(String codigo) {
        for (int i = 0; i < cantidadEquipos; i++) if (codigosEquipo[i].equals(codigo)) return i;
        return -1;
    }

    // Un equipo está disponible cuando no aparece en ningún préstamo activo.
    static boolean comprobarDisponibilidad(int equipo) {
        return buscarPrestamoActivo(equipo) == -1;
    }

    // Busca el préstamo activo del equipo y devuelve su posición o -1.
    static int buscarPrestamoActivo(int equipo) {
        for (int i = 0; i < cantidadPrestamos; i++) if (activo[i] && equipoPrestamo[i] == equipo) return i;
        return -1;
    }

    // Cuenta los préstamos activos del usuario para aplicar el límite de dos.
    static int contarActivosUsuario(String documento) {
        int cantidad = 0;
        for (int i = 0; i < cantidadPrestamos; i++) if (activo[i] && documentos[i].equals(documento)) cantidad++;
        return cantidad;
    }

    // Indica si un carácter representa un dígito decimal.
    static boolean esDigito(char c) { return c >= '0' && c <= '9'; }

    // Lee y valida un entero dentro del intervalo recibido.
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

    // Lee valores monetarios long controlando conversión y límites.
    static long leerLong(Scanner teclado, String mensaje, long minimo, long maximo) {
        while (true) {
            System.out.print(mensaje);
            try {
                long valor = Long.parseLong(teclado.nextLine());
                if (valor >= minimo && valor <= maximo) return valor;
            } catch (NumberFormatException e) { }
            System.out.println("Entrada inválida.");
        }
    }
}
