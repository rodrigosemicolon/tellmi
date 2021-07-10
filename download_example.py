import sys

if __name__ == "__main__":
    filenames = sys.argv[1:]

    for f in filenames:
        response = requests.get(
            'localhost:5000/files/{}'.format(f)
        )

        response.text   