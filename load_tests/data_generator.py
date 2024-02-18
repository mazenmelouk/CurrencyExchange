#!/usr/bin/python3
import random
import argparse

CURRENCIES = ["EUR", "USD", "JPY", "AUD", "CHF", "GBP", "CNY"]


def __enumerate_currency_pairs():
    currencies_combinations = []
    for source in CURRENCIES:
        for target in CURRENCIES:
            if source == target:
                continue
            currencies_combinations.append((source, target))
    return currencies_combinations


def __format_endpoint(source_currency, target_currency, amount):
    return f"http://localhost:8080/convert?from={source_currency}&to={target_currency}&amount={amount}"


def __generate_random_samples(sample_size):
    samples = []
    currency_pairs = __enumerate_currency_pairs()
    http_method = "GET"
    while sample_size > 0:
        random_pair = random.choice(currency_pairs)
        amount = round(random.uniform(10, 1000), 3)
        endpoint = __format_endpoint(random_pair[0], random_pair[1], amount)
        samples.append(f"{http_method} {endpoint}")
        sample_size -= 1
    return samples


def __write_to_file(samples, file_name):
    with open(file_name, "w") as file:
        for item in samples:
            file.write(str(item) + "\n")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Generate random requests for Vegeta")
    parser.add_argument("--sample_size", required=False, default=100, type=int,
                        help="Number of random samples to generate")
    args = parser.parse_args()

    samples = __generate_random_samples(args.sample_size)
    print(f"Generated a sample of size {len(samples)}")
    file_name = "vegeta_input_data.txt"
    __write_to_file(samples, file_name)
    print(f"Wrote samples to {file_name}")
