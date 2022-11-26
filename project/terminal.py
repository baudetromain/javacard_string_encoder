import random
from smartcard.System import readers
from smartcard.util import toHexString

def verify_PIN(pin, card):
    SELECT = [0x01, 0x00, 0x00, 0x00, 0x02]
    DATA = [0x00, 0x01]
    response, sw1, sw2 = card.transmit( SELECT + DATA )
    print(response)
    print(sw1)
    print(sw2)
    return True if random.randint(0, 10) == 0 else False


def encrypt_string(string, card):
    return "hi"

def main():

    card = readers()[0].createConnection()
    card.connect()

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