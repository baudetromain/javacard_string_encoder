package project;

import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.APDU;
import javacard.framework.Util;
import javacard.framework.PIN;
import javacard.framework.OwnerPIN;
import javacard.security.KeyPair;

public class Encrypter extends Applet
{

	/** FINAL FIELDS */

	private final byte CLA_APPLET = 0x25;

        private final byte OP_RESET = 0x00;
	private final byte OP_PIN_CODE = 0x01;
	private final byte OP_ENCRYPT = 0x02;
	private final byte OP_GET_PUB_KEY = 0x03;

	private final byte WRONG_PIN = 0x00;
	private final byte RIGHT_PIN = 0x01;

	private final byte NOT_UNLOCKED = 0x10;

	private final byte[] PIN_CODE = new byte[]{0x30, 0x37, 0x32, 0x37};

	private final byte[] DUMMY = new byte[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39};


	/** INSTANCE FIELDS AND METHODS */

	private final OwnerPIN pin;
	private final KeyPair keyPair;

	public Encrypter()
	{
		this.pin = new OwnerPIN((byte) 3, (byte) 4);
		this.pin.update(PIN_CODE, (short) 0, (byte) 4);

		this.keyPair = new KeyPair(KeyPair.ALG_RSA, (short) 512);
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

		if ((buffer[ISO7816.OFFSET_CLA] == 0) && (buffer[ISO7816.OFFSET_INS] == (byte) 0xA4)) 
		{
            	// return if this is a SELECT FILE command
        	    return;
	        }

		if (buffer[ISO7816.OFFSET_CLA] == CLA_APPLET)
		{
			// We decide what to do depending on the instruction we receive
			switch (buffer[ISO7816.OFFSET_INS])
			{
                                case OP_RESET:
                                        reset();

				// A message code of 0x01 means the user submits a PIN code
				case OP_PIN_CODE:

					if (!this.pin.isValidated())
                                        {
					
						byte dataLength = (byte) apdu.setIncomingAndReceive();
						// we need to compare the PIN code sent by the user to our hard-coded PIN code
						// old comparison
						//byte comparison = Util.arrayCompare(PIN_CODE, (byte) 0, buffer, ISO7816.OFFSET_CDATA, (short) 4);

						// if the user-provided Papdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) dataLength);ode is right, we send back a 0x01 code and set the card to unlocked state
						if (this.pin.check(buffer, ISO7816.OFFSET_CDATA, dataLength))
						{
							return;
						}

						// else we send back a 0x02 code
						else
						{
							ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
						}
					}

					// if the card was already unlocked, sending the 0x01 operation code has no sense, so we treat this like an error
					else
					{
						ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
					}
					return;

				case OP_ENCRYPT:

					if(!this.pin.isValidated())
					{
						ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
					}
					else
					{
                                                byte dataLength = (byte) apdu.setIncomingAndReceive();
						short inputLength = buffer[ISO7816.OFFSET_LC];
                		Util.arrayCopy(DUMMY, (short) 0, buffer, ISO7816.OFFSET_CDATA, (short) DUMMY.length);
						apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) DUMMY.length);
					}
					return;

				default:
					// good practice: If you don't know the INStruction, say so:
					ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			}
		}
	}
}
