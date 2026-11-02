package ServeurPackage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Serveur41 {

    private static final int PORT = 1234;
    private static final int TAILLE_BUFFER = 1024;

    public static void main(String[] args) {
        // Liste des clients "connectés" (ceux qui ont déjà envoyé un message)
        Set<SocketAddress> clients = new CopyOnWriteArraySet<>();

        try (DatagramSocket socket = new DatagramSocket(null)) {
            socket.bind(new InetSocketAddress(PORT));
            System.out.println("Serveur UDP en écoute sur le port " + PORT + "...");

            byte[] buffer = new byte[TAILLE_BUFFER];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                SocketAddress expediteur = packet.getSocketAddress();
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                // Enregistrement de l'expéditeur s'il n'existe pas déjà
                if (clients.add(expediteur)) {
                    System.out.println("Nouveau client enregistré : " + expediteur);
                }

                System.out.println("Reçu de " + expediteur + " -> " + message);

                // Diffusion à tous les autres clients (sauf l'expéditeur)
                byte[] data = message.getBytes(StandardCharsets.UTF_8);
                for (SocketAddress client : clients) {
                    if (!client.equals(expediteur)) {
                        DatagramPacket envoi = new DatagramPacket(data, data.length, client);
                        socket.send(envoi);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
