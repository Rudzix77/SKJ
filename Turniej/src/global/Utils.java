package global;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Utils {

	private static final String ALGORITHM = "AES";

	public static String encrypt(String key, String text) throws Exception {
		byte[] decodedKey = Base64.getDecoder().decode(key);
		SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));
	}

	public static String decrypt(String key, String text) throws Exception {

		byte[] decodedKey = Base64.getDecoder().decode(key);
		SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		return new String(cipher.doFinal(Base64.getDecoder().decode(text)));
	}

	public static String getKey() throws Exception{
		KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
		keyGen.init(256);

		return Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
	}

}
