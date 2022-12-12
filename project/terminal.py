import random
from smartcard.System import readers
from smartcard.util import toHexString

def verify_PIN(pin, card):
    SELECT = [0x25, 0x00, 0x00, 0x00, 0x04]
    if len(pin) != 4:
        print("PIN incorrect length")
        return False
    DATA = list(bytes(pin, "utf-8"))
    response, sw1, sw2 = card.transmit( SELECT + DATA )
    return True if (sw1, sw2) == (144,0) else False

def init_test_PIN(card):
    SELECT = [0x25, 0x01, 0x00, 0x00, 0x01]
    DATA = [0x04]
    response, sw1, sw2 = card.transmit( SELECT + DATA )
    return True if (sw1, sw2) == (105,134) else False

def encrypt_string(string, card):
    SELECT = [0x25, 0x02, 0x00, 0x00, len(string)]
    STRING = list(bytes(string, "utf-8"))
    print (SELECT + STRING)
    response, sw1, sw2 = card.transmit(SELECT + STRING)
    return response if (sw1, sw2) == (144,0) else None

def pk_mod(card):
    SELECT = [0x25, 0x03, 0x00, 0x00, 0x01, 0x01]
    response, sw1, sw2 = card.transmit(SELECT)
    if (sw1 == 97):
        SELECT = [0x25, 0xC0, 0x00, 0x00, sw2]
        response, sw1, sw2 = card.transmit(SELECT)
        return response if (sw1, sw2) == (144,0) else None
    else:
     return None

def pk_exp(card):
    SELECT = [0x25, 0x04, 0x00, 0x00, 0x01, 0x01]
    response, sw1, sw2 = card.transmit(SELECT)
    if (sw1 == 97):
        SELECT = [0x25, 0xC0, 0x00, 0x00, sw2]    
        response, sw1, sw2 = card.transmit(SELECT)
        return response if (sw1, sw2) == (144,0) else None
    else:
     return None


def main():

    card = readers()[0].createConnection()
    card.connect()
    card_reset = [0x25, 0x00, 0x00, 0x00, 0x01, 0x01]
    response, sw1, sw2 = card.transmit(card_reset)
    card_code = [0x00, 0xA4, 0x04, 0x00, 0x08, 0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01, 0x02]
    response, sw1, sw2 = card.transmit(card_code)

    right_PIN = False
    count_PIN = 0
    max_count_PIN = 3
    while (not right_PIN) and (count_PIN < max_count_PIN):
        print("Please enter the 4-digit PIN code: ", end="")
        user_input = input()
        count_PIN += 1
        right_PIN = verify_PIN(user_input, card)
        print("PIN code correct." if right_PIN else "Incorrect PIN code.")

    if (max_count_PIN == count_PIN) and (not right_PIN):
        print("You exceed the number of tentative, Sorry.")
        return

    if right_PIN:
        print("They are information about public key of the program:")
        print("Modulus : ", pk_mod(card))
        print("Exposant : ", pk_exp(card))

    while right_PIN:
        print("Please enter a string to encode, or leave the line empty to exit the program.")
        user_input = input()
        if user_input == "":
            break
        else:
            user_encrypt = encrypt_string(user_input, card)
            print (user_input, user_encrypt)

    print("Goodbye.")
    exit(0)


if __name__ == '__main__':
    main()
