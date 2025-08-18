package src;
// CODIGO PLACEHOLDER PARA SIMULAR EL SERVIDOR

import java.io.*;
import java.net.*;
import java.util.Random;

import src.Algoritmos.Receptor;

public class capas_receptor {
    public static String receive(String trama, int algoritmo){
        if(algoritmo == 1){
            src.Algoritmos.Receptor.Hamming receiver = new src.Algoritmos.Receptor.Hamming();
            String msg = receiver.recibir(trama);
            return msg;
        }
        if(algoritmo == 2){
            src.Algoritmos.Receptor.CRC32MSB receiver = new src.Algoritmos.Receptor.CRC32MSB();
            String msg = receiver.verificar(trama);
            return msg;
        }
        if(algoritmo >= 3 && algoritmo <=5){
            src.Algoritmos.Receptor.Fletcher receiver = new src.Algoritmos.Receptor.Fletcher();
            int tipo = (int)Math.pow(2, algoritmo);
            System.out.println(tipo);
            String msg = receiver.verificar(trama,tipo);
            return msg;
        }

        return "";
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

                        String og_msg = receive(trama, algoritmo);
                        String constructed = decode(og_msg);
                        if(!constructed.isEmpty()){
                            System.out.println("Constructed = "+constructed);
                            out.println("OK");
                            System.out.println("Respuesta enviada: OK");
                        } else{
                            out.println("ERROR");
                            System.out.println("Respuesta enviada: ERROR");
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
