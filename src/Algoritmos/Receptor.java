package src.Algoritmos;
import java.util.*;

public class Receptor {
    static boolean soloBits(String s) {
        if (s == null || s.isEmpty()) return false;
        for (char c : s.toCharArray()) if (c != '0' && c != '1') return false;
        return true;
    }
    // result[0] -> Status:
    //      0 = Success
    //      1 = Fixable
    //      2 = Error
    // ---------------- Hamming (paridad par) -----------------
    public static class Hamming {
        public static String[] recibir(String trama) {
            String[] ret_statement = new String[2];
            if (trama.length() < 3) {
                System.err.println("[RECEPTOR:Hamming] Trama demasiado corta.");
                ret_statement[0] = "2";
                ret_statement[1] = "";
                return ret_statement;
            }

            // separar p0 (último bit) y el resto del código
            int nTotal = trama.length();
            int p0 = trama.charAt(nTotal - 1) - '0';
            String cword = trama.substring(0, nTotal - 1); // sin p0
            int n = cword.length();
            int r = 0;
            while ((1 << r) < (n+1)) r++;
            // 1-indexado sobre 'cword'
            int[] code = new int[n + 1];
            for (int i = 1; i <= n; i++) code[i] = cword.charAt(i - 1) - '0';

            // síndrome (paridad par)
            int syndrome = 0;
            for (int p = 0; p < r; p++) {
                // System.out.println("P"+(p+1));
                int pos = 1 << p, sum = 0;
                for (int i = 1; i <= n; i++) {
                    if ((i & pos) != 0) {
                        // System.out.println("\t"+i+"->"+code[i-1]);
                        sum ^= code[i];
                    }
                }
                // System.out.println("Sum = "+sum);
                if ((sum & 1) == 1) syndrome |= pos;
            }
            // paridad global incluyendo p0
            int parityAll = p0;
            for (int i = 1; i <= n; i++) parityAll ^= code[i]; // XOR de todo
            // casos SECDED:
            // parityAll==0 -> número par de bits en error
            // parityAll==1 -> número impar de bits en error
            if (syndrome == 0 && parityAll == 0) {
                // sin errores
                String msg = extraerDatos(code);
                System.out.println("[RECEPTOR:Hamming-SECDED] Sin errores. Mensaje original: "+ msg);
                ret_statement[0] = "0";
                ret_statement[1] = msg;
                return ret_statement;
            }
            if (syndrome == 0 && parityAll == 1) {
                // error solo en p0
                String msg = extraerDatos(code);
                System.out.println("[RECEPTOR:Hamming-SECDED] Se corrigió el bit de paridad global (p0). Mensaje: "+msg);
                ret_statement[0] = "1";
                ret_statement[1] = msg;
                return ret_statement;
            }
            if (syndrome != 0 && parityAll == 1) {
                if(syndrome > n){
                    // Para tramas que no son potencias exactas de 2
                    // Sindrome se sale del rango, por tanto el error es incorregible.
                    System.out.println("[RECEPTOR:Hamming-SECDED] Sindrome excede longitud de la trama, por tanto hay errores (2+). Mensaje descartado.");
                    ret_statement[0] = "2";
                    ret_statement[1] = "";
                    return ret_statement;
                } else{
                    // un error en alguna posición del código (corregible)
                    code[syndrome] ^= 1;
                    String msg = extraerDatos(code);
                    System.out.println("[RECEPTOR:Hamming-SECDED] Se corrigió 1 bit en posición " + syndrome + ". Mensaje corregido: " + msg);
                    ret_statement[0] = "1";
                    ret_statement[1] = "";
                    return ret_statement;
                }

            }
            // syndrome != 0 && parityAll == 0  ->  2+ errores: DETECTA pero NO corrige
            System.out.println("[RECEPTOR:Hamming-SECDED] Se detectaron múltiples errores (2+). Mensaje descartado.");
            ret_statement[0] = "2";
            ret_statement[1] = "";
            return ret_statement;
        }

        static String extraerDatos(int[] code) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < code.length; i++) {
                if ((i & (i - 1)) != 0) sb.append(code[i]); // saltar potencias de 2
            }
            return sb.toString();
        }
    }

    // ---------------- CRC-32 (MSB-first, polinomio 1||0x04C11DB7) -----------------
    public static class CRC32MSB {
        static final int POLY = 0x04C11DB7;
        static final int DEG = 32;

        public static String verificar(String trama) {
            if (trama.length() <= DEG) {
                System.out.println("[RECEPTOR:CRC-32] Trama demasiado corta.");
                return "";
            }
            
            String msg = trama.substring(0, trama.length() - DEG);
            String remRx = trama.substring(trama.length() - DEG);
            String remCalc = remainder(msg);
            if (remCalc.equals(remRx) && isZeroRemainder(msg + remRx)) {
                System.out.println("[RECEPTOR:CRC-32] Sin errores. Mensaje original: " + msg);
                return msg;
            } else {
                System.out.println("[RECEPTOR:CRC-32] Se detectaron errores. Mensaje descartado.");
                return "";
            }
        }
        static String remainder(String bits) {
            int[] data = strToInts(bits);
            int[] work = Arrays.copyOf(data, data.length + DEG);
            int[] poly = polyVector();
            for (int i = 0; i < bits.length(); i++) {
                if (work[i] == 1) for (int j = 0; j < poly.length; j++) work[i + j] ^= poly[j];
            }
            int[] rem = Arrays.copyOfRange(work, work.length - DEG, work.length);
            return intsToStr(rem);
        }
        static boolean isZeroRemainder(String full) {
            int[] data = strToInts(full);
            int[] work = Arrays.copyOf(data, data.length);
            int[] poly = polyVector();
            for (int i = 0; i <= work.length - (DEG + 1); i++) {
                if (work[i] == 1) for (int j = 0; j < poly.length; j++) work[i + j] ^= poly[j];
            }
            for (int i = work.length - DEG; i < work.length; i++) if (work[i] != 0) return false;
            return true;
        }
        static int[] polyVector() {
            int[] v = new int[DEG + 1];
            v[0] = 1;
            for (int i = DEG - 1, j = 1; i >= 0; i--, j++) v[j] = (POLY >>> i) & 1;
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

    // ---------------- Fletcher-8/16/32 (w = tipo/2; Sum1=1, Sum2=1; mod 2^w-1) -----------------
    public static class Fletcher {
        public static String verificar(String trama, int tipo) {
            if (tipo != 8 && tipo != 16 && tipo != 32){
                System.out.println("[RECEPTOR:Fletcher] Tipo inválido (use 8/16/32).");
                return "";
            }
                
            int w = tipo / 2;

            if (trama.length() <= tipo){
                System.out.println("[RECEPTOR:Fletcher] Trama demasiado corta.");
                return "";
            }

            // separar mensaje y checksum (el mensaje llega SIN padding)
            String msg = trama.substring(0, trama.length() - tipo);
            String ckRx = trama.substring(trama.length() - tipo);

            // padding SOLO para cálculo (a la IZQUIERDA) sobre el MENSAJE
            int need = (w - (msg.length() % w)) % w;
            String padded = "0".repeat(need) + msg;

            int mod = (1 << w) - 1;
            long sum1 = 1, sum2 = 1;
            for (int i = 0; i < padded.length(); i += w) {
                int val = Integer.parseUnsignedInt(padded.substring(i, i + w), 2);
                sum1 = (sum1 + val) % mod;
                sum2 = (sum2 + sum1) % mod;
            }
            long ckVal = (sum2 << w) | sum1;
            String ckCalc = Long.toBinaryString(ckVal);
            if (ckCalc.length() < tipo) ckCalc = "0".repeat(tipo - ckCalc.length()) + ckCalc;
            else if (ckCalc.length() > tipo) ckCalc = ckCalc.substring(ckCalc.length() - tipo);

            if (ckCalc.equals(ckRx)) {
                System.out.println("[RECEPTOR:Fletcher-" + tipo + "] Sin errores. Mensaje original: " + msg);
                return msg;
            } else {
                System.out.println("[RECEPTOR:Fletcher-" + tipo + "] Se detectaron errores. Mensaje descartado.");
                return "";
            }
        }
    }

    // ---------------- Main -----------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Seleccione algoritmo del RECEPTOR:\n  1) Hamming\n  2) CRC-32\n  3) Fletcher checksum\nOpción: ");
        String opt = sc.nextLine().trim();
        System.out.print("Ingrese la trama binaria recibida: ");
        String trama = sc.nextLine().trim();
        if (!soloBits(trama)) { System.out.println("[Error] Solo se permiten 0s y 1s, y longitud > 0"); return; }

        switch (opt) {
            case "1":
                System.out.println(Hamming.recibir(trama));
                break;
            case "2":
                System.out.println(CRC32MSB.verificar(trama));
                break;
            case "3":
                System.out.print("Seleccione Fletcher (8,16,32): ");
                try {
                    int tipo = Integer.parseInt(sc.nextLine().trim());
                    System.out.println(Fletcher.verificar(trama, tipo));
                } catch (Exception e) {
                    System.out.println("[RECEPTOR:Fletcher] Error: " + e.getMessage());
                }
                break;
            default:
                System.out.println("Opción inválida");
        }
    }
}
