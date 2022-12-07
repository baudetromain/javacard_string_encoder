package project;

import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.APDU;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;

public class Encrypter extends Applet
{

	/** FINAL FIELDS */

	private final byte CLA_APPLET = 0x01;
	private final byte OP_PIN_CODE = 0x00;
	private final byte OP_ENCRYPT = 0x01;

	private final byte WRONG_PIN = 0x00;
	private final byte RIGHT_PIN = 0x01;

	private final byte NOT_UNLOCKED = 0x10;
	

	private final static byte PIN_CODE = 0x01;

	/** INSTANCE FIELDS AND METHODS */

	private boolean card_unlocked;
	private OwnerPIN pin;
	
	public Encrypter()
	{
		// The card status is locked while the user hasn't sent the right PIN code
		this.card_unlocked = false;
		this.pin = new OwnerPIN((byte) 5, (byte) 1);
		this.pin.update(new byte[]{0x37}, (short) 0, (byte) 1);
	}

	public static void install(byte[] buffer, short offset, byte length) 
	{
		// GP-compliant JavaCard applet registration
		new Encrypter().register();
	}

	public void process(APDU apdu)
	{
		// Case of selecting APDU ; in this case, it is not our job to handle it
		if (selectingApplet())
		{
			return;
		}

		// We get the sent APDU and store it in a variable
		byte[] buffer = apdu.getBuffer();

		// We have a specific CLA code ; if the sent CLA code is not ours, we propagate an exception
		// it is causing issues, so I'm commenting it, but it could be nice in the future
		// if(buffer[ISO7816.OFFSET_CLA] != CLA_APPLET)
		// {
		// 	ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		// }

		// We decide what to do depending on the instruction we receive
		switch (buffer[ISO7816.OFFSET_INS])
		{
			// A message code of 0x01 means the user submits a PIN code
			case OP_PIN_CODE:

				if (! this.card_unlocked)
				{
					short dataLength = apdu.setIncomingAndReceive();
					// we need to compare the PIN code sent by the user to our hard-coded PIN code
					// old comparison
					//byte comparison = Util.arrayCompare(PIN_CODE, (byte) 0, buffer, ISO7816.OFFSET_CDATA, (short) 4);

					// if the user-provided PIN code is right, we send back a 0x01 code and set the card to unlocked state
					if (this.pin.check(buffer, (short) ISO7816.OFFSET_CDATA, (byte) 1))
					{
						this.card_unlocked = true;
						return;
					}

					// else we send back a 0x02 code
					else
					{
						ISOException.throwIt(ISO7816.SW_WRONG_DATA);
					}
				}

				// if the card was already unlocked, sending the 0x01 operation code has no sense, so we treat this like an error
				else
				{
					ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
				}
				break;

			case OP_ENCRYPT:

				short dataLength = apdu.setIncomingAndReceive();

				if(! this.card_unlocked)
				{
					ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
				}
				
				return;

			default:
				// good practice: If you don't know the INStruction, say so:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}
