package org.apache.pdfbox.pdmodel;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class Main {

	public static void main(final String[] args) throws IOException {
		//
		final PDPage page = new PDPage();
		final PDDocument document = new PDDocument();
		//
		try (final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
			//
			document.setVersion(1.7f);
			document.addPage(page);
			//
			contentStream.beginText();
			contentStream.setFont(PDType1Font.COURIER, 12);
			contentStream.newLineAtOffset(10, page.getMediaBox().getHeight() - 20);
			contentStream.showText("hello world");
			contentStream.endText();
			//
			contentStream.close();
			//
			final File file = new File("test.pdf");
			System.out.println("file=" + file.getAbsolutePath());
			//
			// https://pdfbox.apache.org/1.8/cookbook/encryption.html
			//
			final AccessPermission ap = new AccessPermission();
			//
			final StandardProtectionPolicy spp = new StandardProtectionPolicy("12345", "23456", ap);
			spp.setEncryptionKeyLength(128);
			document.protect(spp);
			//
			document.save(file);
			//
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(document);
		}
		//
	}

}