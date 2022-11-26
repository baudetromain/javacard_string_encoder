import random


def verify_PIN(pin):
    return True if random.randint(0, 10) == 0 else False


def encrypt_string(string):
    return "hi"


def main():

    right_PIN = False
    while not right_PIN:
        print("Please enter the 4-digit PIN code: ", end="")
        user_input = input()
        right_PIN = verify_PIN(user_input)
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