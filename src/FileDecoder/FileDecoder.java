package FileDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
 
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;


public class FileDecoder {
	public Result[] readQRCode(String filePath, String charset, Map<DecodeHintType, Object> hintMap)
			throws FileNotFoundException, IOException, NotFoundException {
		BinaryBitmap binaryBitmap = new BinaryBitmap(
				new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
        Result[] qrCodeResults = new QRCodeMultiReader().decodeMultiple(binaryBitmap, hintMap);
		//Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
//		return qrCodeResult.getRawBytes();
		return qrCodeResults;
	}
	
	public static void main(String[] args) {  
		Map<DecodeHintType, Object> hintMap = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
		
		hintMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		hintMap.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
		hintMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
		
		String charset = "UTF-8"; // 测试时使用"ISO-8859-1"会导致部分二维码无法解析。
		final Base64.Decoder decoder = Base64.getDecoder();
		String imgDir = "d:/workspace/qr-file-transfer/test/images/";

		FileDecoder handler = new FileDecoder();
		
		File folder = new File(imgDir);
		File[] listOfFiles = folder.listFiles();
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("data.rar");
			List<String> qrdata = new ArrayList<String>();
		
		
			// 遍历二维码的图像目录，读取二维码文件。
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					String imgPath = listOfFiles[i].getAbsoluteFile().toString();
					System.out.println(imgPath);
					// 用base64解码并依次写入文件。
					Result[] results;
					try {
						int count = 0;
						results = handler.readQRCode(imgPath, charset, hintMap);
						for(Result r: results) {
							qrdata.add(r.getText());
							count++;
						}
						System.out.println(count);
					} catch (NotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (listOfFiles[i].isDirectory()) {
					System.out.println("Directory " + listOfFiles[i].getName());
				}
			}
		
			
			// 将二维码数据按照序列号进行重排。
			Collections.sort(qrdata);
			FileWriter writer = new FileWriter("test/out.txt");
            for(String text: qrdata){
                System.out.println("=========");
                System.out.println(text); 
                writer.write(text);
                writer.write("\n");
                // 前四位为数据序列号。
                String data = text.substring(4);
                fos.write(decoder.decode(data));
                fos.flush();
            }
			fos.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		  
		System.out.println("QR codes have been transformed successful!");
    }  
	// TODO add file type recognition.
}

