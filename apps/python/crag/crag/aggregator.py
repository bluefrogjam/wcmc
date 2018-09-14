import pandas as pd
import re
import requests

stasis_url = "https://api.metabolomics.us/stasis"
test_url = "https://test-api.metabolomics.us/stasis"


def getExperimentFiles(experiment) -> [str]:
    """
    Calls the stasis api to get a list files for an experiment
    :param experiment: name of experiment for which to get the list of files
    :return: dictionary with results or {error: msg}
    """
    print("\tGetting experiment files")
    response = requests.get(stasis_url + '/experiment/' + experiment)

    files = []
    if response.status_code == 200:
        files = [item['sample'] for item in response.json()]

    return files


def getSampleTracking(filename):
    """
    Calls the stasis api to get the tracking status for a single file
    :param filename: name of file to get tracking info from
    :return: dictionary with tracking or {error: msg}
    """
    print("\tGetting filename status")
    response = requests.get(stasis_url + "/tracking/" + filename)

    if response.status_code == 200:
        return response.json()
    else:
        return {"error": "no tracking info"}


def getFileResults(filename):
    """
    Calls the stasis api to get the results for a single file
    :param filename: name of file to get results from
    :return: dictionary with results or {error: msg}
    """
    print(f"\tGetting results for file '{filename}'")
    response = requests.get(stasis_url + "/result/" + filename)

    if response.status_code == 200:
        return response.json()
    else:
        return {"error": f'no results. {response.reason}'}


def format_sample(data):
    intensities = []
    for k, v in data['injections'].items():
        intensities = {k: [int(r['annotation']['intensity']) for r in v['results']]}

    return pd.DataFrame.from_dict(intensities, orient='columns')


def format_metadata(filename, data):
    names = rts = masses = inchikeys = []

    pattern = re.compile(".*?_[A-Z]{14}-[A-Z]{10}-[A-Z]")

    for k, v in data['injections'].items():
        names = [r['target']['name'] for r in v['results']]
        rts = [r['target']['retentionIndex'] for r in v['results']]
        masses = [r['target']['mass'] for r in v['results']]

    inchikeys = [name.split('_')[-1] if pattern.match(name) else None for name in names]
    names2 = [name.rsplit('_', maxsplit=1)[0] if pattern.match(name) else name for name in names]

    metadata = pd.DataFrame({'name': names2, 'rt(s)': rts, 'mz': masses, 'inchikey': inchikeys})
    metadata[filename] = format_sample(data)
    return metadata


def export_intensity_matrix(data):
    # add metadata
    result =

    # adding intensity matrix
    for file in files[0:3]:
        if 'error' not in data[file]:
            result[file] = format_sample(data)


def export_mass_matrix(data):
    # adding intensity matrix
    for item in data.items():
        if 'error' not in item:
            result[file] = format_sample(data)


def export_rt_matrix(data):
    # adding intensity matrix
    for file in files[0:3]:
        if 'error' not in data[file]:
            result[file] = format_sample(data)


def aggregate(args):
    """
    Collects information on the experiment and decides if aggregation of the full experiment is possible

    :param args: parameters containing the experiment name
    :return: the filename of the aggregated file (csv? xsls?)
    """

    # commented since it returns partial list of experiment files
    # print(f"Aggregating results for experiment '{args.experiment}'")
    # files = getExperimentFiles(args.experiment)
    # print(files)

    with open('/g/study-jenny/aws-results.txt') as processed:
        files = [p.split(' ')[-1].rstrip() for p in processed.readlines()]

    # get all data from aws
    data = {}
    for file in files[0:3]:
        # commented due to missing tracking data for most files
        # status = getSampleTracking(file)
        # print(status)

        data[file] = getFileResults(file)

    export_intensity_matrix(data)

    df_mz = export_mass_matrix(data)

    df_rt = export_rt_matrix(data)

    # saving excel file
    writer = pd.ExcelWriter('/g/study-jenny/jenny-tribe.xlsx')
    df_int.to_excel(writer, "Intensity Matrix")
    df_mz.to_excel(writer, "Mass Matrix")
    df_rt.to_excel(writer, "RT Matrix")
    writer.save()
