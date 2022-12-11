import random
from smartcard.System import readers
from smartcard.util import toHexString

def verify_PIN(pin, card):
    SELECT = [0x25, 0x00, 0x00, 0x00, 0x04]
    if len(pin) != 4:
        print("PIN incorrect length")
        return False
    DATA = list(bytes(pin, "utf-8"))
    print (SELECT + DATA)
    response, sw1, sw2 = card.transmit( SELECT + DATA )
    print ("%x %x" % (sw1, sw2))
    return True if (sw1, sw2) == (144,0) else False

def init_test_PIN(card):
    SELECT = [0x25, 0x00, 0x00, 0x00, 0x01]
    DATA = [0x04]
    response, sw1, sw2 = card.transmit( SELECT + DATA )
    return True if (sw1, sw2) == (105,134) else False

def encrypt_string(string, card):
    SELECT = [0x25, 0x01, 0x00, 0x00, len(string)]
    STRING = list(map(ord, [char for char in string]))
    RETURN_BUFFER_LEN = [0x00]

    print(string)

    response, sw1, sw2 = card.transmit( SELECT + STRING + RETURN_BUFFER_LEN )

    return "YEP CLOCK"

def main():

    card = readers()[0].createConnection()
    card.connect()
    print("You are connected to a card : ", card)

    card_code = [0x00, 0xA4, 0x04, 0x00, 0x08, 0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01, 0x02]
    response, sw1, sw2 = card.transmit(card_code)
    print ("%x %x" % (sw1, sw2))

    right_PIN = False
    while not right_PIN:
        print("Please enter the 4-digit PIN code: ", end="")
        user_input = input()
        right_PIN = verify_PIN(user_input, card)
        print("PIN code correct." if right_PIN else "Incorrect PIN code.")

    while True:
        print("Please enter a string to encode, or leave the line empty to exit the program.")
        user_input = input()

        if user_input == "":
            break

        print(f"The encryption of \"{user_input}\" is \"{encrypt_string(user_input)}\".")

    print("Goodbye.")
    exit(0)


if __name__ == '__main__':
    main()
