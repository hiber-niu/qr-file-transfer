package FileEncoder;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/*
 * A4 纸大小为595*842（像素）。
 */

public class FileEncoder {
	// 每一个二维码编码2000个byte。
	static int Chunk_Size = 2000;
	// 每一页最多容纳的二维码数目。
	static int QR_PAGE_DENSE = 12;  
	
	// If size is too small, generated qrcode size will be changed by zxing library.
	//So set size a little larger than needed.
	static int QR_SIZE = 180;
	
	// 将文件按Chunk_Size切割，并返回字节列表。
	public static List<byte[]> FileSplit(String fname){
		File ifile = new File(fname); 
		FileInputStream fis;
		
		List<byte[]> chunklist = new ArrayList<byte[]>();
		
		int fileSize = (int) ifile.length();
		int nChunks = 0, read = 0, readLength = Chunk_Size;
		byte[] byteChunk;
		try {
			fis = new FileInputStream(ifile);
			while (fileSize > 0) {
				if (fileSize <= Chunk_Size) {
					readLength = fileSize;
				}
				byteChunk = new byte[readLength];
				read = fis.read(byteChunk, 0, readLength);
	
				fileSize -= read;
				assert(read==byteChunk.length);
				nChunks++;
				
				chunklist.add(byteChunk);
				byteChunk = null;
				
			}
			fis.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Split file into " + nChunks + " chunks.");
		return chunklist;
	}
	
	public static void main(String[] args) {
		String fname = "d:/workspace/qr-file-transfer/test/data.rar";
		List<byte[]> chunks = FileSplit(fname);
		
		final Base64.Encoder encoder = Base64.getEncoder();
		
		Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

		// Now with zxing version 3.2.1 you could change border size (white
		// border size to just 1)
		hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		
		
		int size = QR_SIZE; // qr code size.(in pixel)
		
		// 每页35个二维码。
		int pages = chunks.size()/QR_PAGE_DENSE+1;
		PDDocument doc = new PDDocument();
		PDFont font = PDType1Font.HELVETICA_BOLD;
		float fontSize = 12.0f;
		String file = "test/data.pdf"; // store qr codes to pdf file.
		
		
		for(int p=1; p<=pages; p++) {
			// begin to write img to pdf file.
			String message = String.format("Page %d of %d", p, pages);
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);
			PDRectangle pageSize = page.getMediaBox();
			try {
				float stringWidth = font.getStringWidth( message )*fontSize/1000f;
				float textX = (pageSize.getWidth() - stringWidth)/2f;
				float textY = 15;

				// create a page with the message
				PDPageContentStream contents = new PDPageContentStream(doc, page);
				contents.beginText();
				contents.newLineAtOffset(textX, textY);
				contents.setFont(font, fontSize);
				contents.showText(message);
				contents.endText();

				if((p-1)*QR_PAGE_DENSE < chunks.size()) {
					for(int k = 0; k < QR_PAGE_DENSE; k++){
						int index = (p-1)*QR_PAGE_DENSE+k;

						// generate qr code image.
						if(index < chunks.size()) {
							String data = encoder.encodeToString(chunks.get(index));
							// 在前四位添加二维码序号编码。
							data = String.format("%04d", index) + data;

							QRCodeWriter qrCodeWriter = new QRCodeWriter();
							BitMatrix byteMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hintMap);
							int CrunchifyWidth = byteMatrix.getWidth();
							System.out.println(CrunchifyWidth);
							BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
							image.createGraphics();

							Graphics2D graphics = (Graphics2D) image.getGraphics();
							graphics.setColor(Color.WHITE);
							graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
							graphics.setColor(Color.BLACK);

							for (int i = 0; i < CrunchifyWidth; i++) {
								for (int j = 0; j < CrunchifyWidth; j++) {
									if (byteMatrix.get(i, j)) {
										graphics.fillRect(i, j, 1, 1);
									}
								}
							}
							
							// store qr code to pdf file.
							PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
							PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true);

							float scale = 1f;
							
							// qrcode x y coordinate.
							float x_coor = k%3*size+(k%3+1)*15;
							float y_coor = pageSize.getHeight()-((k/3+1)*(size+18));
						
							
							System.out.println(pdImage.getWidth());
							System.out.println(pdImage.getHeight());
							contentStream.drawImage(pdImage, x_coor, y_coor, pdImage.getWidth()*scale, pdImage.getHeight()*scale);
							contentStream.close();
							
							contents.saveGraphicsState();
							contents.close();

							doc.save(file);
							
						} else {
							break;
						}
					}
				} 			
			} catch (WriterException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("\n\nYou have successfully created QR Code.");
		}
	}
}
		
		

		
	
