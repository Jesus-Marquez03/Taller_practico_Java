package com.taller_practico;

import java.util.Scanner;

// Reconstruye un mensaje cuyos fragmentos pueden llegar desordenados o repetidos.
public class Ejercicio02Mensajes {
    static final int MAX_REGISTROS = 30;
    static final int MAX_FRAGMENTOS = 15;
    static String[] fragmentos = new String[MAX_FRAGMENTOS];
    static String identificador = "";
    static int totalFragmentos = 0;
    static int aceptados = 0, rechazados = 0, duplicados = 0, conflictivos = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        int cantidad = leerEntero(teclado, "Cantidad de registros (1 a 30): ", 1, MAX_REGISTROS);
        for (int i = 1; i <= cantidad; i++) {
            System.out.println("Registro " + i + ":");
            analizarRegistro(teclado.nextLine());
        }
        mostrarResultados();
        teclado.close();
    }

    // Valida la estructura completa y registra el fragmento en su posición directa.
    static void analizarRegistro(String registro) {
        int numeral1 = registro.indexOf('#');
        int numeral2 = numeral1 == -1 ? -1 : registro.indexOf('#', numeral1 + 1);
        if (numeral1 == -1 || numeral2 == -1 || registro.indexOf('#', numeral2 + 1) != -1) {
            rechazados++;
            return;
        }
        String id = registro.substring(0, numeral1);
        String parte = registro.substring(numeral1 + 1, numeral2);
        String contenido = registro.substring(numeral2 + 1); // No se usa trim: los espacios importan.
        int barra = parte.indexOf('/');
        if (!validarIdentificador(id) || barra != 2 || parte.length() != 5
                || contenido.length() < 1 || contenido.length() > 25) {
            rechazados++;
            return;
        }
        int posicion = convertirDosDigitos(parte.substring(0, 2));
        int total = convertirDosDigitos(parte.substring(3, 5));
        if (!validarPosiciones(posicion, total)) {
            rechazados++;
            return;
        }

        // El primer registro válido establece los datos que deben compartir los demás.
        if (identificador.equals("")) {
            identificador = id;
            totalFragmentos = total;
        } else if (!identificador.equals(id) || totalFragmentos != total) {
            rechazados++;
            return;
        }
        registrarFragmento(posicion, contenido);
    }

    // Verifica la estructura MSG seguida de dos dígitos.
    static boolean validarIdentificador(String id) {
        return id.length() == 5 && id.substring(0, 3).equals("MSG")
                && esDigito(id.charAt(3)) && esDigito(id.charAt(4));
    }

    // Comprueba que posición y total estén dentro de los límites del mensaje.
    static boolean validarPosiciones(int posicion, int total) {
        return total >= 1 && total <= MAX_FRAGMENTOS && posicion >= 1 && posicion <= total;
    }

    // Distingue un fragmento nuevo, un duplicado idéntico y un conflicto.
    static void registrarFragmento(int posicion, String contenido) {
        int indice = posicion - 1;
        if (fragmentos[indice] == null) {
            fragmentos[indice] = contenido;
            aceptados++;
        } else if (fragmentos[indice].equals(contenido)) {
            duplicados++;
        } else {
            conflictivos++;
        }
    }

    // Une los fragmentos; si provisional es true agrega marcas para los faltantes.
    static String reconstruirMensaje(boolean provisional) {
        String resultado = "";
        for (int i = 0; i < totalFragmentos; i++) {
            if (fragmentos[i] == null) {
                if (provisional) resultado += "[FALTA " + numeroDosDigitos(i + 1) + "]";
            } else {
                resultado += fragmentos[i];
            }
        }
        return resultado;
    }

    // Presenta los contadores, faltantes y las reconstrucciones permitidas.
    static void mostrarResultados() {
        System.out.println("\nAceptados: " + aceptados);
        System.out.println("Rechazados: " + rechazados);
        System.out.println("Duplicados: " + duplicados);
        System.out.println("Conflictivos: " + conflictivos);
        System.out.print("Posiciones faltantes:");
        boolean falta = false;
        for (int i = 0; i < totalFragmentos; i++) {
            if (fragmentos[i] == null) {
                System.out.print(" " + numeroDosDigitos(i + 1));
                falta = true;
            }
        }
        if (!falta) System.out.print(" ninguna");
        System.out.println();

        String provisional = reconstruirMensaje(true);
        System.out.println("Reconstrucción provisional" + (conflictivos > 0 ? " (existen conflictos)" : "") + ": " + provisional);
        if (!falta && conflictivos == 0 && totalFragmentos > 0) {
            String definitivo = reconstruirMensaje(false);
            System.out.println("Mensaje definitivo: " + definitivo);
            System.out.println("Longitud definitiva: " + definitivo.length());
        } else {
            System.out.println("No es posible generar el mensaje definitivo.");
        }
    }

    // Convierte dos dígitos en entero; devuelve -1 si alguno no es válido.
    static int convertirDosDigitos(String texto) {
        if (texto.length() != 2 || !esDigito(texto.charAt(0)) || !esDigito(texto.charAt(1))) return -1;
        return (texto.charAt(0) - '0') * 10 + (texto.charAt(1) - '0');
    }

    // Indica si el carácter recibido está entre 0 y 9.
    static boolean esDigito(char caracter) {
        return caracter >= '0' && caracter <= '9';
    }

    // Presenta una posición con cero inicial cuando es menor que diez.
    static String numeroDosDigitos(int numero) {
        return numero < 10 ? "0" + numero : "" + numero;
    }

    // Solicita un entero hasta recibir un valor dentro del intervalo indicado.
    static int leerEntero(Scanner teclado, String mensaje, int minimo, int maximo) {
        while (true) {
            System.out.print(mensaje);
            try {
                int valor = Integer.parseInt(teclado.nextLine());
                if (valor >= minimo && valor <= maximo) return valor;
            } catch (NumberFormatException e) {
                // El ciclo vuelve a pedir el dato cuando no es un entero.
            }
            System.out.println("Entrada inválida.");
        }
    }
}
