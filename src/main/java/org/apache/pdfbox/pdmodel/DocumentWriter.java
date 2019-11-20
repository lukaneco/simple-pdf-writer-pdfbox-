package org.apache.pdfbox.pdmodel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.google.common.base.Predicates;

import net.miginfocom.swing.MigLayout;

public class DocumentWriter implements ActionListener {

	private static final Logger LOG = Logger.getLogger(DocumentWriter.class.toString());

	private static final String WRAP = "wrap";

	private JTextComponent tfFontSize, tfMargin, tfText, pfOwner, pfUser, tfFile = null;

	private AbstractButton btnColor, btnExecute, btnCopy = null;

	private ComboBoxModel<Object> pageSize = null;

	private Map<String, PDRectangle> pageSizeMap = null;

	private ComboBoxModel<PDFont> font = null;

	private Color color = null;

	private DocumentWriter() {
	}

	private void init(final Container container) {
		//
		final String wrap = String.format("span %1$s,%2$s", 2, WRAP);
		//
		add(container, new JLabel("Page Size"));
		add(container,
				new JComboBox<>(pageSize = new DefaultComboBoxModel<>(
						ArrayUtils.insert(0, (pageSizeMap = getPageSizeMap()).keySet().toArray(), (Object) null))),
				wrap);
		//
		add(container, new JLabel("Font size"));
		add(container, tfFontSize = new JTextField("12"), wrap);
		//
		add(container, new JLabel("Margin"));
		add(container, tfMargin = new JTextField("10"), wrap);
		//
		add(container, new JLabel("Font"));
		add(container,
				new JComboBox<>(font = new DefaultComboBoxModel<>(ArrayUtils.insert(0, getFonts(), (PDFont) null))),
				wrap);
		//
		add(container, new JLabel("Text"));
		add(container, new JScrollPane(tfText = new JTextArea(10, 100)), wrap);
		//
		add(container, new JLabel(""));
		add(container, btnColor = new JButton("Color"), wrap);
		//
		add(container, new JLabel("Owner Password"));
		add(container, pfOwner = new JPasswordField(), wrap);
		//
		add(container, new JLabel("User Password"));
		add(container, pfUser = new JPasswordField(), wrap);
		//
		add(container, new JLabel(""));
		add(container, btnExecute = new JButton("Execute"), wrap);
		//
		add(container, new JLabel("File"));
		add(container, tfFile = new JTextField());
		add(container, btnCopy = new JButton("Copy"), "wrap");
		tfFile.setEditable(false);
		//
		addActionListener(this, btnColor, btnExecute, btnCopy);
		//
		final int width = Math.max(250, (int) getWidth(tfText.getPreferredSize(), 250));
		setWidth(width - (int) btnCopy.getPreferredSize().getWidth(), tfFile);
		setWidth(width, tfFontSize, tfMargin, tfText, pfOwner, pfUser);
		//
	}

	private static void add(final Container container, final Component component) {
		if (container != null) {
			container.add(component);
		}
	}

	private static void add(final Container container, final Component component, final Object object) {
		if (container != null) {
			container.add(component, object);
		}
	}

	private static double getWidth(final Dimension2D instance, final double defaultValue) {
		return instance != null ? instance.getWidth() : defaultValue;
	}

	private static PDFont[] getFonts() {
		return getFonts(PDType1Font.class.getDeclaredFields());
	}

	private static PDFont[] getFonts(final Field[] fs) {
		//
		List<PDFont> result = null;
		//
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
		return getPageSizeMap(PDRectangle.class.getDeclaredFields());
	}

	private static Map<String, PDRectangle> getPageSizeMap(final Field[] fs) {
		//
		Map<String, PDRectangle> result = null;
		//
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
				for (String text : ObjectUtils.defaultIfNull(StringUtils.split(getText(tfText), "\n"), new String[0])) {
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
				contentStream.setNonStrokingColor(
						testAndGet(Predicates.notNull(), color, getForeground(tfText), Color.BLACK));
				contentStream.setFont(font, fontSize);
				contentStream.newLineAtOffset(startX, startY);
				//
				for (final String line : lines) {
					contentStream.showText(line);
					contentStream.newLineAtOffset(0, -leading);
				} // for
					//
				contentStream.endText();
				contentStream.close();
				//
				final File file = new File("test.pdf");
				setText(tfFile, file.getAbsolutePath());
				//
				// https://pdfbox.apache.org/1.8/cookbook/encryption.html
				//
				document.protect(createProtectionPolicy(getText(pfOwner), getText(pfUser), new AccessPermission()));
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
		} else if (Objects.deepEquals(source, btnColor)) {
			//
			tfText.setForeground(color = JColorChooser.showDialog(null, "Font color", null));
			//
		}
		//
	}

	private static ProtectionPolicy createProtectionPolicy(final String ownerPassword, final String userPassword,
			final AccessPermission accessPermission) {
		//
		final StandardProtectionPolicy result = new StandardProtectionPolicy(ownerPassword, userPassword,
				accessPermission);
		result.setPreferAES(true);
		result.setEncryptionKeyLength(128);
		return result;
		//
	}

	private static void setText(final JTextComponent instance, final String text) {
		if (instance != null) {
			instance.setText(text);
		}
	}

	private static <T> T testAndGet(final Predicate<T> predicate, final T... items) {
		//
		T item = null;
		//
		for (int i = 0; items != null && i < items.length && predicate != null; i++) {
			if (predicate.test(item = items[i])) {
				return item;
			}
		} // for
			//
		return item;
		//
	}

	private static Color getForeground(final Component instance) {
		//
		try {
			final Field field = Component.class.getDeclaredField("foreground");
			if (field != null && !field.isAccessible()) {
				field.setAccessible(true);
			}
			return cast(Color.class, field != null && instance != null ? field.get(instance) : null);
		} catch (final NoSuchFieldException | IllegalAccessException e) {
			LOG.severe(e.getMessage());
		}
		//
		return null;
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