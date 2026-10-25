package com.taller_practico;

import java.util.Scanner;

// Procesa reservas y cancelaciones sobre una fila de 24 asientos.
public class Ejercicio04Asientos {
    static final int TOTAL_ASIENTOS = 24;
    static final int MAX_SOLICITUDES = 50;
    // Una posición nula representa un asiento libre; las demás guardan el grupo ocupante.
    static String[] ocupantes = new String[TOTAL_ASIENTOS];
    static int rechazosContinuidad = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        int cantidad = leerEntero(teclado, "Cantidad de solicitudes (1 a 50): ", 1, MAX_SOLICITUDES);
        for (int i = 1; i <= cantidad; i++) {
            System.out.println("Solicitud " + i + ":");
            procesarSolicitud(teclado.nextLine());
        }
        mostrarEstadoFinal();
        teclado.close();
    }

    // Identifica la operación, valida todos sus campos y luego reserva o cancela.
    static void procesarSolicitud(String solicitud) {
        int barra1 = solicitud.indexOf('|');
        if (barra1 == -1) {
            System.out.println("RECHAZADA: estructura inválida.");
            return;
        }
        String operacion = solicitud.substring(0, barra1);
        if (operacion.equals("CAN")) {
            if (solicitud.indexOf('|', barra1 + 1) != -1) {
                System.out.println("RECHAZADA: estructura inválida.");
                return;
            }
            String grupo = solicitud.substring(barra1 + 1);
            if (!validarGrupo(grupo)) {
                System.out.println("RECHAZADA: código de grupo inválido.");
            } else {
                cancelar(grupo);
            }
        } else if (operacion.equals("RES")) {
            int barra2 = solicitud.indexOf('|', barra1 + 1);
            if (barra2 == -1 || solicitud.indexOf('|', barra2 + 1) != -1) {
                System.out.println("RECHAZADA: estructura inválida.");
                return;
            }
            String grupo = solicitud.substring(barra1 + 1, barra2);
            int cantidad;
            try {
                cantidad = Integer.parseInt(solicitud.substring(barra2 + 1));
            } catch (NumberFormatException e) {
                System.out.println("RECHAZADA: cantidad no numérica.");
                return;
            }
            if (!validarGrupo(grupo) || cantidad < 1 || cantidad > 6) {
                System.out.println("RECHAZADA: grupo o cantidad inválida.");
            } else if (buscarGrupo(grupo) != -1) {
                System.out.println("RECHAZADA: el grupo ya tiene una reserva activa.");
            } else {
                reservar(grupo, cantidad);
            }
        } else {
            System.out.println("RECHAZADA: operación desconocida.");
        }
    }

    // Comprueba la estructura GRP-NNN del código recibido.
    static boolean validarGrupo(String grupo) {
        if (grupo.length() != 7 || !grupo.substring(0, 4).equals("GRP-")) return false;
        for (int i = 4; i < 7; i++) if (grupo.charAt(i) < '0' || grupo.charAt(i) > '9') return false;
        return true;
    }

    // Busca un asiento ocupado por el grupo o devuelve -1 si no tiene reserva.
    static int buscarGrupo(String grupo) {
        for (int i = 0; i < TOTAL_ASIENTOS; i++) if (grupo.equals(ocupantes[i])) return i;
        return -1;
    }

    // Recorre todos los bloques libres maximales y elige el menor que alcance.
    static int localizarMejorBloque(int solicitados) {
        int mejorInicio = -1;
        int mejorTamano = TOTAL_ASIENTOS + 1;
        int i = 0;
        while (i < TOTAL_ASIENTOS) {
            if (ocupantes[i] != null) {
                i++;
            } else {
                int inicio = i;
                while (i < TOTAL_ASIENTOS && ocupantes[i] == null) i++;
                int tamano = i - inicio;
                if (tamano >= solicitados && tamano < mejorTamano) {
                    mejorInicio = inicio;
                    mejorTamano = tamano;
                }
                // Al recorrer de izquierda a derecha, el primer empate ya tiene menor asiento.
            }
        }
        return mejorInicio;
    }

    // Asigna los primeros asientos del mejor bloque si existe espacio contiguo.
    static void reservar(String grupo, int cantidad) {
        int inicio = localizarMejorBloque(cantidad);
        if (inicio == -1) {
            rechazosContinuidad++;
            System.out.println("RECHAZADA: no existe un bloque libre contiguo suficiente.");
            return;
        }
        for (int i = inicio; i < inicio + cantidad; i++) ocupantes[i] = grupo;
        System.out.println("ACEPTADA: " + grupo + " recibe los asientos " + (inicio + 1) + " a " + (inicio + cantidad) + ".");
    }

    // La cancelación libera todos los asientos del grupo, sin alterar los demás.
    static void cancelar(String grupo) {
        if (buscarGrupo(grupo) == -1) {
            System.out.println("RECHAZADA: el grupo no tiene reserva activa.");
            return;
        }
        for (int i = 0; i < TOTAL_ASIENTOS; i++) if (grupo.equals(ocupantes[i])) ocupantes[i] = null;
        System.out.println("CANCELACIÓN ACEPTADA para " + grupo + ".");
    }

    // Presenta ocupación, ocupantes y todos los mayores bloques libres empatados.
    static void mostrarEstadoFinal() {
        int ocupados = 0;
        System.out.println("\nESTADO FINAL");
        for (int i = 0; i < TOTAL_ASIENTOS; i++) {
            System.out.println("Asiento " + (i + 1) + ": " + (ocupantes[i] == null ? "LIBRE" : ocupantes[i]));
            if (ocupantes[i] != null) ocupados++;
        }
        System.out.printf("Ocupación: %.2f%%%n", ocupados * 100.0 / TOTAL_ASIENTOS);

        int mayor = 0;
        int i = 0;
        while (i < TOTAL_ASIENTOS) {
            if (ocupantes[i] != null) i++;
            else {
                int inicio = i;
                while (i < TOTAL_ASIENTOS && ocupantes[i] == null) i++;
                if (i - inicio > mayor) mayor = i - inicio;
            }
        }
        if (mayor == 0) {
            System.out.println("No existen bloques libres.");
        } else {
            System.out.println("Mayor bloque libre (longitud " + mayor + "):");
            i = 0;
            while (i < TOTAL_ASIENTOS) {
                if (ocupantes[i] != null) i++;
                else {
                    int inicio = i;
                    while (i < TOTAL_ASIENTOS && ocupantes[i] == null) i++;
                    if (i - inicio == mayor) System.out.println("Asientos " + (inicio + 1) + " a " + i);
                }
            }
        }
        System.out.println("Reservas rechazadas por falta de continuidad: " + rechazosContinuidad);
    }

    // Solicita un entero válido comprendido entre los límites recibidos.
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
