const loadmill = require('loadmill')({token:"pHWUeJWbMLF3sK3RjppcqmQtJpdPuHaQPYRFoEEg"});
const request = require('request');

//fire up the lambda
return request('https://test.isaac.international/statistics/contributions/count/range/date/day', function (error, response, body) {

    //run the actual tests
    return loadmill.runFunctional("test_isaac_api.json")
        .then(result => console.log(result));
});


