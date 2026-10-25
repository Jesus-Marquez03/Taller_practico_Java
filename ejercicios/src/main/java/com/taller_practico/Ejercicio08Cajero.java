package com.taller_practico;

import java.util.Scanner;

// Simula un cajero con cinco cuentas y existencias limitadas de billetes.
public class Ejercicio08Cajero {
    static final int TOTAL_CUENTAS = 5;
    static final int MAX_RETIRO = 600000;
    static final int MAX_ACUMULADO = 1500000;
    static final int[] DENOMINACIONES = {50000, 20000, 10000};

    static String[] cuentas = new String[TOTAL_CUENTAS];
    static String[] claves = new String[TOTAL_CUENTAS];
    static long[] saldos = new long[TOTAL_CUENTAS];
    static int[] intentosFallidos = new int[TOTAL_CUENTAS];
    static boolean[] bloqueadas = new boolean[TOTAL_CUENTAS];
    static int[] totalRetirado = new int[TOTAL_CUENTAS];
    static int[] billetes = new int[3];

    // Guarda los retiros del acceso actual; se reinicia al iniciar una nueva sesión.
    static int[] retirosSesion = new int[150];
    static int cantidadRetirosSesion = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        inicializarCajero(teclado);
        int opcion;
        do {
            System.out.println("\n1. Iniciar sesión");
            System.out.println("2. Finalizar");
            opcion = leerEntero(teclado, "Opción: ", 1, 2);
            if (opcion == 1) {
                int cuenta = autenticar(teclado);
                if (cuenta != -1) atenderSesion(teclado, cuenta);
            }
        } while (opcion != 2);
        mostrarCierre();
        teclado.close();
    }

    // Registra exactamente cinco cuentas válidas y luego las existencias del cajero.
    static void inicializarCajero(Scanner teclado) {
        System.out.println("INICIALIZACIÓN DE CUENTAS");
        for (int i = 0; i < TOTAL_CUENTAS; i++) {
            while (true) {
                System.out.println("Cuenta " + (i + 1) + " de " + TOTAL_CUENTAS);
                System.out.print("Código CTA-NNNN: ");
                String codigo = teclado.nextLine();
                System.out.print("Clave de cuatro dígitos: ");
                String clave = teclado.nextLine();
                if (!validarCuenta(codigo) || buscarCuenta(codigo) != -1 || !validarClave(clave)) {
                    System.out.println("Código o clave inválida/duplicada. Intente de nuevo.");
                    continue;
                }
                long saldo = leerLong(teclado, "Saldo inicial: ", 0, Long.MAX_VALUE);
                cuentas[i] = codigo; claves[i] = clave; saldos[i] = saldo;
                break;
            }
        }
        for (int i = 0; i < DENOMINACIONES.length; i++) {
            billetes[i] = leerEntero(teclado, "Cantidad de billetes de $" + DENOMINACIONES[i] + ": ", 0, Integer.MAX_VALUE);
        }
    }

    // Mantiene por cuenta los intentos consecutivos y bloquea al llegar a tres.
    static int autenticar(Scanner teclado) {
        System.out.print("Código de cuenta: ");
        String codigo = teclado.nextLine();
        int cuenta = buscarCuenta(codigo);
        if (cuenta == -1) {
            System.out.println("Cuenta inexistente.");
            return -1;
        }
        if (bloqueadas[cuenta]) {
            System.out.println("La cuenta está bloqueada.");
            return -1;
        }
        while (!bloqueadas[cuenta]) {
            System.out.print("Clave: ");
            String clave = teclado.nextLine();
            if (claves[cuenta].equals(clave)) {
                intentosFallidos[cuenta] = 0;
                cantidadRetirosSesion = 0;
                System.out.println("Acceso correcto.");
                return cuenta;
            }
            intentosFallidos[cuenta]++;
            if (intentosFallidos[cuenta] == 3) {
                bloqueadas[cuenta] = true;
                System.out.println("Cuenta bloqueada durante esta ejecución.");
            } else {
                System.out.println("Clave incorrecta. Intentos disponibles: " + (3 - intentosFallidos[cuenta]));
            }
        }
        return -1;
    }

    // Mantiene el menú de la cuenta autenticada hasta cerrar la sesión.
    static void atenderSesion(Scanner teclado, int cuenta) {
        int opcion;
        do {
            System.out.println("\n1. Consultar saldo");
            System.out.println("2. Retirar dinero");
            System.out.println("3. Cambiar clave");
            System.out.println("4. Consultar sus retiros de la sesión");
            System.out.println("5. Cerrar sesión");
            opcion = leerEntero(teclado, "Opción: ", 1, 5);
            switch (opcion) {
                case 1: System.out.println("Saldo: $" + saldos[cuenta]); break;
                case 2: retirar(teclado, cuenta); break;
                case 3: cambiarClave(teclado, cuenta); break;
                case 4: mostrarRetirosSesion(); break;
                case 5: System.out.println("Sesión cerrada."); break;
                default: break;
            }
        } while (opcion != 5);
    }

    // Valida monto, saldo, cupo y combinación antes de actualizar los datos.
    static void retirar(Scanner teclado, int cuenta) {
        int monto = leerEntero(teclado, "Monto a retirar: ", 1, Integer.MAX_VALUE);
        if (monto % 10000 != 0 || monto > MAX_RETIRO) {
            System.out.println("Retiro rechazado: debe ser múltiplo de $10.000 y máximo $600.000.");
            return;
        }
        if (monto > saldos[cuenta]) {
            System.out.println("Retiro rechazado: saldo insuficiente.");
            return;
        }
        if (totalRetirado[cuenta] + monto > MAX_ACUMULADO) {
            System.out.println("Retiro rechazado: supera el límite acumulado de la cuenta.");
            return;
        }
        int[] combinacion = buscarCombinacion(monto);
        if (combinacion[0] == -1) {
            System.out.println("Retiro rechazado: el cajero no tiene una combinación posible.");
            return;
        }

        // Las existencias y el saldo cambian solamente después de validar todo.
        for (int i = 0; i < 3; i++) billetes[i] -= combinacion[i];
        saldos[cuenta] -= monto;
        totalRetirado[cuenta] += monto;
        retirosSesion[cantidadRetirosSesion++] = monto;
        System.out.println("Retiro aceptado:");
        for (int i = 0; i < 3; i++) System.out.println("Billetes de $" + DENOMINACIONES[i] + ": " + combinacion[i]);
        System.out.println("Saldo restante: $" + saldos[cuenta]);
        System.out.println("Cupo disponible: $" + (MAX_ACUMULADO - totalRetirado[cuenta]));
    }

    // Explora combinaciones y elige menos billetes; los empates favorecen 50.000 y 20.000.
    static int[] buscarCombinacion(int monto) {
        int[] mejor = {-1, -1, -1};
        int menorCantidad = Integer.MAX_VALUE;
        int max50 = Math.min(billetes[0], monto / DENOMINACIONES[0]);
        for (int c50 = 0; c50 <= max50; c50++) {
            int restante50 = monto - c50 * DENOMINACIONES[0];
            int max20 = Math.min(billetes[1], restante50 / DENOMINACIONES[1]);
            for (int c20 = 0; c20 <= max20; c20++) {
                int restante = restante50 - c20 * DENOMINACIONES[1];
                if (restante % DENOMINACIONES[2] != 0) continue;
                int c10 = restante / DENOMINACIONES[2];
                if (c10 > billetes[2]) continue;
                int cantidad = c50 + c20 + c10;
                if (cantidad < menorCantidad
                        || (cantidad == menorCantidad && c50 > mejor[0])
                        || (cantidad == menorCantidad && c50 == mejor[0] && c20 > mejor[1])) {
                    menorCantidad = cantidad;
                    mejor[0] = c50; mejor[1] = c20; mejor[2] = c10;
                }
            }
        }
        return mejor;
    }

    // Cambia la clave si tiene cuatro dígitos, es diferente y está confirmada.
    static void cambiarClave(Scanner teclado, int cuenta) {
        System.out.print("Nueva clave: ");
        String nueva = teclado.nextLine();
        System.out.print("Confirme la nueva clave: ");
        String confirmacion = teclado.nextLine();
        if (!validarClave(nueva) || nueva.equals(claves[cuenta]) || !nueva.equals(confirmacion)) {
            System.out.println("Cambio rechazado: clave inválida, repetida o sin confirmación correcta.");
            return;
        }
        claves[cuenta] = nueva;
        System.out.println("Clave actualizada.");
    }

    // Lista únicamente los retiros realizados desde el acceso actual.
    static void mostrarRetirosSesion() {
        if (cantidadRetirosSesion == 0) {
            System.out.println("No hay retiros en esta sesión.");
        } else {
            for (int i = 0; i < cantidadRetirosSesion; i++) System.out.println((i + 1) + ". $" + retirosSesion[i]);
        }
    }

    // Presenta las existencias finales y el acumulado retirado por cuenta.
    static void mostrarCierre() {
        System.out.println("\nEXISTENCIAS FINALES DEL CAJERO");
        for (int i = 0; i < 3; i++) System.out.println("$" + DENOMINACIONES[i] + ": " + billetes[i]);
        System.out.println("TOTAL RETIRADO POR CUENTA");
        for (int i = 0; i < TOTAL_CUENTAS; i++) System.out.println(cuentas[i] + ": $" + totalRetirado[i]);
    }

    // Verifica el formato CTA-NNNN.
    static boolean validarCuenta(String cuenta) {
        if (cuenta.length() != 8 || !cuenta.substring(0, 4).equals("CTA-")) return false;
        for (int i = 4; i < 8; i++) if (!esDigito(cuenta.charAt(i))) return false;
        return true;
    }

    // Valida cuatro dígitos conservados como String, incluidos ceros iniciales.
    static boolean validarClave(String clave) {
        if (clave.length() != 4) return false;
        for (int i = 0; i < 4; i++) if (!esDigito(clave.charAt(i))) return false;
        return true;
    }

    // Devuelve el índice de la cuenta o -1 si no existe.
    static int buscarCuenta(String codigo) {
        for (int i = 0; i < TOTAL_CUENTAS; i++) if (codigo.equals(cuentas[i])) return i;
        return -1;
    }

    // Indica si el carácter recibido es un dígito.
    static boolean esDigito(char c) { return c >= '0' && c <= '9'; }

    // Solicita un entero hasta que quede dentro de los límites.
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

    // Solicita un saldo long válido dentro de los límites.
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
