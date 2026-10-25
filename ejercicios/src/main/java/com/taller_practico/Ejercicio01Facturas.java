package com.taller_practico;

import java.util.Scanner;

// Audita una factura, consolida productos repetidos y calcula sus valores.
public class Ejercicio01Facturas {
    static final int MAX_LINEAS = 20;
    static final double IMPUESTO = 0.19;

    static String[] codigos = new String[MAX_LINEAS];
    static int[] cantidades = new int[MAX_LINEAS];
    static long[] precios = new long[MAX_LINEAS];
    static int cantidadProductos = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        System.out.println("Ingrese la factura completa:");
        String factura = teclado.nextLine();

        // Primero se valida toda la factura. Solo después se muestran valores financieros.
        String error = procesarFactura(factura);
        if (!error.equals("")) {
            System.out.println("Factura rechazada: " + error);
        } else {
            mostrarInforme(factura);
        }
        teclado.close();
    }

    // Separa encabezado y líneas, valida todo y consolida los productos.
    static String procesarFactura(String factura) {
        int primerSeparador = factura.indexOf('|');
        int segundoSeparador = primerSeparador == -1 ? -1 : factura.indexOf('|', primerSeparador + 1);
        if (primerSeparador == -1 || segundoSeparador == -1
                || factura.indexOf('|', segundoSeparador + 1) != -1) {
            return "el formato general debe contener exactamente dos separadores |";
        }

        String numero = factura.substring(0, primerSeparador);
        String cliente = factura.substring(primerSeparador + 1, segundoSeparador);
        String detalle = factura.substring(segundoSeparador + 1);
        if (!validarEncabezado(numero, cliente)) {
            return "encabezado inválido";
        }
        if (detalle.length() == 0) {
            return "el detalle está vacío";
        }

        int inicio = 0;
        int lineas = 0;
        while (inicio <= detalle.length()) {
            int fin = detalle.indexOf(';', inicio);
            if (fin == -1) {
                fin = detalle.length();
            }
            if (fin == inicio) {
                return "hay una línea vacía o un separador adicional";
            }
            lineas++;
            if (lineas > MAX_LINEAS) {
                return "se superan las 20 líneas permitidas";
            }

            String error = procesarLinea(detalle.substring(inicio, fin));
            if (!error.equals("")) {
                return "línea " + lineas + ": " + error;
            }
            if (fin == detalle.length()) {
                break;
            }
            inicio = fin + 1;
            if (inicio == detalle.length()) {
                return "no se permite un separador ; al final";
            }
        }
        return "";
    }

    // Valida el número FAC-AAAA-NNNN y el cliente CLI-NNN.
    static boolean validarEncabezado(String numero, String cliente) {
        return numero.length() == 13 && numero.substring(0, 4).equals("FAC-")
                && sonDigitos(numero, 4, 8) && numero.charAt(8) == '-'
                && sonDigitos(numero, 9, 13)
                && cliente.length() == 7 && cliente.substring(0, 4).equals("CLI-")
                && sonDigitos(cliente, 4, 7);
    }

    // Analiza codigo:cantidad:precio. Devuelve vacío si la línea es válida.
    static String procesarLinea(String linea) {
        int dosPuntos1 = linea.indexOf(':');
        int dosPuntos2 = dosPuntos1 == -1 ? -1 : linea.indexOf(':', dosPuntos1 + 1);
        if (dosPuntos1 <= 0 || dosPuntos2 == -1 || dosPuntos2 == linea.length() - 1
                || linea.indexOf(':', dosPuntos2 + 1) != -1) {
            return "estructura inválida";
        }
        String codigo = linea.substring(0, dosPuntos1);
        String textoCantidad = linea.substring(dosPuntos1 + 1, dosPuntos2);
        String textoPrecio = linea.substring(dosPuntos2 + 1);
        if (codigo.length() != 4 || codigo.charAt(0) != 'P' || !sonDigitos(codigo, 1, 4)) {
            return "código de producto inválido";
        }

        int cantidad;
        long precio;
        try {
            cantidad = Integer.parseInt(textoCantidad);
            precio = Long.parseLong(textoPrecio);
        } catch (NumberFormatException e) {
            return "cantidad o precio no numérico";
        }
        if (cantidad < 1 || cantidad > 100 || precio <= 0) {
            return "cantidad fuera de 1 a 100 o precio no positivo";
        }

        int posicion = buscarProducto(codigo);
        if (posicion == -1) {
            codigos[cantidadProductos] = codigo;
            cantidades[cantidadProductos] = cantidad;
            precios[cantidadProductos] = precio;
            cantidadProductos++;
        } else if (precios[posicion] != precio) {
            return "el producto " + codigo + " aparece con precios diferentes";
        } else {
            consolidarCantidad(posicion, cantidad);
        }
        return "";
    }

    // Busca un producto consolidado y devuelve su posición, o -1 si no existe.
    static int buscarProducto(String codigo) {
        for (int i = 0; i < cantidadProductos; i++) {
            if (codigos[i].equals(codigo)) {
                return i;
            }
        }
        return -1;
    }

    // Suma una cantidad a un producto que ya apareció con el mismo precio.
    static void consolidarCantidad(int posicion, int cantidad) {
        cantidades[posicion] += cantidad;
    }

    // Calcula descuento, impuesto y totales una vez que toda la entrada es válida.
    static void mostrarInforme(String factura) {
        int p1 = factura.indexOf('|');
        int p2 = factura.indexOf('|', p1 + 1);
        System.out.println("\nFACTURA: " + factura.substring(0, p1));
        System.out.println("CLIENTE: " + factura.substring(p1 + 1, p2));
        System.out.println("Código  Cantidad  Subtotal  Descuento  Base  Impuesto  Final");

        long totalSubtotal = 0, totalDescuento = 0, totalBase = 0, totalImpuesto = 0, totalFinal = 0;
        long mayorFinal = -1;
        String codigoMayor = "";
        for (int i = 0; i < cantidadProductos; i++) {
            long subtotal = precios[i] * cantidades[i];
            double porcentaje = cantidades[i] >= 10 ? 0.08 : (cantidades[i] >= 5 ? 0.05 : 0.0);
            long descuento = Math.round(subtotal * porcentaje);
            long base = subtotal - descuento;
            long impuesto = Math.round(base * IMPUESTO);
            long valorFinal = base + impuesto;
            System.out.println(codigos[i] + "     " + cantidades[i] + "       $" + subtotal
                    + "   $" + descuento + "      $" + base + "  $" + impuesto + "    $" + valorFinal);

            totalSubtotal += subtotal;
            totalDescuento += descuento;
            totalBase += base;
            totalImpuesto += impuesto;
            totalFinal += valorFinal;
            if (valorFinal > mayorFinal || (valorFinal == mayorFinal && codigos[i].compareTo(codigoMayor) < 0)) {
                mayorFinal = valorFinal;
                codigoMayor = codigos[i];
            }
        }
        System.out.println("TOTALES: subtotal $" + totalSubtotal + ", descuento $" + totalDescuento
                + ", base $" + totalBase + ", impuesto $" + totalImpuesto + ", final $" + totalFinal);
        System.out.println("Producto con mayor valor final: " + codigoMayor + " ($" + mayorFinal + ")");
    }

    // Comprueba que el tramo indicado del texto esté formado únicamente por dígitos.
    static boolean sonDigitos(String texto, int inicio, int fin) {
        for (int i = inicio; i < fin; i++) {
            if (texto.charAt(i) < '0' || texto.charAt(i) > '9') return false;
        }
        return true;
    }
}
