import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Utils {

	public static boolean checkFile(File file, String checksum) throws NoSuchAlgorithmException, IOException {

		String e = checksum(file.getPath());

		System.out.println(e);

		if(e.equals(checksum)){
			return true;
		}

		return false;
	}


	public static String checksum(String path) throws IOException{
		FileInputStream fin = new FileInputStream(path);
		Checksum cs = new CRC32();
		for (int b = fin.read(); b != -1; b = fin.read()) {
			cs.update(b);
		}

		System.out.println(cs.getValue());

		fin.close();

		return Long.valueOf(cs.getValue()).toString();
	}

	/*
	public static String checksum(String filepath, MessageDigest md) throws IOException {

		// file hashing with DigestInputStream
		try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
			while (dis.read() != -1) ; //empty loop to clear the data
			md = dis.getMessageDigest();
		}

		// bytes to hex
		StringBuilder result = new StringBuilder();
		for (byte b : md.digest()) {
			result.append(String.format("%02x", b));
		}
		return result.toString();

	}*/
}
