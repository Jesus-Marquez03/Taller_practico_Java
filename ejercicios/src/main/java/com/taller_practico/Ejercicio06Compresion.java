package com.taller_practico;

import java.util.Scanner;

// Comprime cadenas por grupos consecutivos y valida su restauración exacta.
public class Ejercicio06Compresion {
    static final int MAX_LONGITUD = 200;
    static final int CANTIDAD_CARACTERES = 27;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        System.out.println("1. Comprimir y verificar una cadena original");
        System.out.println("2. Validar y restaurar una cadena comprimida");
        int opcion = leerEntero(teclado, "Seleccione una opción: ", 1, 2);
        if (opcion == 1) {
            String original = leerTextoValido(teclado);
            String comprimido = comprimir(original);
            // El formato generado también pasa por el mismo validador usado para entradas externas.
            if (!validarFormatoComprimido(comprimido)) {
                System.out.println("Error interno: el formato generado no es válido.");
            } else {
                String restaurado = restaurar(comprimido);
                mostrarResultados(original, comprimido, restaurado);
            }
        } else {
            System.out.println("Ingrese la cadena comprimida:");
            String comprimido = teclado.nextLine();
            if (!validarFormatoComprimido(comprimido)) {
                System.out.println("Formato comprimido inválido. No se construyó ninguna restauración.");
            } else {
                String restaurado = restaurar(comprimido);
                System.out.println("Cadena restaurada: " + restaurado);
                System.out.println("Longitud restaurada: " + restaurado.length());
                mostrarFrecuencias(restaurado);
            }
        }
        teclado.close();
    }

    // Solicita una cadena original hasta que cumpla caracteres y longitud.
    static String leerTextoValido(Scanner teclado) {
        while (true) {
            System.out.println("Ingrese una cadena de 1 a 200 caracteres (A-Z y espacios):");
            String texto = teclado.nextLine();
            if (validarTexto(texto)) return texto;
            System.out.println("Cadena inválida.");
        }
    }

    // Acepta únicamente entre 1 y 200 letras mayúsculas o espacios.
    static boolean validarTexto(String texto) {
        if (texto.length() < 1 || texto.length() > MAX_LONGITUD) return false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if ((c < 'A' || c > 'Z') && c != ' ') return false;
        }
        return true;
    }

    // Cuenta cada grupo consecutivo sin usar replace(), repeat() ni bibliotecas.
    static String comprimir(String texto) {
        String resultado = "";
        int i = 0;
        while (i < texto.length()) {
            char actual = texto.charAt(i);
            int cantidad = 1;
            while (i + cantidad < texto.length() && texto.charAt(i + cantidad) == actual) cantidad++;
            resultado += cantidad + ":" + actual + ";";
            i += cantidad;
        }
        return resultado;
    }

    // Revisa todos los grupos y la longitud total antes de construir la salida.
    static boolean validarFormatoComprimido(String texto) {
        if (texto.length() == 0) return false;
        int posicion = 0;
        int longitudRestaurada = 0;
        char anterior = 0;
        while (posicion < texto.length()) {
            int dosPuntos = texto.indexOf(':', posicion);
            if (dosPuntos == -1 || dosPuntos == posicion) return false;
            String numero = texto.substring(posicion, dosPuntos);
            if (numero.length() > 1 && numero.charAt(0) == '0') return false;
            for (int i = 0; i < numero.length(); i++) if (numero.charAt(i) < '0' || numero.charAt(i) > '9') return false;
            int cantidad;
            try {
                cantidad = Integer.parseInt(numero);
            } catch (NumberFormatException e) {
                return false;
            }
            if (cantidad < 1 || cantidad > MAX_LONGITUD) return false;
            if (dosPuntos + 2 >= texto.length() || texto.charAt(dosPuntos + 2) != ';') return false;
            char caracter = texto.charAt(dosPuntos + 1);
            if ((caracter < 'A' || caracter > 'Z') && caracter != ' ') return false;
            if (caracter == anterior) return false; // Dos grupos iguales seguidos no son canónicos.
            anterior = caracter;
            longitudRestaurada += cantidad;
            if (longitudRestaurada > MAX_LONGITUD) return false;
            posicion = dosPuntos + 3;
        }
        return posicion == texto.length();
    }

    // Restaura después de que el formato completo ya fue aprobado.
    static String restaurar(String comprimido) {
        String resultado = "";
        int posicion = 0;
        while (posicion < comprimido.length()) {
            int dosPuntos = comprimido.indexOf(':', posicion);
            int cantidad = Integer.parseInt(comprimido.substring(posicion, dosPuntos));
            char caracter = comprimido.charAt(dosPuntos + 1);
            for (int i = 0; i < cantidad; i++) resultado += caracter;
            posicion = dosPuntos + 3;
        }
        return resultado;
    }

    // Usa 26 posiciones para letras y la posición 26 para el espacio.
    static int[] calcularFrecuencias(String texto) {
        int[] frecuencias = new int[CANTIDAD_CARACTERES];
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            int posicion = c == ' ' ? 26 : c - 'A';
            frecuencias[posicion]++;
        }
        return frecuencias;
    }

    // Compara las cadenas y presenta longitudes, ahorro y frecuencias.
    static void mostrarResultados(String original, String comprimido, String restaurado) {
        System.out.println("Cadena comprimida: " + comprimido);
        System.out.println("Cadena restaurada: " + restaurado);
        System.out.println("Coincidencia exacta: " + (original.equals(restaurado) ? "SÍ" : "NO"));
        System.out.println("Longitud original: " + original.length());
        System.out.println("Longitud comprimida: " + comprimido.length());
        double ahorro = (1.0 - comprimido.length() / (double) original.length()) * 100.0;
        System.out.printf("Porcentaje de ahorro: %.2f%%%s%n", ahorro, ahorro < 0 ? " (aumento de tamaño)" : "");
        mostrarFrecuencias(original);
    }

    // Informa cada carácter presente y todos los empatados con mayor frecuencia.
    static void mostrarFrecuencias(String texto) {
        int[] frecuencias = calcularFrecuencias(texto);
        int mayor = 0;
        System.out.println("Frecuencias:");
        for (int i = 0; i < frecuencias.length; i++) {
            if (frecuencias[i] > 0) {
                String nombre = i == 26 ? "ESPACIO" : Character.toString((char) ('A' + i));
                System.out.println(nombre + ": " + frecuencias[i]);
                if (frecuencias[i] > mayor) mayor = frecuencias[i];
            }
        }
        System.out.println("Carácter(es) más frecuente(s):");
        for (int i = 0; i < frecuencias.length; i++) {
            if (frecuencias[i] == mayor) System.out.println(i == 26 ? "ESPACIO" : Character.toString((char) ('A' + i)));
        }
    }

    // Valida una opción numérica sin finalizar ante letras u otros valores.
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
