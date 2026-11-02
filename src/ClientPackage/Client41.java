package ClientPackage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client41 {

    private static final String HOTE_SERVEUR = "localhost";
    private static final int PORT_SERVEUR = 1234;
    private static final int TAILLE_BUFFER = 1024;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress adresseServeur = InetAddress.getByName(HOTE_SERVEUR);

            System.out.print("Nom d'utilisateur : ");
            String nom = scanner.nextLine().trim();

            // Thread de réception : écoute en continu les messages du serveur
            Thread reception = new Thread(() -> {
                byte[] buffer = new byte[TAILLE_BUFFER];
                while (!socket.isClosed()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                        System.out.println("\n" + message);
                    } catch (Exception e) {
                        if (!socket.isClosed()) {
                            System.err.println("Erreur réception : " + e.getMessage());
                        }
                        break;
                    }
                }
            });
            reception.setDaemon(true);
            reception.start();

            // Premier message : permet au serveur d'enregistrer ce client
            envoyer(socket, adresseServeur, "[" + nom + "] a rejoint le chat");

            System.out.println("Connecté. Tapez vos messages ('quit' pour quitter).");

            // Thread principal : envoi des messages saisis
            while (true) {
                String ligne = scanner.nextLine();
                if (ligne.equalsIgnoreCase("quit")) {
                    envoyer(socket, adresseServeur, "[" + nom + "] a quitté le chat");
                    break;
                }
                envoyer(socket, adresseServeur, "[" + nom + "] : " + ligne);
            }

        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void envoyer(DatagramSocket socket, InetAddress adresse, String message) throws Exception {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(data, data.length, adresse, PORT_SERVEUR);
        socket.send(packet);
    }
}