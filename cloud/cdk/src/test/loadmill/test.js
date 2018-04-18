const loadmill = require('loadmill')({token:"pHWUeJWbMLF3sK3RjppcqmQtJpdPuHaQPYRFoEEg"});

loadmill.runFunctional("test_isaac_api.json")
        .then(result => console.log(result));


