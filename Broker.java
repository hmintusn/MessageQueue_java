import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;

public class Broker {
    // Stream: [1 byte of length, 1 byte of type, the rest of byte of message]
    private Integer brokerPort = 10000;
    public byte[] readFromStream(BufferedInputStream bis){
        try{
            final int length = bis.read(); 
            if (length <= 0) { // validate data.length
                return new byte[0];
            }
            var message = new byte[length];
            bis.read(message, 0, length);
            return message;
        }catch(Exception e){
            System.out.println("Something wrong when reading from stream");
            e.printStackTrace();
            return new byte[0];
        }
    }    

    public void writeToStream(BufferedOutputStream bos, byte[] message){
        try{
            System.out.println("Send");
            bos.write((byte) message.length);          // length
            bos.write(message);                        // message 
            bos.flush();    

        }catch(Exception e){
            System.out.println("Something wrong when writing to stream");
            e.printStackTrace();
        }
    }  

    public void startBrokerServer(){
        try{
            // Socket + bind + Listen
            final ServerSocket server = new ServerSocket(brokerPort, 123, 
                InetAddress.getByName("127.0.0.1"));
            System.out.println("Server waiting client...");
            
            // Multiplex: Connection start and close connection sequetially 
            //          -> make 1 port can serve multiple connection
            while(true){
                Socket socket = server.accept(); // Accept connection
                var bis = new BufferedInputStream(socket.getInputStream());
                var bos = new BufferedOutputStream(socket.getOutputStream());

                // Read 
                var message = readFromStream(bis);
                if (message.length != 0) {
                    // Write back
                    Optional<Message> parsedMessage = parseBrokerMessage(message);
                    if(parsedMessage.isPresent()){
                        byte[] response = processBrokerMessage(parsedMessage.get());
                        writeToStream(bos, response); 
                    }
                }

                // Close the buffered stream to allow new connection  
                bis.close();
                bos.close();
            }
        }catch(Exception e){
            System.out.println("Something wrong when starting server");
        }
        
    }
    
    public Optional<Message> parseBrokerMessage(byte[] message){
        switch (MessageType.fromByte(message[0])) {
            case ECHO:
                byte[] payload = Arrays.copyOfRange(message, 1, message.length);
                return Optional.of(new Message(MessageType.ECHO, payload));
                
                default:
                    return Optional.empty();
        }
    }
    
    public byte[] processBrokerMessage(Message message){
        switch (message.getType()) {
            case ECHO:
                byte[] payload = new byte[message.getPayload().length + 1];
                byte[] result = new byte[payload.length + 1];
                result[0] = message.getType().getCode(); // type
                System.arraycopy(payload, 0, result, 1, payload.length); 
                return result;
        
            default:
                return new byte[0];
        }
    }
}
