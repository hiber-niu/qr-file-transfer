package FileEncoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/*
 * A4 纸大小为595*842（像素）。每页保存5*7个二维码。
 */

public class PdfGenerator {
	
	public static void Img2Pdf(BufferedImage image, int imgNum, int index, int size) throws IOException, TransformerException
	{
		// 每页35个二维码。
		int pages = imgNum/35;
		
		String file = "data.pdf";
		String message = String.format("Page %d of %d", index, pages);

		PDDocument doc = new PDDocument();
		PDFont font = PDType1Font.HELVETICA_BOLD;
		float fontSize = 12.0f;
		try
		{
			PDPage page = new PDPage();
			doc.addPage(page);

			PDRectangle pageSize = page.getMediaBox();
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
			
			PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
			PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true);

			float scale = 1f;
			contentStream.drawImage(pdImage, 20, 20, pdImage.getWidth()*scale, pdImage.getHeight()*scale);
			contentStream.close();
			
			contents.saveGraphicsState();
			contents.close();

			doc.save(file);
		} finally {
			doc.close();
		}
	}
}

