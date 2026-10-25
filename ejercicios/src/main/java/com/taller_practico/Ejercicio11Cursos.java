package com.taller_practico;

import java.util.Scanner;

// Administra cupos de cursos, solicitudes históricas y listas de espera.
public class Ejercicio11Cursos {
    static final int MAX_CURSOS = 8;
    static final int MAX_SOLICITUDES = 80;
    static final String CONFIRMADA = "CONFIRMADA";
    static final String ESPERA = "ESPERA";
    static final String CANCELADA = "CANCELADA";

    static String[] codigosCurso = new String[MAX_CURSOS];
    static String[] nombresCurso = new String[MAX_CURSOS];
    static int[] capacidades = new int[MAX_CURSOS];
    static int cantidadCursos = 0;

    // Arreglos paralelos que conservan incluso las solicitudes canceladas.
    static String[] documentos = new String[MAX_SOLICITUDES];
    static int[] cursosSolicitud = new int[MAX_SOLICITUDES];
    static int[] consecutivos = new int[MAX_SOLICITUDES];
    static String[] estados = new String[MAX_SOLICITUDES];
    static int cantidadSolicitudes = 0;
    static int siguienteConsecutivo = 1;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        int opcion;
        do {
            System.out.println("\n1. Registrar curso");
            System.out.println("2. Inscribir estudiante");
            System.out.println("3. Cancelar inscripción");
            System.out.println("4. Consultar inscritos y lista de espera de un curso");
            System.out.println("5. Consultar cursos de un estudiante");
            System.out.println("6. Generar informe general");
            System.out.println("7. Salir");
            opcion = leerEntero(teclado, "Opción: ", 1, 7);
            switch (opcion) {
                case 1: registrarCurso(teclado); break;
                case 2: inscribirEstudiante(teclado); break;
                case 3: cancelarInscripcion(teclado); break;
                case 4: consultarCurso(teclado); break;
                case 5: consultarEstudiante(teclado); break;
                case 6: generarInforme(); break;
                case 7: System.out.println("Programa finalizado."); break;
                default: break;
            }
        } while (opcion != 7);
        teclado.close();
    }

    // Registra un curso después de validar código, nombre y capacidad.
    static void registrarCurso(Scanner teclado) {
        if (cantidadCursos == MAX_CURSOS) {
            System.out.println("Capacidad de cursos agotada.");
            return;
        }
        System.out.print("Código CUR-AAA-NN: ");
        String codigo = teclado.nextLine();
        if (!validarCodigoCurso(codigo) || buscarCurso(codigo) != -1) {
            System.out.println("Registro rechazado: código inválido o duplicado.");
            return;
        }
        System.out.print("Nombre: ");
        String nombre = teclado.nextLine().trim();
        if (nombre.equals("")) {
            System.out.println("Registro rechazado: nombre vacío.");
            return;
        }
        int capacidad = leerEntero(teclado, "Capacidad (1 a 10): ", 1, 10);
        codigosCurso[cantidadCursos] = codigo;
        nombresCurso[cantidadCursos] = nombre;
        capacidades[cantidadCursos] = capacidad;
        cantidadCursos++;
        System.out.println("Curso registrado.");
    }

    // Valida todos los límites antes de guardar una nueva solicitud histórica.
    static void inscribirEstudiante(Scanner teclado) {
        if (cantidadSolicitudes == MAX_SOLICITUDES) {
            System.out.println("No se admiten más solicitudes: capacidad histórica agotada.");
            return;
        }
        System.out.print("Documento del estudiante: ");
        String documento = teclado.nextLine();
        System.out.print("Código del curso: ");
        int curso = buscarCurso(teclado.nextLine());
        if (!validarDocumento(documento)) {
            System.out.println("Inscripción rechazada: documento inválido.");
        } else if (curso == -1) {
            System.out.println("Inscripción rechazada: curso inexistente.");
        } else if (buscarSolicitudActiva(documento, curso) != -1) {
            System.out.println("Inscripción rechazada: ya existe una solicitud activa para ese curso.");
        } else if (contarSolicitudesActivas(documento) >= 3) {
            System.out.println("Inscripción rechazada: el estudiante ya tiene tres solicitudes activas.");
        } else {
            // El estado se calcula antes de aumentar el contador para no recorrer
            // la posición nueva mientras todavía está vacía.
            String estadoInicial = contarConfirmadas(curso) < capacidades[curso] ? CONFIRMADA : ESPERA;
            int i = cantidadSolicitudes;
            documentos[i] = documento;
            cursosSolicitud[i] = curso;
            consecutivos[i] = siguienteConsecutivo++;
            estados[i] = estadoInicial;
            cantidadSolicitudes++;
            System.out.println("Solicitud " + consecutivos[i] + " registrada en estado " + estados[i] + ".");
        }
    }

    // Cancela una solicitud activa y promueve una espera si liberó un cupo.
    static void cancelarInscripcion(Scanner teclado) {
        int consecutivo = leerEntero(teclado, "Consecutivo a cancelar: ", 1, Integer.MAX_VALUE);
        int solicitud = buscarConsecutivo(consecutivo);
        if (solicitud == -1 || estados[solicitud].equals(CANCELADA)) {
            System.out.println("Cancelación rechazada: solicitud inexistente o ya cancelada.");
            return;
        }
        boolean eraConfirmada = estados[solicitud].equals(CONFIRMADA);
        int curso = cursosSolicitud[solicitud];
        estados[solicitud] = CANCELADA;
        System.out.println("Solicitud cancelada.");
        if (eraConfirmada) promoverSiguiente(curso);
    }

    // Busca el menor consecutivo en espera para el curso y lo promueve.
    static void promoverSiguiente(int curso) {
        int elegida = -1;
        for (int i = 0; i < cantidadSolicitudes; i++) {
            if (cursosSolicitud[i] == curso && estados[i].equals(ESPERA)
                    && (elegida == -1 || consecutivos[i] < consecutivos[elegida])) elegida = i;
        }
        if (elegida != -1) {
            estados[elegida] = CONFIRMADA;
            System.out.println("Promoción automática: solicitud " + consecutivos[elegida] + " ahora está CONFIRMADA.");
        }
    }

    // Presenta capacidad, confirmados, cupos y espera del curso solicitado.
    static void consultarCurso(Scanner teclado) {
        System.out.print("Código del curso: ");
        int curso = buscarCurso(teclado.nextLine());
        if (curso == -1) {
            System.out.println("Curso inexistente.");
            return;
        }
        int confirmadas = contarConfirmadas(curso);
        System.out.println(codigosCurso[curso] + " - " + nombresCurso[curso] + " | capacidad " + capacidades[curso]
                + " | confirmados " + confirmadas + " | disponibles " + (capacidades[curso] - confirmadas));
        System.out.println("Inscritos confirmados:");
        mostrarSolicitudesCurso(curso, CONFIRMADA);
        System.out.println("Lista de espera por consecutivo:");
        mostrarSolicitudesCurso(curso, ESPERA);
    }

    // Las solicitudes se almacenan con consecutivos crecientes; recorrerlas mantiene el orden.
    static void mostrarSolicitudesCurso(int curso, String estado) {
        boolean hay = false;
        for (int i = 0; i < cantidadSolicitudes; i++) {
            if (cursosSolicitud[i] == curso && estados[i].equals(estado)) {
                System.out.println(consecutivos[i] + " - " + documentos[i]);
                hay = true;
            }
        }
        if (!hay) System.out.println("Ninguno.");
    }

    // Muestra el historial de solicitudes asociado al documento.
    static void consultarEstudiante(Scanner teclado) {
        System.out.print("Documento: ");
        String documento = teclado.nextLine();
        if (!validarDocumento(documento)) {
            System.out.println("Documento inválido.");
            return;
        }
        boolean hay = false;
        for (int i = 0; i < cantidadSolicitudes; i++) {
            if (documentos[i].equals(documento)) {
                System.out.println(consecutivos[i] + " | " + codigosCurso[cursosSolicitud[i]] + " | " + estados[i]);
                hay = true;
            }
        }
        if (!hay) System.out.println("El estudiante no tiene solicitudes.");
    }

    // Calcula ocupación, estados y cursos con mayor lista de espera.
    static void generarInforme() {
        int confirmadas = 0, espera = 0, canceladas = 0, mayorEspera = 0;
        for (int i = 0; i < cantidadSolicitudes; i++) {
            if (estados[i].equals(CONFIRMADA)) confirmadas++;
            else if (estados[i].equals(ESPERA)) espera++;
            else canceladas++;
        }
        for (int curso = 0; curso < cantidadCursos; curso++) {
            int ocupados = contarConfirmadas(curso);
            int enEspera = contarEspera(curso);
            System.out.printf("%s: %.2f%% de ocupación%n", codigosCurso[curso], ocupados * 100.0 / capacidades[curso]);
            if (enEspera > mayorEspera) mayorEspera = enEspera;
        }
        System.out.println("Solicitudes CONFIRMADAS: " + confirmadas + " | ESPERA: " + espera + " | CANCELADAS: " + canceladas);
        if (mayorEspera == 0) {
            System.out.println("No hay cursos con estudiantes en espera.");
        } else {
            System.out.println("Cursos con mayor lista de espera (" + mayorEspera + "):");
            for (int i = 0; i < cantidadCursos; i++) if (contarEspera(i) == mayorEspera) System.out.println(codigosCurso[i]);
        }
    }

    // Comprueba el formato CUR-AAA-NN con letras mayúsculas.
    static boolean validarCodigoCurso(String codigo) {
        if (codigo.length() != 10 || !codigo.substring(0, 4).equals("CUR-") || codigo.charAt(7) != '-') return false;
        for (int i = 4; i < 7; i++) if (codigo.charAt(i) < 'A' || codigo.charAt(i) > 'Z') return false;
        return esDigito(codigo.charAt(8)) && esDigito(codigo.charAt(9));
    }

    // Comprueba que el documento contenga entre 6 y 12 dígitos.
    static boolean validarDocumento(String documento) {
        if (documento.length() < 6 || documento.length() > 12) return false;
        for (int i = 0; i < documento.length(); i++) if (!esDigito(documento.charAt(i))) return false;
        return true;
    }

    // Devuelve la posición del curso o -1 cuando no existe.
    static int buscarCurso(String codigo) {
        for (int i = 0; i < cantidadCursos; i++) if (codigosCurso[i].equals(codigo)) return i;
        return -1;
    }

    // Busca una confirmación o espera activa del estudiante en el curso.
    static int buscarSolicitudActiva(String documento, int curso) {
        for (int i = 0; i < cantidadSolicitudes; i++) {
            if (documentos[i].equals(documento) && cursosSolicitud[i] == curso && !estados[i].equals(CANCELADA)) return i;
        }
        return -1;
    }

    // Cuenta solicitudes no canceladas para aplicar el máximo de tres.
    static int contarSolicitudesActivas(String documento) {
        int cantidad = 0;
        for (int i = 0; i < cantidadSolicitudes; i++) if (documentos[i].equals(documento) && !estados[i].equals(CANCELADA)) cantidad++;
        return cantidad;
    }

    // Devuelve la cantidad de cupos ocupados en un curso.
    static int contarConfirmadas(int curso) {
        int cantidad = 0;
        for (int i = 0; i < cantidadSolicitudes; i++) if (cursosSolicitud[i] == curso && estados[i].equals(CONFIRMADA)) cantidad++;
        return cantidad;
    }

    // Devuelve la cantidad de solicitudes en espera de un curso.
    static int contarEspera(int curso) {
        int cantidad = 0;
        for (int i = 0; i < cantidadSolicitudes; i++) if (cursosSolicitud[i] == curso && estados[i].equals(ESPERA)) cantidad++;
        return cantidad;
    }

    // Busca el consecutivo histórico y devuelve su posición o -1.
    static int buscarConsecutivo(int consecutivo) {
        for (int i = 0; i < cantidadSolicitudes; i++) if (consecutivos[i] == consecutivo) return i;
        return -1;
    }

    // Indica si el carácter recibido es un dígito.
    static boolean esDigito(char c) { return c >= '0' && c <= '9'; }

    // Solicita un entero hasta recibir un valor entre los límites.
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
