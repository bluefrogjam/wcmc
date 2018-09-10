import requests

stasis_url = "https://api.metabolomics.us/stasis"


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
    response = requests.get("Https://test-api.metabolomics.us" + "/result/" + filename)

    if response.status_code == 200:
        return response.json()
    else:
        return {"error": "no results"}


def aggregate(args):
    """
    Collects information on the experiment and decides if aggregation of the full experiment is possible

    :param args: parameters containing the experiment name
    :return: the filename of the aggregated file (csv? xsls?)
    """
    print(f"Aggregating results for experiment '{args.experiment}'")
    files = getExperimentFiles(args.experiment)
    print(files)

    for file in files:
        status = getSampleTracking(file)
        print(status)

        result = {file: getFileResults('B5_P20Lipids_Pos_NIST01')}
        if result['error']:
            print("Some samples were not processed yet. Please check again later.")
            SystemExit.code(10)
        else:
            print(result)
