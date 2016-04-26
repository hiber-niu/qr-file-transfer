package FileDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import javax.imageio.ImageIO;
 
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;


public class FileDecoder {
	public String readQRCode(String filePath, String charset, Map<DecodeHintType, Object> hintMap)
			throws FileNotFoundException, IOException, NotFoundException {
		BinaryBitmap binaryBitmap = new BinaryBitmap(
				new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
		Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
//		return qrCodeResult.getRawBytes();
		return qrCodeResult.getText();
	}
	
	public static void main(String[] args) {  
		Map<DecodeHintType, Object> hintMap = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
		
		hintMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		hintMap.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
		hintMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
		
		String charset = "UTF-8"; // or "ISO-8859-1"
		final Base64.Decoder decoder = Base64.getDecoder();
		String imgDir = "d:/workspace/qrcode/images";

		FileDecoder handler = new FileDecoder();
		
		File folder = new File(imgDir);
		File[] listOfFiles = folder.listFiles();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("data.rar");
		
			// 遍历二维码的图像目录，读取二维码文件。
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					String imgPath = listOfFiles[i].getAbsoluteFile().toString();
					System.out.println(imgPath);
					try {
						// 用base64解码并依次写入文件。
						String result = handler.readQRCode(imgPath, charset, hintMap);
						
						System.out.println("=========");
						System.out.println(result);
						
						fos.write(decoder.decode(result));
						fos.flush();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (listOfFiles[i].isDirectory()) {
					System.out.println("Directory " + listOfFiles[i].getName());
				}
			}
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		  
		System.out.println("QR codes have been transformed successful!");
    }  
	// TODO add file type recognition.
}

