package com.taller_practico;

import java.util.Scanner;

// Procesa registros académicos y presenta estadísticas y clasificación del grupo.
public class Ejercicio03Estudiantes {
    static final int MAX_ESTUDIANTES = 35;
    static final double[] PESOS = {0.20, 0.25, 0.25, 0.30};
    static String[] codigos = new String[MAX_ESTUDIANTES];
    static String[] nombres = new String[MAX_ESTUDIANTES];
    // Cada evaluación se guarda en su propio arreglo, como exige el enunciado.
    static double[] nota1 = new double[MAX_ESTUDIANTES];
    static double[] nota2 = new double[MAX_ESTUDIANTES];
    static double[] nota3 = new double[MAX_ESTUDIANTES];
    static double[] nota4 = new double[MAX_ESTUDIANTES];
    static double[] definitivas = new double[MAX_ESTUDIANTES];
    static boolean[] riesgos = new boolean[MAX_ESTUDIANTES];
    static int cantidadEstudiantes = 0;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        int cantidadRegistros = leerEntero(teclado, "Cantidad de registros (1 a 35): ", 1, MAX_ESTUDIANTES);
        for (int i = 1; i <= cantidadRegistros; i++) {
            System.out.println("Registro " + i + ":");
            String error = procesarRegistro(teclado.nextLine());
            if (!error.equals("")) System.out.println("Registro rechazado: " + error);
        }
        mostrarInforme();
        teclado.close();
    }

    // Extrae y valida todos los campos antes de almacenar al estudiante.
    static String procesarRegistro(String registro) {
        int barra1 = registro.indexOf('|');
        int barra2 = barra1 == -1 ? -1 : registro.indexOf('|', barra1 + 1);
        if (barra1 <= 0 || barra2 == -1 || registro.indexOf('|', barra2 + 1) != -1) return "estructura inválida";
        String codigo = registro.substring(0, barra1);
        String nombre = normalizarNombre(registro.substring(barra1 + 1, barra2));
        if (!validarCodigo(codigo)) return "código inválido";
        if (buscarCodigo(codigo) != -1) return "código duplicado";
        if (nombre.equals("")) return "nombre inválido";

        double[] notas = new double[4];
        if (!extraerNotas(registro.substring(barra2 + 1), notas)) return "deben existir cuatro notas válidas entre 0.0 y 5.0";

        int i = cantidadEstudiantes;
        codigos[i] = codigo;
        nombres[i] = nombre;
        nota1[i] = notas[0]; nota2[i] = notas[1]; nota3[i] = notas[2]; nota4[i] = notas[3];
        definitivas[i] = calcularDefinitiva(notas);
        riesgos[i] = identificarRiesgo(notas);
        cantidadEstudiantes++;
        return "";
    }

    // Verifica el prefijo EST- y sus tres dígitos finales.
    static boolean validarCodigo(String codigo) {
        if (codigo.length() != 7 || !codigo.substring(0, 4).equals("EST-")) return false;
        for (int i = 4; i < 7; i++) if (codigo.charAt(i) < '0' || codigo.charAt(i) > '9') return false;
        return true;
    }

    // Elimina espacios sobrantes y cambia cada palabra a formato de nombre propio.
    static String normalizarNombre(String original) {
        String resultado = "";
        boolean inicioPalabra = true;
        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            if (c == ' ') {
                if (!resultado.equals("") && resultado.charAt(resultado.length() - 1) != ' ') {
                    resultado += ' ';
                    inicioPalabra = true;
                }
            } else if (Character.isLetter(c)) {
                resultado += inicioPalabra ? Character.toUpperCase(c) : Character.toLowerCase(c);
                inicioPalabra = false;
            } else {
                return "";
            }
        }
        if (resultado.endsWith(" ")) resultado = resultado.substring(0, resultado.length() - 1);
        return resultado;
    }

    // Lee exactamente cuatro notas separadas por comas, sin usar split().
    static boolean extraerNotas(String texto, double[] notas) {
        int inicio = 0;
        for (int i = 0; i < 4; i++) {
            int coma = texto.indexOf(',', inicio);
            int fin = coma == -1 ? texto.length() : coma;
            if (fin == inicio || (i < 3 && coma == -1) || (i == 3 && coma != -1)) return false;
            try {
                notas[i] = Double.parseDouble(texto.substring(inicio, fin));
            } catch (NumberFormatException e) {
                return false;
            }
            if (!Double.isFinite(notas[i]) || notas[i] < 0.0 || notas[i] > 5.0) return false;
            inicio = fin + 1;
        }
        return true;
    }

    // Calcula la definitiva ponderada sin redondear el valor usado para clasificar.
    static double calcularDefinitiva(double[] notas) {
        double resultado = 0;
        for (int i = 0; i < 4; i++) resultado += notas[i] * PESOS[i];
        return resultado;
    }

    // Devuelve true cuando al menos dos notas son inferiores a 2.0.
    static boolean identificarRiesgo(double[] notas) {
        int bajas = 0;
        for (int i = 0; i < 4; i++) if (notas[i] < 2.0) bajas++;
        return bajas >= 2;
    }

    // Ordena índices y deja intacto el orden original de los arreglos de estudiantes.
    static int[] construirClasificacion() {
        int[] indices = new int[cantidadEstudiantes];
        for (int i = 0; i < cantidadEstudiantes; i++) indices[i] = i;
        for (int i = 0; i < cantidadEstudiantes - 1; i++) {
            for (int j = i + 1; j < cantidadEstudiantes; j++) {
                int a = indices[i], b = indices[j];
                if (definitivas[b] > definitivas[a]
                        || (definitivas[b] == definitivas[a] && codigos[b].compareTo(codigos[a]) < 0)) {
                    int temporal = indices[i]; indices[i] = indices[j]; indices[j] = temporal;
                }
            }
        }
        return indices;
    }

    // Calcula y presenta resultados individuales, promedios y clasificación.
    static void mostrarInforme() {
        if (cantidadEstudiantes == 0) {
            System.out.println("No hay estudiantes válidos para informar.");
            return;
        }
        double[] sumas = new double[4];
        double sumaDefinitivas = 0;
        int aprobados = 0;
        System.out.println("\nRESULTADOS INDIVIDUALES");
        for (int i = 0; i < cantidadEstudiantes; i++) {
            System.out.printf("%s | %s | %.2f | %s | Riesgo: %s%n", codigos[i], nombres[i], definitivas[i],
                    definitivas[i] >= 3.0 ? "APROBADO" : "NO APROBADO", riesgos[i] ? "SÍ" : "NO");
            sumas[0] += nota1[i]; sumas[1] += nota2[i]; sumas[2] += nota3[i]; sumas[3] += nota4[i];
            sumaDefinitivas += definitivas[i];
            if (definitivas[i] >= 3.0) aprobados++;
        }
        double promedioGrupo = sumaDefinitivas / cantidadEstudiantes;
        for (int i = 0; i < 4; i++) System.out.printf("Promedio nota %d: %.2f%n", i + 1, sumas[i] / cantidadEstudiantes);
        System.out.printf("Promedio de definitivas: %.2f%n", promedioGrupo);
        System.out.printf("Porcentaje de aprobación: %.2f%%%n", aprobados * 100.0 / cantidadEstudiantes);
        System.out.println("Superiores al promedio:");
        for (int i = 0; i < cantidadEstudiantes; i++) if (definitivas[i] > promedioGrupo) System.out.println(codigos[i] + " - " + nombres[i]);

        System.out.println("Clasificación:");
        int[] clasificacion = construirClasificacion();
        for (int i = 0; i < clasificacion.length; i++) {
            int p = clasificacion[i];
            System.out.printf("%d. %s - %s - %.2f%n", i + 1, codigos[p], nombres[p], definitivas[p]);
        }
    }

    // Devuelve la posición del código o -1 cuando aún no está registrado.
    static int buscarCodigo(String codigo) {
        for (int i = 0; i < cantidadEstudiantes; i++) if (codigos[i].equals(codigo)) return i;
        return -1;
    }

    // Lee un entero del intervalo y repite la solicitud ante entradas inválidas.
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
