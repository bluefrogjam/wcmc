import argparse

from crag import aggregator

if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('-e', '--experiment', help='name of the experiment to aggregate results', required=True)
    parser.add_argument('-t', '--test', help='Test mode. Run with only 3 samples', action='store_true')

    try:
        args = parser.parse_args()
        aggregator.aggregate(args)

    except Exception as ex:
        parser.print_help()
        SystemExit.code(-1)
