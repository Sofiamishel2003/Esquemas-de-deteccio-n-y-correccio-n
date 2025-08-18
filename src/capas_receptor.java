package src;
// CODIGO PLACEHOLDER PARA SIMULAR EL SERVIDOR

import java.io.*;
import java.net.*;
import java.util.Random;

import src.Algoritmos.Receptor;

public class capas_receptor {
    public static String[] receive(String trama, int algoritmo){
        String[] result = new String[3];
        if(algoritmo == 1){
            long startTime = System.nanoTime();
            src.Algoritmos.Receptor.Hamming receiver = new src.Algoritmos.Receptor.Hamming();
            long endTime = System.nanoTime();
            String elapsedStr = (endTime - startTime)+"";
            String[] msg = receiver.recibir(trama);
            result[0] = msg[0];
            result[1] = msg[1];
            result[2] = elapsedStr;
            return result;
        }
        if(algoritmo == 2){
            src.Algoritmos.Receptor.CRC32MSB receiver = new src.Algoritmos.Receptor.CRC32MSB();
            long startTime = System.nanoTime();

            String msg = receiver.verificar(trama);

            long endTime = System.nanoTime();
            String elapsedStr = (endTime - startTime)+"";
            String status = "0";
            if(msg.isEmpty()) status = "2";
            result[0] = status;
            result[1] = "";
            result[2] = elapsedStr;
            return result;
        }
        if(algoritmo >= 3 && algoritmo <=5){
            src.Algoritmos.Receptor.Fletcher receiver = new src.Algoritmos.Receptor.Fletcher();
            int tipo = (int)Math.pow(2, algoritmo);

            long startTime = System.nanoTime();
            String msg = receiver.verificar(trama,tipo);

            long endTime = System.nanoTime();
            String elapsedStr = (endTime - startTime)+"";

            String status = "0";
            if(msg.isEmpty()) status = "2";
            result[0] = status;
            result[1] = "";
            result[2] = elapsedStr;
            return result;
        } else{
            result[0] = "2";
            result[1] = "";
            result[2] = "";
            return result;
        }

    }
    public static String decode(String trama){
        String msg = "";
        for(int i = 8; i <= trama.length(); i+=8){
            int value = Integer.parseInt(trama.substring(i-8, i), 2);
            char c = (char)value;
            msg+=c;
        }
        return msg;
    }

    public static void main(String[] args) {
        int puerto = 5000;

        try (ServerSocket server = new ServerSocket(puerto)) {
            System.out.println("Servidor escuchando en el puerto " + puerto + "...");

            while (true) {
                Socket socket = server.accept();
                System.out.println("Cliente conectado desde " + socket.getInetAddress());

                try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)  // para responder
                ) {
                    String trama;
                    while ((trama = in.readLine()) != null) {
                        int algoritmo = Integer.parseInt(trama.substring(0, 8), 2);
                        trama = trama.substring(8);
                        System.out.println("Header "+ algoritmo);
                        System.out.println("Trama recibida: " + trama);
                        
                        String[] rslt = receive(trama, algoritmo);
                        if(rslt[0].equals("2")){
                            out.println("ERROR,"+rslt[2]);
                            System.out.println("Respuesta enviada: ERROR");
                        } else{
                            String constructed = decode(rslt[1]);
                            System.out.println("Decoded message = \'"+constructed+"\'");
                            if(rslt[0]=="1"){
                                out.println("FIXED,"+rslt[2]);
                                System.out.println("Respuesta enviada: FIXED");
                            } else{
                                out.println("OK,"+rslt[2]);
                                System.out.println("Respuesta enviada: OK");
                            }
                           
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error en la comunicación con el cliente: " + e.getMessage());
                }

                socket.close();
                System.out.println("Cliente desconectado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
