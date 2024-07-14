package opencart.cryptography;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PropertiesCryptoUtility {
	
	 private static final String ALGORITHM = "AES";
	    private static final String TRANSFORMATION = "AES";

	    public static void main(String[] args) throws Exception {
	        String key = "secret_password_"; // you can set your own key which should be securely stored and retrieved
	        String inputFilePath = System.getProperty("user.dir")+"/config.properties";
	        String encryptedFilePath = "config_encrypted.properties";
	        String decryptedFilePath = "config_decrypted.properties";
	        
	        // Encrypt the properties file
	        encryptFile(key, inputFilePath, encryptedFilePath);
	        System.out.println("Encryption completed!");
	        
	        //trying to read an encrypted file
	        System.out.println("reading the encrypted file:");
	        readFile(encryptedFilePath);

	        // Decrypt the properties file
	        decryptFile(key, encryptedFilePath, decryptedFilePath);
	        System.out.println("Decryption completed!");
	        
	        //trying to read a decrypted file
	        System.out.println("reading the decrypted file:");
	        readFile(decryptedFilePath);
	    }

	    public static void encryptFile(String key, String inputFilePath, String outputFilePath) throws Exception {
	        doCrypto(Cipher.ENCRYPT_MODE, key, inputFilePath, outputFilePath);
	    }

	    public static void decryptFile(String key, String inputFilePath, String outputFilePath) throws Exception {
	        doCrypto(Cipher.DECRYPT_MODE, key, inputFilePath, outputFilePath);
	    }

	    private static void doCrypto(int cipherMode, String key, String inputFilePath, String outputFilePath) throws Exception {
	        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
	        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
	        cipher.init(cipherMode, secretKey);

	        FileInputStream inputStream = new FileInputStream(inputFilePath);
	        byte[] inputBytes = new byte[(int) new File(inputFilePath).length()];
	        inputStream.read(inputBytes);

	        byte[] outputBytes = cipher.doFinal(inputBytes);

	        FileOutputStream outputStream = new FileOutputStream(outputFilePath);
	        outputStream.write(outputBytes);

	        inputStream.close();
	        outputStream.close();
	    }

	    private static Properties loadProperties(String filePath) throws IOException {
	        Properties properties = new Properties();
	        FileInputStream inputStream = new FileInputStream(filePath);
	        properties.load(inputStream);
	        inputStream.close();
	        return properties;
	    }

	    private static void saveProperties(Properties properties, String filePath) throws IOException {
	        FileOutputStream outputStream = new FileOutputStream(filePath);
	        properties.store(outputStream, null);
	        outputStream.close();
	    }
	    
	    public static void readFile(String inputFilePath) {
	        Properties prop = new Properties();
	        InputStream input = null;

	        try {
	            input = new FileInputStream(inputFilePath);

	            // Load the properties file
	            prop.load(input);
	            // Get the properties and print them
	            String url = prop.getProperty("Login_URI");
	            System.out.println("Login_URI: " + url);
	            
	        } catch (IOException ex) {
	            ex.printStackTrace();
	            System.out.println("cannot read data from encrypted file!");
	        } finally {
	            if (input != null) {
	                try {
	                    input.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }

}
