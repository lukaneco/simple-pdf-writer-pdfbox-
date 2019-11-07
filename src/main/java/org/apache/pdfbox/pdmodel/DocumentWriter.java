package org.apache.pdfbox.pdmodel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import net.miginfocom.swing.MigLayout;

public class DocumentWriter implements ActionListener {

	private static final Logger LOG = Logger.getLogger(DocumentWriter.class.toString());

	private JTextComponent tfFontSize, tfMargin, tfText, pfOwner, pfUser, tfFile = null;

	private AbstractButton btnExecute, btnCopy = null;

	private ComboBoxModel<Object> pageSize = null;

	private Map<String, PDRectangle> pageSizeMap = null;

	private ComboBoxModel<PDFont> font = null;

	private DocumentWriter() {
	}

	private void init(final Container container) {
		//
		final String wrap = String.format("span %1$s,%2$s", 2, "wrap");
		//
		container.add(new JLabel("Page Size"));
		container.add(
				new JComboBox<>(pageSize = new DefaultComboBoxModel<>(
						ArrayUtils.insert(0, (pageSizeMap = getPageSizeMap()).keySet().toArray(), (Object) null))),
				wrap);
		//
		container.add(new JLabel("Font size"));
		container.add(tfFontSize = new JTextField("12"), wrap);
		//
		container.add(new JLabel("Margin"));
		container.add(tfMargin = new JTextField("10"), wrap);
		//
		container.add(new JLabel("Font"));
		container.add(
				new JComboBox<>(font = new DefaultComboBoxModel<>(ArrayUtils.insert(0, getFonts(), (PDFont) null))),
				wrap);
		//
		container.add(new JLabel("Text"));
		container.add(new JScrollPane(tfText = new JTextArea(10, 100)), wrap);
		//
		container.add(new JLabel("Owner Password"));
		container.add(pfOwner = new JPasswordField(), wrap);
		//
		container.add(new JLabel("User Password"));
		container.add(pfUser = new JPasswordField(), wrap);
		//
		container.add(new JLabel(""));
		container.add(btnExecute = new JButton("Execute"), wrap);
		//
		container.add(new JLabel("File"));
		container.add(tfFile = new JTextField());
		container.add(btnCopy = new JButton("Copy"), "wrap");
		tfFile.setEditable(false);
		//
		addActionListener(this, btnExecute, btnCopy);
		//
		final int width = 250;
		setWidth(width - (int) btnCopy.getPreferredSize().getWidth(), tfFile);
		setWidth(width, tfFontSize, tfMargin, tfText, pfOwner, pfUser);
		//
	}

	private static PDFont[] getFonts() {
		//
		List<PDFont> result = null;
		//
		final Field[] fs = PDType1Font.class.getDeclaredFields();
		Field f = null;
		PDFont font = null;
		//
		for (int i = 0; fs != null && i < fs.length; i++) {
			//
			if ((f = fs[i]) == null || !Modifier.isStatic(f.getModifiers())) {
				continue;
			} // skip null
				//
			if (!f.isAccessible()) {
				f.setAccessible(true);
			}
			//
			try {
				//
				if ((font = cast(PDFont.class, f.get(null))) == null) {
					continue;
				}
				//
				if (result == null) {
					result = new ArrayList<>();
				}
				result.add(font);
				//
			} catch (final IllegalAccessException e) {
				LOG.severe(e.getMessage());
			}
			//
		} // for
			//
		return result != null ? result.toArray(new PDFont[result.size()]) : null;
		//
	}

	private static Map<String, PDRectangle> getPageSizeMap() {
		//
		Map<String, PDRectangle> result = null;
		//
		final Field[] fs = PDRectangle.class.getDeclaredFields();
		Field f = null;
		PDRectangle pdRectangle = null;
		//
		for (int i = 0; fs != null && i < fs.length; i++) {
			//
			if ((f = fs[i]) == null || !Modifier.isStatic(f.getModifiers())) {
				continue;
			} // skip null
				//
			if (!f.isAccessible()) {
				f.setAccessible(true);
			}
			//
			try {
				//
				if ((pdRectangle = cast(PDRectangle.class, f.get(null))) == null) {
					continue;
				}
				//
				if (result == null) {
					result = new LinkedHashMap<>();
				}
				result.put(f.getName(), pdRectangle);
				//
			} catch (final IllegalAccessException e) {
				LOG.severe(e.getMessage());
			}
			//
		} // for
			//
		return result;
		//
	}

	private static <T> T cast(final Class<T> clz, final Object instance) {
		return clz != null && clz.isInstance(instance) ? clz.cast(instance) : null;
	}

	private static void addActionListener(final ActionListener actionListener, final AbstractButton... bs) {
		//
		AbstractButton b = null;
		//
		for (int i = 0; bs != null && i < bs.length; i++) {
			//
			if ((b = bs[i]) == null) {
				continue;
			} // skip null
				//
			b.addActionListener(actionListener);
			//
		} // for
			//
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		//
		final Object source = evt != null ? evt.getSource() : null;
		//
		if (Objects.deepEquals(source, btnExecute)) {
			//
			final PDFont font = cast(PDFont.class, getSelectedItem(this.font));
			if (font == null) {
				JOptionPane.showMessageDialog(null, "Please select a font");
				return;
			}
			//
			final Integer fontSize = valueOf(getText(tfFontSize));
			if (fontSize == null) {
				JOptionPane.showMessageDialog(null, "Please enter a vaild font size");
				return;
			}
			//
			final Integer margin = valueOf(getText(tfMargin));
			if (margin == null) {
				JOptionPane.showMessageDialog(null, "Please enter a vaild margin");
				return;
			}
			//
			final PDRectangle pageSize = cast(PDRectangle.class, get(pageSizeMap, getSelectedItem(this.pageSize)));
			final PDPage page = pageSize != null ? new PDPage(pageSize) : new PDPage();
			final PDDocument document = new PDDocument();
			//
			try (final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				//
				document.setVersion(1.7f);
				document.addPage(page);
				//
				final List<String> lines = new ArrayList<>();
				//
				final PDRectangle mediabox = page.getMediaBox();
				float width = mediabox.getWidth() - 2 * margin.intValue();
				//
				for (String text : StringUtils.split(getText(tfText), "\n")) {
					int lastSpace = -1;
					while (text.length() > 0) {
						int spaceIndex = text.indexOf(' ', lastSpace + 1);
						if (spaceIndex < 0)
							spaceIndex = text.length();
						String subString = text.substring(0, spaceIndex);
						float size = fontSize * font.getStringWidth(subString) / 1000;
						if (size > width) {
							if (lastSpace < 0) {
								lastSpace = spaceIndex;
							}
							subString = text.substring(0, lastSpace);
							lines.add(subString);
							text = text.substring(lastSpace).trim();
							lastSpace = -1;
						} else if (spaceIndex == text.length()) {
							lines.add(text);
							text = "";
						} else {
							lastSpace = spaceIndex;
						}
					}
				}
				//
				float startX = mediabox.getLowerLeftX() + margin.intValue();
				float startY = mediabox.getUpperRightY() - margin.intValue();
				float leading = 1.5f * fontSize;
				//
				contentStream.beginText();
				contentStream.setFont(font, fontSize);
				contentStream.newLineAtOffset(startX, startY);
				//
				for (final String line : lines) {
					contentStream.showText(line);
					contentStream.newLineAtOffset(0, -leading);
				}
				contentStream.endText();
				contentStream.close();
				//
				final File file = new File("test.pdf");
				tfFile.setText(file.getAbsolutePath());
				//
				// https://pdfbox.apache.org/1.8/cookbook/encryption.html
				//
				final AccessPermission ap = new AccessPermission();
				//
				final StandardProtectionPolicy spp = new StandardProtectionPolicy(getText(pfOwner), getText(pfUser),
						ap);
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
		} else if (Objects.deepEquals(source, btnCopy)) {
			//
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
			final Clipboard clipboard = toolkit != null ? toolkit.getSystemClipboard() : null;
			if (clipboard != null) {
				clipboard.setContents(new StringSelection(getText(tfFile)), null);
			}
			//
		} // if
			//
	}

	private static Integer valueOf(final String instance) {
		try {
			return instance != null ? Integer.valueOf(instance) : null;
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private static <V> V get(final Map<?, V> instance, final Object key) {
		return instance != null ? instance.get(key) : null;
	}

	private static Object getSelectedItem(final ComboBoxModel<?> instance) {
		return instance != null ? instance.getSelectedItem() : null;
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