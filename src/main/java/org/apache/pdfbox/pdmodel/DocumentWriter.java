package org.apache.pdfbox.pdmodel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import net.miginfocom.swing.MigLayout;

public class DocumentWriter implements ActionListener {

	private JTextComponent tfText, pfOwner, pfUser = null;

	private AbstractButton btnExecute = null;

	private DocumentWriter() {
	}

	private void init(final Container container) {
		//
		final String wrap = "wrap";
		//
		container.add(new JLabel("Text"));
		container.add(tfText = new JTextField(), wrap);
		//
		container.add(new JLabel("Owner Password"));
		container.add(pfOwner = new JPasswordField(), wrap);
		//
		container.add(new JLabel("User Password"));
		container.add(pfUser = new JPasswordField(), wrap);
		//
		container.add(new JLabel(""));
		container.add(btnExecute = new JButton("Execute"), wrap);
		btnExecute.addActionListener(this);
		//
		setWidth(250, tfText, pfOwner, pfUser);
		//
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		//
		if (evt != null && Objects.deepEquals(evt.getSource(), btnExecute)) {
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
				contentStream.showText(getText(tfText));
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
				final StandardProtectionPolicy spp = new StandardProtectionPolicy(getText(pfOwner), getText(pfUser),
						ap);
				System.out.println(spp.getOwnerPassword());
				System.out.println(spp.getUserPassword());
				spp.setPreferAES(true);
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
		} // if
			//
	}

	private static void setWidth(final int width, final Component... cs) {
		//
		Component c = null;
		Dimension preferredSize = null;
		//
		for (int i = 0; cs != null && i < cs.length; i++) {
			//
			if ((c = cs[i]) == null || (preferredSize = c.getPreferredSize()) == null) {
				continue;
			} // skip null
				//
			c.setPreferredSize(new Dimension(width, (int) preferredSize.getHeight()));
			//
		} // for
			//
	}

	private static String getText(final JTextComponent instance) {
		return instance != null ? instance.getText() : null;
	}

	public static void main(final String[] args) {
		//
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new MigLayout());
		frame.setTitle("org.apache.pdfbox.pdmodel.PDDocument Writer");
		//
		final DocumentWriter instance = new DocumentWriter();
		instance.init(frame.getContentPane());
		frame.pack();
		frame.setVisible(true);
		//
	}

}
