// CODIGO PLACEHOLDER PARA SIMULAR EL SERVIDOR

import java.io.*;
import java.net.*;
import java.util.Random;

public class capas_receptor {
    public static void main(String[] args) {
        int puerto = 5000;
        Random rand = new Random();

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
                        System.out.println("Trama recibida: " + trama);

                        // Respuesta aleatoria
                        String respuesta = rand.nextBoolean() ? "OK" : "ERROR";
                        out.println(respuesta);
                        System.out.println("Respuesta enviada: " + respuesta);
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
