package com.taller_practico;

import java.util.Scanner;

// Concilia entradas y salidas de inventario sin aplicar movimientos inválidos.
public class Ejercicio05Inventario {
    static final int MAX_PRODUCTOS = 25;
    static final int MAX_MOVIMIENTOS = 60;
    static String[] codigos = new String[MAX_PRODUCTOS];
    static String[] nombres = new String[MAX_PRODUCTOS];
    static int[] iniciales = new int[MAX_PRODUCTOS];
    static int[] minimos = new int[MAX_PRODUCTOS];
    static int[] existencias = new int[MAX_PRODUCTOS];
    static int[] entradas = new int[MAX_PRODUCTOS];
    static int[] salidas = new int[MAX_PRODUCTOS];
    static int cantidadProductos = 0;
    static String[] movimientosAceptados = new String[MAX_MOVIMIENTOS];
    static int cantidadAceptados = 0;
    static String[] rechazos = new String[MAX_MOVIMIENTOS];
    static int cantidadRechazos = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        int cantidad = leerEntero(teclado, "Cantidad de productos iniciales (1 a 25): ", 1, MAX_PRODUCTOS);
        for (int i = 1; i <= cantidad; i++) {
            System.out.println("Producto " + i + " (PRD-NNN|Nombre|existencias|mínimo):");
            String error = registrarProducto(teclado.nextLine());
            if (!error.equals("")) {
                System.out.println("Producto rechazado: " + error + ". Ingréselo nuevamente.");
                i--;
            }
        }
        int cantidadMovimientos = leerEntero(teclado, "Cantidad de movimientos (0 a 60): ", 0, MAX_MOVIMIENTOS);
        for (int i = 1; i <= cantidadMovimientos; i++) {
            System.out.println("Movimiento " + i + ":");
            procesarMovimiento(teclado.nextLine());
        }
        mostrarConciliacion();
        teclado.close();
    }

    // Registra un producto solamente cuando sus cuatro campos son válidos.
    static String registrarProducto(String registro) {
        int p1 = registro.indexOf('|');
        int p2 = p1 == -1 ? -1 : registro.indexOf('|', p1 + 1);
        int p3 = p2 == -1 ? -1 : registro.indexOf('|', p2 + 1);
        if (p1 <= 0 || p2 <= p1 + 1 || p3 <= p2 + 1 || p3 == registro.length() - 1
                || registro.indexOf('|', p3 + 1) != -1) return "estructura inválida";
        String codigo = registro.substring(0, p1);
        String nombre = registro.substring(p1 + 1, p2).trim();
        if (!validarCodigoProducto(codigo) || nombre.equals("")) return "código o nombre inválido";
        if (buscarProducto(codigo) != -1) return "producto duplicado";
        int existencia, minimo;
        try {
            existencia = Integer.parseInt(registro.substring(p2 + 1, p3));
            minimo = Integer.parseInt(registro.substring(p3 + 1));
        } catch (NumberFormatException e) {
            return "existencias o mínimo no numérico";
        }
        if (existencia < 0 || minimo < 0) return "existencias y mínimo deben ser no negativos";
        int i = cantidadProductos++;
        codigos[i] = codigo; nombres[i] = nombre; iniciales[i] = existencia;
        existencias[i] = existencia; minimos[i] = minimo;
        return "";
    }

    // Valida con la prioridad exigida y solo después aplica el movimiento.
    static void procesarMovimiento(String registro) {
        String[] datos = new String[4];
        String errorEstructura = validarMovimiento(registro, datos);
        if (!errorEstructura.equals("")) {
            rechazar(registro, "estructura inválida");
            return;
        }
        if (buscarMovimientoAceptado(datos[0]) != -1) {
            rechazar(registro, "identificador ya aceptado");
            return;
        }
        int producto = buscarProducto(datos[2]);
        if (producto == -1) {
            rechazar(registro, "producto inexistente");
            return;
        }
        int cantidad = Integer.parseInt(datos[3]);
        if (datos[1].equals("SAL") && cantidad > existencias[producto]) {
            rechazar(registro, "existencias insuficientes");
            return;
        }
        aplicarMovimiento(datos[0], datos[1], producto, cantidad);
        System.out.println("Movimiento aceptado.");
    }

    // Extrae los cuatro campos y devuelve un texto vacío cuando la estructura es válida.
    static String validarMovimiento(String registro, String[] datos) {
        int inicio = 0;
        for (int i = 0; i < 4; i++) {
            int barra = registro.indexOf('|', inicio);
            int fin = barra == -1 ? registro.length() : barra;
            if (fin == inicio || (i < 3 && barra == -1) || (i == 3 && barra != -1)) return "error";
            datos[i] = registro.substring(inicio, fin);
            inicio = fin + 1;
        }
        if (!validarIdMovimiento(datos[0]) || (!datos[1].equals("ENT") && !datos[1].equals("SAL"))
                || !validarCodigoProducto(datos[2])) return "error";
        try {
            if (Integer.parseInt(datos[3]) <= 0) return "error";
        } catch (NumberFormatException e) {
            return "error";
        }
        return "";
    }

    // Actualiza los acumulados y reserva el identificador únicamente al aceptar.
    static void aplicarMovimiento(String id, String tipo, int producto, int cantidad) {
        movimientosAceptados[cantidadAceptados++] = id;
        if (tipo.equals("ENT")) {
            entradas[producto] += cantidad;
            existencias[producto] += cantidad;
        } else {
            salidas[producto] += cantidad;
            existencias[producto] -= cantidad;
        }
    }

    // Conserva el movimiento rechazado junto con el único motivo prioritario.
    static void rechazar(String registro, String motivo) {
        rechazos[cantidadRechazos++] = registro + " -> " + motivo;
        System.out.println("Movimiento rechazado: " + motivo + ".");
    }

    // Comprueba la igualdad de inventario y muestra acumulados, alertas y rechazos.
    static void mostrarConciliacion() {
        System.out.println("\nCONCILIACIÓN");
        int totalUnidades = 0, mayorSalida = 0;
        for (int i = 0; i < cantidadProductos; i++) {
            int verificacion = iniciales[i] + entradas[i] - salidas[i];
            System.out.println(codigos[i] + " | inicial " + iniciales[i] + " | entradas " + entradas[i]
                    + " | salidas " + salidas[i] + " | final " + existencias[i]
                    + " | igualdad: " + (verificacion == existencias[i] ? "CORRECTA" : "INCORRECTA"));
            if (existencias[i] < minimos[i]) System.out.println("  STOCK BAJO: mínimo " + minimos[i]);
            if (salidas[i] > mayorSalida) mayorSalida = salidas[i];
            totalUnidades += existencias[i];
        }
        System.out.println("Movimientos rechazados:");
        if (cantidadRechazos == 0) System.out.println("Ninguno.");
        for (int i = 0; i < cantidadRechazos; i++) System.out.println(rechazos[i]);
        if (mayorSalida > 0) {
            System.out.println("Productos con mayor salida acumulada (" + mayorSalida + "):");
            for (int i = 0; i < cantidadProductos; i++) if (salidas[i] == mayorSalida) System.out.println(codigos[i]);
        } else {
            System.out.println("No hubo salidas aceptadas.");
        }
        System.out.println("Total de unidades disponibles: " + totalUnidades);
    }

    // Valida un código con formato PRD-NNN.
    static boolean validarCodigoProducto(String codigo) {
        return validarCodigo(codigo, "PRD-", 7, 4);
    }

    // Valida un identificador con formato MOV-NNNN.
    static boolean validarIdMovimiento(String id) {
        return validarCodigo(id, "MOV-", 8, 4);
    }

    // Reutiliza la comprobación de prefijo, longitud y dígitos para ambos códigos.
    static boolean validarCodigo(String texto, String prefijo, int longitud, int inicioDigitos) {
        if (texto.length() != longitud || !texto.substring(0, inicioDigitos).equals(prefijo)) return false;
        for (int i = inicioDigitos; i < longitud; i++) if (texto.charAt(i) < '0' || texto.charAt(i) > '9') return false;
        return true;
    }

    // Devuelve la posición del producto o -1 cuando no existe.
    static int buscarProducto(String codigo) {
        for (int i = 0; i < cantidadProductos; i++) if (codigos[i].equals(codigo)) return i;
        return -1;
    }

    // Busca únicamente identificadores de movimientos que sí fueron aplicados.
    static int buscarMovimientoAceptado(String id) {
        for (int i = 0; i < cantidadAceptados; i++) if (movimientosAceptados[i].equals(id)) return i;
        return -1;
    }

    // Lee un entero dentro del intervalo y controla errores de conversión.
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
