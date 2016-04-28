package FileDecoder;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;


public class ConvertPDFPagesToImages {
	
	public static void main(String[] args) {
		try {
			String sourceDir = "D:/workspace/qr-file-transfer/test/data.pdf"; // Pdf files are read from this folder
			String destinationDir = "D:/workspace/qr-file-transfer/test/images/"; // converted images from pdf document are saved here

			File sourceFile = new File(sourceDir);
			File destinationFile = new File(destinationDir);
			if (!destinationFile.exists()) {
				destinationFile.mkdir();
				System.out.println("Folder Created -> "+ destinationFile.getAbsolutePath());
			}
			if (sourceFile.exists()) {
				System.out.println("Images copied to Folder: "+ destinationFile.getName());             
				PDDocument document = PDDocument.load(sourceFile);

				PDFRenderer pdfRenderer = new PDFRenderer(document);
				for (int page = 0; page < document.getNumberOfPages(); ++page)
				{ 
					BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 1200, ImageType.BINARY);
					File outputfile = new File(destinationDir +  String.format("%04d", page) +".png");
					System.out.println("Image Created -> "+ outputfile.getName());
					ImageIO.write(bim, "png", outputfile);  
				}
				System.out.println("Total files to be converted -> "+ document.getNumberOfPages());
				document.close();

				System.out.println("Converted Images are saved at -> "+ destinationFile.getAbsolutePath());
			} else {
				System.err.println(sourceFile.getName() +" File not exists");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}