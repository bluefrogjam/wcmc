#!/usr/bin/env python

import argparse
import pandas as pd


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('files', type=str, help='flat result files to transform', nargs='+')
    args = parser.parse_args()

    for f in args.files:
        df = pd.read_csv(f)
        grouped = df.groupby('filename')

        df = None

        for name, group in grouped:
            group = group.set_index(['target'])
            group = group.rename({'height (annotation)': name, 'mass (target)': 'mass', 'retention index (target)': 'retention time (s)'}, axis=1)
            group.loc[:, 'retention time (min)'] = group['retention time (s)'] / 60
            group = group[['mass', 'retention time (s)', 'retention time (min)', name]]
            group = group.transpose()

            if df is None:
                df = group
            else:
                df = df.append(group[group.index == name])

        df.to_csv(f.rsplit('.', 1)[0] +'_tabulated.csv')