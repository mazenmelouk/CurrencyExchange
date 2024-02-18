# Currency Exchange Load Tests

This directory has helping code to generate and load test the Currency Exchange service.

The load test uses [Vegeta](https://github.com/tsenart/vegeta).

A simple python script [data_generator.py](data_generator.py) generates data to be used as input for Vegeta.



## How to run

### Data generation

#### Generate a sample with default size (100)
```shell
python3 data_generator.py
```
>Generated a sample of size 100
>
>Wrote samples to vegeta_input_data.txt

#### Generate a sample of size 10
```shell
python3 data_generator.py --sample_size 10
```
>Generated a sample of size 10
> 
>Wrote samples to vegeta_input_data.txt

### Run load test

#### Run test for 10 minutes at a rate of 1000 requests/s

```shell
vegeta attack -duration=10m -rate=1000/s -targets=vegeta_input_data.txt -output=load-test.bin
```

#### Extract results

```shell
vegeta report load-test.bin
```
#### Plot results
```shell
vegeta plot -title=LoadTestResults load-test.bin > load_test_results.html
```