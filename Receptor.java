import java.util.*;

public class Receptor {
    // ---------------- Utilidades -----------------
    static boolean soloBits(String s) {
        if (s == null || s.isEmpty()) return false;
        for (char c : s.toCharArray()) if (c != '0' && c != '1') return false;
        return true;
    }

    // ---------------- Hamming (paridad par) -----------------
    static class Hamming {
        static String recibir(String trama) {
            int n = trama.length();
            // calcular r tal que 2^r >= n+1
            int r = 0;
            while ((1 << r) < (n + 1)) r++;

            // 1-indexado
            int[] code = new int[n + 1];
            for (int i = 1; i <= n; i++) code[i] = trama.charAt(i - 1) - '0';

            // calcular síndrome
            int syndrome = 0;
            for (int p = 0; p < r; p++) {
                int pos = 1 << p;
                int sum = 0;
                for (int i = 1; i <= n; i++) if ((i & pos) != 0) sum ^= code[i];
                if ((sum & 1) == 1) syndrome |= pos; // paridad par
            }

            if (syndrome == 0) {
                // sin errores
                String msg = extraerDatos(code);
                return "[RECEPTOR:Hamming] Sin errores. Mensaje original: " + msg;
            } else if (syndrome >= 1 && syndrome <= n) {
                // corregir 1 bit
                code[syndrome] ^= 1;
                String msg = extraerDatos(code);
                return "[RECEPTOR:Hamming] Se corrigió 1 bit en posición " + syndrome + ". Mensaje corregido: " + msg;
            } else {
                return "[RECEPTOR:Hamming] Síndrome inválido. Mensaje descartado.";
            }
        }

        static String extraerDatos(int[] code) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < code.length; i++) {
                // omitir posiciones potencia de 2
                if ((i & (i - 1)) != 0) sb.append(code[i]);
            }
            return sb.toString();
        }
    }

    // ---------------- CRC-32 (MSB-first, polinomio 0x04C11DB7) -----------------
    static class CRC32MSB {
        static final int POLY = 0x04C11DB7; // grado 32
        static final int DEG = 32;

        static String verificar(String trama) {
            if (trama.length() <= DEG) return "[RECEPTOR:CRC-32] Trama demasiado corta.";
            String msg = trama.substring(0, trama.length() - DEG);
            String remRx = trama.substring(trama.length() - DEG);
            String remCalc = remainder(msg);
            if (remCalc.equals(remRx) && isZeroRemainder(msg + remRx)) {
                return "[RECEPTOR:CRC-32] Sin errores. Mensaje original: " + msg;
            } else {
                return "[RECEPTOR:CRC-32] Se detectaron errores. Mensaje descartado.";
            }
        }

        static String remainder(String bits) {
            int[] data = strToInts(bits);
            int[] work = Arrays.copyOf(data, data.length + DEG);
            int[] poly = polyVector(); // 33 bits
            for (int i = 0; i < bits.length(); i++) {
                if (work[i] == 1) {
                    for (int j = 0; j < poly.length; j++) work[i + j] ^= poly[j];
                }
            }
            int[] rem = Arrays.copyOfRange(work, work.length - DEG, work.length);
            return intsToStr(rem);
        }

        static boolean isZeroRemainder(String full) {
            // útil para comprobar trabajo completo (msg + resto)
            int[] data = strToInts(full);
            int[] work = Arrays.copyOf(data, data.length);
            int[] poly = polyVector();
            for (int i = 0; i <= work.length - (DEG + 1); i++) {
                if (work[i] == 1) {
                    for (int j = 0; j < poly.length; j++) work[i + j] ^= poly[j];
                }
            }
            for (int i = work.length - DEG; i < work.length; i++) if (work[i] != 0) return false;
            return true;
        }

        static int[] polyVector() {
            int[] v = new int[DEG + 1];
            for (int i = DEG; i >= 0; i--) v[DEG - i] = ((POLY >>> i) & 1);
            return v;
        }

        static int[] strToInts(String s) {
            int[] v = new int[s.length()];
            for (int i = 0; i < s.length(); i++) v[i] = s.charAt(i) == '1' ? 1 : 0;
            return v;
        }

        static String intsToStr(int[] v) {
            StringBuilder sb = new StringBuilder();
            for (int b : v) sb.append(b == 1 ? '1' : '0');
            return sb.toString();
        }
    }

    // ---------------- Main -----------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Seleccione algoritmo del RECEPTOR:\n  1) Hamming\n  2) CRC-32\nOpción: ");
        String opt = sc.nextLine().trim();
        System.out.print("Ingrese la trama binaria recibida: ");
        String trama = sc.nextLine().trim();
        if (!soloBits(trama)) {
            System.out.println("[Error] Solo se permiten 0s y 1s, y longitud > 0");
            return;
        }
        switch (opt) {
            case "1":
                System.out.println(Hamming.recibir(trama));
                break;
            case "2":
                System.out.println(CRC32MSB.verificar(trama));
                break;
            default:
                System.out.println("Opción inválida");
        }
    }
}