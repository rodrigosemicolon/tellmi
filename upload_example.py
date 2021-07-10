import sys

if __name__ == "__main__":
    filenames = sys.argv[1:]

    for f in filenames:
        with open(f) as fp:
            content = fp.read()

        response = requests.post(
            '{}/files/newdata.csv'.format(API_URL), data=content
        )