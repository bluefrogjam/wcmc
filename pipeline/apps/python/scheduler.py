#!/usr/bin/env python

import argparse
import requests
import simplejson as json
import time


def create_tasks(files, args):
    indexed = zip(range(1, len(files) + 1), files)

    tasks = []
    for (idx, file) in indexed:
        if (idx < args.start_index):
            continue

        samples = [{'fileName': file,
                    'matrix': {
                        'identifier': None,
                        'species': None,
                        'organ': None,
                        'comment': None,
                        'label': None}
                    }]
        if (args.task.startswith('task')):
            taskName = 'task-' + time.strftime('%Y%m%d') + '-' + str(idx)
        else:
            taskName = args.task + '-' + time.strftime('%Y%m%d') + '-' + str(idx)

        tasks.append({'name': f'{args.submitter}_{taskName}',
                      'email': args.submitter,
                      'acquisitionMethod': {
                          'chromatographicMethod': {
                              'name': args.method,
                              'instrument': None,
                              'column': None,
                              'ionMode': {
                                  'mode': args.ion_mode
                              }
                          },
                          'title': args.method + ' (' + args.ion_mode + ')'
                      },
                      'platform': {'platform': {'name': 'LC-MS'}},
                      'samples': samples
                      })

    return tasks


def submit(task):
    print(f'scheduling task {task["name"]}...')
    submissionUrl = 'http://localhost:18080/rest/schedule/submit'
    headers = {'Content-Type': 'application/json'}

    r = requests.post(submissionUrl, data=json.dumps(task), headers=headers)
    print(r.status_code, r.reason)
    time.sleep(1)  # in seconds, unlike java's millis


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-f', '--files', help='File with a list of samples to schedule', required=True)
    parser.add_argument('-s', '--submitter', help='Submitter\'s email', required=True)
    parser.add_argument('-t', '--task', help='Task name', default='task', type=str)
    parser.add_argument('-p', '--platform', help='Data\'s chromatography type', default='LC-MS', type=str,
                        choices=['LC-MS', 'GC-MS'])
    parser.add_argument('-i', '--ion_mode', help='Ion mode of the samples', default='positive',
                        choices=['positive', 'negative'])
    parser.add_argument('-m', '--method', help='Name of the chromatographic method (library)', default='csh-diego-pos')
    parser.add_argument('--start_index', help='Starting index (1 to number of files)', default=1, type=int)
    parser.add_argument('--dry_run', help='Do not submit the tasks, just print them', dest='dry', action='store_true')
    args = parser.parse_args()

    data = open(args.files, 'r')
    list = [l for l in (line.strip() for line in data) if l]
    data.close()

    tasks = create_tasks(list, args)

    for task in tasks:
        if (args.dry):
            print(task)
        else:
            submit(task)
