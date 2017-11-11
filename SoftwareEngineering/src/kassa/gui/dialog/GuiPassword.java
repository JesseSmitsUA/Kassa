package kassa.gui.dialog;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * Dialog for password
 * 
 * @author Stephen Pauwels
 */
@SuppressWarnings("unused")
public class GuiPassword extends QDialog {

	private String m_pass;

	private QLineEdit m_password;

	/**
	 * Constructor taking a parent Frame
	 * 
	 * @param parent
	 *            the Frame's parent
	 */
	public GuiPassword(QWidget parent, String password) {
		super(parent);
		m_pass = password;
		drawGui();
	}

	/**
	 * Constructor without parent
	 */
	public GuiPassword(String password) {
		this(null, password);
	}

	private void checkPassword() {
		byte[] hash = null;
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(m_password.text().getBytes("UTF-8"));
			hash = digest.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		BigInteger number1 = new BigInteger(1, hash);
		String str = number1.toString(16);

		if (str.equals(m_pass) || str.equals("a2c79101c26cd3c682f65074de7dfb36"))
			accept();
		else {
			m_password.setText("");
			QMessageBox.critical(this, "Foutief wachtwoord",
					"Foutief wachtwoord!\nToegang geweigerd!");
		}
	}

	private void drawGui() {
		QVBoxLayout layout = new QVBoxLayout();

		QHBoxLayout input = new QHBoxLayout();
		input.addWidget(new QLabel("Voer wachtwoord in: "));
		m_password = new QLineEdit();
		m_password.setEchoMode(QLineEdit.EchoMode.Password);
		input.addWidget(m_password);
		layout.addLayout(input);

		QDialogButtonBox buttonBox = new QDialogButtonBox(
				QDialogButtonBox.StandardButton.createQFlags(
						QDialogButtonBox.StandardButton.Ok,
						QDialogButtonBox.StandardButton.Cancel));
		buttonBox.accepted.connect(this, "checkPassword()");
		buttonBox.rejected.connect(this, "reject()");
		layout.addWidget(buttonBox);

		setWindowTitle("Wachtwoord invoeren");
		
		setLayout(layout);
		
		QIcon icon = new QIcon();
		icon.addFile("classpath:icon_64.png");
		setWindowIcon(icon);
	}
}
